package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.node.POJONode
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.ResultatkodeForskudd
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto

private val logger = KotlinLogging.logger {}

internal class BeregnForskuddService(private val forskuddCore: ForskuddCore = ForskuddCore()) {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetForskuddResultat {
        secureLogger.debug { "Mottatt følgende request: $grunnlag" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
        }

        // Henter sjabloner
        val sjablonTallListe: List<Sjablontall> = SjablonProvider.hentSjablontall()

        if (sjablonTallListe.isEmpty()) {
            logger.error { "Klarte ikke å hente sjabloner" }
            return BeregnetForskuddResultat()
        }

        logger.debug { "Antall sjabloner hentet av type Sjablontall: ${sjablonTallListe.size}" }

        // Lager input-grunnlag til core-modulen
        val grunnlagTilCore =
            CoreMapper.mapGrunnlagTilCore(
                beregnForskuddGrunnlag = grunnlag,
                sjablontallListe = sjablonTallListe,
            )

        secureLogger.debug { "Forskudd - grunnlag for beregning: $grunnlagTilCore" }

        // Kaller core-modulen for beregning av forskudd
        val resultatFraCore =
            try {
                forskuddCore.beregnForskudd(grunnlagTilCore)
            } catch (e: Exception) {
                throw IllegalArgumentException("Ugyldig input ved beregning av forskudd: " + e.message)
            }

        if (resultatFraCore.avvikListe.isNotEmpty()) {
            val avvikTekst = resultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            logger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avvikTekst" }
            secureLogger.warn { "Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avvikTekst" }
            secureLogger.info {
                "Forskudd - grunnlag for beregning: " + System.lineSeparator() +
                    "beregnDatoFra= " + grunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + grunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "soknadBarn= " + grunnlagTilCore.soknadBarn + System.lineSeparator() +
                    "barnIHusstandenPeriodeListe= " + grunnlagTilCore.barnIHusstandenPeriodeListe + System.lineSeparator() +
                    "inntektPeriodeListe= " + grunnlagTilCore.inntektPeriodeListe + System.lineSeparator() +
                    "sivilstandPeriodeListe= " + grunnlagTilCore.sivilstandPeriodeListe + System.lineSeparator()
            }
            throw IllegalArgumentException("Ugyldig input ved beregning av forskudd. Følgende avvik ble funnet: $avvikTekst")
        }

        secureLogger.debug { "Forskudd - resultat av beregning: ${resultatFraCore.beregnetForskuddPeriodeListe}" }

        val grunnlagReferanseListe = lagGrunnlagReferanseListe(forskuddGrunnlag = grunnlag, resultatFraCore = resultatFraCore)

        val respons =
            BeregnetForskuddResultat(
                beregnetForskuddPeriodeListe = mapFraResultatPeriodeCore(resultatFraCore.beregnetForskuddPeriodeListe),
                grunnlagListe = grunnlagReferanseListe,
            )

        secureLogger.debug { "Returnerer følgende respons: $respons" }

        return respons
    }

    private fun mapFraResultatPeriodeCore(resultatPeriodeCoreListe: List<ResultatPeriodeCore>) = resultatPeriodeCoreListe.map {
        ResultatPeriode(
            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
            resultat =
            ResultatBeregning(
                belop = it.resultat.belop,
                kode = ResultatkodeForskudd.valueOf(it.resultat.kode),
                regel = it.resultat.regel,
            ),
            grunnlagsreferanseListe = it.grunnlagsreferanseListe,
        )
    }

    // Lager en liste over resultatgrunnlag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagReferanseListe(forskuddGrunnlag: BeregnGrunnlag, resultatFraCore: BeregnetForskuddResultatCore): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.beregnetForskuddPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            forskuddGrunnlag.grunnlagListe!!
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map { GrunnlagDto(referanse = it.referanse, type = it.type, innhold = it.innhold) },
        )

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            resultatFraCore.sjablonListe
                .map {
                    val map = LinkedHashMap<String, Any>()
                    map["datoFom"] = it.periode.datoFom.toString()
                    map["datoTil"] = it.periode.datoTil.toString()
                    map["sjablonNavn"] = it.navn
                    map["sjablonVerdi"] = it.verdi.toInt()
                    GrunnlagDto(referanse = it.referanse, type = Grunnlagstype.SJABLON, innhold = POJONode(map))
                },
        )

        return resultatGrunnlagListe
    }
}
