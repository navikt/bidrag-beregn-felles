package no.nav.bidrag.beregn.felles.inntekt

import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagUtvidetBarnetrygdFull
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagUtvidetBarnetrygdOvergang
import no.nav.bidrag.beregn.felles.TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdFull
import no.nav.bidrag.beregn.felles.TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdOvergang
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.util.InntektUtil.behandlUtvidetBarnetrygd
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("InntektValidatorTest")
internal class InntektTest {
    private var inntektPeriodeGrunnlagListe = listOf<InntektPeriodeGrunnlag>()
    private var avvikListe = listOf<Avvik>()

    @Test
    @DisplayName("Utvidet barnetrygd - full test av regelverk")
    fun testUtvidetBarnetrygdFullTest() {
        val nyInntektGrunnlagListe =
            behandlUtvidetBarnetrygd(
                inntektPeriodeGrunnlagListe = byggInntektGrunnlagUtvidetBarnetrygdFull(),
                sjablonPeriodeListe = byggSjablontallGrunnlagUtvidetBarnetrygdFull()
            )

        assertAll(
            Executable { assertThat(nyInntektGrunnlagListe).isNotEmpty() },
            Executable { assertThat(nyInntektGrunnlagListe.size).isEqualTo(14) },
            Executable { assertThat(nyInntektGrunnlagListe[9].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-04-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[9].getPeriode().datoTil).isEqualTo(LocalDate.parse("2019-06-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[9].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[9].belop).isEqualTo(BigDecimal.valueOf(13000)) },
            Executable { assertThat(nyInntektGrunnlagListe[10].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[10].getPeriode().datoTil).isEqualTo(LocalDate.parse("2019-08-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[10].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[10].belop).isEqualTo(BigDecimal.valueOf(6500)) },
            Executable { assertThat(nyInntektGrunnlagListe[11].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-04-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[11].getPeriode().datoTil).isEqualTo(LocalDate.parse("2020-07-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[11].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[11].belop).isEqualTo(BigDecimal.valueOf(13000)) },
            Executable { assertThat(nyInntektGrunnlagListe[12].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[12].getPeriode().datoTil).isEqualTo(LocalDate.parse("2020-08-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[12].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[12].belop).isEqualTo(BigDecimal.valueOf(14000)) },
            Executable { assertThat(nyInntektGrunnlagListe[13].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-08-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[13].getPeriode().datoTil).isEqualTo(LocalDate.parse("2021-01-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[13].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[13].belop).isEqualTo(BigDecimal.valueOf(7000)) }
        )
    }

    @Test
    @DisplayName("Utvidet barnetrygd - test av overgang mellom regelverk for skatteklasse 2 og fordel særfradrag")
    fun testUtvidetBarnetrygdOvergangSkatteklasse2FordelSaerfradrag() {
        val nyInntektGrunnlagListe = behandlUtvidetBarnetrygd(
            inntektPeriodeGrunnlagListe = byggInntektGrunnlagUtvidetBarnetrygdOvergang(),
            sjablonPeriodeListe = byggSjablontallGrunnlagUtvidetBarnetrygdOvergang()
        )

        assertAll(
            Executable { assertThat(nyInntektGrunnlagListe).isNotEmpty() },
            Executable { assertThat(nyInntektGrunnlagListe.size).isEqualTo(5) },
            Executable { assertThat(nyInntektGrunnlagListe[2].getPeriode().datoFom).isEqualTo(LocalDate.parse("2012-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[2].getPeriode().datoTil).isEqualTo(LocalDate.parse("2012-07-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[2].type).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2.name) },
            Executable { assertThat(nyInntektGrunnlagListe[2].belop).isEqualTo(BigDecimal.valueOf(7500)) },
            Executable { assertThat(nyInntektGrunnlagListe[3].getPeriode().datoFom).isEqualTo(LocalDate.parse("2012-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[3].getPeriode().datoTil).isEqualTo(LocalDate.parse("2013-01-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[3].type).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2.name) },
            Executable { assertThat(nyInntektGrunnlagListe[3].belop).isEqualTo(BigDecimal.valueOf(8500)) },
            Executable { assertThat(nyInntektGrunnlagListe[4].getPeriode().datoFom).isEqualTo(LocalDate.parse("2013-01-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[4].getPeriode().datoTil).isEqualTo(LocalDate.parse("2013-06-01")) },
//            Executable { assertThat(nyInntektGrunnlagListe[4].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[4].belop).isEqualTo(BigDecimal.valueOf(12500)) }
        )
    }
}
