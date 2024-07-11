package no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning

import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import java.math.BigDecimal

class SærbidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        return when {
            grunnlag.bidragsevne.beløp < grunnlag.bPsAndelSærbidrag.andelBeløp ->
                ResultatBeregning(
                    resultatBeløp = BigDecimal.ZERO,
                    resultatkode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE,
                )

            grunnlag.bPsAndelSærbidrag.barnetErSelvforsørget ->
                ResultatBeregning(
                    resultatBeløp = BigDecimal.ZERO,
                    resultatkode = Resultatkode.BARNET_ER_SELVFORSØRGET,
                )

            else ->
                ResultatBeregning(
                    resultatBeløp = grunnlag.bPsAndelSærbidrag.andelBeløp,
                    resultatkode = Resultatkode.SÆRBIDRAG_INNVILGET,
                )
        }
    }
}
