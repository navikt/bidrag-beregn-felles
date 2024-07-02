package no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.VoksneIHusstandenPeriodeCore
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBidragsevneGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriodeCore>,
    val voksneIHusstandenPeriodeListe: List<VoksneIHusstandenPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

// data class BostatusPeriodeCore(
//    val referanse: String,
//    val periode: PeriodeCore,
//    val kode: String,
// )

// Resultatperiode
data class BeregnBidragsevneResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: List<SjablonResultatGrunnlagCore>,
    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    val periode: PeriodeCore,
    val resultat: ResultatBeregningCore,
    val grunnlagsreferanseListe: List<String>,
)

data class ResultatBeregningCore(
    val belop: BigDecimal,
)
