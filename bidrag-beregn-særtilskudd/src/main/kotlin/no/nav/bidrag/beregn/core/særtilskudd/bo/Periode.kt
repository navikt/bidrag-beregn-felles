package no.nav.bidrag.beregn.core.særtilskudd.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class BidragsevnePeriode(val referanse: String, val periodeDatoFraTil: Periode, val bidragsevneBelop: BigDecimal) : PeriodisertGrunnlag {
    constructor(bidragsevnePeriode: BidragsevnePeriode) :
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
    constructor(bPsAndelSaertilskuddPeriode: BPsAndelSaertilskuddPeriode) :
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
    constructor(lopendeBidragPeriode: LopendeBidragPeriode) :
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
    constructor(samvaersfradragGrunnlagPeriode: SamvaersfradragGrunnlagPeriode) :
        this(
            samvaersfradragGrunnlagPeriode.referanse,
            samvaersfradragGrunnlagPeriode.barnPersonId,
            samvaersfradragGrunnlagPeriode.periodeDatoFraTil.justerDatoer(),
            samvaersfradragGrunnlagPeriode.samvaersfradragBelop,
        )

    override fun getPeriode(): Periode = periodeDatoFraTil
}
