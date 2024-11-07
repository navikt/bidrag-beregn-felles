package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.BpAndelUnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.InntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.BpAndelUnderholdskostnadMapper.mapBpAndelUnderholdskostnadGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnBpAndelUnderholdskostnadService : BeregnService() {

    fun delberegningBpAndelUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        val referanseTilBM = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.bpAndelUnderholdskostnad }

        // Henter sjablonverdi for kapitalinntekt
        val innslagKapitalinntektSjablon = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag)

        // Mapper ut grunnlag som skal brukes for å beregne bidragsevne
        val bpAndelUnderholdskostnadPeriodeGrunnlag = mapBpAndelUnderholdskostnadGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBpAndelUnderholdskostnad(
            grunnlagListe = bpAndelUnderholdskostnadPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val bpAndelUnderholdskostnadBeregningResultatListe = mutableListOf<BpAndelUnderholdskostnadPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner BP's andel av underholdskostnad
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bpAndelUnderholdskostnadBeregningGrunnlag =
                lagBpAndelUnderholdskostnadBeregningGrunnlag(
                    bpAndelUnderholdskostnadPeriodeGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            bpAndelUnderholdskostnadBeregningResultatListe.add(
                BpAndelUnderholdskostnadPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = BpAndelUnderholdskostnadBeregning.beregn(bpAndelUnderholdskostnadBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapBpAndelUnderholdskostnadResultatGrunnlag(
            bpAndelUnderholdskostnadBeregningResultatListe = bpAndelUnderholdskostnadBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut "sub"-delberegninger
        resultatGrunnlagListe.addAll(
            mapDelberegninger(
                mottattGrunnlag = mottattGrunnlag,
                bpAndelUnderholdskostnadPeriodeGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag,
                bpAndelUnderholdskostnadBeregningResultatListe = bpAndelUnderholdskostnadBeregningResultatListe,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilBP = referanseTilBP,
                referanseTilBM = referanseTilBM,
            ),
        )

        // Mapper ut grunnlag for delberegning BP's andel av underholdskostnad
        resultatGrunnlagListe.addAll(
            mapDelberegningBpAndelUnderholdskostnad(
                bpAndelUnderholdskostnadPeriodeResultatListe = bpAndelUnderholdskostnadBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
                referanseTilBP = referanseTilBP,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning)

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBpAndelUnderholdskostnad(
        grunnlagListe: BpAndelUnderholdskostnadPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        // inntektSBPerioder er nullable
        val inntektSBPerioder = grunnlagListe.inntektSBPeriodeGrunnlagListe.asSequence()
            .map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) }
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.underholdskostnadDelberegningPeriodeGrunnlagListe.asSequence().map { it.underholdskostnadPeriode.periode })
            .plus(grunnlagListe.inntektBPPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.inntektBMPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(inntektSBPerioder)
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for BP's andel underholdskostnad som ligger innenfor bruddPeriode
    private fun lagBpAndelUnderholdskostnadBeregningGrunnlag(
        bpAndelUnderholdskostnadPeriodeGrunnlag: BpAndelUnderholdskostnadPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BpAndelUnderholdskostnadBeregningGrunnlag = BpAndelUnderholdskostnadBeregningGrunnlag(
        underholdskostnadBeregningGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag.underholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.underholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let { UnderholdskostnadBeregningGrunnlag(referanse = it.referanse, beløp = it.underholdskostnadPeriode.beløp) }
            ?: throw IllegalArgumentException("Underholdskostnad grunnlag mangler for periode $bruddPeriode"),
        inntektBPBeregningGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektBPPeriodeGrunnlagListe
            .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.beløp) }
            ?: throw IllegalArgumentException("Delberegning sum inntekt for bidragspliktig mangler for periode $bruddPeriode"),
        inntektBMBeregningGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektBMPeriodeGrunnlagListe
            .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.beløp) }
            ?: throw IllegalArgumentException("Delberegning sum inntekt for bidragsmottaker mangler for periode $bruddPeriode"),
        inntektSBBeregningGrunnlag = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektSBPeriodeGrunnlagListe
            .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.beløp) }
            ?: throw IllegalArgumentException("Delberegning sum inntekt for søknadsbarn mangler for periode $bruddPeriode"),
        sjablonSjablontallBeregningGrunnlagListe = bpAndelUnderholdskostnadPeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
            .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonSjablontallBeregningGrunnlag(
                    referanse = it.referanse,
                    type = it.sjablonSjablontallPeriode.sjablon.navn,
                    verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                )
            },
    )

    private fun mapBpAndelUnderholdskostnadResultatGrunnlag(
        bpAndelUnderholdskostnadBeregningResultatListe: List<BpAndelUnderholdskostnadPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            bpAndelUnderholdskostnadBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Matcher sjablongrunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = sjablonGrunnlag,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        return resultatGrunnlagListe
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) = grunnlagListe
        .filter { grunnlagReferanseListe.contains(it.referanse) }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = it.type,
                innhold = it.innhold,
                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                gjelderReferanse = it.gjelderReferanse,
            )
        }

    // Mapper ut DelberegningBpAndelUnderholdskostnad
    private fun mapDelberegningBpAndelUnderholdskostnad(
        bpAndelUnderholdskostnadPeriodeResultatListe: List<BpAndelUnderholdskostnadPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        referanseTilBP: String,
    ): List<GrunnlagDto> = bpAndelUnderholdskostnadPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = referanseTilBP,
                ),
                type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                innhold = POJONode(
                    DelberegningBidragspliktigesAndel(
                        periode = it.periode,
                        endeligAndelFaktor = it.resultat.endeligAndelFaktor,
                        andelBeløp = it.resultat.andelBeløp,
                        beregnetAndelFaktor = it.resultat.beregnetAndelFaktor,
                        barnEndeligInntekt = it.resultat.barnEndeligInntekt,
                        barnetErSelvforsørget = it.resultat.barnetErSelvforsørget,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = referanseTilBP,
            )
        }

    private fun mapDelberegninger(
        mottattGrunnlag: BeregnGrunnlag,
        bpAndelUnderholdskostnadPeriodeGrunnlag: BpAndelUnderholdskostnadPeriodeGrunnlag,
        bpAndelUnderholdskostnadBeregningResultatListe: List<BpAndelUnderholdskostnadPeriodeResultat>,
        innslagKapitalinntektSjablon: Sjablontall?,
        referanseTilBP: String,
        referanseTilBM: String,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            bpAndelUnderholdskostnadBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Mapper ut DelberegningSumInntekt BP. Inntektskategorier summeres opp.
        val sumInntektBPListe = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektBPPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektBPListe,
                beregnGrunnlag = mottattGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = referanseTilBP,
            ),
        )

        // Mapper ut DelberegningSumInntekt BM. Inntektskategorier summeres opp.
        val sumInntektBMListe = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektBMPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektBMListe,
                beregnGrunnlag = mottattGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = referanseTilBM,
            ),
        )

        // Mapper ut DelberegningSumInntekt SB. Inntektskategorier summeres opp.
        val sumInntektSBListe = bpAndelUnderholdskostnadPeriodeGrunnlag.inntektSBPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektSBListe,
                beregnGrunnlag = mottattGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = mottattGrunnlag.søknadsbarnReferanse,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            sumInntektBPListe.flatMap { it.grunnlagsreferanseListe }
                .union(
                    sumInntektBMListe.flatMap { it.grunnlagsreferanseListe }
                        .union(
                            sumInntektSBListe.flatMap { it.grunnlagsreferanseListe },
                        ),
                )
                .distinct()

        resultatGrunnlagListe.addAll(
            mottattGrunnlag.grunnlagListe
                .filter { it.referanse in delberegningReferanseListe }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        return resultatGrunnlagListe
    }

    // Henter sjablonverdi for kapitalinntekt
    // TODO Bør synkes med som ligger i CoreMapper. Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003).
    // TODO Logikken her må utvides hvis det legges inn nye sjablonverdier
    private fun finnInnslagKapitalinntektFraGrunnlag(sjablonListe: List<GrunnlagDto>): Sjablontall? = sjablonListe
        .filter { it.referanse.contains(SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.navn) }
        .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
        .firstOrNull()
        ?.let { innslagKapitalinntektSjablon ->
            Sjablontall(
                typeSjablon = innslagKapitalinntektSjablon.innhold.sjablon.navn,
                datoFom = innslagKapitalinntektSjablon.innhold.periode.fom.atDay(1),
                datoTom = innslagKapitalinntektSjablon.innhold.periode.til?.atEndOfMonth()?.minusMonths(1),
                verdi = innslagKapitalinntektSjablon.innhold.verdi,
            )
        }
}
