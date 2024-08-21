package no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning

import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal

class SærbidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        return when {
            (grunnlag.bidragsevne.beløp == BigDecimal.ZERO) ||
                (grunnlag.bidragsevne.beløp < grunnlag.bPsAndelSærbidrag.andelBeløp) ->
                ResultatBeregning(
                    beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp,
                    resultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE,
                    resultatBeløp = null,
                )

            grunnlag.bPsAndelSærbidrag.barnetErSelvforsørget ->
                ResultatBeregning(
                    beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp,
                    resultatKode = Resultatkode.BARNET_ER_SELVFORSØRGET,
                    resultatBeløp = null,
                )

            else ->
                ResultatBeregning(
                    beregnetBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp,
                    resultatKode = Resultatkode.SÆRBIDRAG_INNVILGET,
                    resultatBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp,
                )
        }
    }
}
