package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class SærbidragPeriodeTest {

    private val særbidragBeregning = SærbidragBeregning()
    private val særbidragPeriode = SærbidragPeriode(særbidragBeregning)

    @Test
    @DisplayName("Test at resultatperiode er innenfor beregnDatoFra og beregnDatoTil og at ingen andre perioder dannes")
    fun testPeriode() {
        val bidragsevnePeriodeListe = listOf(
            BidragsevnePeriode(
                referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2019-07-01")),
                beløp = BigDecimal.valueOf(11000),
            ),
            BidragsevnePeriode(
                referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                periode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(11069),
            ),
        )

        val bPsAndelSærbidragPeriodeListe = listOf(
            BPsAndelSærbidragPeriode(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                andelProsent = BigDecimal.valueOf(60.6),
                andelBeløp = BigDecimal.valueOf(4242),
                barnetErSelvforsørget = false,
            ),
        )

        val beregnSærbidragGrunnlag = BeregnSærbidragGrunnlag(
            beregnDatoFra = LocalDate.parse("2019-08-01"),
            beregnDatoTil = LocalDate.parse("2019-09-01"),
            søknadsbarnPersonId = "1",
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            bPsAndelSærbidragPeriodeListe = bPsAndelSærbidragPeriodeListe,
        )

        val resultat = særbidragPeriode.beregnPerioder(beregnSærbidragGrunnlag)

        assertAll(
            { assertThat(resultat.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2019-08-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2019-09-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultat.resultatBeløp.toDouble()).isEqualTo(4242.0) },
            {
                assertThat(resultat.resultatPeriodeListe[0].resultat.resultatkode).isEqualTo(Resultatkode.SÆRBIDRAG_INNVILGET)
            },
        )
    }
}
