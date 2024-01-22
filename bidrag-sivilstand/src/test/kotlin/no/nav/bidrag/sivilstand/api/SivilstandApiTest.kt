package no.nav.bidrag.sivilstand.api

import no.nav.bidrag.sivilstand.service.SivilstandService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SivilstandApiTest {

    @Mock
    private lateinit var sivilstandService: SivilstandService

    @BeforeEach
    fun initMock() {
        sivilstandService = SivilstandService()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
    }
}
