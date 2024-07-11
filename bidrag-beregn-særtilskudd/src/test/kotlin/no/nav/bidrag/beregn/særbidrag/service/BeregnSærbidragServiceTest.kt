package no.nav.bidrag.beregn.særbidrag.service

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.BPsAndelSærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.SærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.exception.UgyldigInputException
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
internal class BeregnSærbidragServiceTest {
    private lateinit var beregnSærbidragService: BeregnSærbidragService

    @Mock
    private lateinit var bidragsevneCoreMock: BidragsevneCore

    @Mock
    private lateinit var bpAndelSærbidragCoreMock: BPsAndelSærbidragCore

    @Mock
    private lateinit var særbidragCoreMock: SærbidragCore

    @Captor
    private lateinit var beregnSærbidragGrunnlagCoreCaptor: ArgumentCaptor<BeregnSærbidragGrunnlagCore>

    @Captor
    private lateinit var beregnBidragsevneGrunnlagCoreCaptor: ArgumentCaptor<BeregnBidragsevneGrunnlagCore>

    @Captor
    private lateinit var beregnBPsAndelSærbidragGrunnlagCoreCaptor: ArgumentCaptor<BeregnBPsAndelSærbidragGrunnlagCore>

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

        beregnSærbidragService = BeregnSærbidragService(
            bidragsevneCore = bidragsevneCoreMock,
            bpAndelSærbidragCore = bpAndelSærbidragCoreMock,
            særbidragCore = særbidragCoreMock,
        )
    }

    @Test
    @DisplayName("Skal ha korrekt sjablon-grunnlag når beregningsmodulen kalles")
    @Disabled
    fun skalHaKorrektSjablonGrunnlagNårBeregningsmodulenKalles() {
        val bidragsevneGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnBidragsevneGrunnlagCore::class.java,
            )
        val bpAndelSærbidragGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnBPsAndelSærbidragGrunnlagCore::class.java,
            )
        val særbidragGrunnlagTilCoreCaptor =
            ArgumentCaptor.forClass(
                BeregnSærbidragGrunnlagCore::class.java,
            )
        `when`(bidragsevneCoreMock.beregnBidragsevne(bidragsevneGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(bpAndelSærbidragGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCore())
        `when`(særbidragCoreMock.beregnSærbidrag(særbidragGrunnlagTilCoreCaptor.capture()))
            .thenReturn(TestUtil.dummySærbidragResultatCore())
        val beregnTotalSærbidragResultat = beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag())
        val (_, _, _, _, _, sjablonPeriodeListe) = bidragsevneGrunnlagTilCoreCaptor.value
        val (_, _, _, _, _, _, sjablonPeriodeListe1) = bpAndelSærbidragGrunnlagTilCoreCaptor.value

        // For Sjablontall sjekkes at det er riktig type sjablontall. For alle sjabloner sjekkes det at datoen er innenfor beregn-fra-til-dato
        // For å finne riktig tall: Sjekk TestUtil.dummySjablonxxx; tell hvor mange sjabloner som er innenfor dato og (for Sjablontall) av riktig type

        // Bidragsevne: Sjablontall (0004, 0017, 0019, 0023, 0025, 0027, 0028, 0039, 0040) + Bidragsevne + TrinnvisSkattesats
        val forventetAntallSjablonElementerBidragsevne = 9 + 2 + 4
        // BPs andel særbidrag: Sjablontall (0004, 0005, 0030, 0031, 0039)
        val forventetAntallSjablonElementerBPsAndelSærbidrag = 5
        // Særbidrag: Ingen sjabloner
        val forventetAntallSjablonElementerSærbidrag = 0
        assertAll(
            Executable { assertThat(beregnTotalSærbidragResultat).isNotNull() },
            Executable { assertThat(sjablonPeriodeListe).hasSize(forventetAntallSjablonElementerBidragsevne) },
            Executable { assertThat(sjablonPeriodeListe1).hasSize(forventetAntallSjablonElementerBPsAndelSærbidrag) },
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
    @DisplayName("Skal beregne særbidrag")
    fun skalBeregneSærbidrag() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(
            bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCore())
        `when`(
            særbidragCoreMock.beregnSærbidrag(capture(beregnSærbidragGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummySærbidragResultatCore())

        val beregnSærbidragResultat = beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag())
        assertAll(
            { assertNotNull(beregnSærbidragResultat) },
            { assertNotNull(beregnSærbidragResultat.beregnetSærbidragPeriodeListe) },
            { assertEquals(1, beregnSærbidragResultat.beregnetSærbidragPeriodeListe.size) },
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
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra BPsAndelSærbidragCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraBPsAndelSærbidragCore() {
        `when`(
            bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av BPs andel av særbidrag. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra SærbidragCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraSærbidragCore() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(any())).thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCore())
        `when`(
            særbidragCoreMock.beregnSærbidrag(capture(beregnSærbidragGrunnlagCoreCaptor)),
        ).thenReturn(TestUtil.dummySærbidragResultatCoreMedAvvik())
        Assertions.assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av særbidrag. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
