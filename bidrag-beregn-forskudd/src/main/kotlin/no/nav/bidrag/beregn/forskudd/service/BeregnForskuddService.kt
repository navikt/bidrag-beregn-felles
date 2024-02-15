package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningForskudd
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse

private val logger = KotlinLogging.logger {}

internal class BeregnForskuddService(private val forskuddCore: ForskuddCore = ForskuddCore()) {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetForskuddResultat {
        secureLogger.info { "Forskuddsberegning - følgende request mottatt: $grunnlag" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
        }

        // Henter sjabloner
        val sjablontallListe: List<Sjablontall> = SjablonProvider.hentSjablontall()

        if (sjablontallListe.isEmpty()) {
            logger.error { "Klarte ikke å hente sjabloner" }
            return BeregnetForskuddResultat()
        }

        // Lager input-grunnlag til core-modulen
        val grunnlagTilCore =
            CoreMapper.mapGrunnlagTilCore(
                beregnForskuddGrunnlag = grunnlag,
                sjablontallListe = sjablontallListe,
            )

        secureLogger.debug { "Forskuddsberegning - grunnlag for beregning: $grunnlagTilCore" }

        // Kaller core-modulen for beregning av forskudd
        val resultatFraCore =
            try {
                forskuddCore.beregnForskudd(grunnlagTilCore)
            } catch (e: Exception) {
                throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
            }

        if (resultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = resultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            logger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst" }
            secureLogger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst" }
            secureLogger.info {
                "Forskuddsberegning - grunnlag for beregning: " + System.lineSeparator() +
                    "beregnDatoFra= " + grunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + grunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "soknadBarn= " + grunnlagTilCore.søknadsbarn + System.lineSeparator() +
                    "barnIHusstandenPeriodeListe= " + grunnlagTilCore.barnIHusstandenPeriodeListe + System.lineSeparator() +
                    "inntektPeriodeListe= " + grunnlagTilCore.inntektPeriodeListe + System.lineSeparator() +
                    "sivilstandPeriodeListe= " + grunnlagTilCore.sivilstandPeriodeListe + System.lineSeparator()
                "bostatusPeriodeListe= " + grunnlagTilCore.bostatusPeriodeListe + System.lineSeparator()
            }
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.info { "Forskuddberegning - resultat av beregning: ${resultatFraCore.beregnetForskuddPeriodeListe}" }

        val grunnlagReferanseListe =
            lagGrunnlagsreferanseListe(forskuddGrunnlag = grunnlag, resultatFraCore = resultatFraCore, grunnlagTilCore = grunnlagTilCore)

        val resultatPeriodeListe = lagSluttperiodeOgResultatperioder(resultatFraCore.beregnetForskuddPeriodeListe, grunnlagReferanseListe)

        val respons =
            BeregnetForskuddResultat(
                beregnetForskuddPeriodeListe = resultatPeriodeListe,
                grunnlagListe = grunnlagReferanseListe.distinctBy { it.referanse },
            )

        secureLogger.info { "Forskuddsberegning - returnerer følgende respons: $respons" }

        return respons
    }

    // Oppretter resultatperioder som refererer til sluttberegning, som igjen refererer til delberegninger og grunnlag
    private fun lagSluttperiodeOgResultatperioder(
        resultatPeriodeCoreListe: List<ResultatPeriodeCore>,
        grunnlagReferanseListe: List<GrunnlagDto>,
    ): List<ResultatPeriode> {
        return resultatPeriodeCoreListe.map { resultatPeriode ->

            val søknadsbarnReferanse = grunnlagReferanseListe.filter { it.type == Grunnlagstype.PERSON_SØKNADSBARN }.map { it.referanse }.first()
            val sluttberegningReferanse = opprettSluttberegningreferanse(
                barnreferanse = søknadsbarnReferanse,
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
            )

            // Oppretter sluttberegning, som legges til i grunnlagslista
            grunnlagReferanseListe.addFirst(
                GrunnlagDto(
                    referanse = sluttberegningReferanse,
                    type = Grunnlagstype.SLUTTBEREGNING_FORSKUDD,
                    innhold = POJONode(
                        SluttberegningForskudd(
                            periode = ÅrMånedsperiode(resultatPeriode.periode.datoFom, resultatPeriode.periode.datoTil),
                            beløp = resultatPeriode.resultat.beløp,
                            resultatKode = resultatPeriode.resultat.kode,
                            aldersgruppe = resultatPeriode.resultat.aldersgruppe,
                        ),
                    ),
                    grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe,
                    gjelderReferanse = søknadsbarnReferanse,
                ),
            )

            // Oppretter resultatperioder, som refererer til sluttberegning
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
                resultat =
                ResultatBeregning(
                    belop = resultatPeriode.resultat.beløp,
                    kode = resultatPeriode.resultat.kode,
                    regel = resultatPeriode.resultat.regel,
                ),
                grunnlagsreferanseListe = listOf(sluttberegningReferanse),
            )
        }
    }

    // Lager en liste over resultatgrunnlag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagsreferanseListe(
        forskuddGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnetForskuddResultatCore,
        grunnlagTilCore: BeregnForskuddGrunnlagCore,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.beregnetForskuddPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            forskuddGrunnlag.grunnlagListe
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektListe = grunnlagTilCore.inntektPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumAntallBarnListe = grunnlagTilCore.barnIHusstandenPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut delberegninger som er brukt som grunnlag
        resultatGrunnlagListe.addAll(
            sumInntektListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = bestemGrunnlagstype(it.referanse),
                        innhold = POJONode(
                            DelberegningInntekt(
                                periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                                summertBeløp = it.beløp,
                            ),
                        ),
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                    )
                },
        )

        resultatGrunnlagListe.addAll(
            sumAntallBarnListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = bestemGrunnlagstype(it.referanse),
                        innhold = POJONode(
                            DelberegningBarnIHusstand(
                                periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                                antallBarn = it.antall,
                            ),
                        ),
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                    )
                },
        )

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe = sumInntektListe
            .flatMap { it.grunnlagsreferanseListe }
            .union(
                sumAntallBarnListe
                    .flatMap { it.grunnlagsreferanseListe },
            )
            .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            forskuddGrunnlag.grunnlagListe
                .filter { it.referanse in delberegningReferanseListe }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            resultatFraCore.sjablonListe
                .map {
                    val map = LinkedHashMap<String, Any>()
                    map["periode"] = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil)
                    map["sjablonNavn"] = it.navn
                    map["sjablonVerdi"] = it.verdi.toInt()
                    GrunnlagDto(referanse = it.referanse, type = Grunnlagstype.SJABLON, innhold = POJONode(map))
                },
        )

        return resultatGrunnlagListe
    }

    private fun bestemGrunnlagstype(referanse: String) = when {
        referanse.contains(Grunnlagstype.DELBEREGNING_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_INNTEKT
        referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
        else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
    }
}
