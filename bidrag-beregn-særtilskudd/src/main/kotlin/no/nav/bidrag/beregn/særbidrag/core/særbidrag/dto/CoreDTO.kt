package no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnSærbidragGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val søknadsbarnPersonId: String,
    val bidragsevnePeriodeListe: List<BidragsevnePeriodeCore>,
    val bPsAndelSærbidragPeriodeListe: List<BPsAndelSærbidragPeriodeCore>,
)

data class BidragsevnePeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val beløp: BigDecimal
)

data class BPsAndelSærbidragPeriodeCore(
    val referanse: String,
    val periode: PeriodeCore,
    val andelProsent: BigDecimal,
    val andelBeløp: BigDecimal,
    val barnetErSelvforsørget: Boolean,
)

// Resultatperiode
data class BeregnSærbidragResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val avvikListe: List<AvvikCore>
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val søknadsbarnPersonId: String,
    val resultat: ResultatBeregningCore,
    override val grunnlagsreferanseListe: List<String>,
) : IResultatPeriode

data class ResultatBeregningCore(
    val beløp: BigDecimal,
    val kode: String
)
