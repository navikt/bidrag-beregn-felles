package no.nav.bidrag.beregn.felles.bidragsevne.dto

import no.nav.bidrag.beregn.felles.enums.SaerfradragKode
import java.time.LocalDate

// Grunnlag periode
data class BeregnBidragsevneGrunnlagAltCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriodeCore>,
    val saerfradragPeriodeListe: List<SaerfradragPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>
)

data class SjablonPeriodeCore(
    val sjablonPeriodeDatoFraTil: PeriodeCore,
    val sjablonnavn: String?,
    val sjablonVerdi1: Double?,
    val sjablonVerdi2: Double?
)

data class InntektPeriodeCore(
    val inntektPeriodeDatoFraTil: PeriodeCore,
    val inntektType: String,
    val skatteklasse: Int,
    val inntektBelop: Double
)

data class BostatusPeriodeCore(
    val bostatusPeriodeDatoFraTil: PeriodeCore,
    val bostatusKode: String
)

data class AntallBarnIEgetHusholdPeriodeCore(
    val antallBarnIEgetHusholdPeriodeDatoFraTil: PeriodeCore,
    val antallBarn: Int
)

data class SaerfradragPeriodeCore(
    val saerfradragPeriodeDatoFraTil: PeriodeCore,
    val saerfradragKode: String
)



// Resultat
data class BeregnBidragsevneResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val avvikListe: List<AvvikCore>
)

data class ResultatPeriodeCore(
    val resultatDatoFraTil: PeriodeCore,
    val resultatBeregning: ResultatBeregningCore,
    val resultatGrunnlag: ResultatGrunnlagCore
)

data class ResultatBeregningCore(
    val resultatEvne: Double
)

data class ResultatGrunnlagCore(
    val inntektListe: List<InntektCore>,
    val skatteklasse: Int,
    val bostatusKode: String,
    val antallEgneBarnIHusstand: Int,
    val saerfradragkode: String,
    val sjablonListe: List<SjablonCore>
)

data class SjablonCore(
    val sjablonnavn: String,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double
)

data class InntektCore(
    val inntektType: String,
    val inntektBelop: Double
)

data class AvvikCore(
    val avvikTekst: String,
    val avvikType: String
)

// Felles
data class PeriodeCore(
    val periodeDatoFra: LocalDate,
    val periodeDatoTil: LocalDate?
)