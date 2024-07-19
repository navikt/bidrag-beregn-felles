package no.nav.bidrag.beregn.core.inntekt

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.PeriodisertGrunnlag
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import java.math.BigDecimal

data class InntektPeriodeGrunnlag(
    val referanse: String,
    val inntektPeriode: Periode,
    val type: Inntektstype,
    val belop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean,
) : PeriodisertGrunnlag {
    constructor(inntektPeriodeGrunnlag: InntektPeriodeGrunnlag) :
        this(
            referanse = inntektPeriodeGrunnlag.referanse,
            inntektPeriode = inntektPeriodeGrunnlag.inntektPeriode.justerDatoer(),
            type = inntektPeriodeGrunnlag.type,
            belop = inntektPeriodeGrunnlag.belop,
            deltFordel = inntektPeriodeGrunnlag.deltFordel,
            skatteklasse2 = inntektPeriodeGrunnlag.skatteklasse2,
        )

    override fun getPeriode(): Periode {
        return inntektPeriode
    }
}

// TODO Midlertidig duplisert InntektPeriodeGrunnlag. Denne bør etterhvert erstatte InntektPeriodeGrunnlag (dvs. bruke String i stedet for InntektType)
data class InntektPeriodeGrunnlagUtenInntektType(
    val referanse: String,
    val inntektPeriode: Periode,
    val type: String,
    val belop: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean,
) : PeriodisertGrunnlag {
    constructor(inntektPeriodeGrunnlag: InntektPeriodeGrunnlagUtenInntektType) :
        this(
            referanse = inntektPeriodeGrunnlag.referanse,
            inntektPeriode = inntektPeriodeGrunnlag.inntektPeriode.justerDatoer(),
            type = inntektPeriodeGrunnlag.type,
            belop = inntektPeriodeGrunnlag.belop,
            deltFordel = inntektPeriodeGrunnlag.deltFordel,
            skatteklasse2 = inntektPeriodeGrunnlag.skatteklasse2,
        )

    override fun getPeriode(): Periode {
        return inntektPeriode
    }
}

data class PeriodisertInntekt(
    val periode: Periode,
    val summertBeløp: BigDecimal,
    var fordelSærfradragBeløp: BigDecimal,
    val sjablon0004FordelSkatteklasse2Beløp: BigDecimal,
    val sjablon0030ØvreInntektsgrenseBeløp: BigDecimal,
    val sjablon0031NedreInntektsgrenseBeløp: BigDecimal,
    val sjablon0039FordelSærfradragBeløp: BigDecimal,
    val deltFordel: Boolean,
    val skatteklasse2: Boolean,
)
