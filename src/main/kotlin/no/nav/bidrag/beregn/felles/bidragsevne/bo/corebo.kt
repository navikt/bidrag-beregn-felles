package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.enums.AvvikType
import no.nav.bidrag.beregn.felles.enums.BostatusKode
import no.nav.bidrag.beregn.felles.enums.InntektType
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode
import java.math.BigDecimal
import java.time.LocalDate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAlt(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>,
    val saerfradragPeriodeListe: List<SaerfradragPeriode>
)

// Resultatperiode
data class BeregnBidragsevneResultat(
    val resultatPeriodeListe: List<ResultatPeriode>
)

data class ResultatPeriode(
    val resultatDatoFraTil: Periode,
    val resultatBeregning: ResultatBeregning,
    val resultatGrunnlag: BeregnBidragsevneGrunnlagPeriodisert
)

/*
data class SjablonPeriodeVerdi(
    val sjablonnavn: String,
    val sjablonVerdi: Int)
*/


// Avvikperiode
data class Avvik(
    val avvikTekst: String,
    val avvikType: AvvikType
)


// Grunnlag beregning
data class BeregnBidragsevneGrunnlagPeriodisert(
    val inntektListe: List<Inntekt>,
    val skatteklasse: Int,
    val bostatusKode: BostatusKode,
    val antallEgneBarnIHusstand: Int,
    val saerfradragkode: SaerfradragKode,
    val sjablonPeriodeListe: List<SjablonPeriode>) {
  fun hentSjablon(sjablonnavn: String?): SjablonPeriode? = sjablonPeriodeListe.first() { it.sjablonnavn == sjablonnavn }
}

data class Inntekt(
    val inntektType: InntektType,
    val inntektBelop: Double
)

data class ResultatBeregning(
    val resultatBelopEvne: Double
)
