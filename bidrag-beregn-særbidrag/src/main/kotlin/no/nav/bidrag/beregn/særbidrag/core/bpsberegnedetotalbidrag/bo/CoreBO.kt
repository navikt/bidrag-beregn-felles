package no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregnetBidragPerBarn
import java.math.BigDecimal

data class BeregnBPsBeregnedeTotalbidragResultat(val resultatPeriode: ResultatPeriode)

data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: LøpendeBidragGrunnlagCore)

data class ResultatBeregning(
    val bPsBeregnedeTotalbidrag: BigDecimal,
    val beregnetBidragPerBarn: List<BeregnetBidragPerBarn>,
    val sjablonListe: List<SjablonPeriodeNavnVerdi>,
)
