package no.nav.bidrag.beregn.særbidrag.core.sluttberegning

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.LopendeBidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.SamvaersfradragGrunnlagPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class SluttberegningPeriodeTest {

    private val særbidragBeregning = SærbidragBeregning()
    private val særbidragPeriode = SærbidragPeriode(særbidragBeregning)

    @Test
    @DisplayName("Test at resultatperiode er lik beregn-fra-og-til-periode i input og ingen andre perioder dannes")
    fun testPaaPeriode() {
        val bidragsevnePeriodeListe = mutableListOf<BidragsevnePeriode>()

        var bidragsevnePeriode = BidragsevnePeriode(
            referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
            periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2019-07-01")),
            bidragsevneBelop = BigDecimal.valueOf(11000),
        )
        bidragsevnePeriodeListe.add(bidragsevnePeriode)

        bidragsevnePeriode = BidragsevnePeriode(
            referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
            periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = LocalDate.parse("2020-01-01")),
            bidragsevneBelop = BigDecimal.valueOf(11069),
        )
        bidragsevnePeriodeListe.add(bidragsevnePeriode)

        val bPsAndelSaertilskuddPeriodeListe = listOf(
            BPsAndelSaertilskuddPeriode(
                referanse = TestUtil.BPS_ANDEL_SÆRTILSKUDD_REFERANSE,
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                bPsAndelSaertilskuddProsent = BigDecimal.valueOf(60.6),
                bPsAndelSaertilskuddBelop = BigDecimal.valueOf(4242),
                barnetErSelvforsorget = false,
            ),
        )

        val lopendeBidragPeriodeListe = listOf(
            LopendeBidragPeriode(
                referanse = TestUtil.LØPENDE_BIDRAG_REFERANSE,
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                barnPersonId = 1,
                lopendeBidragBelop = BigDecimal.valueOf(2500),
                opprinneligBPsAndelUnderholdskostnadBelop = BigDecimal.valueOf(2958),
                opprinneligBidragBelop = BigDecimal.valueOf(2500),
                opprinneligSamvaersfradragBelop = BigDecimal.valueOf(457),
            ),
        )

        val samvaersfradragPeriodeListe = listOf(
            SamvaersfradragGrunnlagPeriode(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 1,
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                samvaersfradragBelop = BigDecimal.valueOf(457),
            ),
        )

        val beregnSaertilskuddGrunnlag = BeregnSaertilskuddGrunnlag(
            beregnDatoFra = LocalDate.parse("2019-08-01"),
            beregnDatoTil = LocalDate.parse("2019-09-01"),
            soknadsbarnPersonId = 1,
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            bPsAndelSaertilskuddPeriodeListe = bPsAndelSaertilskuddPeriodeListe,
            lopendeBidragPeriodeListe = lopendeBidragPeriodeListe,
            samvaersfradragGrunnlagPeriodeListe = samvaersfradragPeriodeListe,
            sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe(),
        )

        val resultat = særbidragPeriode.beregnPerioder(beregnSaertilskuddGrunnlag)

        assertAll(
            { assertThat(resultat.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2019-08-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2019-09-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultat.resultatBelop.toDouble()).isEqualTo(4242.0) },
            {
                assertThat(resultat.resultatPeriodeListe[0].resultat.resultatkode).isEqualTo(Resultatkode.SÆRTILSKUDD_INNVILGET)
            },
        )
    }
}
