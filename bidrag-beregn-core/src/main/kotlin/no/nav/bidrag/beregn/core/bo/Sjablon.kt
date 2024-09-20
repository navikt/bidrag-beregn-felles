package no.nav.bidrag.beregn.core.bo

import java.math.BigDecimal

// Nye sjablonklasser
data class Sjablon(val navn: String, val nøkkelListe: List<SjablonNøkkel>? = emptyList(), val innholdListe: List<SjablonInnhold>)

data class SjablonNøkkel(val navn: String, val verdi: String)

data class SjablonInnhold(val navn: String, val verdi: BigDecimal)

data class SjablonSingelNøkkel(val navn: String, val verdi: String, val innholdListe: List<SjablonInnhold>)

data class SjablonSingelNøkkelSingelInnhold(val navn: String, val nøkkelVerdi: String, val innholdVerdi: BigDecimal)

data class TrinnvisSkattesats(val inntektGrense: BigDecimal, val sats: BigDecimal)

data class SjablonPeriode(val sjablonPeriode: Periode, val sjablon: Sjablon) : PeriodisertGrunnlag {
    constructor(sjablonPeriode: SjablonPeriode) : this(
        sjablonPeriode.sjablonPeriode.justerDatoer(),
        sjablonPeriode.sjablon,
    )

    override fun getPeriode(): Periode = sjablonPeriode
}

data class SjablonPeriodeInnhold(val sjablonPeriode: Periode, val sjablonInnhold: List<SjablonInnhold>)

data class SjablonNavnVerdi(val navn: String, val verdi: BigDecimal)

data class SjablonPeriodeNavnVerdi(val periode: Periode, val navn: String, val verdi: BigDecimal)
