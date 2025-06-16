package no.nav.bidrag.beregn.forskudd.core.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Collections.emptyList

// Grunnlag periode
data class BeregnForskuddGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val søknadsbarn: Søknadsbarn,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val sivilstandPeriodeListe: List<SivilstandPeriode>,
    val barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
    val åpenSluttperiode: Boolean = true,
)

data class Søknadsbarn(val referanse: String, val fødselsdato: LocalDate)

// Resultat periode
data class BeregnForskuddResultat(val beregnetForskuddPeriodeListe: List<ResultatPeriode>)

data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: GrunnlagBeregning)

// Grunnlag beregning
data class GrunnlagBeregning(
    val inntektListe: List<Inntekt>,
    val sivilstand: Sivilstand,
    val barnIHusstandenListe: List<BarnIHusstanden>,
    val søknadsbarnAlder: Alder,
    val søknadsbarnBostatus: Bostatus,
    val sjablonListe: List<SjablonPeriode>,
)

data class Inntekt(val referanse: String, val type: String, val beløp: BigDecimal)

data class Sivilstand(val referanse: String, val kode: Sivilstandskode)

data class BarnIHusstanden(val referanse: String, val antall: Int)

data class Alder(val referanse: String, val alder: Int)

data class Bostatus(val referanse: String, val kode: Bostatuskode)

// Resultat beregning
data class ResultatBeregning(val beløp: BigDecimal, val kode: Resultatkode, val regel: String, val sjablonListe: List<SjablonPeriodeNavnVerdi>)

// Hjelpeklasser
data class GrunnlagTilBeregning(
    val periodeResultatListe: MutableList<ResultatPeriode> = mutableListOf(),
    var inntektPeriodeListe: List<InntektPeriode> = emptyList(),
    var sivilstandPeriodeListe: List<SivilstandPeriode> = emptyList(),
    var barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriode> = emptyList(),
    var bostatusPeriodeListe: List<BostatusPeriode> = emptyList(),
    var alderPeriodeListe: List<AlderPeriode> = emptyList(),
    var sjablonPeriodeListe: List<SjablonPeriode> = emptyList(),
    var bruddPeriodeListe: MutableList<Periode> = mutableListOf(),
)

data class Sjablonverdier(
    var maksInntektForskuddMottakerMultiplikator: BigDecimal = BigDecimal.ZERO,
    var inntektsintervallForskuddBeløp: BigDecimal = BigDecimal.ZERO,
    var forskuddssats75ProsentBeløp: BigDecimal = BigDecimal.ZERO,
    var forskuddssats100ProsentBeløp: BigDecimal = BigDecimal.ZERO,
    var inntektsgrense100ProsentForskuddBeløp: BigDecimal = BigDecimal.ZERO,
    var inntektsgrenseEnslig75ProsentForskuddBeløp: BigDecimal = BigDecimal.ZERO,
    var inntektsgrenseGiftSamboer75ProsentForskuddBeløp: BigDecimal = BigDecimal.ZERO,
)
