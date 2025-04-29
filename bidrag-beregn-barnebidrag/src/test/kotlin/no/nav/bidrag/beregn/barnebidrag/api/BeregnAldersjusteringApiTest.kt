package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.core.exception.AldersjusteringLavereEnnEllerLikLøpendeBidragException
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiBarnetilsynMedStønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiDelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiDelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiSamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonForbruksutgifterPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidragAldersjustering
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
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnAldersjusteringApiTest : FellesApiTest() {
    private lateinit var filnavn: String
    private val beregningsperiode = ÅrMånedsperiode(YearMonth.parse("2024-07"), YearMonth.parse("2024-08"))

    // Vedtak
    private var forventetVedtakId: Long = 123456L

    // Sluttberegning (endelig bidrag)
    private lateinit var forventetResultatbeløp: BigDecimal
    private lateinit var forventetBeregnetBeløp: BigDecimal
    private lateinit var forventetBpAndelBeløp: BigDecimal
    private var forventetBpAndelFaktorVedDeltBosted: BigDecimal? = null
    private var forventetDeltBosted: Boolean = false

    // Delberegning underholdskostnad
    private lateinit var forventetForbruksutgift: BigDecimal
    private val forventetBoutgift: BigDecimal = BigDecimal(3596).setScale(2)
    private val forventetBarnetilsynMedStønad: BigDecimal? = null
    private var forventetNettoTilsynsutgift: BigDecimal = BigDecimal(1000).setScale(2)
    private val forventetBarnetrygd: BigDecimal = BigDecimal(1510).setScale(2)
    private lateinit var forventetUnderholdskostnad: BigDecimal
    private var forventetAntallBarnetilsynMedStønad = 0

    // Delberegning samværsfradrag
    private lateinit var forventetSamværsfradragBeløp: BigDecimal

    // Kopi delberegning bidragspliktiges andel
    private var forventetEndeligAndelFaktor: BigDecimal = BigDecimal(0.6758471615).avrundetMedTiDesimaler

    // Kopi samværsperiode
    private var forventetSamværsklasse: Samværsklasse = Samværsklasse.SAMVÆRSKLASSE_2

    // Sjablon sjablontall
    private val forventetSjablonverdiBoutgifter: BigDecimal = BigDecimal(3596).setScale(0)
    private val forventetSjablonverdiOrdinærBarnetrygd: BigDecimal = BigDecimal(1510).setScale(0)

    // Sjablon forbruksutgifter
    private var forventetSjablonverdiAlderTom: Int = 0
    private lateinit var forventetSjablonverdiBeløpForbruk: BigDecimal

    // Sjablon samværsfradrag
    private var forventetAntallSjablonerSamværsfradrag: Int = 1
    private var forventetSjablonverdiSamværsklasse: String = "02"
    private lateinit var forventetSjablonverdiBeløpSamværsfradrag: BigDecimal

    // Exception
    private var forventetExceptionAldersjusteringErLavereEnnLøpendeBidrag = false
    private var forventetFeilmelding = ""

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 1 - barnet blir 6 år")
    fun testAldersjustering_Eksempel01() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel1.json"

        forventetResultatbeløp = BigDecimal.valueOf(4930).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(4933.95).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(6400.95).setScale(2)

        forventetForbruksutgift = BigDecimal.valueOf(6385).setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(9471).setScale(2)

        forventetSamværsfradragBeløp = BigDecimal.valueOf(1467).setScale(2)

        forventetSjablonverdiAlderTom = 10
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(6385).setScale(0)

        forventetSjablonverdiBeløpSamværsfradrag = BigDecimal.valueOf(1467).setScale(0)

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 2 - barnet blir 11 år")
    fun testAldersjustering_Eksempel02() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel2.json"

        forventetResultatbeløp = BigDecimal.valueOf(5400).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(5400.32).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(7213.32).setScale(2)

        forventetForbruksutgift = BigDecimal.valueOf(7587).setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(10673).setScale(2)

        forventetSamværsfradragBeløp = BigDecimal.valueOf(1813).setScale(2)

        forventetSjablonverdiAlderTom = 14
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(7587).setScale(0)

        forventetSjablonverdiBeløpSamværsfradrag = BigDecimal.valueOf(1813).setScale(0)

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 3A - barnet blir 15 år")
    fun testAldersjustering_Eksempel03A() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel3A.json"

        forventetResultatbeløp = BigDecimal.valueOf(5900).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(5897.13).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(7960.13).setScale(2)

        forventetForbruksutgift = BigDecimal.valueOf(8692).setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(11778).setScale(2)

        forventetSamværsfradragBeløp = BigDecimal.valueOf(2063).setScale(2)

        forventetSjablonverdiAlderTom = 18
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(8692).setScale(0)

        forventetSjablonverdiBeløpSamværsfradrag = BigDecimal.valueOf(2063).setScale(0)

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 3B - barnet blir 15 år - delt bosted")
    fun testAldersjustering_Eksempel03B() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel3B.json"

        forventetResultatbeløp = BigDecimal.valueOf(2070).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(2071.13).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(2071.13).setScale(2)
        forventetBpAndelFaktorVedDeltBosted = BigDecimal.valueOf(0.1758471615).setScale(10)
        forventetDeltBosted = true

        forventetForbruksutgift = BigDecimal.valueOf(8692).setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(11778).setScale(2)

        forventetSamværsklasse = Samværsklasse.DELT_BOSTED
        forventetSamværsfradragBeløp = BigDecimal.ZERO.setScale(2)

        forventetSjablonverdiAlderTom = 18
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(8692).setScale(0)

        forventetAntallSjablonerSamværsfradrag = 0

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 4 - ingen vedtak funnet for søknadsbarn - skal kaste exception")
    fun testAldersjustering_Eksempel04() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel4.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Ingen vedtak funnet for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 5 - flere vedtak funnet for søknadsbarn - skal kaste exception")
    fun testAldersjustering_Eksempel05() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel5.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Flere vedtak funnet for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 6 - ingen stønadsendringer av type BIDRAG funnet for søknadsbarn - skal kaste exception")
    fun testAldersjustering_Eksempel06() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel6.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo("Aldersjustering: Ingen stønadsendringer av type BIDRAG funnet for vedtak med id 123456")
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 7 - stønadsendringen inneholder ingen perioder som matcher grunnlagsperioden - skal kaste exception")
    fun testAldersjustering_Eksempel07() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel7.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Stønadsendring av type BIDRAG inneholder ingen perioder som inneholder grunnlagsperiode for vedtak med id 123456",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 8 - stønadsendringen inneholder ikke sluttperiode - skal kaste exception")
    fun testAldersjustering_Eksempel08() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel8.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Sluttberegning ikke funnet for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138 " +
                "og vedtak med id 123456",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 9 - stønadsendringen inneholder ikke delberegning underholdskostnad - skal kaste exception")
    fun testAldersjustering_Eksempel09() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel9.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Delberegning underholdskostnad ikke funnet for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138 " +
                "og vedtak med id 123456",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 10 - stønadsendringen inneholder ikke delberegning bidragspliktiges andel - skal kaste exception")
    fun testAldersjustering_Eksempel10() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel10.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Delberegning bidragspliktiges andel ikke funnet for " +
                "søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138 og vedtak med id 123456",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 11 - stønadsendringen inneholder ikke samværsperiode - skal kaste exception")
    fun testAldersjustering_Eksempel11() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel11.json"

        val exception = assertThrows(UgyldigInputException::class.java) {
            utførBeregningerOgEvaluerResultatAldersjustering()
        }
        assertThat(exception.message).isEqualTo(
            "Aldersjustering: Samværsperiode ikke funnet for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138 " +
                "og vedtak med id 123456",
        )
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 12 - aldersjustert beløp er lavere enn løpende beløp - skal kaste exception")
    fun testAldersjustering_Eksempel12() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel12.json"

        forventetResultatbeløp = BigDecimal.valueOf(4930).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(4933.95).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(6400.95).setScale(2)

        forventetForbruksutgift = BigDecimal.valueOf(6385).setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(9471).setScale(2)

        forventetSamværsfradragBeløp = BigDecimal.valueOf(1467).setScale(2)

        forventetSjablonverdiAlderTom = 10
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(6385).setScale(0)

        forventetSjablonverdiBeløpSamværsfradrag = BigDecimal.valueOf(1467).setScale(0)

        forventetExceptionAldersjusteringErLavereEnnLøpendeBidrag = true
        forventetFeilmelding =
            "Alderjustert beløp er lavere enn løpende beløp fra beløpshistorikken for søknadsbarn med referanse person_PERSON_SØKNADSBARN_20230901_47138"

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    @Test
    @DisplayName("Aldersjustering - eksempel 13 - stønadsendringer for andre barn enn søknadsbarnet skal ignoreres")
    fun testAldersjustering_Eksempel13() {
        filnavn = "src/test/resources/testfiler/aldersjustering/aldersjustering_eksempel13.json"

        forventetVedtakId = 1009748L

        forventetResultatbeløp = BigDecimal.valueOf(5490).setScale(0)
        forventetBeregnetBeløp = BigDecimal.valueOf(5488.13).setScale(2)
        forventetBpAndelBeløp = BigDecimal.valueOf(6111.13).setScale(2)

        forventetForbruksutgift = BigDecimal.valueOf(8692).setScale(2)
        forventetNettoTilsynsutgift = BigDecimal.ZERO.setScale(2)
        forventetUnderholdskostnad = BigDecimal.valueOf(10778).setScale(2)
        forventetAntallBarnetilsynMedStønad = 1

        forventetSamværsfradragBeløp = BigDecimal.valueOf(623).setScale(2)

        forventetEndeligAndelFaktor = BigDecimal.valueOf(0.567).avrundetMedTiDesimaler

        forventetSamværsklasse = Samværsklasse.SAMVÆRSKLASSE_1

        forventetSjablonverdiAlderTom = 18
        forventetSjablonverdiBeløpForbruk = BigDecimal.valueOf(8692).setScale(0)

        forventetSjablonverdiBeløpSamværsfradrag = BigDecimal.valueOf(623).setScale(0)
        forventetSjablonverdiSamværsklasse = "01"

        utførBeregningerOgEvaluerResultatAldersjustering()
    }

    private fun utførBeregningerOgEvaluerResultatAldersjustering() {
        val request = lesFilOgByggRequestAldersjustering(filnavn)

        val exception: RuntimeException
        var feilmelding = ""
        val aldersjusteringResultat: BeregnetBarnebidragResultat

        if (forventetExceptionAldersjusteringErLavereEnnLøpendeBidrag == true) {
            exception = assertThrows(AldersjusteringLavereEnnEllerLikLøpendeBidragException::class.java) {
                api.beregnAldersjustering(request)
            }
            aldersjusteringResultat = exception.data
            feilmelding = exception.message!!
        } else {
            aldersjusteringResultat = api.beregnAldersjustering(request)
        }

        printJson(aldersjusteringResultat)

        val aldersjusteringResultatGrunnlagListe = aldersjusteringResultat.grunnlagListe
        val alleReferanser = hentAlleReferanser(aldersjusteringResultatGrunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(aldersjusteringResultatGrunnlagListe)

        val endeligBidragResultatListe = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidragAldersjustering>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG_ALDERSJUSTERING)
            .map {
                SluttberegningBarnebidragAldersjustering(
                    periode = it.innhold.periode,
                    beregnetBeløp = it.innhold.beregnetBeløp,
                    resultatBeløp = it.innhold.resultatBeløp,
                    bpAndelBeløp = it.innhold.bpAndelBeløp,
                    bpAndelFaktorVedDeltBosted = it.innhold.bpAndelFaktorVedDeltBosted,
                    deltBosted = it.innhold.deltBosted,
                )
            }

        val underholdskostnadResultatListe = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
            .map {
                DelberegningUnderholdskostnad(
                    periode = it.innhold.periode,
                    forbruksutgift = it.innhold.forbruksutgift,
                    boutgift = it.innhold.boutgift,
                    barnetilsynMedStønad = it.innhold.barnetilsynMedStønad,
                    nettoTilsynsutgift = it.innhold.nettoTilsynsutgift,
                    barnetrygd = it.innhold.barnetrygd,
                    underholdskostnad = it.innhold.underholdskostnad,
                )
            }

        val samværsfradragResultatListe = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
            .map {
                DelberegningSamværsfradrag(
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }

        val kopiDelberegningUnderholdskostnad = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<KopiDelberegningUnderholdskostnad>(Grunnlagstype.KOPI_DELBEREGNING_UNDERHOLDSKOSTNAD)
            .map {
                KopiDelberegningUnderholdskostnad(
                    periode = it.innhold.periode,
                    fraVedtakId = it.innhold.fraVedtakId,
                    nettoTilsynsutgift = it.innhold.nettoTilsynsutgift,
                )
            }

        val kopiDelberegningBidragspliktigesAndel = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<KopiDelberegningBidragspliktigesAndel>(Grunnlagstype.KOPI_DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
            .map {
                KopiDelberegningBidragspliktigesAndel(
                    periode = it.innhold.periode,
                    fraVedtakId = it.innhold.fraVedtakId,
                    endeligAndelFaktor = it.innhold.endeligAndelFaktor,
                )
            }

        val kopiSamværsperiode = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<KopiSamværsperiodeGrunnlag>(Grunnlagstype.KOPI_SAMVÆRSPERIODE)
            .map {
                KopiSamværsperiodeGrunnlag(
                    periode = it.innhold.periode,
                    fraVedtakId = it.innhold.fraVedtakId,
                    samværsklasse = it.innhold.samværsklasse,
                )
            }

        val kopiBarnetilsynMedStønadPeriode = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<KopiBarnetilsynMedStønadPeriode>(Grunnlagstype.KOPI_BARNETILSYN_MED_STØNAD_PERIODE)
            .map {
                KopiBarnetilsynMedStønadPeriode(
                    periode = it.innhold.periode,
                    fraVedtakId = it.innhold.fraVedtakId,
                    tilsynstype = it.innhold.tilsynstype,
                    skolealder = it.innhold.skolealder,
                    manueltRegistrert = it.innhold.manueltRegistrert
                )
            }

        val sjablonSjablontallListe = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>(Grunnlagstype.SJABLON_SJABLONTALL)
            .map {
                SjablonSjablontallPeriode(
                    periode = it.innhold.periode,
                    sjablon = it.innhold.sjablon,
                    verdi = it.innhold.verdi,
                )
            }.sortedBy { it.sjablon.navn }

        val sjablonForbruksutgifter = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<SjablonForbruksutgifterPeriode>(Grunnlagstype.SJABLON_FORBRUKSUTGIFTER)
            .map {
                SjablonForbruksutgifterPeriode(
                    periode = it.innhold.periode,
                    alderTom = it.innhold.alderTom,
                    beløpForbruk = it.innhold.beløpForbruk,
                )
            }

        val sjablonSamværsfradrag = aldersjusteringResultatGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<SjablonSamværsfradragPeriode>(Grunnlagstype.SJABLON_SAMVARSFRADRAG)
            .map {
                SjablonSamværsfradragPeriode(
                    periode = it.innhold.periode,
                    samværsklasse = it.innhold.samværsklasse,
                    alderTom = it.innhold.alderTom,
                    antallDagerTom = it.innhold.antallDagerTom,
                    antallNetterTom = it.innhold.antallNetterTom,
                    beløpFradrag = it.innhold.beløpFradrag,
                )
            }

        assertAll(
            { assertThat(aldersjusteringResultat).isNotNull },
            { assertThat(aldersjusteringResultat.beregnetBarnebidragPeriodeListe).hasSize(1) },
            { assertThat(aldersjusteringResultatGrunnlagListe).isNotNull },

            // Resultat
            // Total
            { assertThat(aldersjusteringResultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp).isEqualTo(forventetResultatbeløp) },

            // Sluttberegning (endelig bidrag)
            { assertThat(endeligBidragResultatListe).hasSize(1) },
            { assertThat(endeligBidragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(endeligBidragResultatListe[0].beregnetBeløp).isEqualTo(forventetBeregnetBeløp) },
            { assertThat(endeligBidragResultatListe[0].resultatBeløp).isEqualTo(forventetResultatbeløp) },
            { assertThat(endeligBidragResultatListe[0].bpAndelBeløp).isEqualTo(forventetBpAndelBeløp) },
            { assertThat(endeligBidragResultatListe[0].bpAndelFaktorVedDeltBosted).isEqualTo(forventetBpAndelFaktorVedDeltBosted) },
            { assertThat(endeligBidragResultatListe[0].deltBosted).isEqualTo(forventetDeltBosted) },

            // Delberegning underholdskostnad
            { assertThat(underholdskostnadResultatListe).hasSize(1) },
            { assertThat(underholdskostnadResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(underholdskostnadResultatListe[0].forbruksutgift).isEqualTo(forventetForbruksutgift) },
            { assertThat(underholdskostnadResultatListe[0].boutgift).isEqualTo(forventetBoutgift) },
            { assertThat(underholdskostnadResultatListe[0].barnetilsynMedStønad).isEqualTo(forventetBarnetilsynMedStønad) },
            { assertThat(underholdskostnadResultatListe[0].nettoTilsynsutgift).isEqualTo(forventetNettoTilsynsutgift) },
            { assertThat(underholdskostnadResultatListe[0].barnetrygd).isEqualTo(forventetBarnetrygd) },
            { assertThat(underholdskostnadResultatListe[0].underholdskostnad).isEqualTo(forventetUnderholdskostnad) },

            // Delberegning samværsfradrag
            { assertThat(samværsfradragResultatListe).hasSize(1) },
            { assertThat(samværsfradragResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(samværsfradragResultatListe[0].beløp).isEqualTo(forventetSamværsfradragBeløp) },

            // Exception
            { assertThat(feilmelding).isEqualTo(forventetFeilmelding) },

            // Grunnlag
            // Kopi delberegning underholdskostnad (fra vedtak)
            { assertThat(kopiDelberegningUnderholdskostnad).hasSize(1) },
            { assertThat(kopiDelberegningUnderholdskostnad[0].periode).isEqualTo(beregningsperiode) },
            { assertThat(kopiDelberegningUnderholdskostnad[0].fraVedtakId).isEqualTo(forventetVedtakId) },
            { assertThat(kopiDelberegningUnderholdskostnad[0].nettoTilsynsutgift?.setScale(2)).isEqualTo(forventetNettoTilsynsutgift) },

            // Kopi delberegning bidragspliktiges andel (fra vedtak)
            { assertThat(kopiDelberegningBidragspliktigesAndel).hasSize(1) },
            { assertThat(kopiDelberegningBidragspliktigesAndel[0].periode).isEqualTo(beregningsperiode) },
            { assertThat(kopiDelberegningBidragspliktigesAndel[0].fraVedtakId).isEqualTo(forventetVedtakId) },
            { assertThat(kopiDelberegningBidragspliktigesAndel[0].endeligAndelFaktor.avrundetMedTiDesimaler).isEqualTo(forventetEndeligAndelFaktor) },

            // Kopi samværsperiode (fra vedtak)
            { assertThat(kopiSamværsperiode).hasSize(1) },
            { assertThat(kopiSamværsperiode[0].periode).isEqualTo(beregningsperiode) },
            { assertThat(kopiSamværsperiode[0].fraVedtakId).isEqualTo(forventetVedtakId) },
            { assertThat(kopiSamværsperiode[0].samværsklasse).isEqualTo(forventetSamværsklasse) },

            // Kopi barnetilsyn med stønad (fra vedtak)
            { assertThat(kopiBarnetilsynMedStønadPeriode).hasSize(forventetAntallBarnetilsynMedStønad) },

            // Sjablon sjablontall
            { assertThat(sjablonSjablontallListe).hasSize(2) },
            { assertThat(sjablonSjablontallListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(sjablonSjablontallListe[0].sjablon.navn).isEqualTo(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELØP.navn) },
            { assertThat(sjablonSjablontallListe[0].verdi).isEqualTo(forventetSjablonverdiBoutgifter) },
            { assertThat(sjablonSjablontallListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(sjablonSjablontallListe[1].sjablon.navn).isEqualTo(SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn) },
            { assertThat(sjablonSjablontallListe[1].verdi).isEqualTo(forventetSjablonverdiOrdinærBarnetrygd) },

            // Sjablon forbruksutgifter
            { assertThat(sjablonForbruksutgifter).hasSize(1) },
            { assertThat(sjablonForbruksutgifter[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null)) },
            { assertThat(sjablonForbruksutgifter[0].alderTom).isEqualTo(forventetSjablonverdiAlderTom) },
            { assertThat(sjablonForbruksutgifter[0].beløpForbruk).isEqualTo(forventetSjablonverdiBeløpForbruk) },

            // Sjablon samværsfradrag
            { assertThat(sjablonSamværsfradrag).hasSize(forventetAntallSjablonerSamværsfradrag) },
            {
                if (sjablonSamværsfradrag.isNotEmpty()) {
                    assertThat(sjablonSamværsfradrag[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-07"), null))
                }
            },
            {
                if (sjablonSamværsfradrag.isNotEmpty()) {
                    assertThat(sjablonSamværsfradrag[0].samværsklasse).isEqualTo(forventetSjablonverdiSamværsklasse)
                }
            },
            {
                if (sjablonSamværsfradrag.isNotEmpty()) {
                    assertThat(sjablonSamværsfradrag[0].alderTom).isEqualTo(forventetSjablonverdiAlderTom)
                }
            },
            {
                if (sjablonSamværsfradrag.isNotEmpty()) {
                    assertThat(sjablonSamværsfradrag[0].beløpFradrag).isEqualTo(forventetSjablonverdiBeløpSamværsfradrag)
                }
            },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun lesFilOgByggRequestAldersjustering(filnavn: String): BeregnGrunnlagAldersjustering {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlagAldersjustering::class.java)
    }
}
