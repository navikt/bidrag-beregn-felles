package no.nav.bidrag.beregn.bpsandelsaertilskudd.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import java.math.BigDecimal

data class NettoSaertilskuddPeriode(
    val referanse: String,
    val periodeDatoFraTil: Periode,
    val nettoSaertilskuddBelop: BigDecimal,
) : PeriodisertGrunnlag {
    constructor(nettoSaertilskuddPeriode: NettoSaertilskuddPeriode) :
        this(
            nettoSaertilskuddPeriode.referanse,
            nettoSaertilskuddPeriode.periodeDatoFraTil.justerDatoer(),
            nettoSaertilskuddPeriode.nettoSaertilskuddBelop,
        )

    override fun getPeriode(): Periode {
        return periodeDatoFraTil
    }
}

data class InntektPeriode(
    val referanse: String,
    val periodeDatoFraTil: Periode,
    val inntektType: String,
    val inntektBelop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean,
) : PeriodisertGrunnlag {
    constructor(inntektPeriode: InntektPeriode) :
        this(
            inntektPeriode.referanse,
            inntektPeriode.periodeDatoFraTil.justerDatoer(),
            inntektPeriode.inntektType,
            inntektPeriode.inntektBelop,
            inntektPeriode.deltFordel,
            inntektPeriode.skatteklasse2,
        )

    override fun getPeriode(): Periode {
        return periodeDatoFraTil
    }
}
