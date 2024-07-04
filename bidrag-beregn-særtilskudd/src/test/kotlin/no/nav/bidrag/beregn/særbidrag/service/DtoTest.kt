package no.nav.bidrag.beregn.særbidrag.service

import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DtoTest {

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når beregningsperiode til er null")
    fun skalKasteIllegalArgumentExceptionNårBeregningsperiodeTilErNull() {
        val grunnlag = TestUtil.byggSærtilskuddGrunnlagUtenBeregningsperiodeTil()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("beregningsperiode til kan ikke være null")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når grunnlagListe er null")
    fun skalKasteIllegalArgumentExceptionNårGrunnlagListeErNull() {
        val grunnlag = TestUtil.byggSærtilskuddGrunnlagUtenGrunnlagListe()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("grunnlagListe kan ikke være tom")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når referanse er null")
    fun skalKasteIllegalArgumentExceptionNårReferanseErNull() {
        val grunnlag = TestUtil.byggSærtilskuddGrunnlagUtenReferanse()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("referanse kan ikke være en tom streng")
    }

    @Test
    @DisplayName("Skal kaste IllegalArgumentException når innhold er null")
    fun skalKasteIllegalArgumentExceptionNårInnholdErNull() {
        val grunnlag = TestUtil.byggSærtilskuddGrunnlagUtenInnhold()
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { grunnlag.valider() }
            .withMessage("innhold kan ikke være null")
    }

    @Test
    @DisplayName("Skal ikke kaste exception")
    fun skalIkkeKasteException() {
        val grunnlag = TestUtil.byggDummySærtilskuddGrunnlag()
        assertThatCode { grunnlag.valider() }.doesNotThrowAnyException()
    }
}
