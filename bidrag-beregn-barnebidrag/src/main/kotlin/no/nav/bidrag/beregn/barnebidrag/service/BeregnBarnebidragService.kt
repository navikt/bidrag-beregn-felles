package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.beregnBeløpFaktiskUtgift
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.beregnBeløpTilleggsstønad
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnetilleggSkattesatsService.delberegningBarnetilleggSkattesats
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBidragsevneService.delberegningBidragsevne
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBpAndelUnderholdskostnadService.delberegningBpAndelUnderholdskostnad
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndeligBidragService.delberegningEndeligBidrag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoBarnetilleggService.delberegningNettoBarnetillegg
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoTilsynsutgiftService.delberegningNettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.service.BeregnSamværsfradragService.delberegningSamværsfradrag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnUnderholdskostnadService.delberegningUnderholdskostnad
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal
import java.time.YearMonth

class BeregnBarnebidragService : BeregnService() {

    // Komplett beregning av barnebidrag
    fun beregnBarnebidrag(mottattGrunnlag: BeregnGrunnlag): BeregnetBarnebidragResultat {
        secureLogger.debug { "Beregning av barnebidrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av barnebidrag: " + e.message)
        }

        var åpenSluttperiode: Boolean

        // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden
        var utvidetGrunnlag = justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag).also { oppdatertGrunnlag ->
            åpenSluttperiode = oppdatertGrunnlag.periode.til == mottattGrunnlag.periode.til
        }

        // Kaller delberegninger
        val delberegningBidragsevneResultat = delberegningBidragsevne(utvidetGrunnlag, åpenSluttperiode)
        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(utvidetGrunnlag, åpenSluttperiode)
        utvidetGrunnlag = utvidetGrunnlag.copy(
            grunnlagListe = (utvidetGrunnlag.grunnlagListe + delberegningNettoTilsynsutgiftResultat).distinctBy(GrunnlagDto::referanse),
        )
        val delberegningUnderholdskostnadResultat = delberegningUnderholdskostnad(utvidetGrunnlag, åpenSluttperiode)
        utvidetGrunnlag = utvidetGrunnlag.copy(
            grunnlagListe = (utvidetGrunnlag.grunnlagListe + delberegningUnderholdskostnadResultat).distinctBy(GrunnlagDto::referanse),
        )
        val delberegningBpAndelUnderholdskostnadResultat = delberegningBpAndelUnderholdskostnad(utvidetGrunnlag, åpenSluttperiode)
        val delberegningSamværsfradragResultat = delberegningSamværsfradrag(utvidetGrunnlag, åpenSluttperiode)
        utvidetGrunnlag = utvidetGrunnlag.copy(
            grunnlagListe = (
                utvidetGrunnlag.grunnlagListe + delberegningBidragsevneResultat + delberegningNettoTilsynsutgiftResultat +
                    delberegningUnderholdskostnadResultat +
                    delberegningBpAndelUnderholdskostnadResultat + delberegningSamværsfradragResultat
                )
                .distinctBy(GrunnlagDto::referanse),
        )
        val delberegningEndeligBidragResultat = delberegningEndeligBidrag(utvidetGrunnlag, åpenSluttperiode)

        val resultatGrunnlagListe = (
                delberegningBidragsevneResultat + delberegningNettoTilsynsutgiftResultat + delberegningUnderholdskostnadResultat +
                    delberegningBpAndelUnderholdskostnadResultat + delberegningSamværsfradragResultat + delberegningEndeligBidragResultat
                )
                .distinctBy { it.referanse }
                .sortedBy { it.referanse }

        return BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = lagResultatPerioder(delberegningEndeligBidragResultat),
            grunnlagListe = resultatGrunnlagListe,
        )
    }

    // Beregning av bidragsevne
    fun beregnBidragsevne(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av bidragsevne - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av bidragsevne: " + e.message)
        }

        // Kaller delberegninger
        val delberegningBidragsevneResultat = delberegningBidragsevne(mottattGrunnlag)

        return delberegningBidragsevneResultat
    }

    // Beregning av netto tilsynsutgift
    fun beregnNettoTilsynsutgift(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av netto tilsynsutgift - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }
        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av netto tilsynsutgift: " + e.message)
        }

        // Kaller delberegninger
        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(mottattGrunnlag)
        return delberegningNettoTilsynsutgiftResultat
    }

    // Beregning av underholdskostnad
    fun beregnUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av underholdskostnad - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av underholdskostnad: " + e.message)
        }

        val delberegningUnderholdskostnadResultat = delberegningUnderholdskostnad(mottattGrunnlag)

        return delberegningUnderholdskostnadResultat
    }

    // Beregning av først netto tilsynsutgift så underholdskostnad
    fun beregnNettoTilsynsutgiftOgUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av netto tilsynsutgift og så underholdskostnad - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av underholdskostnad: " + e.message)
        }

        var åpenSluttperiode: Boolean

        // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden (gjøres her fordi dette er en metode som vil bli kalt direkte fra
        // bidrag-behandling)
        val utvidetGrunnlag = justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag).also { oppdatertGrunnlag ->
            åpenSluttperiode = oppdatertGrunnlag.periode.til == mottattGrunnlag.periode.til
        }

        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(
            mottattGrunnlag = utvidetGrunnlag,
            åpenSluttperiode = åpenSluttperiode
        )

        val delberegningUnderholdskostnadResultat = delberegningUnderholdskostnad(
            mottattGrunnlag = BeregnGrunnlag(
                periode = utvidetGrunnlag.periode,
                søknadsbarnReferanse = utvidetGrunnlag.søknadsbarnReferanse,
                grunnlagListe = (utvidetGrunnlag.grunnlagListe + delberegningNettoTilsynsutgiftResultat).distinctBy { it.referanse },
            ),
            åpenSluttperiode = åpenSluttperiode
        )

        return (delberegningNettoTilsynsutgiftResultat + delberegningUnderholdskostnadResultat).distinctBy { it.referanse }
    }

    // Beregning av BP's andel av underholdskostnad
    fun beregnBpAndelUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av BP's andel av underholdskostnad - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av BP's andel av underholdskostnad: " + e.message)
        }

        // Kaller delberegninger
        val delberegningBpAndelUnderholdskostnadResultat = delberegningBpAndelUnderholdskostnad(mottattGrunnlag)

        return delberegningBpAndelUnderholdskostnadResultat
    }

    // Beregning av netto barnetillegg. Kan gjelde både BM og BP.
    fun beregnNettoBarnetillegg(mottattGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av netto barnetillegg - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av netto barnetillegg: " + e.message)
        }

        // Kaller delberegninger
        val delberegningNettoBarnetilleggResultat = delberegningNettoBarnetillegg(mottattGrunnlag, rolle)

        return delberegningNettoBarnetilleggResultat
    }

    // Beregning av samværsfradrag
    fun beregnSamværsfradrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av samværsfradrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av samværsfradrag: " + e.message)
        }

        // Kaller delberegninger
        val delberegningSamværsfradragResultat = delberegningSamværsfradrag(mottattGrunnlag)

        return delberegningSamværsfradragResultat
    }

    // Beregning av barnetillegg skattesats
    fun beregnBarnetilleggSkattesats(mottattGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av barnetillegg skattesats - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av barnetillegg skattesats " + e.message)
        }

        // Kaller delberegninger
        val delberegningBarnetilleggSkattesatsResultat = delberegningBarnetilleggSkattesats(mottattGrunnlag, rolle)

        return delberegningBarnetilleggSkattesatsResultat
    }

    // Beregning av endelig bidrag (sluttberegning)
    fun beregnEndeligBidrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av endelig bidrag (sluttberegning) - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av endelig bidrag (sluttberegning): " + e.message)
        }

        // Kaller delberegninger
        val delberegningEndeligBidragResultat = delberegningEndeligBidrag(mottattGrunnlag)

        return delberegningEndeligBidragResultat
    }

    private fun lagResultatPerioder(delberegningEndeligBidragResultat: List<GrunnlagDto>): List<ResultatPeriode> = delberegningEndeligBidragResultat
        .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
        .map {
            ResultatPeriode(
                periode = it.innhold.periode,
                resultat = ResultatBeregning(
                    beløp = it.innhold.resultatBeløp,
                ),
                grunnlagsreferanseListe = listOf(it.referanse),
            )
        }

    // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden. Justerer i så fall til-periode.
    private fun justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag: BeregnGrunnlag): BeregnGrunnlag {
        val periodeSøknadsbarnetFyller18År = mottattGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
            .map { YearMonth.from(it.innhold.fødselsdato.plusYears(18)) }
            .first()

        return if (!periodeSøknadsbarnetFyller18År.isAfter(mottattGrunnlag.periode.til)) {
            mottattGrunnlag.copy(
                periode = mottattGrunnlag.periode.copy(til = periodeSøknadsbarnetFyller18År)
            )
        } else mottattGrunnlag
    }

    fun beregnMånedsbeløpFaktiskUtgift(faktiskUtgift: BigDecimal, kostpenger: BigDecimal = BigDecimal.ZERO): BigDecimal =
        beregnBeløpFaktiskUtgift(faktiskUtgift, kostpenger).avrundetMedToDesimaler

    fun beregnMånedsbeløpTilleggsstønad(tilleggsstønad: BigDecimal): BigDecimal = beregnBeløpTilleggsstønad(tilleggsstønad).avrundetMedToDesimaler
}
