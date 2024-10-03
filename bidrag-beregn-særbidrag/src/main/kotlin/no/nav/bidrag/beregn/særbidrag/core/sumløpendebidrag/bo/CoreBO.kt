package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore
import java.math.BigDecimal

data class BeregnSumLøpendeBidragResultat(val resultatPeriode: ResultatPeriode)

data class ResultatPeriode(val periode: Periode, val resultat: ResultatBeregning, val grunnlag: LøpendeBidragGrunnlagCore)

data class ResultatBeregning(val sum: BigDecimal, val sjablonListe: List<SjablonPeriodeNavnVerdi>)
