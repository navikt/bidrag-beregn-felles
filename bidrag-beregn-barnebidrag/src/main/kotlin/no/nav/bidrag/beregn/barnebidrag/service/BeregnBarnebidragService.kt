package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BeregnEndeligBidragServiceRespons
import no.nav.bidrag.beregn.barnebidrag.bo.SluttberegningPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnetilleggSkattesatsService.delberegningBarnetilleggSkattesats
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBidragsevneService.delberegningBidragsevne
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBpAndelUnderholdskostnadService.delberegningBpAndelUnderholdskostnad
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndeligBidragService.delberegningEndeligBidrag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrensePeriodeService.delberegningEndringSjekkGrensePeriode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnEndringSjekkGrenseService.delberegningEndringSjekkGrense
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoBarnetilleggService.delberegningNettoBarnetillegg
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoTilsynsutgiftService.delberegningNettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.service.BeregnSamværsfradragService.delberegningSamværsfradrag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnUnderholdskostnadService.delberegningUnderholdskostnad
import no.nav.bidrag.beregn.core.exception.BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException
import no.nav.bidrag.beregn.core.exception.BegrensetRevurderingLøpendeForskuddManglerException
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SøknadGrunnlag
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

        // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden
        val utvidetGrunnlagJustert = justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag)
        var utvidetGrunnlag = utvidetGrunnlagJustert.beregnGrunnlag
        val åpenSluttperiode = utvidetGrunnlagJustert.åpenSluttperiode

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
                    delberegningUnderholdskostnadResultat + delberegningBpAndelUnderholdskostnadResultat + delberegningSamværsfradragResultat
                )
                .distinctBy(GrunnlagDto::referanse),
        )
        val delberegningEndeligBidragResultat = delberegningEndeligBidrag(utvidetGrunnlag, åpenSluttperiode)

        val resultatPeriodeListe: List<ResultatPeriode>
        val beløpshistorikkGrunnlag = emptyList<GrunnlagDto>()
        var delberegningEndringSjekkGrensePeriodeResultat = emptyList<GrunnlagDto>()
        var delberegningEndringSjekkGrenseResultat = emptyList<GrunnlagDto>()

        // Skal sjekke mot minimumsgrense for endring ("12%-regelen") hvis egetTiltak er false
        if (skalSjekkeMotMinimumsgrenseForEndring(mottattGrunnlag)) {
            val sjekkMotMinimumsgrenseForEndringResultat = sjekkMotMinimumsgrenseForEndring(
                mottattGrunnlag = mottattGrunnlag,
                utvidetGrunnlagJustert = utvidetGrunnlagJustert,
                delberegningEndeligBidragResultat = delberegningEndeligBidragResultat,
                åpenSluttperiode = åpenSluttperiode,
            )
            resultatPeriodeListe = sjekkMotMinimumsgrenseForEndringResultat.resultatPeriodeListe
            delberegningEndringSjekkGrensePeriodeResultat = sjekkMotMinimumsgrenseForEndringResultat.delberegningEndringSjekkGrensePeriodeResultat
            delberegningEndringSjekkGrenseResultat = sjekkMotMinimumsgrenseForEndringResultat.delberegningEndringSjekkGrenseResultat
        } else {
            resultatPeriodeListe = lagResultatPerioder(delberegningEndeligBidragResultat.grunnlagListe)
        }

        // Slår sammen grunnlag fra alle delberegninger
        val foreløpigResultatGrunnlagListe = (
            delberegningBidragsevneResultat + delberegningNettoTilsynsutgiftResultat + delberegningUnderholdskostnadResultat +
                delberegningBpAndelUnderholdskostnadResultat + delberegningSamværsfradragResultat + delberegningEndeligBidragResultat.grunnlagListe +
                beløpshistorikkGrunnlag
            )
            .distinctBy { it.referanse }
            .sortedBy { it.referanse }

        // Filtrerer bort grunnlag som ikke blir referert (dette vil skje f.eks. hvis barnet er selvforsørget og hvis barnet bor hos BP - da
        // regnes ikke alle delberegninger som relevante). Delberegninger for sjekk mot minimumsgrense for endring står i ensærstilling ettersom de
        // ikke refereres noe sted, men likevel skal være med i resultatgrunnlaget om de finnes.
        val endeligResultatGrunnlagListe = (
            filtrerResultatGrunnlag(
                foreløpigResultatGrunnlagListe = foreløpigResultatGrunnlagListe,
                refererteReferanserListe = resultatPeriodeListe.flatMap { it.grunnlagsreferanseListe }
            ) + delberegningEndringSjekkGrenseResultat + delberegningEndringSjekkGrensePeriodeResultat
            )
            .distinctBy { it.referanse }
            .sortedBy { it.referanse }

        val beregnetBarnebidragResultat = BeregnetBarnebidragResultat(
            beregnetBarnebidragPeriodeListe = resultatPeriodeListe,
            grunnlagListe = endeligResultatGrunnlagListe,
        )

        // Kaster exception hvis det er utført begrenset revurdering og det er minst ett tilfelle hvor beregnet bidrag er lavere enn løpende bidrag
        // eller hvis løpende forskudd mangler i første beregningsperiode
        if (delberegningEndeligBidragResultat.skalKasteBegrensetRevurderingException) {
            if (delberegningEndeligBidragResultat.feilmelding.contains("løpende forskudd mangler")) {
                throw BegrensetRevurderingLøpendeForskuddManglerException(
                    melding = delberegningEndeligBidragResultat.feilmelding,
                    periodeListe = delberegningEndeligBidragResultat.perioderMedFeilListe,
                    data = beregnetBarnebidragResultat,
                )
            } else {
                throw BegrensetRevurderingLikEllerLavereEnnLøpendeBidragException(
                    melding = delberegningEndeligBidragResultat.feilmelding,
                    periodeListe = delberegningEndeligBidragResultat.perioderMedFeilListe,
                    data = beregnetBarnebidragResultat,
                )
            }
        }

        secureLogger.debug { "Beregning av barnebidrag - følgende respons returnert: ${tilJson(beregnetBarnebidragResultat)}" }
        return beregnetBarnebidragResultat
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

        // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden (gjøres her fordi dette er en metode som vil bli kalt direkte fra
        // bidrag-behandling)
        val utvidetGrunnlagJustert = justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag)
        val utvidetGrunnlag = utvidetGrunnlagJustert.beregnGrunnlag
        val åpenSluttperiode = utvidetGrunnlagJustert.åpenSluttperiode

        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(
            mottattGrunnlag = utvidetGrunnlag,
            åpenSluttperiode = åpenSluttperiode,
        )

        val delberegningUnderholdskostnadResultat = delberegningUnderholdskostnad(
            mottattGrunnlag = BeregnGrunnlag(
                periode = utvidetGrunnlag.periode,
                søknadsbarnReferanse = utvidetGrunnlag.søknadsbarnReferanse,
                grunnlagListe = (utvidetGrunnlag.grunnlagListe + delberegningNettoTilsynsutgiftResultat).distinctBy { it.referanse },
            ),
            åpenSluttperiode = åpenSluttperiode,
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
        val delberegningBarnetilleggSkattesatsResultat = delberegningBarnetilleggSkattesats(mottattGrunnlag = mottattGrunnlag, rolle = rolle)

        return delberegningBarnetilleggSkattesatsResultat
    }

    // Beregning av endelig bidrag (sluttberegning)
    fun beregnEndeligBidrag(mottattGrunnlag: BeregnGrunnlag): BeregnEndeligBidragServiceRespons {
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

    // Beregning av om endelig bidrag (sluttberegning) er under eller over grense ("12%"-regelen) ifht løpende bidrag (per periode)
    fun beregnEndringSjekkGrensePeriode(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug {
            "Beregning av om endring i bidrag er over eller under grense (periode) - følgende request mottatt: " +
                tilJson(mottattGrunnlag)
        }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av endring sjekk grense (periode): " + e.message)
        }

        // Kaller delberegninger
        val delberegningEndringSjekkGrensePeriodeResultat = delberegningEndringSjekkGrensePeriode(mottattGrunnlag)

        return delberegningEndringSjekkGrensePeriodeResultat
    }

    // Beregning av om endelig bidrag (sluttberegning) er under eller over grense ("12%"-regelen) ifht løpende bidrag (totalt)
    fun beregnEndringSjekkGrense(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av om endring i bidrag er over eller under grense - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av endring sjekk grense: " + e.message)
        }

        // Kaller delberegninger
        val delberegningEndringSjekkGrenseResultat = delberegningEndringSjekkGrense(mottattGrunnlag)

        return delberegningEndringSjekkGrenseResultat
    }

    // Sjekk mot minimumsgrense for endring ("12%-regelen")
    private fun sjekkMotMinimumsgrenseForEndring(
        mottattGrunnlag: BeregnGrunnlag,
        utvidetGrunnlagJustert: BeregnGrunnlagJustert,
        delberegningEndeligBidragResultat: BeregnEndeligBidragServiceRespons,
        åpenSluttperiode: Boolean,
    ): SjekkMotMinimumsgrenseForEndringResultat {
        // Filtrerer ut beløpshistorikk
        val beløpshistorikkGrunnlag = filtrerBeløpshistorikkGrunnlag(mottattGrunnlag)

        // Delberegning for å sjekke om endring i bidrag er over grense (pr periode)
        var grunnlagTilEndringSjekkGrense = utvidetGrunnlagJustert.beregnGrunnlag.copy(
            grunnlagListe = beløpshistorikkGrunnlag + delberegningEndeligBidragResultat.grunnlagListe,
        )
        val delberegningEndringSjekkGrensePeriodeResultat =
            delberegningEndringSjekkGrensePeriode(grunnlagTilEndringSjekkGrense, åpenSluttperiode)

        // Delberegning for å sjekke om endring i bidrag er over grense (totalt)
        grunnlagTilEndringSjekkGrense = grunnlagTilEndringSjekkGrense.copy(
            grunnlagListe = (grunnlagTilEndringSjekkGrense.grunnlagListe + delberegningEndringSjekkGrensePeriodeResultat),
        )
        val delberegningEndringSjekkGrenseResultat = delberegningEndringSjekkGrense(grunnlagTilEndringSjekkGrense, åpenSluttperiode)
        val beregnetBidragErOverMinimumsgrenseForEndring = erOverMinimumsgrenseForEndring(delberegningEndringSjekkGrenseResultat)
        val alleResultatBeløpErNull = erAlleResultatbeløpNull(grunnlagTilEndringSjekkGrense)

        val resultatPeriodeListe = lagResultatPerioder(
            delberegningEndeligBidragResultat = delberegningEndeligBidragResultat.grunnlagListe,
            beregnetBidragErOverMinimumsgrenseForEndring = beregnetBidragErOverMinimumsgrenseForEndring,
            alleResultatBeløpErNull = alleResultatBeløpErNull,
            beløpshistorikkGrunnlag = beløpshistorikkGrunnlag,
        )

        return SjekkMotMinimumsgrenseForEndringResultat(
            resultatPeriodeListe = resultatPeriodeListe,
            delberegningEndringSjekkGrensePeriodeResultat = delberegningEndringSjekkGrensePeriodeResultat,
            delberegningEndringSjekkGrenseResultat = delberegningEndringSjekkGrenseResultat,
        )
    }

    // Sjekk mot minimumsgrense for endring ("12%-regelen") skal bare utføres hvis det ikke er eget tiltak
    private fun skalSjekkeMotMinimumsgrenseForEndring(mottattGrunnlag: BeregnGrunnlag): Boolean = mottattGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<SøknadGrunnlag>(Grunnlagstype.SØKNAD)
        .map { !it.innhold.egetTiltak }
        .firstOrNull() ?: true

    // Henter ut verdi fra delberegning for endring sjekk av grense (her skal det være kun en forekomst)
    private fun erOverMinimumsgrenseForEndring(endringSjekkGrenseGrunnlagliste: List<GrunnlagDto>): Boolean = endringSjekkGrenseGrunnlagliste
        .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(grunnlagType = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
        .map { it.innhold.endringErOverGrense }
        .firstOrNull() ?: true

    // Sjekker om alle resultatbeløp fra sluttberegningen er null
    private fun erAlleResultatbeløpNull(mottattGrunnlag: BeregnGrunnlag): Boolean = mottattGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(grunnlagType = Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
        .filter { it.innhold.resultatBeløp != null }
        .isEmpty()

    private fun filtrerBeløpshistorikkGrunnlag(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> =
        beregnGrunnlag.grunnlagListe.filter { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG }

    // Standardlogikk for å lage resultatperioder
    private fun lagResultatPerioder(
        delberegningEndeligBidragResultat: List<GrunnlagDto>,
        beløpshistorikkPeriodeGrunnlagReferanse: String? = null,
    ): List<ResultatPeriode> = delberegningEndeligBidragResultat
        .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
        .map {
            ResultatPeriode(
                periode = it.innhold.periode,
                resultat = ResultatBeregning(
                    beløp = it.innhold.resultatBeløp,
                ),
                grunnlagsreferanseListe = listOfNotNull(it.referanse, beløpshistorikkPeriodeGrunnlagReferanse),
            )
        }

    // Lager resultatperioder basert på beløpshistorikk hvis beregnet bidrag ikke er over minimumsgrense for endring
    private fun lagResultatPerioder(
        delberegningEndeligBidragResultat: List<GrunnlagDto>,
        beregnetBidragErOverMinimumsgrenseForEndring: Boolean,
        alleResultatBeløpErNull: Boolean,
        beløpshistorikkGrunnlag: List<GrunnlagDto>,
    ): List<ResultatPeriode> {
        // Henter beløpshistorikk (det finnes kun en forekomst, som dekker hele perioden)
        val beløpshistorikkPeriodeGrunnlag = beløpshistorikkGrunnlag
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()

        // Hvis minst en periode er over grenseverdi for endring eller alle resultatbeløp er null kjøres ordinær logikk, med link til evt. beløpshistorikk
        if (beregnetBidragErOverMinimumsgrenseForEndring || alleResultatBeløpErNull) {
            return lagResultatPerioder(delberegningEndeligBidragResultat, beløpshistorikkPeriodeGrunnlag?.referanse)
        }

        // Hvis ingen perioder er over grenseverdi for endring skal beløp fra beløpshistorikken benyttes. Kaster exception hvis beløpshistorikk mangler.
        requireNotNull(beløpshistorikkPeriodeGrunnlag) { "Fant ikke beløpshistorikk for bidrag" }

        val sluttberegningPeriodeGrunnlagListe = delberegningEndeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
            .map {
                SluttberegningPeriodeGrunnlag(
                    referanse = it.referanse,
                    sluttberegningPeriode = it.innhold,
                )
            }

        val resultatperiodeListe = beløpshistorikkPeriodeGrunnlag.beløpshistorikkPeriode.beløpshistorikk
            .map {
                ResultatPeriode(
                    periode = it.periode,
                    resultat = ResultatBeregning(
                        beløp = it.beløp,
                    ),
                    grunnlagsreferanseListe = listOf(beløpshistorikkPeriodeGrunnlag.referanse) + finnSluttberegningReferanserSomMatcher(
                        periode = it.periode,
                        sluttberegningPeriodeGrunnlagListe = sluttberegningPeriodeGrunnlagListe,
                    ),
                )
            }

        return resultatperiodeListe
    }

    private fun finnSluttberegningReferanserSomMatcher(
        periode: ÅrMånedsperiode,
        sluttberegningPeriodeGrunnlagListe: List<SluttberegningPeriodeGrunnlag>,
    ): List<String> = sluttberegningPeriodeGrunnlagListe
        .filter { it.sluttberegningPeriode.periode.inneholder(periode) }
        .map { it.referanse }

    // Sjekker om søknadsbarnet fyller 18 år i beregningsperioden. Justerer i så fall til-periode. Hvis stønadstype er BIDRAG18AAR skal det ikke
    // sjekkes om barnet blir 18 år i perioden,
    private fun justerTilPeriodeHvisBarnetBlir18ÅrIBeregningsperioden(mottattGrunnlag: BeregnGrunnlag): BeregnGrunnlagJustert {
        if (mottattGrunnlag.stønadstype == Stønadstype.BIDRAG18AAR) {
            return BeregnGrunnlagJustert(beregnGrunnlag = mottattGrunnlag, åpenSluttperiode = !mottattGrunnlag.opphørSistePeriode)
        }

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
            BeregnGrunnlagJustert(beregnGrunnlag = mottattGrunnlag, åpenSluttperiode = !mottattGrunnlag.opphørSistePeriode)
        }
    }

    // Rekursiv funksjon som traverserer gjennom alle grunnlag fra toppnivået og nedover og filtrerer bort alle grunnlag som ikke blir referert
    private fun filtrerResultatGrunnlag(
        foreløpigResultatGrunnlagListe: List<GrunnlagDto>,
        refererteReferanserListe: List<String>,
        referanserAlleredeLagtTil: MutableSet<String> = mutableSetOf()
    ): List<GrunnlagDto> {
        // Stopper hvis det ikke finnes flere refererte referanser
        if (refererteReferanserListe.isEmpty()) {
            return emptyList()
        }

        // Filtrer ut grunnlag som er referert og som ikke allerede er lagt til
        val endeligResultatGrunnlagListe = foreløpigResultatGrunnlagListe
            .filter { it.referanse in refererteReferanserListe && it.referanse !in referanserAlleredeLagtTil }

        // Henter ut referanser til neste nivå
        val nesteNivåReferanseListe = endeligResultatGrunnlagListe.flatMap { it.grunnlagsreferanseListe }

        // Legger til referanser som allerede er lagt til
        referanserAlleredeLagtTil.addAll(endeligResultatGrunnlagListe.map { it.referanse })

        // Gjør rekursivt kall og returnerer det endelige resultatet til slutt
        return endeligResultatGrunnlagListe + filtrerResultatGrunnlag(
            foreløpigResultatGrunnlagListe = foreløpigResultatGrunnlagListe,
            refererteReferanserListe = nesteNivåReferanseListe,
            referanserAlleredeLagtTil = referanserAlleredeLagtTil,
        )
    }

    fun beregnMånedsbeløpFaktiskUtgift(faktiskUtgift: BigDecimal, kostpenger: BigDecimal = BigDecimal.ZERO): BigDecimal =
        NettoTilsynsutgiftMapper.beregnMånedsbeløpFaktiskUtgift(faktiskUtgift, kostpenger).avrundetMedToDesimaler

    fun beregnMånedsbeløpTilleggsstønad(tilleggsstønad: BigDecimal): BigDecimal =
        NettoTilsynsutgiftMapper.beregnMånedsbeløpTilleggsstønad(tilleggsstønad).avrundetMedToDesimaler

    data class BeregnGrunnlagJustert(val beregnGrunnlag: BeregnGrunnlag, val åpenSluttperiode: Boolean)

    data class SjekkMotMinimumsgrenseForEndringResultat(
        val resultatPeriodeListe: List<ResultatPeriode>,
        val delberegningEndringSjekkGrensePeriodeResultat: List<GrunnlagDto>,
        val delberegningEndringSjekkGrenseResultat: List<GrunnlagDto>,
    )
}
