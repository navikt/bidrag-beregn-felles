package no.nav.bidrag.beregn.core.felles.dto

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import java.math.BigDecimal

data class InntektPeriodeCore(val referanse: String, val periode: PeriodeCore, val beløp: BigDecimal, val grunnlagsreferanseListe: List<String>)

interface DelberegningSærtilskudd {
    val referanse: String
    val periode: PeriodeCore
    val grunnlagsreferanseListe: List<String>
}

data class BarnIHusstandenPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val antall: Int,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

data class BidragsevnePeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val beløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

data class BpsAndelSærtilskuddPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val andel: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd

data class SamværsfradragPeriodeCore(
    override val referanse: String,
    override val periode: PeriodeCore,
    val beløp: BigDecimal,
    override val grunnlagsreferanseListe: List<String>,
) : DelberegningSærtilskudd
