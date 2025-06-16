package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnIHusstandenBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevnePeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.InntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.VoksneIHusstandenBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.finnInnslagKapitalinntektFraGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.mapBidragsevneGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.mapping.mapTilGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal

internal object BeregnBidragsevneService : BeregnService() {

    fun delberegningBidragsevne(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        val referanseTilBP = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.bidragsevne }

        // Henter sjablonverdi for kapitalinntekt
        val innslagKapitalinntektSjablon = finnInnslagKapitalinntektFraGrunnlag(sjablonGrunnlag)

        // Mapper ut grunnlag som skal brukes for å beregne bidragsevne
        val bidragsevnePeriodeGrunnlag =
            mapBidragsevneGrunnlag(
                mottattGrunnlag = mottattGrunnlag,
                sjablonGrunnlag = sjablonGrunnlag,
                åpenSluttperiode = åpenSluttperiode,
                innslagKapitalInntekt = innslagKapitalinntektSjablon?.verdi ?: BigDecimal.ZERO,
            )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBidragsevne(
            grunnlagListe = bidragsevnePeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val bidragsevneBeregningResultatListe = mutableListOf<BidragsevnePeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidragsevne
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragsevneBeregningGrunnlag = lagBidragsevneBeregningGrunnlag(
                bidragsevnePeriodeGrunnlag = bidragsevnePeriodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )
            bidragsevneBeregningResultatListe.add(
                BidragsevnePeriodeResultat(
                    periode = bruddPeriode,
                    resultat = BidragsevneBeregning.beregn(bidragsevneBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (bidragsevneBeregningResultatListe.isNotEmpty()) {
            val sisteElement = bidragsevneBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                bidragsevneBeregningResultatListe[bidragsevneBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = bidragsevneBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
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
                åpenSluttperiode = åpenSluttperiode,
            ),
        )

        // Mapper ut grunnlag for delberegning bidragsevne
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragsevne(
                bidragsevnePeriodeResultatListe = bidragsevneBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
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
            .plus(grunnlagListe.boforholdPeriodeGrunnlagListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
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
        val borMedAndre = bidragsevnePeriodeGrunnlag.boforholdPeriodeGrunnlagListe
            .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            ?.borMedAndreVoksne
        val bostatus = if (borMedAndre == true) "GS" else "EN"

        return BidragsevneBeregningGrunnlag(
            inntektBPBeregningGrunnlag = bidragsevnePeriodeGrunnlag.inntektBPPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.beløp) }
                ?: throw IllegalArgumentException("Delberegning sum inntekt for bidragspliktig mangler for periode $bruddPeriode"),
            barnIHusstandenBeregningGrunnlag = bidragsevnePeriodeGrunnlag.boforholdPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { BarnIHusstandenBeregningGrunnlag(referanse = it.referanse, antallBarn = it.antallBarn) }
                ?: throw IllegalArgumentException("Grunnlag barn i husstanden mangler for periode $bruddPeriode"),
            voksneIHusstandenBeregningGrunnlag = bidragsevnePeriodeGrunnlag.boforholdPeriodeGrunnlagListe
                .firstOrNull { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                ?.let { VoksneIHusstandenBeregningGrunnlag(referanse = it.referanse, borMedAndre = it.borMedAndreVoksne) }
                ?: throw IllegalArgumentException("Grunnlag voksne i husstanden mangler for periode $bruddPeriode"),
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

    // Mapper ut DelberegningBidragsevne
    private fun mapDelberegningBidragsevne(
        bidragsevnePeriodeResultatListe: List<BidragsevnePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = bidragsevnePeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = finnReferanseTilRolle(
                        grunnlagListe = mottattGrunnlag.grunnlagListe,
                        grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                    ),
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
                            sumSkattFaktor = it.resultat.sumSkattFaktor,
                        ),
                        underholdBarnEgenHusstand = it.resultat.underholdBarnEgenHusstand,
                        sumInntekt25Prosent = it.resultat.sumInntekt25Prosent,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.distinct().sorted(),
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
        referanseTilBP: String,
        åpenSluttperiode: Boolean,
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

        // Mapper ut DelberegningBoforhold
        val boforholdListe = bidragsevnePeriodeGrunnlag.boforholdPeriodeGrunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
            .toMutableList()
        if (boforholdListe.isNotEmpty()) {
            val sisteElement = boforholdListe.last()
            // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
            if (sisteElement.periode.datoTil != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(datoTil = null))
                boforholdListe[boforholdListe.size - 1] = oppdatertSisteElement
            }
        }
        resultatGrunnlagListe.addAll(
            boforholdListe.mapTilGrunnlag(referanseTilBP),
        )

        // Mapper ut DelberegningBarnIHusstand (sub-delberegning til DelberegningBoforhold)
        val sumAntallBarnListe = bidragsevnePeriodeGrunnlag.barnIHusstandenPeriodeGrunnlagListe
            .filter { barnIHustandenPeriode ->
                boforholdListe.flatMap { it.grunnlagsreferanseListe }.contains(barnIHustandenPeriode.referanse)
            }
            .toMutableList()
        if (sumAntallBarnListe.isNotEmpty()) {
            val sisteElement = sumAntallBarnListe.last()
            // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
            if (sisteElement.periode.datoTil != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(datoTil = null))
                sumAntallBarnListe[sumAntallBarnListe.size - 1] = oppdatertSisteElement
            }
        }
        resultatGrunnlagListe.addAll(
            sumAntallBarnListe.mapTilGrunnlag(referanseTilBP),
        )

        // Mapper ut DelberegningVoksneIHusstand (sub-delberegning til DelberegningBoforhold)
        val voksneIHusstandenListe = bidragsevnePeriodeGrunnlag.voksneIHusstandenPeriodeGrunnlagListe
            .filter { voksneIHusstandenPeriode ->
                boforholdListe.flatMap { it.grunnlagsreferanseListe }.contains(voksneIHusstandenPeriode.referanse)
            }
            .toMutableList()
        if (voksneIHusstandenListe.isNotEmpty()) {
            val sisteElement = voksneIHusstandenListe.last()
            // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
            if (sisteElement.periode.datoTil != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(datoTil = null))
                voksneIHusstandenListe[voksneIHusstandenListe.size - 1] = oppdatertSisteElement
            }
        }
        resultatGrunnlagListe.addAll(
            voksneIHusstandenListe.mapTilGrunnlag(referanseTilBP),
        )

        // Lager en liste av referanser som refereres til av delberegningene på laveste nivå og mapper ut tilhørende grunnlag
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
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                        gjelderReferanse = it.gjelderReferanse,
                        gjelderBarnReferanse = it.gjelderBarnReferanse,
                    )
                },
        )

        return resultatGrunnlagListe
    }
}
