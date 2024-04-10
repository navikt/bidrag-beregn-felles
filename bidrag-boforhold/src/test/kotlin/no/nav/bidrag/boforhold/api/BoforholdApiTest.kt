package no.nav.bidrag.boforhold.api

import no.nav.bidrag.boforhold.service.BoforholdServiceV1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock

internal class BoforholdApiTest {

    @Mock
    private lateinit var boforholdServiceV1: BoforholdServiceV1

    @BeforeEach
    fun initMock() {
        boforholdServiceV1 = BoforholdServiceV1()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
    }
}
