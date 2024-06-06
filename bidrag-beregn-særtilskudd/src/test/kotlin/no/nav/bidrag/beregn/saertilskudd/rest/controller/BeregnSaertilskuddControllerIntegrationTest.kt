package no.nav.bidrag.beregn.saertilskudd.rest.controller

import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest
import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest.Companion.TEST_PROFILE
import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.wiremockstub.SjablonApiStub
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.domain.enums.resultatkoder.ResultatKodeSaertilskudd
import no.nav.bidrag.transport.beregning.felles.Grunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths

@SpringBootTest(classes = [BidragBeregnSaertilskuddTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8096)
@EnableMockOAuth2Server
@ActiveProfiles(TEST_PROFILE)
internal class BeregnSaertilskuddControllerIntegrationTest {
    @Autowired
    private lateinit var httpHeaderTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    private lateinit var sjablonApiStub: SjablonApiStub

    @LocalServerPort
    private val port = 0
    private lateinit var url: String
    private lateinit var filnavn: String

    private var forventetBidragsevneBelop: BigDecimal? = null
    private var forventetBPAndelSaertilskuddProsentBarn: BigDecimal? = null
    private var forventetBPAndelSaertilskuddBelopBarn: BigDecimal? = null
    private var forventetSamvaersfradragBelopBarn1: BigDecimal? = null
    private var forventetSamvaersfradragBelopBarn2: BigDecimal? = null
    private var forventetSaertilskuddBelopBarn: BigDecimal? = null
    private var forventetSaertilskuddResultatkodeBarn: String? = null

    @BeforeEach
    fun init() {
        // Sett opp wiremock mot sjablon-tjenestene
        sjablonApiStub.settOppSjablonStub()

        // Bygg opp url
        url = "http://localhost:$port/beregn/saertilskudd"
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 1")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel01() {
        // Enkel beregning med full evne, ett barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel1.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(11069)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(60.6)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(4242)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(457)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(4242)
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_INNVILGET.toString()
        utfoerBeregningerOgEvaluerResultat_EttSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 2")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel02() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel2.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6696)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(49.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(2982)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(2982)
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_INNVILGET.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 3")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel03() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel3.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_INNVILGET.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 4")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel04() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel4.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 5")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel05() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel5.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(9962)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(62.8)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(7536)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 6")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel06() {
        // Enkel beregning med full evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel6.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(10891)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.1)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6612)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.valueOf(6612)
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_INNVILGET.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 7")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel07() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel7.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    @Test
    @DisplayName("skal kalle core og returnere et resultat - eksempel 8")
    fun skalKalleCoreOgReturnereEtResultat_Eksempel08() {
        // Beregning med manglende evne, to barn
        filnavn = "src/test/resources/testfiler/saertilskudd_eksempel8.json"
        forventetBidragsevneBelop = BigDecimal.valueOf(6149)
        forventetBPAndelSaertilskuddProsentBarn = BigDecimal.valueOf(55.7)
        forventetBPAndelSaertilskuddBelopBarn = BigDecimal.valueOf(6684)
        forventetSamvaersfradragBelopBarn1 = BigDecimal.valueOf(1513)
        forventetSamvaersfradragBelopBarn2 = BigDecimal.valueOf(1513)
        forventetSaertilskuddBelopBarn = BigDecimal.ZERO
        forventetSaertilskuddResultatkodeBarn = ResultatKodeSaertilskudd.SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE.toString()
        utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn()
    }

    private fun utfoerBeregningerOgEvaluerResultat_EttSoknadsbarn() {
        val request = lesFilOgByggRequest(filnavn!!)

        // Kall rest-API for saertilskudd
        val responseEntity =
            httpHeaderTestRestTemplate?.exchange(
                url,
                HttpMethod.POST,
                request,
                BeregnetTotalSaertilskuddResultat::class.java,
            )
        val totalSaertilskuddResultat = responseEntity?.body
        val saertilskuddDelberegningResultat = totalSaertilskuddResultat?.let { SaertilskuddDelberegningResultat(it) }
        val alleReferanser = totalSaertilskuddResultat?.let { TestUtil.hentAlleReferanser(it) }
        val referanserIGrunnlagListe = totalSaertilskuddResultat!!.grunnlagListe.map(Grunnlag::referanse).toList()
        assertAll(
            Executable { assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(totalSaertilskuddResultat).isNotNull() },
            Executable { assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe).isNotNull() },
            Executable { assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe).hasSize(1) },
            Executable {
                assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.belop).isEqualTo(
                    forventetSaertilskuddBelopBarn,
                )
            },
            Executable {
                assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.kode.toString()).isEqualTo(
                    forventetSaertilskuddResultatkodeBarn,
                )
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe).size().isEqualTo(1) },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].belop).isEqualTo(forventetBidragsevneBelop) },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe).size().isEqualTo(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].belop).isEqualTo(
                    forventetBPAndelSaertilskuddBelopBarn,
                )
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].prosent).isEqualTo(
                    forventetBPAndelSaertilskuddProsentBarn,
                )
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe).size().isEqualTo(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].belop).isEqualTo(forventetSamvaersfradragBelopBarn1)
            },
            Executable { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

    private fun utfoerBeregningerOgEvaluerResultat_ToSoknadsbarn() {
        val request = lesFilOgByggRequest(filnavn!!)

        // Kall rest-API for Saertilskudd
        val responseEntity =
            httpHeaderTestRestTemplate!!.exchange(
                url,
                HttpMethod.POST,
                request,
                BeregnetTotalSaertilskuddResultat::class.java,
            )
        val totalSaertilskuddResultat = responseEntity.body
        val saertilskuddDelberegningResultat = totalSaertilskuddResultat?.let { SaertilskuddDelberegningResultat(it) }
        val alleReferanser = totalSaertilskuddResultat?.let { TestUtil.hentAlleReferanser(it) }
        val referanserIGrunnlagListe = totalSaertilskuddResultat!!.grunnlagListe.stream().map(Grunnlag::referanse).toList()
        assertAll(
            Executable { assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(totalSaertilskuddResultat).isNotNull() },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe).hasSize(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].belop).isEqualTo(forventetBidragsevneBelop)
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe).hasSize(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].belop).isEqualTo(
                    forventetBPAndelSaertilskuddBelopBarn,
                )
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].prosent).isEqualTo(
                    forventetBPAndelSaertilskuddProsentBarn,
                )
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe).hasSize(2) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].belop).isEqualTo(forventetSamvaersfradragBelopBarn1)
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[1].belop).isEqualTo(forventetSamvaersfradragBelopBarn2)
            },
            Executable { assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe).hasSize(1) },
            Executable {
                assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.belop).isEqualTo(
                    forventetSaertilskuddBelopBarn,
                )
            },
            Executable {
                assertThat(totalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe[0].resultat.kode.toString()).isEqualTo(
                    forventetSaertilskuddResultatkodeBarn,
                )
            },
            Executable { assertThat(referanserIGrunnlagListe).containsAll(alleReferanser) },
        )
    }

    private fun lesFilOgByggRequest(filnavn: String): HttpEntity<String> {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            org.junit.jupiter.api.Assertions.fail<Any>("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return initHttpEntity(json)
    }

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
