package no.nav.bidrag.beregn.core.bo

import no.nav.bidrag.commons.service.sjablon.SjablonDto
import java.math.BigDecimal
import java.util.Collections.emptyList

// Nye sjablonklasser
data class Sjablon(
    val navn: String,
    val nøkkelListe: List<SjablonNøkkel>? = emptyList(),
    val innholdListe: List<SjablonInnhold>,
    val grunnlag: SjablonDto? = null,
)

data class SjablonNøkkel(val navn: String, val verdi: String)

data class SjablonInnhold(val navn: String, val verdi: BigDecimal, val grunnlag: SjablonDto? = null)
data class SjablonVerdiGrunnlag(var verdi: BigDecimal, val grunnlag: SjablonDto? = null)

data class SjablonSingelNøkkel(val navn: String, val verdi: String, val innholdListe: List<SjablonInnhold>, val grunnlag: SjablonDto? = null)

data class SjablonSingelNøkkelSingelInnhold(val navn: String, val nøkkelVerdi: String, val innholdVerdi: BigDecimal)

data class TrinnvisSkattesats(val inntektGrense: BigDecimal, val sats: BigDecimal)

data class SjablonPeriode(val sjablonPeriode: Periode, val sjablon: Sjablon, val grunnlag: SjablonDto? = null) : PeriodisertGrunnlag {
    constructor(sjablonPeriode: SjablonPeriode) : this(
        sjablonPeriode.sjablonPeriode.justerDatoer(),
        sjablonPeriode.sjablon,
    )

    override fun getPeriode(): Periode = sjablonPeriode
}

data class SjablonPeriodeNavnVerdi(val periode: Periode, val navn: String, val verdi: BigDecimal, val grunnlag: SjablonDto? = null)
