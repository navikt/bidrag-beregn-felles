package no.nav.bidrag.beregn.særbidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.beregning.SumLøpendeBidragBeregning
import no.nav.bidrag.beregn.særbidrag.service.BeregnSærbidragService
import no.nav.bidrag.beregn.særbidrag.testdata.SjablonApiStub
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndelSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumLøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningVoksneIHustand
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnSærbidragApiTest {
    private lateinit var filnavn: String

    private lateinit var forventetBidragsevneBeløp: BigDecimal
    private lateinit var forventetSumLøpendeBidrag: BigDecimal
    private lateinit var forventetBPAndelSærbidragFaktor: BigDecimal
    private lateinit var forventetBPAndelSærbidragBeløp: BigDecimal
    private lateinit var forventetSærbidragBeregnetBeløp: BigDecimal
    private lateinit var forventetSærbidragResultatKode: Resultatkode
    private var forventetSærbidragResultatBeløp: BigDecimal? = null
    private lateinit var forventetSumInntektBP: BigDecimal
    private lateinit var forventetSumInntektBM: BigDecimal
    private lateinit var forventetSumInntektSB: BigDecimal
    private var forventetAntallBarnIHusstand: Double = 0.0
    private var forventetVoksneIHusstand: Boolean = false

    @Mock
    private lateinit var beregnSærbidragService: BeregnSærbidragService

    // Mock SumLøpendeBidragBeregning
    @Mock
    var mockSumLøpendeBidragBeregning = Mockito.mock(SumLøpendeBidragBeregning::class.java)

    @BeforeEach
    fun initMock() {
        SjablonApiStub().settOppSjablonStub()
        beregnSærbidragService = BeregnSærbidragService()
        mockSumLøpendeBidragBeregning = SumLøpendeBidragBeregning()
    }

    // Eksempel 1-8 refererer til de opprinnelige eksemplene til John, men er modifisert til å ikke ta hensyn til løpende bidrag
    // De øvrige eksemplene er lagt til for å teste spesiell logikk

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
        // Enkel beregning med full evne, ett barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel1.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(11069)
        forventetSumLøpendeBidrag = BigDecimal.valueOf(773)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.6056)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(4239)
        forventetSumInntektBP = BigDecimal.valueOf(500000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Enkel beregning med full evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel2.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6696)
        forventetSumLøpendeBidrag = BigDecimal.ZERO
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.4967)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(2980)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(2980)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(2980)
        forventetSumInntektBP = BigDecimal.valueOf(420000)
        forventetSumInntektBM = BigDecimal.valueOf(425655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Enkel beregning med evne lavere enn summen av løpende bidrag
        // Samværsfradrag: 1048.-
        val dato = LocalDate.now()

        Mockito.`when`(
            (
                mockSumLøpendeBidragBeregning.finnAlder(
                    dato,
                    LocalDate.of(2015, 12, 17),
                )
                ),
        ).thenReturn(4)

        Mockito.`when`(
            (
                mockSumLøpendeBidragBeregning.finnAlder(
                    dato,
                    LocalDate.of(2004, 2, 1),
                )
                ),
        ).thenReturn(16)

        filnavn = "src/test/resources/testfiler/særbidrag_eksempel3.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetSumLøpendeBidrag = BigDecimal.valueOf(6150)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.5573)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.valueOf(410000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Beregning med manglende evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel4.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.5573)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.valueOf(410000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Beregning med manglende evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel5.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(9962)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.6281)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(7537)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(7537)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(7537)
        forventetSumInntektBP = BigDecimal.valueOf(550000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 1.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Enkel beregning med full evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel6.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(10891)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.5512)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(6614)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(6614)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(6614)
        forventetSumInntektBP = BigDecimal.valueOf(400000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Beregning med manglende evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel7.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.5573)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.valueOf(410000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Beregning med manglende evne, to barn (tilpasset opprinnelig eksempel med 2 løpende bidrag)
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel8.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(6149)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.5573)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(6688)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.valueOf(410000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - barn regnes som voksen")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel_BarnRegnesSomVoksen() {
        // Beregning med full evne, hvor barn regnes som voksen
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_barn_regnes_som_voksen.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(19091)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.6281)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(7537)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(7537)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(7537)
        forventetSumInntektBP = BigDecimal.valueOf(550000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - BP's inntekt mangler")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel_BPsInntektMangler() {
        // Beregning hvor BP's inntekt mangler
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_BPs_inntekt_mangler.json"
        forventetBidragsevneBeløp = BigDecimal.ZERO
        forventetBPAndelSærbidragFaktor = BigDecimal("0.0000")
        forventetBPAndelSærbidragBeløp = BigDecimal.ZERO
        forventetSærbidragBeregnetBeløp = BigDecimal.ZERO
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_IKKE_FULL_BIDRAGSEVNE
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.ZERO
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - deler av utgift er betalt av BP")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel_DelerAvUtgiftErBetaltAvBP() {
        // Enkel beregning med full evne, hvor deler av utgift er betalt av BP
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_deler_av_utgift_betalt_av_BP.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(11069)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.6056)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(4239)
        forventetSumInntektBP = BigDecimal.valueOf(500000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - BP har betalt mer enn godkjent beløp")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel_BPHarBetaltMerEnnGodkjentBeløp() {
        // Enkel beregning med full evne, hvor BP har betalt for mye ifht godkjent beløp
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_BP_har_betalt_mer_enn_godkjent_beløp.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(11069)
        forventetBPAndelSærbidragFaktor = BigDecimal.valueOf(0.6056)
        forventetBPAndelSærbidragBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragBeregnetBeløp = BigDecimal.valueOf(4239)
        forventetSærbidragResultatKode = Resultatkode.SÆRBIDRAG_INNVILGET
        forventetSærbidragResultatBeløp = BigDecimal.valueOf(4239)
        forventetSumInntektBP = BigDecimal.valueOf(500000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.ZERO
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - barnet er selvforsørget")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel_BarnetErSelvforsørget() {
        // Enkel beregning med full evne, hvor barnet er selvforsørget
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_barnet_er_selvforsørget.json"
        forventetBidragsevneBeløp = BigDecimal.valueOf(11069)
        forventetBPAndelSærbidragFaktor = BigDecimal.ZERO
        forventetBPAndelSærbidragBeløp = BigDecimal.ZERO
        forventetSærbidragBeregnetBeløp = BigDecimal.ZERO
        forventetSærbidragResultatKode = Resultatkode.BARNET_ER_SELVFORSØRGET
        forventetSærbidragResultatBeløp = null
        forventetSumInntektBP = BigDecimal.valueOf(500000)
        forventetSumInntektBM = BigDecimal.valueOf(325655)
        forventetSumInntektSB = BigDecimal.valueOf(200000)
        forventetAntallBarnIHusstand = 0.0
        forventetVoksneIHusstand = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - beløpet er mindre enn forskuddssats")
    fun skalReturnereResultatBeløpetErUnderForskuddssatsFastsettelse() {
        // Skal returnere uten å beregne pga for lavt godkjent beløp
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_beløp_under_forskuddssats.json"
        val request = lesFilOgByggRequest(filnavn)
        val totalSærbidragResultat = beregnSærbidragService.beregn(request, Vedtakstype.FASTSETTELSE)
        val beregnetSærbidragPeriodeListe = totalSærbidragResultat.beregnetSærbidragPeriodeListe
        val grunnlagliste = totalSærbidragResultat.grunnlagListe

        TestUtil.printJson(totalSærbidragResultat)

        assertAll(
            { assertThat(totalSærbidragResultat).isNotNull },

            // Resultat
            { assertThat(beregnetSærbidragPeriodeListe).hasSize(1) },
            { assertThat(beregnetSærbidragPeriodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2020-08")) },
            { assertThat(beregnetSærbidragPeriodeListe[0].periode.til).isEqualTo(YearMonth.parse("2020-09")) },
            { assertThat(beregnetSærbidragPeriodeListe[0].resultat.beløp).isNull() },
            { assertThat(beregnetSærbidragPeriodeListe[0].resultat.resultatkode).isEqualTo(Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS) },
            { assertThat(beregnetSærbidragPeriodeListe[0].grunnlagsreferanseListe).hasSize(1) },
            { assertThat(grunnlagliste).hasSize(3) },

        )
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - sjekk på om beløpet er mindre enn forskuddssats skal ikke gjøres for Endring")
    fun skalIkkeReturnereResultatBeløpetErUnderForskuddssatsEndring() {
        // Skal returnere uten å beregne pga for lavt godkjent beløp
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_beløp_under_forskuddssats.json"
        val request = lesFilOgByggRequest(filnavn)
        val totalSærbidragResultat = beregnSærbidragService.beregn(request, Vedtakstype.ENDRING)
        val beregnetSærbidragPeriodeListe = totalSærbidragResultat.beregnetSærbidragPeriodeListe
        val grunnlagliste = totalSærbidragResultat.grunnlagListe

        TestUtil.printJson(totalSærbidragResultat)

        assertAll(
            { assertThat(totalSærbidragResultat).isNotNull },

            // Resultat
            { assertThat(beregnetSærbidragPeriodeListe).hasSize(1) },
            {
                assertThat(
                    beregnetSærbidragPeriodeListe[0].resultat.resultatkode,
                ).isNotEqualTo(Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS)
            },

        )
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - _eksempel_inntekt_skjønn_mangler_dokumentasjon")
    fun skalKalleCoreOgReturnereEtResultat_særbidrag_eksempel_inntekt_skjønn_mangler_dokumentasjon() {
        // Enkel beregning med full evne, ett barn
        filnavn = "src/test/resources/testfiler/særbidrag_eksempel_inntekt_skjønn_mangler_dokumentasjon.json"
        val request = lesFilOgByggRequest(filnavn)
        val totalSærbidragResultat = beregnSærbidragService.beregn(request, Vedtakstype.FASTSETTELSE)

        TestUtil.printJson(totalSærbidragResultat)

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val objectMapper = ObjectMapper()

        val delberegningSumInntektBPListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
        val delberegningSumInntektBPResultat = objectMapper.treeToValue(delberegningSumInntektBPListe[0].innhold, DelberegningSumInntekt::class.java)

        assertAll(
            { assertThat(totalSærbidragResultat).isNotNull },
            { assertThat(delberegningSumInntektBPResultat.skattepliktigInntekt).isEqualTo(BigDecimal.valueOf(50000)) },
        )
    }

    private fun utførBeregningerOgEvaluerResultat() {
        val request = lesFilOgByggRequest(filnavn)

        val totalSærbidragResultat = beregnSærbidragService.beregn(request, Vedtakstype.FASTSETTELSE)

        TestUtil.printJson(totalSærbidragResultat)

        val objectMapper = ObjectMapper()
        val alleReferanser = TestUtil.hentAlleReferanser(totalSærbidragResultat)
        val referanserIGrunnlagListe = totalSærbidragResultat.grunnlagListe.map(GrunnlagDto::referanse)

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val referanseSB = request.søknadsbarnReferanse

        val delberegningBidragsevneListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSEVNE }
        val bidragsevneResultat = objectMapper.treeToValue(delberegningBidragsevneListe[0].innhold, DelberegningBidragsevne::class.java)

        val sumLøpendeBidragResultat = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_LØPENDE_BIDRAG }
            .map { objectMapper.treeToValue(it.innhold, DelberegningSumLøpendeBidrag::class.java) }
            .first()

        val delberegningBPAndelSærbidragListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRBIDRAG }
        val bPAndelSærbidragResultat =
            objectMapper.treeToValue(delberegningBPAndelSærbidragListe[0].innhold, DelberegningBidragspliktigesAndelSærbidrag::class.java)

        val sluttberegningSærbidragListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG }
        val sluttberegningSærbidragResultat = objectMapper.treeToValue(sluttberegningSærbidragListe[0].innhold, SluttberegningSærbidrag::class.java)

        val delberegningSumInntektBPListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
        val delberegningSumInntektBPResultat = objectMapper.treeToValue(delberegningSumInntektBPListe[0].innhold, DelberegningSumInntekt::class.java)

        val delberegningSumInntektBMListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBM }
        val delberegningSumInntektBMResultat = objectMapper.treeToValue(delberegningSumInntektBMListe[0].innhold, DelberegningSumInntekt::class.java)

        val delberegningSumInntektSBListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseSB }
        val delberegningSumInntektSBResultat = objectMapper.treeToValue(delberegningSumInntektSBListe[0].innhold, DelberegningSumInntekt::class.java)

        val delberegningBarnIHusstandListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND }
        val delberegningBarnIHusstandResultat =
            objectMapper.treeToValue(delberegningBarnIHusstandListe[0].innhold, DelberegningBarnIHusstand::class.java)

        val delberegningVoksneIHusstandListe = totalSærbidragResultat.grunnlagListe
            .filter { it.type == Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND }
        val delberegningVoksneIHusstandResultat =
            objectMapper.treeToValue(delberegningVoksneIHusstandListe[0].innhold, DelberegningVoksneIHustand::class.java)

        assertAll(
            { assertThat(totalSærbidragResultat).isNotNull },

            // Resultat
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe).hasSize(1) },
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe).isNotNull },
            { assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.beløp).isEqualTo(forventetSærbidragResultatBeløp) },
            {
                assertThat(totalSærbidragResultat.beregnetSærbidragPeriodeListe[0].resultat.resultatkode).isEqualTo(
                    forventetSærbidragResultatKode,
                )
            },

            // Sluttberegning
            { assertThat(sluttberegningSærbidragListe).hasSize(1) },
            { assertThat(sluttberegningSærbidragResultat.beregnetBeløp).isEqualTo(forventetSærbidragBeregnetBeløp) },
            { assertThat(sluttberegningSærbidragResultat.resultatKode).isEqualTo(forventetSærbidragResultatKode) },
            { assertThat(sluttberegningSærbidragResultat.resultatBeløp).isEqualTo(forventetSærbidragResultatBeløp) },

            // Delberegning Bidragsevne
            { assertThat(delberegningBidragsevneListe).hasSize(1) },
            { assertThat(bidragsevneResultat.beløp).isEqualTo(forventetBidragsevneBeløp) },

            // Delberegning sumLøpendeBidrag
            { assertThat(sumLøpendeBidragResultat.sum).isEqualTo(forventetSumLøpendeBidrag) },

            // Delberegning BP's andel særbidrag
            { assertThat(delberegningBPAndelSærbidragListe).hasSize(1) },
            { assertThat(bPAndelSærbidragResultat.andelBeløp).isEqualTo(forventetBPAndelSærbidragBeløp) },
            { assertThat(bPAndelSærbidragResultat.andelFaktor).isEqualTo(forventetBPAndelSærbidragFaktor) },

            // Delberegning Sum inntekt BP
            { assertThat(delberegningSumInntektBPListe).hasSize(1) },
            { assertThat(delberegningSumInntektBPResultat.totalinntekt).isEqualTo(forventetSumInntektBP) },

            // Delberegning Sum inntekt BM
            { assertThat(delberegningSumInntektBMListe).hasSize(1) },
            { assertThat(delberegningSumInntektBMResultat.totalinntekt).isEqualTo(forventetSumInntektBM) },

            // Delberegning Sum inntekt SB
            { assertThat(delberegningSumInntektSBListe).hasSize(1) },
            { assertThat(delberegningSumInntektSBResultat.totalinntekt).isEqualTo(forventetSumInntektSB) },

            // Delberegning Barn i husstand
            { assertThat(delberegningBarnIHusstandListe).hasSize(1) },
            { assertThat(delberegningBarnIHusstandResultat.antallBarn).isEqualTo(forventetAntallBarnIHusstand) },

            // Delberegning Voksne i husstand
            { assertThat(delberegningVoksneIHusstandListe).hasSize(1) },
            { assertThat(delberegningVoksneIHusstandResultat.borMedAndreVoksne).isEqualTo(forventetVoksneIHusstand) },

            // Referanser
            { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

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
}
