package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.BarnetilleggSkattesatsBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggSkattesatsPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.InntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonTrinnvisSkattesatsBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BarnetilleggSkattesatsMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.BarnetilleggSkattesatsMapper.mapBarnetilleggSkattesatsGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnBarnetilleggSkattesatsService : BeregnService() {

    fun delberegningBarnetilleggSkattesats(mottattGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> {
        val referanseTilRolle = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = rolle,
        )

        // Lager sjablon grunnlagsobjekter
        // TODO - sjabloner for barnetillegg skattesats
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.barnetilleggSkattesats }

        // Mapper ut grunnlag som skal brukes for å beregne barnetilleggSkattesats
        val barnetilleggSkattesatsPeriodeGrunnlag = mapBarnetilleggSkattesatsGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
            referanseTilRolle = referanseTilRolle,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBarnetilleggSkattesats(
            grunnlagListe = barnetilleggSkattesatsPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val barnetilleggSkattesatsBeregningResultatListe = mutableListOf<BarnetilleggSkattesatsPeriodeResultat>()

        // Beregner barnetilleggSkattesats for siste bruddperiode
        bruddPeriodeListe.last { bruddPeriode ->
            val barnetilleggSkattesatsBeregningGrunnlag = lagBarnetilleggSkattesatsBeregningGrunnlag(
                barnetilleggSkattesatsPeriodeGrunnlag = barnetilleggSkattesatsPeriodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )
            barnetilleggSkattesatsBeregningResultatListe.add(
                BarnetilleggSkattesatsPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = BarnetilleggSkattesatsBeregning.beregn(barnetilleggSkattesatsBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det (åpen sluttdato)
        if (barnetilleggSkattesatsBeregningResultatListe.isNotEmpty()) {
            val sisteElement = barnetilleggSkattesatsBeregningResultatListe.last()
            if (sisteElement.periode.til != null) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                barnetilleggSkattesatsBeregningResultatListe[barnetilleggSkattesatsBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapBarnetilleggSkattesatsResultatGrunnlag(
            barnetilleggSkattesatsBeregningResultatListe = barnetilleggSkattesatsBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning barnetilleggSkattesats
        resultatGrunnlagListe.addAll(
            mapDelberegningBarnetilleggSkattesats(
                barnetilleggSkattesatsPeriodeResultatListe = barnetilleggSkattesatsBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
                referanseTilRolle = referanseTilRolle,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning) +
            mapSjablonTrinnvisSkattesatsGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonTrinnvisSkattesats())

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBarnetilleggSkattesats(
        grunnlagListe: BarnetilleggSkattesatsPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.sumInntektBeregningGrunnlag.asSequence().map { it.sumInntektPeriode.periode })
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })
            .plus(
                grunnlagListe.sjablonTrinnvisSkattesatsPeriodeGrunnlagListe.asSequence()
                    .map { it.sjablonTrinnvisSkattesatsPeriode.periode },
            )

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for barnetilleggSkattesatsberegning som ligger innenfor bruddPeriode
    private fun lagBarnetilleggSkattesatsBeregningGrunnlag(
        barnetilleggSkattesatsPeriodeGrunnlag: BarnetilleggSkattesatsPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BarnetilleggSkattesatsBeregningGrunnlag = BarnetilleggSkattesatsBeregningGrunnlag(
        inntektBeregningGrunnlag = barnetilleggSkattesatsPeriodeGrunnlag.sumInntektBeregningGrunnlag
            .firstOrNull { it.sumInntektPeriode.periode.inneholder(bruddPeriode) }
            ?.let { InntektBeregningGrunnlag(referanse = it.referanse, sumInntekt = it.sumInntektPeriode.totalinntekt) }
            ?: throw IllegalArgumentException("Delberegning sum inntekt mangler for periode $bruddPeriode"),
        sjablonSjablontallBeregningGrunnlagListe = barnetilleggSkattesatsPeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
            .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonSjablontallBeregningGrunnlag(
                    referanse = it.referanse,
                    type = it.sjablonSjablontallPeriode.sjablon.navn,
                    verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                )
            },
        sjablonTrinnvisSkattesatsBeregningGrunnlag = barnetilleggSkattesatsPeriodeGrunnlag.sjablonTrinnvisSkattesatsPeriodeGrunnlagListe
            .firstOrNull { it.sjablonTrinnvisSkattesatsPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                SjablonTrinnvisSkattesatsBeregningGrunnlag(
                    referanse = it.referanse,
                    trinnliste = it.sjablonTrinnvisSkattesatsPeriode.trinnliste,
                )
            }
            ?: throw IllegalArgumentException("Ingen sjablonverdier for trinnvis skattesats funnet for periode $bruddPeriode"),
    )

    private fun mapBarnetilleggSkattesatsResultatGrunnlag(
        barnetilleggSkattesatsBeregningResultatListe: List<BarnetilleggSkattesatsPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            barnetilleggSkattesatsBeregningResultatListe
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

    // Mapper ut DelberegningBarnetilleggSkattesats. Fom-periode settes lik start beregningsperiode, ettersom skatteprosenten skal vær den samme for
    // hele beregningsperioden.
    private fun mapDelberegningBarnetilleggSkattesats(
        barnetilleggSkattesatsPeriodeResultatListe: List<BarnetilleggSkattesatsPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        referanseTilRolle: String,
    ): List<GrunnlagDto> = barnetilleggSkattesatsPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS,
                    periode = ÅrMånedsperiode(fom = mottattGrunnlag.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = referanseTilRolle,
                ),
                type = Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS,
                innhold = POJONode(
                    DelberegningBarnetilleggSkattesats(
                        periode = ÅrMånedsperiode(fom = mottattGrunnlag.periode.fom, til = it.periode.til),
                        skattFaktor = it.resultat.skattFaktor,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.distinct().sorted(),
                gjelderReferanse = referanseTilRolle,
            )
        }
}
