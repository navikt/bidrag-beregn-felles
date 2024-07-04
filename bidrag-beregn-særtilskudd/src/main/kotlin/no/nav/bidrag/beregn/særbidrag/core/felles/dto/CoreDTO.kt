package no.nav.bidrag.beregn.særbidrag.core.felles.dto

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import java.math.BigDecimal

interface DelberegningSærtilskudd {
    val referanse: String
    val periode: PeriodeCore
    val grunnlagsreferanseListe: List<String>
}

data class InntektPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val beløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

data class BarnIHusstandenPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val antall: Double,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

data class VoksneIHusstandenPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val borMedAndre: Boolean,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

// data class BidragsevnePeriodeCore(
//    override val referanse: String,
//    override val periode: PeriodeCore,
//    val beløp: BigDecimal,
//    override val grunnlagsreferanseListe: List<String>,
// ) : DelberegningSærtilskudd

// data class BpsAndelSærtilskuddPeriodeCore(
//    override val referanse: String,
//    override val periode: PeriodeCore,
//    val andel: BigDecimal,
//    override val grunnlagsreferanseListe: List<String>,
// ) : DelberegningSærtilskudd

// data class SamværsfradragPeriodeCore(
//    override val referanse: String,
//    override val periode: PeriodeCore,
//    val beløp: BigDecimal,
//    override val grunnlagsreferanseListe: List<String>,
// ) : DelberegningSærtilskudd
