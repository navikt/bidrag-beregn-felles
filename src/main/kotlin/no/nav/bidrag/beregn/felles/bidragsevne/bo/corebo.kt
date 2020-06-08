package no.nav.bidrag.beregn.felles.bidragsevne.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.enums.AvvikType
import no.nav.bidrag.beregn.felles.enums.BostatusKode
import no.nav.bidrag.beregn.felles.enums.InntektType
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode
import java.time.LocalDate


// Grunnlag periode
data class BeregnBidragsevneGrunnlagAlt(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektPeriodeListe: List<InntektPeriode>,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val antallBarnIEgetHusholdPeriodeListe: List<AntallBarnIEgetHusholdPeriode>,
    val saerfradragPeriodeListe: List<SaerfradragPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val sjablonPeriodeListeNy: List<SjablonPeriodeNy>
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
    val sjablonListe: List<Sjablon>) {
  fun hentSjablon(sjablonnavn: String?): Sjablon? = sjablonListe.first() { it.sjablonnavn == sjablonnavn }
}

data class Sjablon(
    val sjablonnavn: String,
    val sjablonVerdi1: Double,
    val sjablonVerdi2: Double?)


data class Inntekt(
    val inntektType: InntektType,
    val inntektBelop: Double
)

data class ResultatBeregning(
    val resultatBelopEvne: Double
)
