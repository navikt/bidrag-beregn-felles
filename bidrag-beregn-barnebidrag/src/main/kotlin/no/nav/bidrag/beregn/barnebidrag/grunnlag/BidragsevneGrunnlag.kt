package no.nav.bidrag.beregn.barnebidrag.grunnlag

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import java.math.BigDecimal
import java.time.LocalDate

data class BidragsevnePeriodeGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPerioder: List<InntektPeriodeCore>,
    val barnIHusstandenPerioder: List<BarnIHusstandenPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class BidragsevneBeregningGrunnlag(
    val inntekt: Inntekt?,
    val antallBarnIHusstand: AntallBarnIHusstand,
    val bostatusVoksneIHusstand: BostatusVoksneIHusstand,
    val sjablonListe: List<SjablonPeriode>,
)

data class Inntekt(val referanse: String, val inntektBeløp: BigDecimal)

data class AntallBarnIHusstand(val referanse: String, val antallBarn: Double)

data class BostatusVoksneIHusstand(val referanse: String, val borMedAndre: Boolean)
