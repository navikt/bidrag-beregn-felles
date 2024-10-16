package no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.beregning.BPsBeregnedeTotalbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Collections.emptyList

internal class BPsBeregnedeTotalbidragBeregningTest {

    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val bPsBeregnedeTotalbidrag = BPsBeregnedeTotalbidragBeregning()

    @DisplayName("Beregning med flere saker og barn")
    @Test
    fun beregning() {
        val grunnlag = LøpendeBidragGrunnlagCore(
            beregnDatoFra = LocalDate.of(2020, 8, 1),
            beregnDatoTil = LocalDate.of(2020, 9, 1),
            referanse = "referanse",
            løpendeBidragCoreListe = listOf(
                LøpendeBidragCore(
                    saksnummer = Saksnummer("1"),
                    fødselsdatoBarn = LocalDate.of(2000, 5, 4),
                    personidentBarn = Personident("04050078901"),
                    referanseBarn = "referanseBarn",
                    løpendeBeløp = BigDecimal.valueOf(1200),
                    valutakode = "NOK",
                    samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1, // 528
                    beregnetBeløp = BigDecimal.valueOf(1004),
                    faktiskBeløp = BigDecimal.valueOf(900),
                ),
                LøpendeBidragCore(
                    saksnummer = Saksnummer("2"),
                    fødselsdatoBarn = LocalDate.of(2001, 5, 4),
                    personidentBarn = Personident("040501678901"),
                    referanseBarn = "referanseBarn2",
                    løpendeBeløp = BigDecimal.valueOf(1350),
                    valutakode = "NOK",
                    samværsklasse = Samværsklasse.SAMVÆRSKLASSE_2, // 1749
                    beregnetBeløp = BigDecimal.valueOf(1164),
                    faktiskBeløp = BigDecimal.valueOf(1010),
                ),
                LøpendeBidragCore(
                    saksnummer = Saksnummer("3"),
                    fødselsdatoBarn = LocalDate.of(2002, 5, 4),
                    personidentBarn = Personident("04050278901"),
                    referanseBarn = "referanseBarn3",
                    løpendeBeløp = BigDecimal.valueOf(2140),
                    valutakode = "NOK",
                    samværsklasse = Samværsklasse.SAMVÆRSKLASSE_3, // 3528
                    beregnetBeløp = BigDecimal.valueOf(1725),
                    faktiskBeløp = BigDecimal.valueOf(1700),
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
        val resultat = bPsBeregnedeTotalbidrag.beregn(grunnlag)

        assertThat(resultat.bPsBeregnedeTotalbidrag).isEqualTo(BigDecimal.valueOf(10775))
    }
}
