package no.nav.bidrag.beregn.core.dto

import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Collections.emptyList

// Felles Periode
data class PeriodeCore(val datoFom: LocalDate, val datoTil: LocalDate?)

// Felles Avvik
data class AvvikCore(val avvikTekst: String, val avvikType: String)

// Felles Delberegninger
interface Delberegning {
    val referanse: String
    val periode: PeriodeCore
    val grunnlagsreferanseListe: List<String>
}

data class InntektPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val beløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

data class BarnIHusstandenPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val antall: Double,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

data class VoksneIHusstandenPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val borMedAndreVoksne: Boolean,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

data class BoforholdPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val antallBarn: Double,
    val borMedAndreVoksne: Boolean,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

// Felles Sjabloner
data class SjablonPeriodeCore(
    val periode: PeriodeCore,
    val navn: String,
    val nøkkelListe: List<SjablonNøkkelCore>? = emptyList(),
    val innholdListe: List<SjablonInnholdCore>,
)

data class FaktiskUtgiftPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val gjelderBarn: Grunnlagsreferanse,
    val beregnetBeløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

data class TilleggsstønadPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val gjelderBarn: Grunnlagsreferanse,
    val beregnetBeløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : Delberegning

data class SjablonCore(val navn: String, val nøkkelListe: List<SjablonNøkkelCore>? = emptyList(), val innholdListe: List<SjablonInnholdCore>)

data class SjablonNøkkelCore(val navn: String, val verdi: String)

data class SjablonInnholdCore(val navn: String, val verdi: BigDecimal)

data class SjablonNavnVerdiCore(val navn: String, val verdi: BigDecimal)

data class SjablonResultatGrunnlagCore(val referanse: String, val periode: PeriodeCore, val navn: String, val verdi: BigDecimal)
