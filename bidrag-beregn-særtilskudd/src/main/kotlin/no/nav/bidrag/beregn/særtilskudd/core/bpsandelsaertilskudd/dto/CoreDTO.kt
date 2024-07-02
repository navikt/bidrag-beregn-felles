package no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.dto

import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.Inntekt
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.IResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.InntektPeriodeCore
import java.math.BigDecimal
import java.time.LocalDate

// Grunnlag periode
data class BeregnBPsAndelSaertilskuddGrunnlagCore(
    val beregnDatoFra: LocalDate,
    val beregnDatoTil: LocalDate,
    val nettoSaertilskuddPeriodeListe: List<NettoSaertilskuddPeriodeCore>,
    val inntektBPPeriodeListe: List<InntektPeriodeCore>,
    val inntektBMPeriodeListe: List<InntektPeriodeCore>,
    val inntektBBPeriodeListe: List<InntektPeriodeCore>,
    var sjablonPeriodeListe: List<SjablonPeriodeCore>,
)

data class NettoSaertilskuddPeriodeCore(val referanse: String, val periodeDatoFraTil: PeriodeCore, val nettoSaertilskuddBelop: BigDecimal)

// Resultatperiode
data class BeregnBPsAndelSaertilskuddResultatCore(
    val resultatPeriodeListe: List<ResultatPeriodeCore>,
    val sjablonListe: List<SjablonResultatGrunnlagCore>,
    val avvikListe: List<AvvikCore>,
)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultatBeregning: ResultatBeregningCore,
    val beregnedeGrunnlag: BeregnedeGrunnlagCore,
    override val grunnlagReferanseListe: List<String>,
) : IResultatPeriode

data class ResultatBeregningCore(val resultatAndelProsent: BigDecimal, val resultatAndelBelop: BigDecimal, val barnetErSelvforsorget: Boolean)

data class BeregnedeGrunnlagCore(val inntektBPListe: List<Inntekt>, val inntektBMListe: List<Inntekt>, val inntektBBListe: List<Inntekt>)
