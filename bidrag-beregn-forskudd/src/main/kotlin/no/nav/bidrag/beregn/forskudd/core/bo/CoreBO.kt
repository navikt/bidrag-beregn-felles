package no.nav.bidrag.beregn.forskudd.core.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.domene.enums.beregning.ResultatkodeForskudd
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnForskuddGrunnlag(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val soknadBarn: SoknadBarn,
    val bostatusPeriodeListe: List<BostatusPeriode>,
    val inntektPeriodeListe: List<InntektPeriode>,
    val sivilstandPeriodeListe: List<SivilstandPeriode>,
    val barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriode>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
)

data class SoknadBarn(
    val referanse: String,
    val fodselsdato: LocalDate,
)

// Resultat periode
data class BeregnForskuddResultat(
    val beregnetForskuddPeriodeListe: List<ResultatPeriode>,
)

data class ResultatPeriode(
    val periode: Periode,
    val resultat: ResultatBeregning,
    val grunnlag: GrunnlagBeregning,
)

// Grunnlag beregning
data class GrunnlagBeregning(
    val inntektListe: List<Inntekt>,
    val sivilstand: Sivilstand,
    val barnIHusstandenListe: List<BarnIHusstanden>,
    val soknadBarnAlder: Alder,
    val soknadBarnBostatus: Bostatus,
    val sjablonListe: List<SjablonPeriode>,
)

data class Inntekt(
    val referanse: String,
    val type: String,
    val belop: BigDecimal,
)

data class Sivilstand(
    val referanse: String,
    val kode: Sivilstandskode,
)

data class BarnIHusstanden(
    val referanse: String,
)

data class Alder(
    val referanse: String,
    val alder: Int,
)

data class Bostatus(
    val referanse: String,
    val kode: Bostatuskode,
)

// Resultat beregning
data class ResultatBeregning(
    val belop: BigDecimal,
    val kode: ResultatkodeForskudd,
    val regel: String,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)

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
    var inntektsintervallForskuddBelop: BigDecimal = BigDecimal.ZERO,
    var forskuddssats75ProsentBelop: BigDecimal = BigDecimal.ZERO,
    var forskuddssats100ProsentBelop: BigDecimal = BigDecimal.ZERO,
    var inntektsgrense100ProsentForskuddBelop: BigDecimal = BigDecimal.ZERO,
    var inntektsgrenseEnslig75ProsentForskuddBelop: BigDecimal = BigDecimal.ZERO,
    var inntektsgrenseGiftSamboer75ProsentForskuddBelop: BigDecimal = BigDecimal.ZERO,
)
