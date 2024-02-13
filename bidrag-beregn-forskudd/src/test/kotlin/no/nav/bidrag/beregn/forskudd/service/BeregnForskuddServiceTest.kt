package no.nav.bidrag.beregn.forskudd.service

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.beregn.forskudd.core.ForskuddCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
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

@ExtendWith(MockitoExtension::class)
internal class BeregnForskuddServiceTest {
    private lateinit var beregnForskuddService: BeregnForskuddService

    @Mock
    private lateinit var forskuddCoreMock: ForskuddCore

    @Captor
    private lateinit var grunnlagTilCoreCaptor: ArgumentCaptor<BeregnForskuddGrunnlagCore>

    @BeforeEach
    fun initMock() {
        mockkObject(SjablonProvider)
        every {
            SjablonProvider.hentSjablontall()
        } returns TestUtil.dummySjablonSjablontallListe()
        beregnForskuddService = BeregnForskuddService(forskuddCoreMock)
    }

    @Test
    @DisplayName("Skal beregne forskudd")
    fun skalBeregneForskudd() {
        `when`(forskuddCoreMock.beregnForskudd(capture(grunnlagTilCoreCaptor))).thenReturn(TestUtil.dummyForskuddResultatCore())

        val beregnForskuddResultat = beregnForskuddService.beregn(TestUtil.byggForskuddGrunnlag())
        val grunnlagTilCore = grunnlagTilCoreCaptor.value

        assertAll(
            Executable { assertThat(beregnForskuddResultat.beregnetForskuddPeriodeListe).isNotNull() },
            Executable { assertThat(beregnForskuddResultat.beregnetForskuddPeriodeListe).hasSize(1) },
            // Sjablontyper som ikke er gyldige for forskudd og sjabloner som ikke er innenfor beregn-fra-til-dato filtreres bort
            Executable { assertThat(grunnlagTilCore.sjablonPeriodeListe).hasSize(21) },
        )
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException ved feil retur fra Core")
    fun skalKasteUgyldigInputExceptionVedFeilReturFraCore() {
        `when`(forskuddCoreMock.beregnForskudd(any())).thenReturn(TestUtil.dummyForskuddResultatCoreMedAvvik())
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { beregnForskuddService.beregn(TestUtil.byggForskuddGrunnlag()) }
            .withMessageContaining("beregnDatoFra kan ikke være null")
            .withMessageContaining("periodeDatoTil må være etter periodeDatoFra")
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
