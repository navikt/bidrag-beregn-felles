package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.barnebidrag.service.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

@ExtendWith(MockitoExtension::class)
internal class BeregnBarnebidragApiTest {
    private lateinit var filnavn: String

    private lateinit var forventetBidragsevneBeløp: BigDecimal
    private lateinit var forventetBPAndelSærbidragFaktor: BigDecimal
    private lateinit var forventetBPAndelSærbidragBeløp: BigDecimal
    private lateinit var forventetSærbidragBeregnetBeløp: BigDecimal
    private lateinit var forventetSærbidragResultatKode: Resultatkode
    private var forventetSærbidragResultatBeløp: BigDecimal? = null
    private lateinit var forventetSumInntektBP: BigDecimal
    private lateinit var forventetSumInntektBM: BigDecimal
    private lateinit var forventetSumInntektSB: BigDecimal
    private var forventetAntallBarnIHusstand: Double = 0.0
    private var forventetVoksneIHusstand: Boolean = false

    @Mock
    private lateinit var beregnBarnebidragService: BeregnBarnebidragService

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        beregnBarnebidragService = BeregnBarnebidragService()
    }

    // Eksempel 1-8 refererer til de opprinnelige eksemplene til John, men er modifisert til å ikke ta hensyn til løpende bidrag
    // De øvrige eksemplene er lagt til for å teste spesiell logikk

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
        filnavn = "src/test/resources/testfiler/barnebidrag_eksempelNY.json"
        val request = lesFilOgByggRequest(filnavn)

        val totalBarnebidragResultat = beregnBarnebidragService.beregnBarnebidrag(request)

        printJson(totalBarnebidragResultat)
    }

    private fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    private fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
