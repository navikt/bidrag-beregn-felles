package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnSamværsfradragApiTest: FellesApiTest() {
    private lateinit var filnavn: String
    private lateinit var forventetSamværsfradrag: BigDecimal
    private var forventetAntallSjablonSamværsfradrag: Int = 1

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel 1 - samværsklasse mangler for deler av perioden")
    fun testSamværsfradrag_Eksempel01() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel1.json"
        val request = lesFilOgByggRequest(filnavn)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            api.beregnSamværsfradrag(request)
        }
        assertThat(exception.message).contains("Ingen samværsklasse funnet")
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel 2 - samværsklasse er delt bosted")
    fun testSamværsfradrag_Eksempel02() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel2.json"
        forventetSamværsfradrag = BigDecimal.ZERO.setScale(2)
        forventetAntallSjablonSamværsfradrag = 0
        utførBeregningerOgEvaluerResultatSamværsfradrag()
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel 3 - vanlig samværsklasse")
    fun testSamværsfradrag_Eksempel03() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel3.json"
        forventetSamværsfradrag = BigDecimal.valueOf(547).setScale(2)
        forventetAntallSjablonSamværsfradrag = 1
        utførBeregningerOgEvaluerResultatSamværsfradrag()
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel 4 - ugyldig samværsklasse")
    fun testSamværsfradrag_Eksempel04() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel4.json"
        val request = lesFilOgByggRequest(filnavn)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            api.beregnSamværsfradrag(request)
        }
        assertThat(exception.message).contains("Innhold i Grunnlagstype.SAMVÆRSPERIODE er ikke gyldig")
    }

    @Test
    @DisplayName("Samværsfradrag - eksempel med flere perioder")
    fun testSamværsfradrag_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/samværsfradrag/samværsfradrag_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatSamværsfradragFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatSamværsfradrag() {
        val request = lesFilOgByggRequest(filnavn)
        val samværsfradragResultat = api.beregnSamværsfradrag(request)
        printJson(samværsfradragResultat)

        val alleReferanser = hentAlleReferanser(samværsfradragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(samværsfradragResultat)

        val samværsfradragResultatListe = samværsfradragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
            .map {
                DelberegningSamværsfradrag(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }

        val antallSamværsklasse = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallSjablonSamværsfradrag = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SJABLON_SAMVARSFRADRAG }
            .size

        assertAll(
            { assertThat(samværsfradragResultat).isNotNull },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(1) },

            // Resultat
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(forventetSamværsfradrag) },

            // Grunnlag
            { assertThat(antallSamværsklasse).isEqualTo(1) },
            { assertThat(antallSjablonSamværsfradrag).isEqualTo(forventetAntallSjablonSamværsfradrag) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatSamværsfradragFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val samværsfradragResultat = api.beregnSamværsfradrag(request)
        printJson(samværsfradragResultat)

        val alleReferanser = hentAlleReferanser(samværsfradragResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(samværsfradragResultat)

        val samværsfradragResultatListe = samværsfradragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
            .map {
                DelberegningSamværsfradrag(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }

        val antallSamværsklasse = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SAMVÆRSPERIODE }
            .size

        val antallSjablonSamværsfradrag = samværsfradragResultat
            .filter { it.type == Grunnlagstype.SJABLON_SAMVARSFRADRAG }
            .size

        assertAll(
            { assertThat(samværsfradragResultat).isNotNull },
            { assertThat(samværsfradragResultatListe).isNotNull },
            { assertThat(samværsfradragResultatListe).hasSize(6) },

            // Resultat
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2021-05", "2021-07")) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(353.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2021-07", "2022-07")) },
            { assertThat(samværsfradragResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(354.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2022-07", "2023-01")) },
            { assertThat(samværsfradragResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(365.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2023-01", "2023-07")) },
            { assertThat(samværsfradragResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(1209.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2023-07", "2024-07")) },
            { assertThat(samværsfradragResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(1760.00).setScale(2)) },
            { assertThat(samværsfradragResultatListe[5].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(samværsfradragResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(1813.00).setScale(2)) },

            // Grunnlag
            { assertThat(antallSamværsklasse).isEqualTo(2) },
            { assertThat(antallSjablonSamværsfradrag).isEqualTo(6) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
