package no.nav.bidrag.beregn.forskudd.core.dto

import no.nav.bidrag.beregn.felles.dto.AvvikCore
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonResultatGrunnlagCore
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag
data class BeregnForskuddGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val soknadBarn: SoknadBarnCore,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val sivilstandPeriodeListe: List<SivilstandPeriodeCore>,
    val barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class SoknadBarnCore(
    val referanse: String,
    val fodselsdato: LocalDate,
)

data class BostatusPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val kode: String,
)

data class InntektPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val type: String,
    val belop: BigDecimal,
)

data class SivilstandPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val kode: String,
)

data class BarnIHusstandenPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
)

// Resultat
data class BeregnetForskuddResultatCore(
    val beregnetForskuddPeriodeListe: List<ResultatPeriodeCore>,
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
    val kode: String,
    val regel: String,
)
