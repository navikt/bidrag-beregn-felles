package no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning

import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal
import java.math.RoundingMode

class SærbidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning = when {
        (grunnlag.bidragsevne.beløp == (BigDecimal.valueOf(0.00).setScale(2))) ||
            (grunnlag.bidragsevne.beløp < grunnlag.bPsBeregnedeTotalbidrag.bPsBeregnedeTotalbidrag) ->
            ResultatBeregning(
                beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp.setScale(2, RoundingMode.HALF_UP),
                resultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE,
                resultatBeløp = null,
            )

        grunnlag.bPsAndelSærbidrag.barnetErSelvforsørget ->
            ResultatBeregning(
                beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp.setScale(2, RoundingMode.HALF_UP),
                resultatKode = Resultatkode.BARNET_ER_SELVFORSØRGET,
                resultatBeløp = null,
            )

        else ->
            ResultatBeregning(
                beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp.setScale(2, RoundingMode.HALF_UP),
                resultatKode = Resultatkode.SÆRBIDRAG_INNVILGET,
                resultatBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp.setScale(0, RoundingMode.HALF_UP),
            )
    }
}
