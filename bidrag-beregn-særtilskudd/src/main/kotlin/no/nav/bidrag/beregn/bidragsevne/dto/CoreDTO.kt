package no.nav.bidrag.beregn.bidragsevne.dto

import no.nav.bidrag.beregn.felles.dto.AvvikCore
import no.nav.bidrag.beregn.felles.dto.IResultatPeriode
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonResultatGrunnlagCore
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBidragsevneGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val skatteklassePeriodeListe: List<SkatteklassePeriodeCore>,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriodeCore>,
    val saerfradragPeriodeListe: List<SaerfradragPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class InntektPeriodeCore(
    val referanse: String,
    val periodeDatoFraTil: PeriodeCore,
    val inntektType: String,
    val inntektBelop: BigDecimal,
)

data class SkatteklassePeriodeCore(
    val referanse: String,
    val periodeDatoFraTil: PeriodeCore,
    val skatteklasse: Int,
)

data class BostatusPeriodeCore(
    val referanse: String,
    val periodeDatoFraTil: PeriodeCore,
    val bostatusKode: String,
)

data class AntallBarnIEgetHusholdPeriodeCore(
    val referanse: String,
    val periodeDatoFraTil: PeriodeCore,
    val antallBarn: Double,
)

data class SaerfradragPeriodeCore(
    val referanse: String,
    val periodeDatoFraTil: PeriodeCore,
    val saerfradragKode: String,
)

// Resultatperiode
data class BeregnBidragsevneResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: List<SjablonResultatGrunnlagCore>,
    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultatBeregning: ResultatBeregningCore,
    override val grunnlagReferanseListe: List<String>,
) : IResultatPeriode

data class ResultatBeregningCore(
    val belop: BigDecimal,
)
