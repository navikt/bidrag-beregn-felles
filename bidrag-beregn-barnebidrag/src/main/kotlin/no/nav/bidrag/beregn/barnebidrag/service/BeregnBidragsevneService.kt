package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnIHusstandenBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.InntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.VoksneIHusstandenBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.mapBidragsevneGrunnlag
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningVoksneIHustand
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnBidragsevneService : BeregnService() {

    fun delberegningBidragsevne(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {

        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.bidragsevne }

        // Henter sjablonverdi for kapitalinntekt
        val innslagKapitalinntektSjablon = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag)

        // Mapper ut grunnlag som skal brukes for å beregne bidragsevne
        val bidragsevnePeriodeGrunnlag = mapBidragsevneGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBidragsevne(bidragsevnePeriodeGrunnlag, mottattGrunnlag.periode)

        val bidragsevneBeregningResultatListe = mutableListOf<BidragsevnePeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidragsevne
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragsevneBeregningGrunnlag = lagBidragsevneBeregningGrunnlag(bidragsevnePeriodeGrunnlag, bruddPeriode)
            bidragsevneBeregningResultatListe.add(
                BidragsevnePeriodeResultat(
                    periode = bruddPeriode,
                    resultat = BidragsevneBeregning.beregn(bidragsevneBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapBidragsevneResultatGrunnlag(
            bidragsevneBeregningResultatListe = bidragsevneBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut "sub"-delberegninger
        resultatGrunnlagListe.addAll(
            mapDelberegninger(
                mottattGrunnlag = mottattGrunnlag,
                bidragsevnePeriodeGrunnlag = bidragsevnePeriodeGrunnlag,
                bidragsevneBeregningResultatListe = bidragsevneBeregningResultatListe,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilBP = referanseTilBP,
            )
        )

        // Mapper ut grunnlag for delberegning bidragsevne
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragsevne(
                bidragsevnePeriodeResultatListe = bidragsevneBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning) +
            mapSjablonBidragsevneGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonBidragsevne()) +
            mapSjablonTrinnvisSkattesatsGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonTrinnvisSkattesats())

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBidragsevne(
        grunnlagListe: BidragsevnePeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.inntektBPPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.barnIHusstandenPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.voksneIHusstandenPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })
            .plus(grunnlagListe.sjablonBidragsevnePeriodeGrunnlagListe.asSequence().map { it.sjablonBidragsevnePeriode.periode })
            .plus(grunnlagListe.sjablonTrinnvisSkattesatsPeriodeGrunnlagListe.asSequence().map { it.sjablonTrinnvisSkattesatsPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for bidragsevneberegning som ligger innenfor bruddPeriode
    private fun lagBidragsevneBeregningGrunnlag(
        bidragsevnePeriodeGrunnlag: BidragsevnePeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BidragsevneBeregningGrunnlag {

        val borMedAndre = bidragsevnePeriodeGrunnlag.voksneIHusstandenPeriodeGrunnlagListe
            .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            ?.borMedAndre
        val bostatus = if (borMedAndre == true) "GS" else "EN"

        return BidragsevneBeregningGrunnlag(
            inntektBPBeregningGrunnlag = bidragsevnePeriodeGrunnlag.inntektBPPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.beløp) }
                ?: throw IllegalArgumentException("Ingen inntekt funnet for periode $bruddPeriode"),
            barnIHusstandenBeregningGrunnlag = bidragsevnePeriodeGrunnlag.barnIHusstandenPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { BarnIHusstandenBeregningGrunnlag(referanse = it.referanse, antallBarn = it.antall) }
                ?: throw IllegalArgumentException("Ingen barn i husstanden funnet for periode $bruddPeriode"),
            voksneIHusstandenBeregningGrunnlag = bidragsevnePeriodeGrunnlag.voksneIHusstandenPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { VoksneIHusstandenBeregningGrunnlag(referanse = it.referanse, borMedAndre = it.borMedAndre) }
                ?: throw IllegalArgumentException("Ingen voksne i husstanden funnet for periode $bruddPeriode"),
            sjablonSjablontallBeregningGrunnlagListe = bidragsevnePeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
                .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
                .map {
                    SjablonSjablontallBeregningGrunnlag(
                        referanse = it.referanse,
                        type = it.sjablonSjablontallPeriode.sjablon.navn,
                        verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                    )
                },
            sjablonBidragsevneBeregningGrunnlagListe = bidragsevnePeriodeGrunnlag.sjablonBidragsevnePeriodeGrunnlagListe
                .filter { it.sjablonBidragsevnePeriode.periode.inneholder(bruddPeriode) }
                .filter { it.sjablonBidragsevnePeriode.bostatus == bostatus }
                .map {
                    SjablonBidragsevneBeregningGrunnlag(
                        referanse = it.referanse,
                        bostatus = it.sjablonBidragsevnePeriode.bostatus,
                        boutgift = it.sjablonBidragsevnePeriode.boutgiftBeløp,
                        underhold = it.sjablonBidragsevnePeriode.underholdBeløp,
                    )
                },
            sjablonTrinnvisSkattesatsBeregningGrunnlag = bidragsevnePeriodeGrunnlag.sjablonTrinnvisSkattesatsPeriodeGrunnlagListe
                .firstOrNull { it.sjablonTrinnvisSkattesatsPeriode.periode.inneholder(bruddPeriode) }
                ?.let {
                    SjablonTrinnvisSkattesatsBeregningGrunnlag(
                        referanse = it.referanse,
                        trinnliste = it.sjablonTrinnvisSkattesatsPeriode.trinnliste,
                    )
                }
                ?: throw IllegalArgumentException("Ingen sjablonverdier for trinnvis skattesats funnet for periode $bruddPeriode"),
        )
    }

    private fun mapBidragsevneResultatGrunnlag(
        bidragsevneBeregningResultatListe: List<BidragsevnePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            bidragsevneBeregningResultatListe
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

    // Mapper ut DelberegningBidragsevne
    private fun mapDelberegningBidragsevne(
        bidragsevnePeriodeResultatListe: List<BidragsevnePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = bidragsevnePeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                    periode = it.periode,
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                innhold = POJONode(
                    DelberegningBidragsevne(
                        periode = it.periode,
                        beløp = it.resultat.bidragsevne,
                        skatt = DelberegningBidragsevne.Skatt(
                            minstefradrag = it.resultat.minstefradrag,
                            skattAlminneligInntekt = it.resultat.skattAlminneligInntekt,
                            trinnskatt = it.resultat.trinnskatt,
                            trygdeavgift = it.resultat.trygdeavgift,
                            sumSkatt = it.resultat.sumSkatt,
                        ),
                        underholdBarnEgenHusstand = it.resultat.underholdBarnEgenHusstand,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderReferanse = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
            )
        }


    private fun mapDelberegninger(
        mottattGrunnlag: BeregnGrunnlag,
        bidragsevnePeriodeGrunnlag: BidragsevnePeriodeGrunnlag,
        bidragsevneBeregningResultatListe: List<BidragsevnePeriodeResultat>,
        innslagKapitalinntektSjablon: Sjablontall?,
        referanseTilBP: String
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            bidragsevneBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        val sumInntektListe = bidragsevnePeriodeGrunnlag.inntektBPPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektListe,
                beregnGrunnlag = mottattGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = referanseTilBP,
            ),
        )

        // Mapper ut DelberegningBarnIHusstand
        val sumAntallBarnListe = bidragsevnePeriodeGrunnlag.barnIHusstandenPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningBarnIHusstand(
                sumAntallBarnListe = sumAntallBarnListe,
                bidragspliktigReferanse = referanseTilBP,
            ),
        )

        // Mapper ut DelberegningVoksneIHusstand
        val voksneIHusstandenListe = bidragsevnePeriodeGrunnlag.voksneIHusstandenPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningVoksneIHusstand(
                voksneIHusstandenListe = voksneIHusstandenListe,
                bidragspliktigReferanse = referanseTilBP,
            ),
        )


        // Lager en liste av referanser som refereres til av delberegningene og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            sumInntektListe.flatMap { it.grunnlagsreferanseListe }
                .union(
                    sumAntallBarnListe.flatMap { it.grunnlagsreferanseListe }
                        .union(
                            voksneIHusstandenListe.flatMap { it.grunnlagsreferanseListe },
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
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                        gjelderReferanse = it.gjelderReferanse,
                    )
                }
        )

        return resultatGrunnlagListe
    }

    // Mapper ut DelberegningBarnIHusstand
    private fun mapDelberegningBarnIHusstand(sumAntallBarnListe: List<BarnIHusstandenPeriodeCore>, bidragspliktigReferanse: String) =
        sumAntallBarnListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningBarnIHusstand(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            antallBarn = it.antall,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                    gjelderReferanse = bidragspliktigReferanse,
                )
            }

    // Mapper ut DelberegningVoksneIHusstand
    private fun mapDelberegningVoksneIHusstand(voksneIHusstandenListe: List<VoksneIHusstandenPeriodeCore>, bidragspliktigReferanse: String) =
        voksneIHusstandenListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningVoksneIHustand(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            borMedAndreVoksne = it.borMedAndre,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                    gjelderReferanse = bidragspliktigReferanse,
                )
            }

    // Henter sjablonverdi for kapitalinntekt
    // TODO Bør synkes med som ligger i CoreMapper. Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003).
    // TODO Logikken her må utvides hvis det legges inn nye sjablonverdier
    private fun finnInnslagKapitalinntektFraGrunnlag(sjablonListe: List<GrunnlagDto>): Sjablontall? = sjablonListe
        .filter { it.referanse.contains("SJABLONTALL") }
        .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
        .firstOrNull { it.innhold.sjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP }
        ?.let { innslagKapitalinntektSjablon ->
            Sjablontall(
                typeSjablon = innslagKapitalinntektSjablon.innhold.sjablon.navn,
                datoFom = innslagKapitalinntektSjablon.innhold.periode.fom.atDay(1),
                datoTom = innslagKapitalinntektSjablon.innhold.periode.til?.atEndOfMonth()?.minusMonths(1),
                verdi = innslagKapitalinntektSjablon.innhold.verdi,
            )
        }
}
