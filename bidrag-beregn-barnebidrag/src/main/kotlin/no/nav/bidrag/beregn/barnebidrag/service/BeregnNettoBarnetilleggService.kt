package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoBarnetilleggBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggBeregningsgrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SkattFaktorBeregningsgrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoBarnetilleggMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoBarnetilleggMapper.mapNettoBarnetilleggGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnNettoBarnetilleggService : BeregnService() {

    fun delberegningNettoBarnetillegg(mottattGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> {
        val referanseTilRolle = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = rolle,
        )

        // Mapper ut grunnlag som skal brukes for å beregne nettoBarnetillegg
        val nettoBarnetilleggPeriodeGrunnlag = mapNettoBarnetilleggGrunnlag(mottattGrunnlag, referanseTilRolle)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeNettoBarnetillegg(nettoBarnetilleggPeriodeGrunnlag, mottattGrunnlag.periode)

        val nettoBarnetilleggBeregningResultatListe = mutableListOf<NettoBarnetilleggPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner nettoBarnetillegg
        bruddPeriodeListe.forEach { bruddPeriode ->
            val nettoBarnetilleggBeregningGrunnlag = lagNettoBarnetilleggBeregningGrunnlag(nettoBarnetilleggPeriodeGrunnlag, bruddPeriode)
            if (nettoBarnetilleggBeregningGrunnlag != null) {
                nettoBarnetilleggBeregningResultatListe.add(
                    NettoBarnetilleggPeriodeResultat(
                        periode = bruddPeriode,
                        resultat = NettoBarnetilleggBeregning.beregn(nettoBarnetilleggBeregningGrunnlag),
                    ),
                )
            }
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det (åpen sluttdato)
        if (nettoBarnetilleggBeregningResultatListe.isNotEmpty()) {
            val sisteElement = nettoBarnetilleggBeregningResultatListe.last()
            if (sisteElement.periode.til != null && sisteElement.periode.til!! == mottattGrunnlag.periode.til) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                nettoBarnetilleggBeregningResultatListe[nettoBarnetilleggBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapNettoBarnetilleggResultatGrunnlag(
            nettoBarnetilleggBeregningResultatListe = nettoBarnetilleggBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
        )

        // Mapper ut grunnlag for delberegning nettoBarnetillegg
        resultatGrunnlagListe.addAll(
            mapDelberegningNettoBarnetillegg(
                nettoBarnetilleggPeriodeResultatListe = nettoBarnetilleggBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeNettoBarnetillegg(
        grunnlagListe: NettoBarnetilleggPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.barnetilleggPeriodeGrunnlagListe.asSequence().map { it.barnetilleggPeriode.periode })
            .plus(grunnlagListe.barnetilleggSkattesatsListe.asSequence().map { it.barnetilleggSkattesatsPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for nettoBarnetilleggberegning som ligger innenfor bruddPeriode
    private fun lagNettoBarnetilleggBeregningGrunnlag(
        nettoBarnetilleggPeriodeGrunnlag: NettoBarnetilleggPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): NettoBarnetilleggBeregningGrunnlag? {
        val skattFaktor = nettoBarnetilleggPeriodeGrunnlag.barnetilleggSkattesatsListe
            .firstOrNull {
                ÅrMånedsperiode(it.barnetilleggSkattesatsPeriode.periode.fom, it.barnetilleggSkattesatsPeriode.periode.til).inneholder(
                    bruddPeriode,
                )
            }
            ?.let {
                SkattFaktorBeregningsgrunnlag(
                    referanse = it.referanse,
                    skattFaktor = it.barnetilleggSkattesatsPeriode.skattFaktor,
                )
            }

        val barnetilleggBeregningGrunnlagListe = nettoBarnetilleggPeriodeGrunnlag.barnetilleggPeriodeGrunnlagListe
            .filter {
                ÅrMånedsperiode(it.barnetilleggPeriode.periode.fom, it.barnetilleggPeriode.periode.til).inneholder(
                    bruddPeriode,
                )
            }.map {
                BarnetilleggBeregningsgrunnlag(
                    referanse = it.referanse,
                    barnetilleggstype = it.barnetilleggPeriode.type,
                    bruttoBarnetillegg = it.barnetilleggPeriode.beløp,
                )
            }

        return if (skattFaktor == null || barnetilleggBeregningGrunnlagListe.isEmpty()) {
            null
        } else {
            NettoBarnetilleggBeregningGrunnlag(
                skattFaktorGrunnlag = skattFaktor,
                barnetilleggBeregningGrunnlagListe = barnetilleggBeregningGrunnlagListe,

            )
        }
    }

    private fun mapNettoBarnetilleggResultatGrunnlag(
        nettoBarnetilleggBeregningResultatListe: List<NettoBarnetilleggPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            nettoBarnetilleggBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
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

    private fun mapDelberegninger(
        mottattGrunnlag: BeregnGrunnlag,
        nettoBarnetilleggPeriodeGrunnlag: NettoBarnetilleggPeriodeGrunnlag,
        delberegningNettoBarnetilleggResultat: List<NettoBarnetilleggPeriodeResultat>,
//        referanseTilSøknadsbarn: String,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            delberegningNettoBarnetilleggResultat
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        resultatGrunnlagListe.addAll(
            mapDelberegningNettoBarnetillegg(
                nettoBarnetilleggPeriodeResultatListe = delberegningNettoBarnetilleggResultat,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene på laveste nivå og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            delberegningNettoBarnetilleggResultat.flatMap { it.resultat.grunnlagsreferanseListe }
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

    // Mapper ut DelberegningNettoBarnetillegg
    private fun mapDelberegningNettoBarnetillegg(
        nettoBarnetilleggPeriodeResultatListe: List<NettoBarnetilleggPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = nettoBarnetilleggPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG,
                    periode = it.periode,
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_NETTO_BARNETILLEGG,
                innhold = POJONode(
                    DelberegningNettoBarnetillegg(
                        periode = it.periode,
                        summertBruttoBarnetillegg = it.resultat.summertBruttoBarnetillegg,
                        summertNettoBarnetillegg = it.resultat.summertNettoBarnetillegg,
                        barnetilleggTypeListe = it.resultat.barnetilleggTypeListe,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderReferanse = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
            )
        }
}
