package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregningSumLøpendeBidragPerBarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagInnhold
import java.math.BigDecimal
import java.time.LocalDate

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
    val personidentBarn: Personident,
    val referanseBarn: String,
    val løpendeBeløp: BigDecimal,
    val valutakode: String,
    val samværsklasse: Samværsklasse,
    val beregnetBeløp: BigDecimal,
    val faktiskBeløp: BigDecimal,
)

// Resultatperiode
data class BeregnSumLøpendeBidragResultatCore(val resultatPeriode: ResultatPeriodeCore, val sjablonListe: List<SjablonResultatGrunnlagCore>)

data class ResultatPeriodeCore(
    override val periode: PeriodeCore,
    val resultat: ResultatBeregningCore,
    override val grunnlagsreferanseListe: MutableList<String>,
) : IResultatPeriode

data class ResultatBeregningCore(val sumLøpendeBidrag: BigDecimal, val beregningPerBarn: List<BeregningSumLøpendeBidragPerBarn>)
