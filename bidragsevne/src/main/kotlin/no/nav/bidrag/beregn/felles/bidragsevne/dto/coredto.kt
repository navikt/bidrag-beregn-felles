package no.nav.bidrag.beregn.forskudd.core.dto

import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag
data class BeregnForskuddGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val soknadBarn: SoknadBarnCore,
    val bidragMottakerInntektPeriodeListe: List<InntektPeriodeCore>,
    val bidragMottakerSivilstandPeriodeListe: List<SivilstandPeriodeCore>,
    val bidragMottakerBarnPeriodeListe: List<PeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>
)

data class SoknadBarnCore(
    val soknadBarnFodselsdato: LocalDate,
    val soknadBarnBostatusPeriodeListe: List<BostatusPeriodeCore>
)

data class BostatusPeriodeCore(
    val bostatusDatoFraTil: PeriodeCore,
    val bostatusKode: String
)

data class InntektPeriodeCore(
    val inntektDatoFraTil: PeriodeCore,
    val inntektBelop: BigDecimal
)

data class SivilstandPeriodeCore(
    val sivilstandDatoFraTil: PeriodeCore,
    val sivilstandKode: String
)

data class SjablonPeriodeCore(
    val sjablonDatoFraTil: PeriodeCore,
    val sjablonType: String,
    val sjablonVerdi: BigDecimal
)


// Resultat
data class BeregnForskuddResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>
)

data class ResultatPeriodeCore(
    val resultatDatoFraTil: PeriodeCore,
    val resultatBeregning: ResultatBeregningCore
)

data class ResultatBeregningCore(
    val resultatBelop: BigDecimal,
    val resultatKode: String,
    val resultatBeskrivelse: String
)


// Felles
data class PeriodeCore(
    val periodeDatoFra: LocalDate,
    val periodeDatoTil: LocalDate
)
