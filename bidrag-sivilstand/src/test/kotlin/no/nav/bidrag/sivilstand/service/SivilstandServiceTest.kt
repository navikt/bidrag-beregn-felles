package no.nav.bidrag.sivilstand.service

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SivilstandServiceTest {
    private lateinit var sivilstandService: SivilstandService

    @Test
    @DisplayName("Skal beregne sivilstand")
    fun skalBeregneSivilstand() {
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
