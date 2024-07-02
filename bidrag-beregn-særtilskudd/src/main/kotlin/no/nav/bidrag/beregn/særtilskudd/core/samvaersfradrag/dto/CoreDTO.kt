package no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.IResultatPeriode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnSamvaersfradragGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val samvaersklassePeriodeListe: List<SamvaersklassePeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class SamvaersklassePeriodeCore(
    val referanse: String,
    val samvaersklassePeriodeDatoFraTil: PeriodeCore,
    val barnPersonId: Int,
    val barnFodselsdato: LocalDate,
    val samvaersklasse: String,
)

// Resultatperiode
data class BeregnSamvaersfradragResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: List<SjablonResultatGrunnlagCore>,
    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultatBeregningListe: List<ResultatBeregningCore>,
    override val grunnlagReferanseListe: List<String>,
) : IResultatPeriode

data class ResultatBeregningCore(val barnPersonId: Int, val belop: BigDecimal)
