package no.nav.bidrag.beregn.core.bidragsevne.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.core.felles.dto.IResultatPeriode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBidragsevneGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val skatteklassePeriodeListe: List<SkatteklassePeriodeCore>,
    val antallBarnIEgetHusholdPeriodeCoreListe: List<AntallBarnIEgetHusholdPeriodeCore>,
    val særfradragPeriodeListe: List<SærfradragPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class InntektPeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val inntektType: String, val inntektBelop: BigDecimal)

data class SkatteklassePeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val skatteklasse: Int)

data class BostatusPeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val bostatusKode: String)

data class AntallBarnIEgetHusholdPeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val antallBarn: Double)

data class SærfradragPeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val saerfradragKode: String)

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

data class ResultatBeregningCore(val belop: BigDecimal)
