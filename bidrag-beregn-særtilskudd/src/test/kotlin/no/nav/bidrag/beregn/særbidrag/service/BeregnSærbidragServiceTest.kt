package no.nav.bidrag.beregn.særbidrag.service

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.BPsAndelSærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.SærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.exception.UgyldigInputException
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture

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
        every { SjablonProvider.hentSjablontall() } returns TestUtil.dummySjablonSjablontallListe()
        every { SjablonProvider.hentSjablonSamværsfradrag() } returns TestUtil.dummySjablonSamværsfradragListe()
        every { SjablonProvider.hentSjablonBidragsevne() } returns TestUtil.dummySjablonBidragsevneListe()
        every { SjablonProvider.hentSjablonTrinnvisSkattesats() } returns TestUtil.dummySjablonTrinnvisSkattesatsListe()

        beregnSærbidragService = BeregnSærbidragService(
            bidragsevneCore = bidragsevneCoreMock,
            bpAndelSærbidragCore = bpAndelSærbidragCoreMock,
            særbidragCore = særbidragCoreMock,
        )
    }

    @Test
    @DisplayName("Skal beregne særbidrag")
    fun skalBeregneSærbidrag() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCore())
        `when`(særbidragCoreMock.beregnSærbidrag(capture(beregnSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummySærbidragResultatCore())

        val beregnSærbidragResultat = beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag())

        assertAll(
            { assertNotNull(beregnSærbidragResultat) },
            { assertNotNull(beregnSærbidragResultat.beregnetSærbidragPeriodeListe) },
            { assertEquals(1, beregnSærbidragResultat.beregnetSærbidragPeriodeListe.size) },
        )
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra BidragsevneCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraBidragsevneCore() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBidragsevneResultatCoreMedAvvik())

        assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra BPsAndelSærbidragCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraBPsAndelSærbidragCore() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCoreMedAvvik())

        assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av BPs andel av særbidrag. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra SærbidragCore")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraSærbidragCore() {
        `when`(bidragsevneCoreMock.beregnBidragsevne(capture(beregnBidragsevneGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBidragsevneResultatCore())
        `when`(bpAndelSærbidragCoreMock.beregnBPsAndelSærbidrag(capture(beregnBPsAndelSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummyBPsAndelSærbidragResultatCore())
        `when`(særbidragCoreMock.beregnSærbidrag(capture(beregnSærbidragGrunnlagCoreCaptor)))
            .thenReturn(TestUtil.dummySærbidragResultatCoreMedAvvik())

        assertThatExceptionOfType(UgyldigInputException::class.java)
            .isThrownBy { beregnSærbidragService.beregn(TestUtil.byggTotalSærbidragGrunnlag()) }
            .withMessageContaining("Ugyldig input ved beregning av særbidrag. Følgende avvik ble funnet:")
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }
}
