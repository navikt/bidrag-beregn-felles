package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.InntektPeriodeCore
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBPsAndelSærbidragGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val inntektBPPeriodeListe: List<InntektPeriodeCore>,
    val inntektBMPeriodeListe: List<InntektPeriodeCore>,
    val inntektSBPeriodeListe: List<InntektPeriodeCore>,
    val utgiftPeriodeListe: List<UtgiftPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class UtgiftPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val beløp: BigDecimal,
)

// Resultatperiode
data class BeregnBPsAndelSærbidragResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: List<SjablonResultatGrunnlagCore>,
    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultat: ResultatBeregningCore,
    override val grunnlagsreferanseListe: MutableList<String>,
) : IResultatPeriode

data class ResultatBeregningCore(
    val resultatAndelProsent: BigDecimal,
    val resultatAndelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)
