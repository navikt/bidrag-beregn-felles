package no.nav.bidrag.beregn.core.bo

import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBarnetilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonForbruksutgifterPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import java.math.BigDecimal
import java.util.Collections.emptyList

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

data class SjablonSjablontallPeriodeGrunnlag(val referanse: String, val sjablonSjablontallPeriode: SjablonSjablontallPeriode)

data class SjablonSjablontallBeregningGrunnlag(val referanse: String, val type: String, val verdi: Double)

data class SjablonTrinnvisSkattesatsPeriodeGrunnlag(val referanse: String, val sjablonTrinnvisSkattesatsPeriode: SjablonTrinnvisSkattesatsPeriode)

data class SjablonBarnetilsynPeriodeGrunnlag(val referanse: String, val sjablonBarnetilsynPeriode: SjablonBarnetilsynPeriode)

data class SjablonForbruksutgifterPeriodeGrunnlag(val referanse: String, val sjablonForbruksutgifterPeriode: SjablonForbruksutgifterPeriode)

data class SjablonSamværsfradragPeriodeGrunnlag(val referanse: String, val sjablonSamværsfradragPeriode: SjablonSamværsfradragPeriode)
