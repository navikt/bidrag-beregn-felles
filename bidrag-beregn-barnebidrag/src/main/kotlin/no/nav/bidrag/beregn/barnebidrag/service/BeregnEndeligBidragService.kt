package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndeligBidragBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BeregnEndeligBidragServiceRespons
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.DeltBostedBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnetBorHosBpGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndeligBidragMapper.mapEndeligBidragGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoBarnetilleggMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnetilleggSkattesatsService.delberegningBarnetilleggSkattesats
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoBarnetilleggService.delberegningNettoBarnetillegg
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import java.math.BigDecimal

internal object BeregnEndeligBidragService : BeregnService() {

    fun delberegningEndeligBidrag(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): BeregnEndeligBidragServiceRespons {
        var delberegningBarnetilleggSkattesatsBPResultat = listOf<GrunnlagDto>()
        var delberegningNettoBarnetilleggBPResultat = listOf<GrunnlagDto>()
        var delberegningBarnetilleggSkattesatsBMResultat = listOf<GrunnlagDto>()
        var delberegningNettoBarnetilleggBMResultat = listOf<GrunnlagDto>()

        // Kaller delberegninger for barnetillegg BP
        if (barnetilleggEksisterer(mottattGrunnlag, Grunnlagstype.PERSON_BIDRAGSPLIKTIG)) {
            delberegningBarnetilleggSkattesatsBPResultat =
                delberegningBarnetilleggSkattesats(mottattGrunnlag, Grunnlagstype.PERSON_BIDRAGSPLIKTIG)
            delberegningNettoBarnetilleggBPResultat = delberegningNettoBarnetillegg(
                mottattGrunnlag = (
                    mottattGrunnlag.copy(
                        grunnlagListe = mottattGrunnlag.grunnlagListe + delberegningBarnetilleggSkattesatsBPResultat,
                    )
                    ),
                rolle = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
            )
        }

        // Kaller delberegninger for barnetillegg BM
        if (barnetilleggEksisterer(mottattGrunnlag, Grunnlagstype.PERSON_BIDRAGSMOTTAKER)) {
            delberegningBarnetilleggSkattesatsBMResultat =
                delberegningBarnetilleggSkattesats(mottattGrunnlag, Grunnlagstype.PERSON_BIDRAGSMOTTAKER)
            delberegningNettoBarnetilleggBMResultat = delberegningNettoBarnetillegg(
                mottattGrunnlag = (
                    mottattGrunnlag.copy(
                        grunnlagListe = mottattGrunnlag.grunnlagListe + delberegningBarnetilleggSkattesatsBMResultat,
                    )
                    ),
                rolle = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
            )
        }

        // Legger til delberegningsobjekter i grunnlaget
        val utvidetGrunnlag = mottattGrunnlag.copy(
            grunnlagListe =
            (mottattGrunnlag.grunnlagListe + delberegningNettoBarnetilleggBPResultat + delberegningNettoBarnetilleggBMResultat)
                .distinctBy(GrunnlagDto::referanse),
        )

        // Mapper ut grunnlag som skal brukes for å beregne endelig bidrag
        val endeligBidragPeriodeGrunnlag = mapEndeligBidragGrunnlag(utvidetGrunnlag)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeEndeligBidrag(
            grunnlagListe = endeligBidragPeriodeGrunnlag,
            beregningsperiode = utvidetGrunnlag.periode,
        )

        val endeligBidragBeregningResultatListe = mutableListOf<EndeligBidragPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner endelig bidrag
        bruddPeriodeListe.forEachIndexed { indeks, bruddPeriode ->
            val førsteElement = indeks == 0

            val endeligBidragBeregningGrunnlag = lagEndeligBidragBeregningGrunnlag(
                endeligBidragPeriodeGrunnlag = endeligBidragPeriodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )
            endeligBidragBeregningResultatListe.add(
                EndeligBidragPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregning.beregn(endeligBidragBeregningGrunnlag, førsteElement),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (endeligBidragBeregningResultatListe.isNotEmpty()) {
            val sisteElement = endeligBidragBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                endeligBidragBeregningResultatListe[endeligBidragBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Løper gjennom resultatlista for å sjekke om:
        // - Det finnes elementer hvor det er begrenset revurdering og beregnet bidrag er lavere enn løpende bidrag (hvis beregnet bidrag > 0)
        // - Det finnes elementer hvor løpende forskudd mangler (vil kun være aktuelt i første element)
        // Disse situasjonene skal resultere i exception lenger ned i koden. Selve exceptionen kastes i BeregnBarnebidragService
        // Exception hvor løpende forskudd mangler har forrang
        var feilmelding = "Kan ikke fatte vedtak fordi løpende forskudd mangler i første beregningsperiode:"
        val perioderMedFeilListe = mutableListOf<ÅrMånedsperiode>()
        var skalKasteBegrensetRevurderingException = false
        endeligBidragBeregningResultatListe.forEach {
            if (it.resultat.løpendeForskuddMangler) {
                skalKasteBegrensetRevurderingException = true
                val periodeTil = it.periode.til ?: ""
                feilmelding += " ${it.periode.fom} - $periodeTil,"
                perioderMedFeilListe.add(it.periode)
            }
        }

        if (!skalKasteBegrensetRevurderingException) {
            feilmelding = "Kan ikke fatte vedtak fordi beregnet bidrag for følgende perioder er lavere enn løpende bidrag:"
            endeligBidragBeregningResultatListe.forEach {
                if ((it.resultat.beregnetBidragErLavereEnnLøpendeBidrag) && (it.resultat.beregnetBeløp!! > BigDecimal.ZERO)) {
                    skalKasteBegrensetRevurderingException = true
                    val periodeTil = it.periode.til ?: ""
                    feilmelding += " ${it.periode.fom} - $periodeTil,"
                    perioderMedFeilListe.add(it.periode)
                }
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = endeligBidragBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = utvidetGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning endelig bidrag (sluttberegning)
        resultatGrunnlagListe.addAll(
            mapDelberegningEndeligBidrag(
                endeligBidragPeriodeResultatListe = endeligBidragBeregningResultatListe,
                mottattGrunnlag = utvidetGrunnlag,
            ),
        )

        // Legger til delberegningsobjekter som er brukt av barnetillegg skattesats og netto barnetillegg
        resultatGrunnlagListe.addAll(delberegningBarnetilleggSkattesatsBPResultat)
        resultatGrunnlagListe.addAll(delberegningBarnetilleggSkattesatsBMResultat)
        resultatGrunnlagListe.addAll(delberegningNettoBarnetilleggBPResultat)
        resultatGrunnlagListe.addAll(delberegningNettoBarnetilleggBMResultat)

        val resultat = resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }

        feilmelding = if (skalKasteBegrensetRevurderingException) {
            feilmelding.dropLast(1)
        } else {
            ""
        }

        return BeregnEndeligBidragServiceRespons(
            grunnlagListe = resultat,
            feilmelding = feilmelding,
            perioderMedFeilListe = perioderMedFeilListe,
            skalKasteBegrensetRevurderingException = skalKasteBegrensetRevurderingException,
        )
    }

    // Sjekker om barnetillegg eksisterer for en gitt rolle
    private fun barnetilleggEksisterer(mottattGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): Boolean = mottattGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåFremmedReferanse<InntektsrapporteringPeriode>(
            grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
            referanse = finnReferanseTilRolle(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagstype = rolle,
            ),
        )
        .filter { it.innhold.inntektsrapportering == Inntektsrapportering.BARNETILLEGG }
        .any { it.innhold.gjelderBarn == mottattGrunnlag.søknadsbarnReferanse }

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeEndeligBidrag(
        grunnlagListe: EndeligBidragPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.bidragsevneDelberegningPeriodeGrunnlagListe.asSequence().map { it.bidragsevnePeriode.periode })
            .plus(grunnlagListe.underholdskostnadDelberegningPeriodeGrunnlagListe.asSequence().map { it.underholdskostnadPeriode.periode })
            .plus(
                grunnlagListe.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe.asSequence()
                    .map { it.bpAndelUnderholdskostnadPeriode.periode },
            )
            .plus(grunnlagListe.samværsfradragDelberegningPeriodeGrunnlagListe.asSequence().map { it.samværsfradragPeriode.periode })
            .plus(grunnlagListe.samværsklassePeriodeGrunnlagListe.asSequence().map { it.samværsklassePeriode.periode })
            .plus(grunnlagListe.bostatusPeriodeGrunnlagListe.asSequence().map { it.bostatusPeriode.periode })
            .plus(grunnlagListe.nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe.asSequence().map { it.nettoBarnetilleggPeriode.periode })
            .plus(grunnlagListe.nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe.asSequence().map { it.nettoBarnetilleggPeriode.periode })
            .let { periode ->
                grunnlagListe.beløpshistorikkForskuddPeriodeGrunnlag?.beløpshistorikkPeriode?.beløpshistorikk?.asSequence()?.map { it.periode }
                    ?.let { periode.plus(it) } ?: periode
            }
            .let { periode ->
                grunnlagListe.beløpshistorikkBidragPeriodeGrunnlag?.beløpshistorikkPeriode?.beløpshistorikk?.asSequence()?.map { it.periode }
                    ?.let { periode.plus(it) } ?: periode
            }

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for endelig bidrag beregning som ligger innenfor bruddPeriode
    private fun lagEndeligBidragBeregningGrunnlag(
        endeligBidragPeriodeGrunnlag: EndeligBidragPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): EndeligBidragBeregningGrunnlag {
        val bidragsevneBeregningGrunnlag = endeligBidragPeriodeGrunnlag.bidragsevneDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bidragsevnePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BidragsevneDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.bidragsevnePeriode.beløp,
                    sumInntekt25Prosent = it.bidragsevnePeriode.sumInntekt25Prosent,
                )
            }
            ?: throw IllegalArgumentException("Bidragsevne grunnlag mangler for periode $bruddPeriode")
        val underholdskostnadBeregningGrunnlag = endeligBidragPeriodeGrunnlag.underholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.underholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                UnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.underholdskostnadPeriode.underholdskostnad,
                )
            }
            ?: throw IllegalArgumentException("Underholdskostnad grunnlag mangler for periode $bruddPeriode")
        val bpAndelUnderholdskostnadBeregningGrunnlag = endeligBidragPeriodeGrunnlag.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bpAndelUnderholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    andelBeløp = it.bpAndelUnderholdskostnadPeriode.andelBeløp,
                    andelFaktor = it.bpAndelUnderholdskostnadPeriode.endeligAndelFaktor,
                    barnetErSelvforsørget = it.bpAndelUnderholdskostnadPeriode.barnetErSelvforsørget,
                )
            }
            ?: throw IllegalArgumentException("BP andel underholdskostnad grunnlag mangler for periode $bruddPeriode")
        val samværsfradragBeregningGrunnlag = endeligBidragPeriodeGrunnlag.samværsfradragDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.samværsfradragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { SamværsfradragDelberegningBeregningGrunnlag(referanse = it.referanse, beløp = it.samværsfradragPeriode.beløp) }
            ?: throw IllegalArgumentException("Samværsfradrag grunnlag mangler for periode $bruddPeriode")
        val deltBostedBeregningGrunnlag = endeligBidragPeriodeGrunnlag.samværsklassePeriodeGrunnlagListe
            .firstOrNull { it.samværsklassePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                DeltBostedBeregningGrunnlag(
                    referanse = it.referanse,
                    deltBosted = it.samværsklassePeriode.samværsklasse == Samværsklasse.DELT_BOSTED,
                )
            }
            ?: throw IllegalArgumentException("Delt bosted grunnlag mangler for periode $bruddPeriode")
        val søknadsbarnetBorHosBpGrunnlag = endeligBidragPeriodeGrunnlag.bostatusPeriodeGrunnlagListe
            .firstOrNull { it.bostatusPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                SøknadsbarnetBorHosBpGrunnlag(
                    referanse = it.referanse,
                    søknadsbarnetBorHosBp = it.bostatusPeriode.bostatus == Bostatuskode.MED_FORELDER,
                )
            }
            ?: throw IllegalArgumentException("Bostatus grunnlag mangler for periode $bruddPeriode")
        val barnetilleggBPBeregningGrunnlag = endeligBidragPeriodeGrunnlag.nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.nettoBarnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.nettoBarnetilleggPeriode.summertNettoBarnetillegg,
                )
            }
        val barnetilleggBMBeregningGrunnlag = endeligBidragPeriodeGrunnlag.nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.nettoBarnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.nettoBarnetilleggPeriode.summertNettoBarnetillegg,
                )
            }
        // Hvis grunnlaget er null settes beløpet til null. Hvis grunnlaget finnes, men perioden mangler settes beløpet til 0
        val løpendeForskuddBeløp = endeligBidragPeriodeGrunnlag.beløpshistorikkForskuddPeriodeGrunnlag?.let { grunnlag ->
            grunnlag.beløpshistorikkPeriode.beløpshistorikk
                .firstOrNull { it.periode.inneholder(bruddPeriode) }?.beløp ?: BigDecimal.ZERO
        }
        // Hvis grunnlaget er null settes beløpet til null. Hvis grunnlaget finnes, men perioden mangler settes beløpet til 0
        val løpendeBidragBeløp = endeligBidragPeriodeGrunnlag.beløpshistorikkBidragPeriodeGrunnlag?.let { grunnlag ->
            grunnlag.beløpshistorikkPeriode.beløpshistorikk
                .firstOrNull { it.periode.inneholder(bruddPeriode) }?.beløp ?: BigDecimal.ZERO
        }
        // Kaster exception hvis det skal utføres begrenset revurdering, men beløpshistorikk for forskudd eller bidrag mangler
        val utførBegrensetRevurdering = endeligBidragPeriodeGrunnlag.begrensetRevurderingPeriodeGrunnlag?.begrensetRevurdering ?: false
        require(!(utførBegrensetRevurdering && (løpendeForskuddBeløp == null || løpendeBidragBeløp == null))) {
            "Beløpshistorikk grunnlag mangler for begrenset revurdering"
        }

        // Legger til referanser for grunnlagsobjekter som ikke er lister (skal refereres av alle perioder hvis det er begrenset revurdering
        val engangsreferanser = listOfNotNull(
            endeligBidragPeriodeGrunnlag.beløpshistorikkForskuddPeriodeGrunnlag?.referanse,
            endeligBidragPeriodeGrunnlag.beløpshistorikkBidragPeriodeGrunnlag?.referanse,
            endeligBidragPeriodeGrunnlag.begrensetRevurderingPeriodeGrunnlag?.referanse,
        )

        return EndeligBidragBeregningGrunnlag(
            bidragsevneBeregningGrunnlag = bidragsevneBeregningGrunnlag,
            underholdskostnadBeregningGrunnlag = underholdskostnadBeregningGrunnlag,
            bpAndelUnderholdskostnadBeregningGrunnlag = bpAndelUnderholdskostnadBeregningGrunnlag,
            samværsfradragBeregningGrunnlag = samværsfradragBeregningGrunnlag,
            deltBostedBeregningGrunnlag = deltBostedBeregningGrunnlag,
            søknadsbarnetBorHosBpGrunnlag = søknadsbarnetBorHosBpGrunnlag,
            barnetilleggBPBeregningGrunnlag = barnetilleggBPBeregningGrunnlag,
            barnetilleggBMBeregningGrunnlag = barnetilleggBMBeregningGrunnlag,
            løpendeForskuddBeløp = løpendeForskuddBeløp,
            løpendeBidragBeløp = løpendeBidragBeløp,
            utførBegrensetRevurdering = utførBegrensetRevurdering,
            engangsreferanser = engangsreferanser,
        )
    }

    // Mapper ut DelberegningEndeligBidrag
    private fun mapDelberegningEndeligBidrag(
        endeligBidragPeriodeResultatListe: List<EndeligBidragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = endeligBidragPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettSluttberegningreferanse(
                    barnreferanse = mottattGrunnlag.søknadsbarnReferanse,
                    periode = it.periode,
                ),
                type = Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
                innhold = POJONode(
                    SluttberegningBarnebidrag(
                        periode = it.periode,
                        beregnetBeløp = it.resultat.beregnetBeløp,
                        resultatBeløp = it.resultat.resultatBeløp,
                        uMinusNettoBarnetilleggBM = it.resultat.uMinusNettoBarnetilleggBM,
                        bruttoBidragEtterBarnetilleggBM = it.resultat.bruttoBidragEtterBarnetilleggBM,
                        nettoBidragEtterBarnetilleggBM = it.resultat.nettoBidragEtterBarnetilleggBM,
                        bruttoBidragJustertForEvneOg25Prosent = it.resultat.bruttoBidragJustertForEvneOg25Prosent,
                        bruttoBidragEtterBegrensetRevurdering = it.resultat.bruttoBidragEtterBegrensetRevurdering,
                        bruttoBidragEtterBarnetilleggBP = it.resultat.bruttoBidragEtterBarnetilleggBP,
                        nettoBidragEtterSamværsfradrag = it.resultat.nettoBidragEtterSamværsfradrag,
                        bpAndelAvUVedDeltBostedFaktor = it.resultat.bpAndelAvUVedDeltBostedFaktor,
                        bpAndelAvUVedDeltBostedBeløp = it.resultat.bpAndelAvUVedDeltBostedBeløp,
                        løpendeForskudd = it.resultat.løpendeForskudd,
                        løpendeBidrag = it.resultat.løpendeBidrag,
                        barnetErSelvforsørget = it.resultat.barnetErSelvforsørget,
                        bidragJustertForDeltBosted = it.resultat.bidragJustertForDeltBosted,
                        bidragJustertForNettoBarnetilleggBP = it.resultat.bidragJustertForNettoBarnetilleggBP,
                        bidragJustertForNettoBarnetilleggBM = it.resultat.bidragJustertForNettoBarnetilleggBM,
                        bidragJustertNedTilEvne = it.resultat.bidragJustertNedTilEvne,
                        bidragJustertNedTil25ProsentAvInntekt = it.resultat.bidragJustertNedTil25ProsentAvInntekt,
                        bidragJustertTilForskuddssats = it.resultat.bidragJustertTilForskuddssats,
                        begrensetRevurderingUtført = it.resultat.begrensetRevurderingUtført,
                        ikkeOmsorgForBarnet = it.resultat.ikkeOmsorgForBarnet,
                    ),
                ),
                grunnlagsreferanseListe = (it.resultat.grunnlagsreferanseListe + mottattGrunnlag.søknadsbarnReferanse).sorted(),
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }
}
