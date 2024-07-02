package no.nav.bidrag.beregn.særtilskudd.service

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.BPsAndelSaertilskuddCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.SamvaersfradragCore
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.dto.BeregnSamvaersfradragGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.SærtilskuddCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.exception.UgyldigInputException
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class BeregnSærtilskuddServiceTest {
    private lateinit var beregnSaertilskuddService: BeregnSærtilskuddService

    @Mock
    private lateinit var bidragsevneCoreMock: BidragsevneCore

    @Mock
    private lateinit var bpAndelSaertilskuddCoreMock: BPsAndelSaertilskuddCore

    @Mock
    private lateinit var samvaersfradragCoreMock: SamvaersfradragCore

    @Mock
    private lateinit var saertilskuddCoreMock: SærtilskuddCore

    @Captor
    private lateinit var beregnSaertilskuddGrunnlagCoreCaptor: ArgumentCaptor<BeregnSaertilskuddGrunnlagCore>

    @Captor
    private lateinit var beregnBidragsevneGrunnlagCoreCaptor: ArgumentCaptor<BeregnBidragsevneGrunnlagCore>

    @Captor
    private lateinit var beregnSamvaersfradragGrunnlagCoreCaptor: ArgumentCaptor<BeregnSamvaersfradragGrunnlagCore>

    @Captor
    private lateinit var beregnBPsAndelSaertilskuddGrunnlagCoreCaptor: ArgumentCaptor<BeregnBPsAndelSaertilskuddGrunnlagCore>

    @BeforeEach
    fun initMock() {
        mockkObject(SjablonProvider)
        every {
            SjablonProvider.hentSjablontall()
        } returns TestUtil.dummySjablonSjablontallListe()
        every {
            SjablonProvider.hentSjablonSamværsfradrag()
        } returns TestUtil.dummySjablonSamværsfradragListe()
        every {
            SjablonProvider.hentSjablonBidragsevne()
        } returns TestUtil.dummySjablonBidragsevneListe()
        every {
            SjablonProvider.hentSjablonTrinnvisSkattesats()
        } returns TestUtil.dummySjablonTrinnvisSkattesatsListe()

        beregnSaertilskuddService = BeregnSærtilskuddService(
            bidragsevneCore = bidragsevneCoreMock,
            bpAndelSaertilskuddCore = bpAndelSaertilskuddCoreMock,
            samvaersfradragCore = samvaersfradragCoreMock,
            saertilskuddCore = saertilskuddCoreMock,
        )
    }

    @Test
    @DisplayName("Skal ha korrekt sjablon-grunnlag når beregningsmodulen kalles")
    @Disabled
    fun skalHaKorrektSjablonGrunnlagNaarBeregningsmodulenKalles() {
        val bidragsevneGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnBidragsevneGrunnlagCore::class.java,
            )
        val samvaersfradragGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnSamvaersfradragGrunnlagCore::class.java,
            )
        val bpAndelSaertilskuddGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnBPsAndelSaertilskuddGrunnlagCore::class.java,
            )
        val saertilskuddGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnSaertilskuddGrunnlagCore::class.java,
            )
        `when`(bidragsevneCoreMock.beregnBidragsevne(bidragsevneGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSaertilskuddCoreMock.beregnBPsAndelSaertilskudd(bpAndelSaertilskuddGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummyBPsAndelSaertilskuddResultatCore())
        `when`(samvaersfradragCoreMock.beregnSamvaersfradrag(samvaersfradragGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummySamværsfradragResultatCore())
        `when`(saertilskuddCoreMock.beregnSaertilskudd(saertilskuddGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummySaertilskuddResultatCore())
        val beregnTotalSaertilskuddResultat = beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag())
        val (_, _, _, _, _, sjablonPeriodeListe) = bidragsevneGrunnlagTilCoreCaptor.value
        val (_, _, _, _, _, _, sjablonPeriodeListe1) = bpAndelSaertilskuddGrunnlagTilCoreCaptor.value
        val (_, _, _, sjablonPeriodeListe2) = samvaersfradragGrunnlagTilCoreCaptor.value
        val (_, _, _, _, _, _, _, sjablonPeriodeListe3) = saertilskuddGrunnlagTilCoreCaptor.value

        // For Sjablontall sjekkes at det er riktig type sjablontall. For alle sjabloner sjekkes det at datoen er innenfor beregn-fra-til-dato
        // For å finne riktig tall: Sjekk TestUtil.dummySjablonxxx; tell hvor mange sjabloner som er innenfor dato og (for Sjablontall) av riktig type

        // Bidragsevne: Sjablontall (0004, 0017, 0019, 0023, 0025, 0027, 0028, 0039, 0040) + Bidragsevne + TrinnvisSkattesats
        val forventetAntallSjablonElementerBidragsevne = 9 + 2 + 4
        // BPs andel særtilskudd: Sjablontall (0004, 0005, 0030, 0031, 0039)
        val forventetAntallSjablonElementerBPsAndelSaertilskudd = 5
        // Samværsfradrag: Samvaersfradrag
        val forventetAntallSjablonElementerSamvaersfradrag = 21
        // Saertilskudd: Ingen sjabloner
        val forventetAntallSjablonElementerSaertilskudd = 0
        assertAll(
            Executable { assertThat(beregnTotalSaertilskuddResultat).isNotNull() },
            Executable { assertThat(sjablonPeriodeListe).hasSize(forventetAntallSjablonElementerBidragsevne) },
            Executable { assertThat(sjablonPeriodeListe1).hasSize(forventetAntallSjablonElementerBPsAndelSaertilskudd) },
            Executable { assertThat(sjablonPeriodeListe2).hasSize(forventetAntallSjablonElementerSamvaersfradrag) },
            Executable { assertThat(sjablonPeriodeListe3).hasSize(forventetAntallSjablonElementerSaertilskudd) },
            // Sjekk at det mappes ut riktig antall for en gitt sjablon av type Sjablontall
            Executable {
                assertThat(
                    sjablonPeriodeListe.count { (_, navn): SjablonPeriodeCore -> navn == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn },
                )
                    .isEqualTo(
                        TestUtil.dummySjablonSjablontallListe()
                            .filter { (typeSjablon): Sjablontall -> typeSjablon == "0017" }.count { (_, datoFom, datoTom): Sjablontall ->
                                !datoFom!!.isAfter(LocalDate.parse("2020-09-01")) &&
                                    !datoTom
                                        ?.isBefore(LocalDate.parse("2020-08-01"))!!
                            },
                    )
            },
            Executable {
                assertThat(
                    sjablonPeriodeListe.count { (_, navn): SjablonPeriodeCore -> navn == SjablonNavn.BIDRAGSEVNE.navn },
                )
                    .isEqualTo(
                        TestUtil.dummySjablonBidragsevneListe().count { (_, datoFom, datoTom): Bidragsevne ->
                            !datoFom!!.isAfter(LocalDate.parse("2020-09-01")) &&
                                !datoTom
                                    ?.isBefore(LocalDate.parse("2020-08-01"))!!
                        },
                    )
            },
            Executable {
                assertThat(
                    sjablonPeriodeListe.stream()
                        .filter { (periode, navn): SjablonPeriodeCore ->
                            navn == SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn &&
                                periode.datoFom ==
                                LocalDate.parse(
                                    "2014-01-01",
                                )
                        }
                        .flatMap { it.innholdListe.stream() }
                        .findFirst()
                        .map { it.verdi }
                        .orElse(BigDecimal.ZERO),
                )
                    .isEqualTo(BigDecimal.valueOf(8.2))
            },
        )
    }

    @Test
    @DisplayName("Skal beregne særtilskudd")
    fun skalBeregneSaertilskudd() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(
            bpAndelSaertilskuddCoreMock.beregnBPsAndelSaertilskudd(capture(beregnBPsAndelSaertilskuddGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBPsAndelSaertilskuddResultatCore())
        `when`(
            samvaersfradragCoreMock.beregnSamvaersfradrag(capture(beregnSamvaersfradragGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummySamværsfradragResultatCore())
        `when`(
            saertilskuddCoreMock.beregnSaertilskudd(capture(beregnSaertilskuddGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummySaertilskuddResultatCore())

        val beregnSaertilskuddResultat = beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag())
        assertAll(
            { assertNotNull(beregnSaertilskuddResultat) },
            { assertNotNull(beregnSaertilskuddResultat.beregnetSærtilskuddPeriodeListe) },
            { assertEquals(1, beregnSaertilskuddResultat.beregnetSærtilskuddPeriodeListe.size) },
        )
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra BidragsevneCore")
    @Disabled
    fun skalKasteUgyldigInputExceptionVedFeilReturFraBidragsevneCore() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra BPsAndelSaertilskuddCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraBPsAndelSaertilskuddCore() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSaertilskuddCoreMock.beregnBPsAndelSaertilskudd(capture(beregnBPsAndelSaertilskuddGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSaertilskuddResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av BPs andel av særtilskudd. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra SamvaersfradragCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraSamvaersfradragCore() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSaertilskuddCoreMock.beregnBPsAndelSaertilskudd(capture(beregnBPsAndelSaertilskuddGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSaertilskuddResultatCore())
        `when`(samvaersfradragCoreMock.beregnSamvaersfradrag(capture(beregnSamvaersfradragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummySamværsfradragResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra SaertilskuddCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraSaertilskuddCore() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(any())).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSaertilskuddCoreMock.beregnBPsAndelSaertilskudd(capture(beregnBPsAndelSaertilskuddGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSaertilskuddResultatCore())
        `when`(samvaersfradragCoreMock.beregnSamvaersfradrag(capture(beregnSamvaersfradragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummySamværsfradragResultatCoreMedAvvik())
        `when`(
            saertilskuddCoreMock.beregnSaertilskudd(capture(beregnSaertilskuddGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummySaertilskuddResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSaertilskuddService.beregn(TestUtil.byggTotalSaertilskuddGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
