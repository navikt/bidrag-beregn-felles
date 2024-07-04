package no.nav.bidrag.beregn.særbidrag.core.samvaersfradrag

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.samvaersfradrag.beregning.SamvaersfradragBeregning
import no.nav.bidrag.beregn.særbidrag.core.samvaersfradrag.bo.GrunnlagBeregningPeriodisert
import no.nav.bidrag.beregn.særbidrag.core.samvaersfradrag.bo.SamvaersfradragGrunnlagPerBarn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class SamvaersfradragBeregningTest {

    private val sjablonPeriodeListe = TestUtil.byggSjablonPeriodeListe()
    private val samvaersfradragBeregning = SamvaersfradragBeregning()

    @DisplayName("Test av beregning av samvaersfradrag for fireåring")
    @Test
    fun testFireAar() {
        val samvaersfradragGrunnlagPerBarnListe = listOf(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 1,
                barnAlder = 4,
                samvaersklasse = "03",
            ),
        )
        val resultatGrunnlag = GrunnlagBeregningPeriodisert(
            samvaersfradragGrunnlagPerBarnListe = samvaersfradragGrunnlagPerBarnListe,
            sjablonListe = sjablonPeriodeListe,
        )

        assertAll(
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].barnPersonId).isEqualTo(1) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].belop.toDouble()).isEqualTo(2272.0) },
        )
    }

    @DisplayName("Test av beregning av samvaersfradrag for seksåring")
    @Test
    fun testSeksAar() {
        val samvaersfradragGrunnlagPerBarnListe = listOf(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 1,
                barnAlder = 6,
                samvaersklasse = "03",
            ),
        )
        val resultatGrunnlag = GrunnlagBeregningPeriodisert(
            samvaersfradragGrunnlagPerBarnListe = samvaersfradragGrunnlagPerBarnListe,
            sjablonListe = sjablonPeriodeListe,
        )

        assertAll(
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].barnPersonId).isEqualTo(1) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].belop.toDouble()).isEqualTo(2716.0) },
        )
    }

    @DisplayName("Test av beregning av samvaersfradrag for fire-, seks- og elleveåring")
    @Test
    fun testFireSeksElleveAar() {
        val samvaersfradragGrunnlagPerBarnListe = mutableListOf<SamvaersfradragGrunnlagPerBarn>()
        samvaersfradragGrunnlagPerBarnListe.add(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 1,
                barnAlder = 4,
                samvaersklasse = "03",
            ),
        )
        samvaersfradragGrunnlagPerBarnListe.add(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 3,
                barnAlder = 6,
                samvaersklasse = "03",
            ),
        )
        samvaersfradragGrunnlagPerBarnListe.add(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 5,
                barnAlder = 11,
                samvaersklasse = "01",
            ),
        )

        val resultatGrunnlag = GrunnlagBeregningPeriodisert(
            samvaersfradragGrunnlagPerBarnListe = samvaersfradragGrunnlagPerBarnListe,
            sjablonListe = sjablonPeriodeListe,
        )

        assertAll(
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)).hasSize(3) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].barnPersonId).isEqualTo(1) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[1].barnPersonId).isEqualTo(3) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[2].barnPersonId).isEqualTo(5) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].belop.toDouble()).isEqualTo(2272.0) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[1].belop.toDouble()).isEqualTo(2716.0) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[2].belop.toDouble()).isEqualTo(457.0) },
        )
    }

    @DisplayName("Test av beregning av samvaersfradrag for fjortenåring")
    @Test
    fun testFjortenAar() {
        val samvaersfradragGrunnlagPerBarnListe = mutableListOf(
            SamvaersfradragGrunnlagPerBarn(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                barnPersonId = 2,
                barnAlder = 14,
                samvaersklasse = "01",
            ),
        )
        val resultatGrunnlag = GrunnlagBeregningPeriodisert(
            samvaersfradragGrunnlagPerBarnListe = samvaersfradragGrunnlagPerBarnListe,
            sjablonListe = sjablonPeriodeListe,
        )

        assertAll(
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].barnPersonId).isEqualTo(2) },
            { assertThat(samvaersfradragBeregning.beregn(resultatGrunnlag)[0].belop.toDouble()).isEqualTo(457.0) },
        )
    }
}
