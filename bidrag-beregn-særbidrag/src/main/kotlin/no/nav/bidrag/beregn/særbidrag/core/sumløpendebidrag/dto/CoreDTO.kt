package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagInnhold
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
// data class BeregnSumLøpendeBidragGrunnlagCore(
//    val beregnDatoFra: LocalDate,
//    val beregnDatoTil: LocalDate,
//    val referanse: String,
//    val løpendeBidragCoreListe: List<LøpendeBidragCore>,
//    val sjablonPeriodeListe: List<SjablonPeriodeCore>,
// )

data class LøpendeBidragGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val referanse: String,
    val løpendeBidragCoreListe: List<LøpendeBidragCore>,
    val grunnlagsreferanseListe: List<String>,
    val sjablonPeriodeListe: List<SjablonPeriode>,
) : GrunnlagInnhold

data class LøpendeBidragCore(
    val saksnummer: Saksnummer,
    val fødselsdatoBarn: LocalDate,
    val løpendeBeløp: BigDecimal,
    val samværsklasse: Samværsklasse,
    val beregnetBeløp: BigDecimal,
    val faktiskBeløp: BigDecimal,
)

// Resultatperiode
data class BeregnSumLøpendeBidragResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: MutableList<SjablonResultatGrunnlagCore>,
//    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultat: ResultatBeregningCore,
    override val grunnlagsreferanseListe: MutableList<String>,
) : IResultatPeriode

data class ResultatBeregningCore(val sum: BigDecimal)
