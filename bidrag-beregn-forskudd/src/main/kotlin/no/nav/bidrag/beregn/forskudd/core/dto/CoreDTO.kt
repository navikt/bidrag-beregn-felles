package no.nav.bidrag.beregn.forskudd.core.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.person.AldersgruppeForskudd
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag
data class BeregnForskuddGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val søknadsbarn: SøknadsbarnCore,
    val bostatusPeriodeListe: List<BostatusPeriodeCore>,
    val inntektPeriodeListe: List<InntektPeriodeCore>,
    val sivilstandPeriodeListe: List<SivilstandPeriodeCore>,
    val barnIHusstandenPeriodeListe: List<BarnIHusstandenPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class SøknadsbarnCore(
    val referanse: String,
    val fødselsdato: LocalDate,
)

data class BostatusPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val kode: String,
)

data class SivilstandPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val kode: String,
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
    val beløp: BigDecimal,
    val kode: Resultatkode,
    val regel: String,
    val aldersgruppe: AldersgruppeForskudd,
)
