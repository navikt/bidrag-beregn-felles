package no.nav.bidrag.sivilstand.api

import no.nav.bidrag.sivilstand.service.SivilstandServiceV1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock

internal class Sivilstand2V1ApiTest {

    @Mock
    private lateinit var sivilstandServiceV1: SivilstandServiceV1

    @BeforeEach
    fun initMock() {
        sivilstandServiceV1 = SivilstandServiceV1()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
    }
}
