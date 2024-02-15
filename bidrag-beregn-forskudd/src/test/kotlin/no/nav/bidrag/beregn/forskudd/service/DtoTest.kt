package no.nav.bidrag.beregn.forskudd.service

import no.nav.bidrag.beregn.forskudd.TestUtil
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DtoTest {
    @Test
    @DisplayName("Skal kaste IllegalArgumentException når beregningsperiode til er null")
    fun skalKasteIllegalArgumentExceptionNårBeregningsperiodeTilErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenBeregningsperiodeTil()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("beregningsperiode til kan ikke være null")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når grunnlagListe er null")
    fun skalKasteIllegalArgumentExceptionNårGrunnlagListeErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenGrunnlagListe()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("grunnlagListe kan ikke være tom")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når referanse er null")
    fun skalKasteIllegalArgumentExceptionNårReferanseErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenReferanse()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("referanse kan ikke være en tom streng")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når innhold er null")
    fun skalKasteIllegalArgumentExceptionNårInnholdErNull() {
        val grunnlag = TestUtil.byggForskuddGrunnlagUtenInnhold()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("innhold kan ikke være null")
    }

    @Test
    @DisplayName("Skal ikke kaste exception")
    fun skalIkkeKasteException() {
        val grunnlag = TestUtil.byggDummyForskuddGrunnlag()
        assertThatCode { grunnlag.valider() }.doesNotThrowAnyException()
    }
}
