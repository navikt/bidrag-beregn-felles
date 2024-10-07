package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregningSumLøpendeBidragPerBarn
import java.math.BigDecimal

data class BeregnSumLøpendeBidragResultat(val resultatPeriode: ResultatPeriode)

data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: LøpendeBidragGrunnlagCore)

data class ResultatBeregning(
    val sumLøpendeBidrag: BigDecimal,
    val beregningPerBarn: List<BeregningSumLøpendeBidragPerBarn>,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)
