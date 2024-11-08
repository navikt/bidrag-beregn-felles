package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
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
internal class BeregnBpAndelUnderholdskostnadApiTest {
    private lateinit var filnavn: String
    private lateinit var forventetEndeligAndelFaktor: BigDecimal
    private lateinit var forventetAndelBeløp: BigDecimal
    private lateinit var forventetBeregnetAndelFaktor: BigDecimal
    private lateinit var forventetBarnEndeligInntekt: BigDecimal
    private var forventetBarnetErSelvforsørget: Boolean = false
    private var forventetAntallInntektRapporteringPeriodeBP: Int = 1
    private var forventetAntallInntektRapporteringPeriodeBM: Int = 1
    private var forventetAntallInntektRapporteringPeriodeSB: Int = 1
    private var forventetAntallDelberegningSumInntektPeriodeBP: Int = 1
    private var forventetAntallDelberegningSumInntektPeriodeBM: Int = 1
    private var forventetAntallDelberegningSumInntektPeriodeSB: Int = 1
    private var forventetAntallUnderholdskostnad: Int = 1
    private var forventetAntallSjablon: Int = 6

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 1 - Barnet er selvforsørget")
    fun testBpAndelUnderholdskostnad_Eksempel01() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel1.json"
        forventetEndeligAndelFaktor = BigDecimal.ZERO.setScale(10)
        forventetAndelBeløp = BigDecimal.ZERO.setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.ZERO.setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(200000.00).setScale(2)
        forventetBarnetErSelvforsørget = true
        forventetAntallInntektRapporteringPeriodeBP = 1
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 1
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 2 - BPs andel er høyere enn fem sjettedeler")
    fun testBpAndelUnderholdskostnad_Eksempel02() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel2.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.8333333333).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(7500.00).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.8765010080).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900.00).setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetAntallInntektRapporteringPeriodeBP = 1
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 1
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 3 - BPs andel er lavere enn fem sjettedeler")
    fun testBpAndelUnderholdskostnad_Eksempel03() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel3.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(7021.38).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.7801529100).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.valueOf(40900.00).setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetAntallInntektRapporteringPeriodeBP = 1
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 1
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 4 - Barnets endelige inntekt er lavere enn 0")
    fun testBpAndelUnderholdskostnad_Eksempel04() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel4.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(5625.00).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetAntallInntektRapporteringPeriodeBP = 1
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 1
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 5 - Barnet har ikke inntekt")
    fun testBpAndelUnderholdskostnad_Eksempel05() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel5.json"
        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetAndelBeløp = BigDecimal.valueOf(5625.00).setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.valueOf(0.6250000000).setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetAntallInntektRapporteringPeriodeBP = 1
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 0
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 6 - BP har ikke inntekt")
    fun testBpAndelUnderholdskostnad_Eksempel06() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel6.json"
        forventetEndeligAndelFaktor = BigDecimal.ZERO.setScale(10)
        forventetAndelBeløp = BigDecimal.ZERO.setScale(2)
        forventetBeregnetAndelFaktor = BigDecimal.ZERO.setScale(10)
        forventetBarnEndeligInntekt = BigDecimal.ZERO.setScale(2)
        forventetBarnetErSelvforsørget = false
        forventetAntallInntektRapporteringPeriodeBP = 0
        forventetAntallInntektRapporteringPeriodeBM = 1
        forventetAntallInntektRapporteringPeriodeSB = 1
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad()
    }

    @Test
    @DisplayName("BP Andel underholdskostnad - eksempel 7 - underholdskostnad mangler")
    fun testBpAndelUnderholdskostnad_Eksempel07() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel7.json"
        val request = lesFilOgByggRequest(filnavn)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            beregnBarnebidragService.beregnBpAndelUnderholdskostnad(request)
        }
        assertThat(exception.message).contains("Underholdskostnad grunnlag mangler")
    }

    @Test
    @DisplayName("BP andel underholdskostnad - eksempel med flere perioder")
    fun testBpAndelUnderholdskostnad_Eksempel_Flere_Perioder() {
        filnavn = "src/test/resources/testfiler/bpandelunderholdskostnad/bpandel_eksempel_flere_perioder.json"
        forventetAntallInntektRapporteringPeriodeBP = 3
        forventetAntallInntektRapporteringPeriodeBM = 2
        forventetAntallInntektRapporteringPeriodeSB = 3
        forventetAntallDelberegningSumInntektPeriodeBP = 3
        forventetAntallDelberegningSumInntektPeriodeBM = 2
        forventetAntallDelberegningSumInntektPeriodeSB = 4
        forventetAntallUnderholdskostnad = 2
        forventetAntallSjablon = 7
        utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnadFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnad() {
        val request = lesFilOgByggRequest(filnavn)
        val bpAndelUnderholdskostnadResultat = beregnBarnebidragService.beregnBpAndelUnderholdskostnad(request)
        printJson(bpAndelUnderholdskostnadResultat)

        val alleReferanser = hentAlleReferanser(bpAndelUnderholdskostnadResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bpAndelUnderholdskostnadResultat)

        val bpAndelUnderholdskostnadResultatListe = bpAndelUnderholdskostnadResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                DelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                    andelBeløp = it.innhold.andelBeløp,
                    beregnetAndelFaktor = it.innhold.beregnetAndelFaktor,
                    barnEndeligInntekt = it.innhold.barnEndeligInntekt,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                )
            }

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val referanseSB = request.søknadsbarnReferanse

        val antallInntektRapporteringPeriodeBP = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallInntektRapporteringPeriodeBM = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallInntektRapporteringPeriodeSB = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallDelberegningSumInntektPeriodeBP = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningSumInntektPeriodeBM = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallDelberegningSumInntektPeriodeSB = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallUnderholdskostnad = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallSjablonSjablonTall = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        assertAll(
            { assertThat(bpAndelUnderholdskostnadResultat).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(1) },

            // Resultat
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2024-08", "2024-09")) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor).isEqualTo(forventetEndeligAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp).isEqualTo(forventetAndelBeløp) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor).isEqualTo(forventetBeregnetAndelFaktor) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt).isEqualTo(forventetBarnEndeligInntekt) },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isEqualTo(forventetBarnetErSelvforsørget) },

            // Grunnlag
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(forventetAntallInntektRapporteringPeriodeBP) },
            { assertThat(antallInntektRapporteringPeriodeBM).isEqualTo(forventetAntallInntektRapporteringPeriodeBM) },
            { assertThat(antallInntektRapporteringPeriodeSB).isEqualTo(forventetAntallInntektRapporteringPeriodeSB) },
            { assertThat(antallDelberegningSumInntektPeriodeBP).isEqualTo(1) },
            { assertThat(antallDelberegningSumInntektPeriodeBM).isEqualTo(1) },
            { assertThat(antallDelberegningSumInntektPeriodeSB).isEqualTo(1) },
            { assertThat(antallUnderholdskostnad).isEqualTo(1) },
            { assertThat(antallSjablonSjablonTall).isEqualTo(6) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatBpAndelUnderholdskostnadFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val bpAndelUnderholdskostnadResultat = beregnBarnebidragService.beregnBpAndelUnderholdskostnad(request)
        printJson(bpAndelUnderholdskostnadResultat)

        val alleReferanser = hentAlleReferanser(bpAndelUnderholdskostnadResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(bpAndelUnderholdskostnadResultat)

        val referanseBP = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .first()

        val referanseBM = request.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
            .map { it.referanse }
            .first()

        val referanseSB = request.søknadsbarnReferanse

        val antallInntektRapporteringPeriodeBP = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallInntektRapporteringPeriodeBM = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallInntektRapporteringPeriodeSB = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallDelberegningSumInntektPeriodeBP = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBP }
            .size

        val antallDelberegningSumInntektPeriodeBM = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseBM }
            .size

        val antallDelberegningSumInntektPeriodeSB = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseSB }
            .size

        val antallUnderholdskostnad = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD }
            .size

        val antallSjablonSjablontall = bpAndelUnderholdskostnadResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        val bpAndelUnderholdskostnadResultatListe = bpAndelUnderholdskostnadResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                DelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                    andelBeløp = it.innhold.andelBeløp,
                    beregnetAndelFaktor = it.innhold.beregnetAndelFaktor,
                    barnEndeligInntekt = it.innhold.barnEndeligInntekt,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                )
            }

        assertAll(
            { assertThat(bpAndelUnderholdskostnadResultat).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).isNotNull },
            { assertThat(bpAndelUnderholdskostnadResultatListe).hasSize(6) },

            // Resultat
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode("2023-09", "2023-11")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[0].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.ZERO.setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[0].andelBeløp)
                    .isEqualTo(BigDecimal.ZERO.setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[0].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.ZERO.setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[0].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.valueOf(200000.00).setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[0].barnetErSelvforsørget).isTrue() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[1].periode).isEqualTo(ÅrMånedsperiode("2023-11", "2024-01")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[1].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[1].andelBeløp)
                    .isEqualTo(BigDecimal.valueOf(7500.00).setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[1].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8744316194).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[1].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.valueOf(43600.00).setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[1].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[2].periode).isEqualTo(ÅrMånedsperiode("2024-01", "2024-05")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[2].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[2].andelBeløp)
                    .isEqualTo(BigDecimal.valueOf(7500.00).setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[2].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[2].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.ZERO.setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[2].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[3].periode).isEqualTo(ÅrMånedsperiode("2024-05", "2024-07")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[3].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[3].andelBeløp)
                    .isEqualTo(BigDecimal.valueOf(6666.67).setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[3].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[3].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.ZERO.setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[3].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[4].periode).isEqualTo(ÅrMånedsperiode("2024-07", "2024-09")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[4].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[4].andelBeløp)
                    .isEqualTo(BigDecimal.valueOf(6666.67).setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[4].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.8333333333).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[4].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.ZERO.setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[4].barnetErSelvforsørget).isFalse() },

            { assertThat(bpAndelUnderholdskostnadResultatListe[5].periode).isEqualTo(ÅrMånedsperiode("2024-09", "2024-10")) },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[5].endeligAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.5866823115).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[5].andelBeløp)
                    .isEqualTo(BigDecimal.valueOf(4693.46).setScale(2))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[5].beregnetAndelFaktor)
                    .isEqualTo(BigDecimal.valueOf(0.5866823115).setScale(10))
            },
            {
                assertThat(bpAndelUnderholdskostnadResultatListe[5].barnEndeligInntekt)
                    .isEqualTo(BigDecimal.valueOf(40900.00).setScale(2))
            },
            { assertThat(bpAndelUnderholdskostnadResultatListe[5].barnetErSelvforsørget).isFalse() },

            // Grunnlag
            { assertThat(antallInntektRapporteringPeriodeBP).isEqualTo(forventetAntallInntektRapporteringPeriodeBP) },
            { assertThat(antallInntektRapporteringPeriodeBM).isEqualTo(forventetAntallInntektRapporteringPeriodeBM) },
            { assertThat(antallInntektRapporteringPeriodeSB).isEqualTo(forventetAntallInntektRapporteringPeriodeSB) },
            { assertThat(antallDelberegningSumInntektPeriodeBP).isEqualTo(forventetAntallDelberegningSumInntektPeriodeBP) },
            { assertThat(antallDelberegningSumInntektPeriodeBM).isEqualTo(forventetAntallDelberegningSumInntektPeriodeBM) },
            { assertThat(antallDelberegningSumInntektPeriodeSB).isEqualTo(forventetAntallDelberegningSumInntektPeriodeSB) },
            { assertThat(antallUnderholdskostnad).isEqualTo(forventetAntallUnderholdskostnad) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablon) },

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
