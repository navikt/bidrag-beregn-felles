package no.nav.bidrag.beregn.forskudd.core

import no.nav.bidrag.beregn.forskudd.TestUtil.byggAvvikListe
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddResultat
import no.nav.bidrag.beregn.forskudd.TestUtil.byggSjablonPeriodeListe
import no.nav.bidrag.beregn.forskudd.core.periode.ForskuddPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ForskuddCoreTest {
    private lateinit var forskuddCore: ForskuddCore

    @Mock
    private lateinit var forskuddPeriode: ForskuddPeriode

    private val beregnForskuddGrunnlagCore = byggForskuddGrunnlagCore()
    private val beregnForskuddResultat = byggForskuddResultat()
    private val avvikListe = byggAvvikListe()

    @BeforeEach
    fun initMock() {
        forskuddCore = ForskuddCore(forskuddPeriode)
    }

    @Test
    @DisplayName("Skal beregne forskudd")
    fun skalBeregneForskudd() {
        Mockito.`when`(forskuddPeriode.beregnPerioder(MockitoHelper.any())).thenReturn(beregnForskuddResultat)
        val beregnForskuddResultatCore = forskuddCore.beregnForskudd(beregnForskuddGrunnlagCore)

        assertAll(
            { assertThat(beregnForskuddResultatCore).isNotNull() },
            { assertThat(beregnForskuddResultatCore.avvikListe).isEmpty() },
            { assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe).isNotEmpty() },
            { assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe).hasSize(3) },
            { assertThat(beregnForskuddResultatCore.sjablonListe).isNotEmpty() },
            { assertThat(beregnForskuddResultatCore.sjablonListe).hasSameSizeAs(byggSjablonPeriodeListe()) },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].periode.datoFom)
                    .isEqualTo(LocalDate.parse("2017-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].periode.datoTil)
                    .isEqualTo(LocalDate.parse("2018-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].resultat.beløp)
                    .isEqualTo(BigDecimal.valueOf(1600))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].resultat.kode)
                    .isEqualTo(Resultatkode.FORHØYET_FORSKUDD_100_PROSENT)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].resultat.regel)
                    .isEqualTo("REGEL 1")
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(BARN_I_HUSSTANDEN_REFERANSE_1)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(BARN_I_HUSSTANDEN_REFERANSE_2)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(BOSTATUS_REFERANSE_MED_FORELDRE_1)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(INNTEKT_REFERANSE_1)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(SIVILSTAND_REFERANSE_ENSLIG)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[0].grunnlagsreferanseListe)
                    .contains(SØKNADSBARN_REFERANSE)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[1].periode.datoFom)
                    .isEqualTo(LocalDate.parse("2018-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[1].periode.datoTil)
                    .isEqualTo(LocalDate.parse("2019-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[1].resultat.beløp)
                    .isEqualTo(BigDecimal.valueOf(1200))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[1].resultat.kode)
                    .isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[1].resultat.regel)
                    .isEqualTo("REGEL 2")
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[2].periode.datoFom)
                    .isEqualTo(LocalDate.parse("2019-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[2].periode.datoTil)
                    .isEqualTo(LocalDate.parse("2020-01-01"))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[2].resultat.beløp)
                    .isEqualTo(BigDecimal.valueOf(0))
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[2].resultat.kode)
                    .isEqualTo(Resultatkode.AVSLAG)
            },
            {
                assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe[2].resultat.regel)
                    .isEqualTo("REGEL 11")
            },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne forskudd ved avvik")
    fun skalIkkeBeregneForskuddVedAvvik() {
        Mockito.`when`(forskuddPeriode.validerInput(MockitoHelper.any())).thenReturn(avvikListe)
        val beregnForskuddResultatCore = forskuddCore.beregnForskudd(beregnForskuddGrunnlagCore)

        assertAll(
            { assertThat(beregnForskuddResultatCore).isNotNull() },
            { assertThat(beregnForskuddResultatCore.avvikListe).isNotEmpty() },
            { assertThat(beregnForskuddResultatCore.avvikListe).hasSize(1) },
            {
                assertThat(
                    beregnForskuddResultatCore.avvikListe[0].avvikTekst,
                ).isEqualTo("beregnDatoTil må være etter beregnDatoFra")
            },
            {
                assertThat(
                    beregnForskuddResultatCore.avvikListe[0].avvikType,
                ).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString())
            },
            { assertThat(beregnForskuddResultatCore.beregnetForskuddPeriodeListe).isEmpty() },
        )
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException ved ugyldig enum")
    fun skalKasteIllegalArgumentExceptionVedUgyldigEnum() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { forskuddCore.beregnForskudd(byggForskuddGrunnlagCore("BOR_HELT_ALENE")) }
            .withMessage("No enum constant no.nav.bidrag.domene.enums.person.Bostatuskode.BOR_HELT_ALENE")
    }

    companion object {
        private const val INNTEKT_REFERANSE_1 = "INNTEKT_REFERANSE_1"
        private const val SIVILSTAND_REFERANSE_ENSLIG = "SIVILSTAND_REFERANSE_ENSLIG"
        private const val BARN_I_HUSSTANDEN_REFERANSE_1 = "BARN_I_HUSSTANDEN_REFERANSE_1"
        private const val BARN_I_HUSSTANDEN_REFERANSE_2 = "BARN_I_HUSSTANDEN_REFERANSE_2"
        private const val SØKNADSBARN_REFERANSE = "SØKNADSBARN_REFERANSE"
        private const val BOSTATUS_REFERANSE_MED_FORELDRE_1 = "BOSTATUS_REFERANSE_MED_FORELDRE_1"
    }

    object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
