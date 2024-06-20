package no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class BidragsevnePeriode(val referanse: String, val periodeDatoFraTil: Periode, val bidragsevneBelop: BigDecimal) : PeriodisertGrunnlag {
    constructor(bidragsevnePeriode: no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BidragsevnePeriode) :
        this(
            bidragsevnePeriode.referanse,
            bidragsevnePeriode.periodeDatoFraTil.justerDatoer(),
            bidragsevnePeriode.bidragsevneBelop,
        )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class BPsAndelSaertilskuddPeriode(
    val referanse: String,
    val periodeDatoFraTil: Periode,
    val bPsAndelSaertilskuddProsent: BigDecimal,
    val bPsAndelSaertilskuddBelop: BigDecimal,
    val barnetErSelvforsorget: Boolean,
) : PeriodisertGrunnlag {
    constructor(bPsAndelSaertilskuddPeriode: no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskuddPeriode) :
        this(
            bPsAndelSaertilskuddPeriode.referanse,
            bPsAndelSaertilskuddPeriode.periodeDatoFraTil.justerDatoer(),
            bPsAndelSaertilskuddPeriode.bPsAndelSaertilskuddProsent,
            bPsAndelSaertilskuddPeriode.bPsAndelSaertilskuddBelop,
            bPsAndelSaertilskuddPeriode.barnetErSelvforsorget,
        )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class LopendeBidragPeriode(
    val referanse: String,
    val periodeDatoFraTil: Periode,
    val barnPersonId: Int,
    val lopendeBidragBelop: BigDecimal,
    val opprinneligBPsAndelUnderholdskostnadBelop: BigDecimal,
    val opprinneligBidragBelop: BigDecimal,
    val opprinneligSamvaersfradragBelop: BigDecimal,
) : PeriodisertGrunnlag {
    constructor(lopendeBidragPeriode: no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidragPeriode) :
        this(
            lopendeBidragPeriode.referanse,
            lopendeBidragPeriode.periodeDatoFraTil.justerDatoer(),
            lopendeBidragPeriode.barnPersonId,
            lopendeBidragPeriode.lopendeBidragBelop,
            lopendeBidragPeriode.opprinneligBPsAndelUnderholdskostnadBelop,
            lopendeBidragPeriode.opprinneligBidragBelop,
            lopendeBidragPeriode.opprinneligSamvaersfradragBelop,
        )

    override fun getPeriode(): Periode = periodeDatoFraTil
}

data class SamvaersfradragGrunnlagPeriode(
    val referanse: String,
    val barnPersonId: Int,
    val periodeDatoFraTil: Periode,
    val samvaersfradragBelop: BigDecimal,
) : PeriodisertGrunnlag {
    constructor(samvaersfradragGrunnlagPeriode: no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlagPeriode) :
        this(
            samvaersfradragGrunnlagPeriode.referanse,
            samvaersfradragGrunnlagPeriode.barnPersonId,
            samvaersfradragGrunnlagPeriode.periodeDatoFraTil.justerDatoer(),
            samvaersfradragGrunnlagPeriode.samvaersfradragBelop,
        )

    override fun getPeriode(): Periode = periodeDatoFraTil
}
