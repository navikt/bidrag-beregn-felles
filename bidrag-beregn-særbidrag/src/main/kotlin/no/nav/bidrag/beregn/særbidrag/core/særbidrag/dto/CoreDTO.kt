package no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregningSumLøpendeBidragPerBarn
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnSærbidragGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val søknadsbarnPersonId: String,
    val betaltAvBpPeriodeListe: List<BetaltAvBpPeriodeCore>,
    val bidragsevnePeriodeListe: List<BidragsevnePeriodeCore>,
    val sumLøpendeBidrag: SumLøpendeBidragPeriodeCore,
    val bPsAndelSærbidragPeriodeListe: List<BPsAndelSærbidragPeriodeCore>,
)

data class BetaltAvBpPeriodeCore(val referanse: String, val periode: PeriodeCore, val beløp: BigDecimal)

data class BidragsevnePeriodeCore(val referanse: String, val periode: PeriodeCore, val beløp: BigDecimal)

data class SumLøpendeBidragPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val sumLøpendeBidrag: BigDecimal,
    val beregningPerBarn: List<BeregningSumLøpendeBidragPerBarn>,
)

data class BPsAndelSærbidragPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val andelFaktor: BigDecimal,
    val andelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)

// Resultatperiode
data class BeregnSærbidragResultatCore(val resultatPeriodeListe: List<ResultatPeriodeCore>, val avvikListe: List<AvvikCore>)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val søknadsbarnPersonId: String,
    val resultat: ResultatBeregningCore,
    override val grunnlagsreferanseListe: MutableList<String>,
) : IResultatPeriode

data class ResultatBeregningCore(val beregnetBeløp: BigDecimal, val resultatKode: Resultatkode, val resultatBeløp: BigDecimal?)
