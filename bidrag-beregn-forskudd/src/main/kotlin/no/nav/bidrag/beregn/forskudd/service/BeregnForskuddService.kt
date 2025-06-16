package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.core.mapping.mapTilGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.forskudd.service.ForskuddCoreMapper.finnInnslagKapitalinntektFraSjablontallListe
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningForskudd
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import java.time.YearMonth

private val logger = KotlinLogging.logger {}

internal class BeregnForskuddService(private val forskuddCore: ForskuddCore = ForskuddCore()) : BeregnService() {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetForskuddResultat {
        secureLogger.info { "Forskuddsberegning - følgende request mottatt: ${tilJson(grunnlag)}" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
        }

        // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden
        val utvidetGrunnlagJustert = justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(grunnlag)
        val utvidetGrunnlag = utvidetGrunnlagJustert.beregnGrunnlag
        val åpenSluttperiode = utvidetGrunnlagJustert.åpenSluttperiode

        // Henter sjabloner
        val sjablontallListe: List<Sjablontall> = SjablonProvider.hentSjablontall().sortedWith(compareBy({ it.typeSjablon }, { it.datoFom }))

        if (sjablontallListe.isEmpty()) {
            logger.error { "Klarte ikke å hente sjabloner" }
            return BeregnetForskuddResultat()
        }

        // Lager input-grunnlag til core-modulen
        val grunnlagTilCore =
            ForskuddCoreMapper.mapGrunnlagTilCore(
                beregnForskuddGrunnlag = utvidetGrunnlag,
                sjablontallListe = sjablontallListe,
                åpenSluttperiode = åpenSluttperiode,
            )

        secureLogger.debug { "Forskuddsberegning - grunnlag for beregning: ${tilJson(grunnlagTilCore)}" }

        // Kaller core-modulen for beregning av forskudd
        val resultatFraCore =
            try {
                forskuddCore.beregnForskudd(grunnlagTilCore)
            } catch (e: Exception) {
                throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
            }

        håndterAvvik(resultatFraCore.avvikListe, "forskudd")

        secureLogger.debug { "Forskuddberegning - resultat av beregning: ${tilJson(resultatFraCore.beregnetForskuddPeriodeListe)}" }

        // Henter sjablonverdi for kapitalinntekt
        val innslagKapitalinntektSjablon = finnInnslagKapitalinntektFraSjablontallListe(sjablontallListe)

        val grunnlagsliste =
            lagGrunnlagsliste(
                forskuddGrunnlag = grunnlag,
                resultatFraCore = resultatFraCore,
                grunnlagTilCore = grunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
            )

        val resultatPeriodeListe = lagSluttperiodeOgResultatperioder(resultatFraCore.beregnetForskuddPeriodeListe, grunnlagsliste)

        val respons =
            BeregnetForskuddResultat(
                beregnetForskuddPeriodeListe = resultatPeriodeListe,
                grunnlagListe = grunnlagsliste.distinctBy { it.referanse },
            )

        secureLogger.info { "Forskuddsberegning - returnerer følgende respons: ${tilJson(respons)}" }

        return respons
    }

    // Oppretter resultatperioder som refererer til sluttberegning, som igjen refererer til delberegninger og grunnlag
    private fun lagSluttperiodeOgResultatperioder(
        resultatPeriodeCoreListe: List<ResultatPeriodeCore>,
        grunnlagReferanseListe: MutableList<GrunnlagDto>,
    ): List<ResultatPeriode> = resultatPeriodeCoreListe.map { resultatPeriode ->

        val søknadsbarnReferanse = grunnlagReferanseListe.filter { it.type == Grunnlagstype.PERSON_SØKNADSBARN }.map { it.referanse }.first()
        val sluttberegningReferanse = opprettSluttberegningreferanse(
            barnreferanse = søknadsbarnReferanse,
            periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
        )

        // Oppretter sluttberegning, som legges til i grunnlagslista
        grunnlagReferanseListe.add(
            0,
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

    // Lager en liste over resultatgrunnlag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagsliste(
        forskuddGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnetForskuddResultatCore,
        grunnlagTilCore: BeregnForskuddGrunnlagCore,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): MutableList<GrunnlagDto> {
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
                        gjelderBarnReferanse = it.gjelderBarnReferanse,
                    )
                },
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektListe = grunnlagTilCore.inntektPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumAntallBarnListe = grunnlagTilCore.barnIHusstandenPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektListe,
                beregnGrunnlag = forskuddGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = forskuddGrunnlag.grunnlagListe.bidragsmottaker?.referanse,
            ),
        )

        // Mapper ut DelberegningBarnIHusstand
        resultatGrunnlagListe.addAll(
            sumAntallBarnListe.mapTilGrunnlag(),
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
                        gjelderBarnReferanse = it.gjelderBarnReferanse,
                    )
                },
        )

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            resultatFraCore.sjablonListe
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = Grunnlagstype.SJABLON_SJABLONTALL,
                        innhold = POJONode(
                            SjablonSjablontallPeriode(
                                periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                                sjablon = SjablonTallNavn.from(it.navn),
                                verdi = it.verdi,
                            ),
                        ),
                    )
                },
        )

        // Lager grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
        if (delberegningReferanseListe.any { it.contains("kapitalinntekt", ignoreCase = true) } && innslagKapitalinntektSjablon != null) {
            resultatGrunnlagListe.add(mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon))
        }

        return resultatGrunnlagListe
    }

    // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden. Justerer i så fall til-periode.
    private fun justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag: BeregnGrunnlag): BeregnGrunnlagJustert {
        val periodeSøknadsbarnetFyller18År = mottattGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
            .map { YearMonth.from(it.innhold.fødselsdato.plusYears(18)) }
            .first()

        return if (periodeSøknadsbarnetFyller18År.isBefore(mottattGrunnlag.periode.til)) {
            BeregnGrunnlagJustert(
                beregnGrunnlag = mottattGrunnlag.copy(
                    periode = mottattGrunnlag.periode.copy(til = periodeSøknadsbarnetFyller18År.plusMonths(1)),
                ),
                åpenSluttperiode = false,
            )
        } else {
            BeregnGrunnlagJustert(
                beregnGrunnlag = mottattGrunnlag,
                åpenSluttperiode =
                mottattGrunnlag.opphørsdato == null || mottattGrunnlag.opphørsdato!!.isAfter(YearMonth.now()),
            )
        }
    }

    data class BeregnGrunnlagJustert(val beregnGrunnlag: BeregnGrunnlag, val åpenSluttperiode: Boolean)
}
