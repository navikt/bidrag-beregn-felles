package no.nav.bidrag.beregn.særtilskudd.beregning

import no.nav.bidrag.beregn.felles.FellesBeregning
import no.nav.bidrag.beregn.særtilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.bo.ResultatBeregning
import no.nav.bidrag.domene.enums.beregning.ResultatkodeSærtilskudd
import java.math.BigDecimal

class SaertilskuddBeregningImpl : FellesBeregning(), SaertilskuddBeregning {

    override fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        val totaltBidragBleRedusertMedBelop = grunnlag.lopendeBidragListe.sumOf {
            it.opprinneligBPsAndelUnderholdskostnadBelop - (it.opprinneligBidragBelop + it.opprinneligSamvaersfradragBelop)
        }

        val totaltLopendeBidragBelop = grunnlag.lopendeBidragListe.sumOf { it.lopendeBidragBelop }

        val totaltSamvaersfradragBelop = grunnlag.samvaersfradragGrunnlagListe.sumOf { it.samvaersfradragBelop }

        val totaltBidragBelop = totaltBidragBleRedusertMedBelop + totaltLopendeBidragBelop + totaltSamvaersfradragBelop

        return when {
            grunnlag.bidragsevne.bidragsevneBelop < totaltBidragBelop -> ResultatBeregning(
                resultatBelop = BigDecimal.ZERO,
                resultatkode = ResultatkodeSærtilskudd.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE,
            )
            grunnlag.bPsAndelSaertilskudd.barnetErSelvforsorget -> ResultatBeregning(
                resultatBelop = BigDecimal.ZERO,
                resultatkode = ResultatkodeSærtilskudd.BARNET_ER_SELVFORSØRGET,
            )
            else -> ResultatBeregning(
                resultatBelop = grunnlag.bPsAndelSaertilskudd.bPsAndelSaertilskuddBelop,
                resultatkode = ResultatkodeSærtilskudd.SÆRTILSKUDD_INNVILGET,
            )
        }
    }
}
