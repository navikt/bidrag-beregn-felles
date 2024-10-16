package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

@ExtendWith(MockitoExtension::class)
internal class BeregnBidragsevneApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetBidragsevne: BigDecimal
    private lateinit var forventetMinstefradrag: BigDecimal
    private lateinit var forventetSkattAlminneligInntekt: BigDecimal
    private lateinit var forventetTrinnskatt: BigDecimal
    private lateinit var forventetTrygdeavgift: BigDecimal
    private lateinit var forventetSumSkatt: BigDecimal
    private lateinit var forventetUnderholdBarnEgenHusstand: BigDecimal

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 1 - Inntekt 200000 - Bor ikke med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel01() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel1.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 2 - Inntekt 200000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel02() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel2.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 3 - Inntekt 200000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel03() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel3.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 4 - Inntekt 200000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel04() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel4.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 5 - Inntekt 200000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel05() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel5.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 6 - Inntekt 200000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel06() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel6.json"
        forventetBidragsevne = BigDecimal.ZERO
        forventetMinstefradrag = BigDecimal.valueOf(80000)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985)
        forventetTrinnskatt = BigDecimal.ZERO
        forventetTrygdeavgift = BigDecimal.valueOf(15600)
        forventetSumSkatt = BigDecimal.valueOf(22585)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 7 - Inntekt 700000 - Bor ikke med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel07() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel7.json"
        forventetBidragsevne = BigDecimal.valueOf(19090)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 8 - Inntekt 700000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel08() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel8.json"
        forventetBidragsevne = BigDecimal.valueOf(14837)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 9 - Inntekt 700000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel09() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel9.json"
        forventetBidragsevne = BigDecimal.valueOf(8457)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 10 - Inntekt 700000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel10() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel10.json"
        forventetBidragsevne = BigDecimal.valueOf(25189)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 11 - Inntekt 700000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel11() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel11.json"
        forventetBidragsevne = BigDecimal.valueOf(20936)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 12 - Inntekt 700000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel12() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel12.json"
        forventetBidragsevne = BigDecimal.valueOf(14556)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610)
        forventetTrinnskatt = BigDecimal.valueOf(20608)
        forventetTrygdeavgift = BigDecimal.valueOf(54600)
        forventetSumSkatt = BigDecimal.valueOf(190818)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 13 - Inntekt 1500000 - Bor ikke med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel13() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel13.json"
        forventetBidragsevne = BigDecimal.valueOf(55293)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 14 - Inntekt 1500000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel14() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel14.json"
        forventetBidragsevne = BigDecimal.valueOf(51040)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 15 - Inntekt 1500000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel15() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel15.json"
        forventetBidragsevne = BigDecimal.valueOf(44660)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 16 - Inntekt 1500000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel16() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel16.json"
        forventetBidragsevne = BigDecimal.valueOf(61392)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 17 - Inntekt 1500000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel17() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel17.json"
        forventetBidragsevne = BigDecimal.valueOf(57139)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 18 - Inntekt 1500000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel18() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel18.json"
        forventetBidragsevne = BigDecimal.valueOf(50759)
        forventetMinstefradrag = BigDecimal.valueOf(86250)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610)
        forventetTrinnskatt = BigDecimal.valueOf(147771)
        forventetTrygdeavgift = BigDecimal.valueOf(117000)
        forventetSumSkatt = BigDecimal.valueOf(556381)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel med flere perioder")
    fun testBidragsevne_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatBidragsevneFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatBidragsevne() {
        val request = lesFilOgByggRequest(filnavn)
        val bidragsevneResultat = beregnBarnebidragService.beregnBidragsevne(request)
        printJson(bidragsevneResultat)

        val alleReferanser = hentAlleReferanser(bidragsevneResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bidragsevneResultat)

        val bidragsevneResultatListe = bidragsevneResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragsevne>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE)
            .map {
                DelberegningBidragsevne(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                    skatt = it.innhold.skatt,
                    underholdBarnEgenHusstand = it.innhold.underholdBarnEgenHusstand,
                )
            }

        assertAll(
            { assertThat(bidragsevneResultat).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
            { assertThat(bidragsevneResultatListe).hasSize(1) },

            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(forventetBidragsevne) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(forventetMinstefradrag) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(forventetSkattAlminneligInntekt) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(forventetTrygdeavgift) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(forventetTrinnskatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(forventetSumSkatt) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(forventetUnderholdBarnEgenHusstand) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatBidragsevneFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val bidragsevneResultat = beregnBarnebidragService.beregnBidragsevne(request)
        printJson(bidragsevneResultat)

        val alleReferanser = hentAlleReferanser(bidragsevneResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bidragsevneResultat)

        val bidragsevneResultatListe = bidragsevneResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragsevne>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE)
            .map {
                DelberegningBidragsevne(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                    skatt = it.innhold.skatt,
                    underholdBarnEgenHusstand = it.innhold.underholdBarnEgenHusstand,
                )
            }

        assertAll(
            { assertThat(bidragsevneResultat).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
            { assertThat(bidragsevneResultatListe).hasSize(7) },

            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-09", "2023-11")) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(19800)) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513)) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(55300)) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(23627)) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(196440)) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(BigDecimal.ZERO) },

            { assertThat(bidragsevneResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-11", "2024-01")) },
            { assertThat(bidragsevneResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(15623)) },
            { assertThat(bidragsevneResultatListe[1].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[1].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513)) },
            { assertThat(bidragsevneResultatListe[1].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(55300)) },
            { assertThat(bidragsevneResultatListe[1].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(23627)) },
            { assertThat(bidragsevneResultatListe[1].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(196440)) },
            { assertThat(bidragsevneResultatListe[1].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124)) },

            { assertThat(bidragsevneResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-03")) },
            { assertThat(bidragsevneResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(15933)) },
            { assertThat(bidragsevneResultatListe[2].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[2].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513)) },
            { assertThat(bidragsevneResultatListe[2].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600)) },
            { assertThat(bidragsevneResultatListe[2].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20608)) },
            { assertThat(bidragsevneResultatListe[2].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192721)) },
            { assertThat(bidragsevneResultatListe[2].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124)) },

            { assertThat(bidragsevneResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2024-03", "2024-05")) },
            { assertThat(bidragsevneResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(21916)) },
            { assertThat(bidragsevneResultatListe[3].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[3].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513)) },
            { assertThat(bidragsevneResultatListe[3].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600)) },
            { assertThat(bidragsevneResultatListe[3].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20608)) },
            { assertThat(bidragsevneResultatListe[3].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192721)) },
            { assertThat(bidragsevneResultatListe[3].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124)) },

            { assertThat(bidragsevneResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(bidragsevneResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(19828)) },
            { assertThat(bidragsevneResultatListe[4].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[4].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513)) },
            { assertThat(bidragsevneResultatListe[4].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600)) },
            { assertThat(bidragsevneResultatListe[4].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20608)) },
            { assertThat(bidragsevneResultatListe[4].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192721)) },
            { assertThat(bidragsevneResultatListe[4].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(75186)) },

            { assertThat(bidragsevneResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            { assertThat(bidragsevneResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(18809)) },
            { assertThat(bidragsevneResultatListe[5].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[5].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(115610)) },
            { assertThat(bidragsevneResultatListe[5].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600)) },
            { assertThat(bidragsevneResultatListe[5].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20608)) },
            { assertThat(bidragsevneResultatListe[5].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(190818)) },
            { assertThat(bidragsevneResultatListe[5].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(76554)) },

            { assertThat(bidragsevneResultatListe[6].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-10")) },
            { assertThat(bidragsevneResultatListe[6].beløp).isEqualTo(BigDecimal.valueOf(32804)) },
            { assertThat(bidragsevneResultatListe[6].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250)) },
            { assertThat(bidragsevneResultatListe[6].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(181610)) },
            { assertThat(bidragsevneResultatListe[6].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(78000)) },
            { assertThat(bidragsevneResultatListe[6].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(63271)) },
            { assertThat(bidragsevneResultatListe[6].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(322881)) },
            { assertThat(bidragsevneResultatListe[6].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(76554)) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

    private fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
