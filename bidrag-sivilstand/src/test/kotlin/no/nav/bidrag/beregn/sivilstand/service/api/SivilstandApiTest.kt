package no.nav.bidrag.beregn.sivilstand.service.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.sivilstand.service.SivilstandService
import no.nav.bidrag.beregn.sivilstand.service.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths

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
