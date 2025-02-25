package no.nav.bidrag.sivilstand.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock

internal class IndeksreguleringApiTest {

    @Mock
    private lateinit var indeksreguleringService: IndeksreguleringService

    @BeforeEach
    fun initMock() {
        indeksreguleringService = IndeksreguleringService()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
    }
}
