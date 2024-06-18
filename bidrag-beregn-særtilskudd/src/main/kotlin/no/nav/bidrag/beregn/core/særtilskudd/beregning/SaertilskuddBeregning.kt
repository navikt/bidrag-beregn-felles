package no.nav.bidrag.beregn.core.særtilskudd.beregning

import no.nav.bidrag.beregn.core.felles.FellesBeregning
import no.nav.bidrag.beregn.core.særtilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.core.særtilskudd.bo.ResultatBeregning
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal

class SaertilskuddBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        val totaltBidragBleRedusertMedBelop = grunnlag.lopendeBidragListe.sumOf {
            it.opprinneligBPsAndelUnderholdskostnadBelop - (it.opprinneligBidragBelop + it.opprinneligSamvaersfradragBelop)
        }

        val totaltLopendeBidragBelop = grunnlag.lopendeBidragListe.sumOf { it.lopendeBidragBelop }

        val totaltSamvaersfradragBelop = grunnlag.samvaersfradragGrunnlagListe.sumOf { it.samvaersfradragBelop }

        val totaltBidragBelop = totaltBidragBleRedusertMedBelop + totaltLopendeBidragBelop + totaltSamvaersfradragBelop

        return when {
            grunnlag.bidragsevne.bidragsevneBelop < totaltBidragBelop -> ResultatBeregning(
                resultatBelop = BigDecimal.ZERO,
                resultatkode = Resultatkode.SÆRTILSKUDD_IKKE_FULL_BIDRAGSEVNE,
            )
            grunnlag.bPsAndelSaertilskudd.barnetErSelvforsorget -> ResultatBeregning(
                resultatBelop = BigDecimal.ZERO,
                resultatkode = Resultatkode.BARNET_ER_SELVFORSØRGET,
            )
            else -> ResultatBeregning(
                resultatBelop = grunnlag.bPsAndelSaertilskudd.bPsAndelSaertilskuddBelop,
                resultatkode = Resultatkode.SÆRTILSKUDD_INNVILGET,
            )
        }
    }
}
