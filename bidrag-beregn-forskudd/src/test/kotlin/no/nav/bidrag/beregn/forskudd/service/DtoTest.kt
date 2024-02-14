package no.nav.bidrag.beregn.forskudd.service

import no.nav.bidrag.transport.behandling.beregning.felles.valider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DtoTest {
    @Test
    @DisplayName("Skal kaste IllegalArgumentException når beregningsperiode til er null")
    fun skalKasteIllegalArgumentExceptionNaarBeregningsperiodeTilErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenBeregningsperiodeTil()
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("beregningsperiode til kan ikke være null")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når grunnlagListe er null")
    fun skalKasteIllegalArgumentExceptionNaarGrunnlagListeErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenGrunnlagListe()
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("grunnlagListe kan ikke være tom")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når referanse er null")
    fun skalKasteIllegalArgumentExceptionNaarReferanseErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenReferanse()
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("referanse kan ikke være en tom streng")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når innhold er null")
    fun skalKasteIllegalArgumentExceptionNaarInnholdErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenInnhold()
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("innhold kan ikke være null")
    }

    @Test
    @DisplayName("Skal ikke kaste exception")
    fun skalIkkeKasteException() {
        val grunnlag = TestUtil.byggDummyForskuddGrunnlag()
        Assertions.assertThatCode { grunnlag.valider() }.doesNotThrowAnyException()
    }
}
