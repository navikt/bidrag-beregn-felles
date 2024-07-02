package no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBidragsevneGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPeriodeListe: List<InntektPeriode>,
    val barnIHusstandPeriodeListe: List<BarnIHusstandPeriode>,
    val voksneIHusstandPeriodeListe: List<VoksneIHusstandPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
)

// Resultatperiode
data class BeregnBidragsevneResultat(val resultatPeriodeListe: List<ResultatPeriode>)

data class ResultatPeriode(
    val periode: Periode,
    val resultat: ResultatBeregning,
    val grunnlag: GrunnlagBeregning,
)

data class ResultatBeregning(val belop: BigDecimal, val sjablonListe: List<SjablonPeriodeNavnVerdi>)

// Grunnlag beregning
data class GrunnlagBeregning(
    val inntektListe: List<Inntekt>,
    val antallBarnIHusstand: AntallBarnIHusstand,
    val bostatusVoksneIHusstand: BostatusVoksneIHusstand,
    val sjablonListe: List<SjablonPeriode>,
)

data class Inntekt(val referanse: String, val inntektType: String, val inntektBelop: BigDecimal)

data class AntallBarnIHusstand(val referanse: String, val antallBarn: Double)

data class BostatusVoksneIHusstand(val referanse: String, val borMedAndre: Boolean)

// Hjelpeklasser
data class BeregnBidragsevneListeGrunnlag(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var inntektPeriodeListe: List<InntektPeriode> = listOf(),
    var barnIHusstandPeriodeListe: List<BarnIHusstandPeriode> = listOf(),
    var voksneIHusstandPeriodeListe: List<VoksneIHusstandPeriode> = listOf(),
    var sjablonPeriodeListe: List<SjablonPeriode> = listOf(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)
