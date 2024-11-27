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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnBidragsevneApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetBidragsevne: BigDecimal
    private lateinit var forventetMinstefradrag: BigDecimal
    private lateinit var forventetSkattAlminneligInntekt: BigDecimal
    private lateinit var forventetTrinnskatt: BigDecimal
    private lateinit var forventetTrygdeavgift: BigDecimal
    private lateinit var forventetSumSkatt: BigDecimal
    private lateinit var forventetSumSkattFaktor: BigDecimal
    private lateinit var forventetUnderholdBarnEgenHusstand: BigDecimal
    private lateinit var forventetSumInntekt25Prosent: BigDecimal
    private var forventetAntallInntektRapporteringPeriodeBP: Int = 1
    private var forventetAntallDelberegningSumInntektPeriodeBP: Int = 1
    private var forventetAntallDelberegningBoforholdPeriode: Int = 1
    private var forventetAntallDelberegningBarnIHusstandPeriode: Int = 1
    private var forventetAntallDelberegningVoksneIHusstandPeriode: Int = 1
    private var forventetAntallBostatusPeriodeBP: Int = 1
    private var forventetAntallBostatusPeriodeSB: Int = 1
    private var forventetAntallSjablonSjablontall: Int = 7
    private var forventetAntallSjablonBidragsevne: Int = 1
    private var forventetAntallSjablonTrinnvisSkattesats: Int = 1

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
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 2 - Inntekt 200000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel02() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel2.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 3 - Inntekt 200000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel03() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel3.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 4 - Inntekt 200000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel04() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel4.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 5 - Inntekt 200000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel05() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel5.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 6 - Inntekt 200000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel06() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel6.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(80000.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(6985.00).setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(15600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(22585.00).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.112925).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(4166.67).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 7 - Inntekt 700000 - Bor ikke med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel07() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel7.json"
        forventetBidragsevne = BigDecimal.valueOf(19089.87).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 8 - Inntekt 700000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel08() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel8.json"
        forventetBidragsevne = BigDecimal.valueOf(14836.87).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 9 - Inntekt 700000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel09() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel9.json"
        forventetBidragsevne = BigDecimal.valueOf(8457.37).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 10 - Inntekt 700000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel10() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel10.json"
        forventetBidragsevne = BigDecimal.valueOf(25188.87).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 11 - Inntekt 700000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel11() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel11.json"
        forventetBidragsevne = BigDecimal.valueOf(20935.87).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 12 - Inntekt 700000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel12() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel12.json"
        forventetBidragsevne = BigDecimal.valueOf(14556.37).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(115610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(20607.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(54600.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(190817.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.2725965714).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(14583.33).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 13 - Inntekt 1500000 - Bor ikke med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel13() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel13.json"
        forventetBidragsevne = BigDecimal.valueOf(55292.95).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 14 - Inntekt 1500000 - Bor ikke med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel14() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel14.json"
        forventetBidragsevne = BigDecimal.valueOf(51039.95).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 15 - Inntekt 1500000 - Bor ikke med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel15() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel15.json"
        forventetBidragsevne = BigDecimal.valueOf(44660.45).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 16 - Inntekt 1500000 - Bor med andre voksne - 0 barn i husstand")
    fun testBidragsevne_Eksempel16() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel16.json"
        forventetBidragsevne = BigDecimal.valueOf(61391.95).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 17 - Inntekt 1500000 - Bor med andre voksne - 1 barn i husstand")
    fun testBidragsevne_Eksempel17() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel17.json"
        forventetBidragsevne = BigDecimal.valueOf(57138.95).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(51036.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 18 - Inntekt 1500000 - Bor med andre voksne - 2,5 barn i husstand")
    fun testBidragsevne_Eksempel18() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel18.json"
        forventetBidragsevne = BigDecimal.valueOf(50759.45).setScale(2)
        forventetMinstefradrag = BigDecimal.valueOf(86250.00).setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.valueOf(291610.00).setScale(2)
        forventetTrinnskatt = BigDecimal.valueOf(147770.60).setScale(2)
        forventetTrygdeavgift = BigDecimal.valueOf(117000.00).setScale(2)
        forventetSumSkatt = BigDecimal.valueOf(556380.60).setScale(2)
        forventetSumSkattFaktor = BigDecimal.valueOf(0.3709204).setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.valueOf(127590.00).setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.valueOf(31250.00).setScale(2)
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel 19 - BP's inntekt mangler")
    fun testBidragsevne_Eksempel19() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel19.json"
        forventetBidragsevne = BigDecimal.ZERO.setScale(2)
        forventetMinstefradrag = BigDecimal.ZERO.setScale(2)
        forventetSkattAlminneligInntekt = BigDecimal.ZERO.setScale(2)
        forventetTrinnskatt = BigDecimal.ZERO.setScale(2)
        forventetTrygdeavgift = BigDecimal.ZERO.setScale(2)
        forventetSumSkatt = BigDecimal.ZERO.setScale(2)
        forventetSumSkattFaktor = BigDecimal.ZERO.setScale(10)
        forventetUnderholdBarnEgenHusstand = BigDecimal.ZERO.setScale(2)
        forventetSumInntekt25Prosent = BigDecimal.ZERO.setScale(2)
        forventetAntallInntektRapporteringPeriodeBP = 0
        utførBeregningerOgEvaluerResultatBidragsevne()
    }

    @Test
    @DisplayName("Bidragsevne - eksempel med flere perioder")
    fun testBidragsevne_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/bidragsevne/bidragsevne_eksempel_flere_perioder.json"
        forventetAntallInntektRapporteringPeriodeBP = 2
        forventetAntallDelberegningSumInntektPeriodeBP = 2
        forventetAntallDelberegningBoforholdPeriode = 4
        forventetAntallDelberegningBarnIHusstandPeriode = 3
        forventetAntallDelberegningVoksneIHusstandPeriode = 2
        forventetAntallBostatusPeriodeBP = 2
        forventetAntallBostatusPeriodeSB = 2
        forventetAntallSjablonSjablontall = 10
        forventetAntallSjablonBidragsevne = 3
        forventetAntallSjablonTrinnvisSkattesats = 2
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
                    sumInntekt25Prosent = it.innhold.sumInntekt25Prosent,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseSB = request.søknadsbarnReferanse

        val antallInntektRapporteringPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningSumInntektPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBoforholdPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BOFORHOLD }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBarnIHusstandPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningVoksneIHusstandPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallBostatusPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .filter { it.gjelderBarnReferanse == null }
            .size

        val antallBostatusPeriodeSB = bidragsevneResultat
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .filter { it.gjelderBarnReferanse == referanseSB }
            .size

        val antallSjablonSjablontall = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        val antallSjablonBidragsevne = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_BIDRAGSEVNE }
            .size

        val antallSjablonTrinnvisSkattesats = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
            .size

        assertAll(
            { assertThat(bidragsevneResultat).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
            { assertThat(bidragsevneResultatListe).hasSize(1) },

            // Resultat
            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(forventetBidragsevne) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(forventetMinstefradrag) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(forventetSkattAlminneligInntekt) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(forventetTrygdeavgift) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(forventetTrinnskatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(forventetSumSkatt) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkattFaktor).isEqualTo(forventetSumSkattFaktor) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(forventetUnderholdBarnEgenHusstand) },
            { assertThat(bidragsevneResultatListe[0].sumInntekt25Prosent).isEqualTo(forventetSumInntekt25Prosent) },

            // Grunnlag
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(forventetAntallInntektRapporteringPeriodeBP) },
            { assertThat(antallDelberegningSumInntektPeriodeBP).isEqualTo(1) },
            { assertThat(antallDelberegningBoforholdPeriode).isEqualTo(1) },
            { assertThat(antallDelberegningBarnIHusstandPeriode).isEqualTo(1) },
            { assertThat(antallDelberegningVoksneIHusstandPeriode).isEqualTo(1) },
            { assertThat(antallBostatusPeriodeBP).isEqualTo(1) },
            { assertThat(antallBostatusPeriodeSB).isEqualTo(1) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },
            { assertThat(antallSjablonBidragsevne).isEqualTo(1) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(1) },

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
                    sumInntekt25Prosent = it.innhold.sumInntekt25Prosent,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseSB = request.søknadsbarnReferanse

        val antallInntektRapporteringPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningSumInntektPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBoforholdPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BOFORHOLD }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningBarnIHusstandPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningVoksneIHusstandPeriode = bidragsevneResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallBostatusPeriodeBP = bidragsevneResultat
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .filter { it.gjelderBarnReferanse == null }
            .size

        val antallBostatusPeriodeSB = bidragsevneResultat
            .filter { it.type == Grunnlagstype.BOSTATUS_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .filter { it.gjelderBarnReferanse == referanseSB }
            .size

        val antallSjablonSjablontall = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        val antallSjablonBidragsevne = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_BIDRAGSEVNE }
            .size

        val antallSjablonTrinnvisSkattesats = bidragsevneResultat
            .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
            .size

        assertAll(
            { assertThat(bidragsevneResultat).isNotNull },
            { assertThat(bidragsevneResultatListe).isNotNull },
            { assertThat(bidragsevneResultatListe).hasSize(7) },

            // Resultat
            { assertThat(bidragsevneResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-09", "2023-11")) },
            { assertThat(bidragsevneResultatListe[0].beløp).isEqualTo(BigDecimal.valueOf(19800.30).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(55300.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(23627.35).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(196440.35).setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2806290714).setScale(10)) },
            { assertThat(bidragsevneResultatListe[0].underholdBarnEgenHusstand).isEqualTo(BigDecimal.ZERO.setScale(2)) },
            { assertThat(bidragsevneResultatListe[0].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-11", "2024-01")) },
            { assertThat(bidragsevneResultatListe[1].beløp).isEqualTo(BigDecimal.valueOf(15623.30).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(55300.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(23627.35).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(196440.35).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2806290714).setScale(10)) },
            { assertThat(bidragsevneResultatListe[1].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[1].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-03")) },
            { assertThat(bidragsevneResultatListe[2].beløp).isEqualTo(BigDecimal.valueOf(15933.28).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20607.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192720.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2753151429).setScale(10)) },
            { assertThat(bidragsevneResultatListe[2].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[2].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2024-03", "2024-05")) },
            { assertThat(bidragsevneResultatListe[3].beløp).isEqualTo(BigDecimal.valueOf(21916.28).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20607.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192720.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2753151429).setScale(10)) },
            { assertThat(bidragsevneResultatListe[3].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(50124.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[3].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            { assertThat(bidragsevneResultatListe[4].beløp).isEqualTo(BigDecimal.valueOf(19827.78).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(117513.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20607.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(192720.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2753151429).setScale(10)) },
            { assertThat(bidragsevneResultatListe[4].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(75186.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[4].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            { assertThat(bidragsevneResultatListe[5].beløp).isEqualTo(BigDecimal.valueOf(18809.37).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(115610.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(54600.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(20607.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(190817.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.2725965714).setScale(10)) },
            { assertThat(bidragsevneResultatListe[5].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(76554.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[5].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(14583.33).setScale(2)) },

            { assertThat(bidragsevneResultatListe[6].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-09"), null)) },
            { assertThat(bidragsevneResultatListe[6].beløp).isEqualTo(BigDecimal.valueOf(32804.12).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.minstefradrag).isEqualTo(BigDecimal.valueOf(86250.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.skattAlminneligInntekt).isEqualTo(BigDecimal.valueOf(181610.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.trygdeavgift).isEqualTo(BigDecimal.valueOf(78000.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.trinnskatt).isEqualTo(BigDecimal.valueOf(63270.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.sumSkatt).isEqualTo(BigDecimal.valueOf(322880.60).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].skatt.sumSkattFaktor).isEqualTo(BigDecimal.valueOf(0.3228806).setScale(10)) },
            { assertThat(bidragsevneResultatListe[6].underholdBarnEgenHusstand).isEqualTo(BigDecimal.valueOf(76554.00).setScale(2)) },
            { assertThat(bidragsevneResultatListe[6].sumInntekt25Prosent).isEqualTo(BigDecimal.valueOf(20833.33).setScale(2)) },

            // Grunnlag
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(forventetAntallInntektRapporteringPeriodeBP) },
            { assertThat(antallDelberegningSumInntektPeriodeBP).isEqualTo(forventetAntallDelberegningSumInntektPeriodeBP) },
            { assertThat(antallDelberegningBoforholdPeriode).isEqualTo(forventetAntallDelberegningBoforholdPeriode) },
            { assertThat(antallDelberegningBarnIHusstandPeriode).isEqualTo(forventetAntallDelberegningBarnIHusstandPeriode) },
            { assertThat(antallDelberegningVoksneIHusstandPeriode).isEqualTo(forventetAntallDelberegningVoksneIHusstandPeriode) },
            { assertThat(antallBostatusPeriodeBP).isEqualTo(forventetAntallBostatusPeriodeBP) },
            { assertThat(antallBostatusPeriodeSB).isEqualTo(forventetAntallBostatusPeriodeSB) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },
            { assertThat(antallSjablonBidragsevne).isEqualTo(forventetAntallSjablonBidragsevne) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(forventetAntallSjablonTrinnvisSkattesats) },

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
