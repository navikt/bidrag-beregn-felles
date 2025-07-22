package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnIndeksreguleringPrivatAvtaleApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnIndeksregulerPrivatAvtaleTest : FellesTest() {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnIndeksreguleringPrivatAvtaleApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnIndeksreguleringPrivatAvtaleApi()
    }

    @Test
    @DisplayName("Privat avtale - uten indeksregulering")
    fun testIndeksreguleringPrivatAvtaleUtenIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_uten_indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale().perioder

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2024-09")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(100.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-11")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(150.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-11"), null)) },
            { assertThat(resultat[2].beløp.compareTo(BigDecimal.valueOf(210.00))).isEqualTo(0) },
            { assertThat(resultat[2].indeksreguleringFaktor).isNull() },
        )
    }

    @Test
    @DisplayName("Privat avtale - med indeksregulering")
    fun testIndeksreguleringPrivatAvtaleMedIndeksregulering() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_med_indeksregulering.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale().perioder

        assertAll(
            { assertThat(resultat).hasSize(5) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2021-01", "2022-01")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(500.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2022-01", "2023-07")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(1000.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[2].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(resultat[2].beløp.compareTo(BigDecimal.valueOf(1070.00))).isEqualTo(0) },
            { assertThat(resultat[2].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0700))).isEqualTo(0) },

            { assertThat(resultat[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2025-07"))) },
            { assertThat(resultat[3].beløp.compareTo(BigDecimal.valueOf(1120.00))).isEqualTo(0) },
            { assertThat(resultat[3].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0470))).isEqualTo(0) },
        )
    }

    @Test
    @DisplayName("Privat avtale - med indeksregulering der tildato er satt. Skal da returnere uten å indeksregulere. Skal egentlig ikke skje.")
    fun testIndeksreguleringPrivatAvtaleMedIndeksreguleringTildatoSatt() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_med_indeksregulering_siste_periode_med_satt_tildato.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale().perioder

        assertAll(
            { assertThat(resultat).hasSize(2) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2021-01", "2022-01")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(500.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode("2022-01", "2023-10")) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(1000.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor).isNull() },
        )
    }

    @Test
    @DisplayName("Privat avtale - med indeksregulering Test periode hentes fra privatavtaleperioder")
    fun testIndeksreguleringPrivatAvtaleMedIndeksreguleringPerioderFraPrivatAvtalePerioder() {
        filnavn = "src/test/resources/testfiler/indeksreguleringprivatavtale/privat_avtale_med_indeksregulering_periode.json"
        val resultat = utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale().perioder

        assertAll(
            { assertThat(resultat).hasSize(3) },

            // Resultat
            { assertThat(resultat[0].periode).isEqualTo(ÅrMånedsperiode("2023-06", "2024-07")) },
            { assertThat(resultat[0].beløp.compareTo(BigDecimal.valueOf(2000.00))).isEqualTo(0) },
            { assertThat(resultat[0].indeksreguleringFaktor).isNull() },

            { assertThat(resultat[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2025-07"))) },
            { assertThat(resultat[1].beløp.compareTo(BigDecimal.valueOf(2090.00))).isEqualTo(0) },
            { assertThat(resultat[1].indeksreguleringFaktor?.compareTo(BigDecimal.valueOf(0.0470))).isEqualTo(0) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatIndeksreguleringPrivatAvtale(): DelberegningPrivatAvtale {
        val request = lesFilOgByggRequest(filnavn)
        val resultat = api.beregnIndeksreguleringPrivatAvtale(request)
        printJson(resultat)

        val alleReferanser = hentAlleReferanser(resultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(resultat)

        val resultatListe = resultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningPrivatAvtale>(Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE)
            .first().innhold

        assertAll(
            { assertThat(resultat).isNotNull },
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
        return resultatListe
    }
}
