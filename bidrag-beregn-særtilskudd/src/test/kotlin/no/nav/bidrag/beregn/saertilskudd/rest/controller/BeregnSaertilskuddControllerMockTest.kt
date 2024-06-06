package no.nav.bidrag.beregn.saertilskudd.rest.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.saertilskudd.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest
import no.nav.bidrag.beregn.saertilskudd.rest.BidragBeregnSaertilskuddTest.Companion.TEST_PROFILE
import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil
import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil.BIDRAGSEVNE_REFERANSE
import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil.BPS_ANDEL_SAERTILSKUDD_REFERANSE
import no.nav.bidrag.beregn.saertilskudd.rest.TestUtil.SAMVAERSFRADRAG_REFERANSE
import no.nav.bidrag.beregn.saertilskudd.rest.service.BeregnSaertilskuddService
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.domain.enums.resultatkoder.ResultatKodeSaertilskudd
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.felles.Periode
import no.nav.bidrag.transport.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
import no.nav.bidrag.transport.beregning.saertilskudd.ResultatBeregning
import no.nav.bidrag.transport.beregning.saertilskudd.ResultatPeriode
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("BeregnSaertilskuddControllerMockTest")
@SpringBootTest(classes = [BidragBeregnSaertilskuddTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8096)
@ActiveProfiles(TEST_PROFILE)
@EnableMockOAuth2Server
@ExtendWith(SpringExtension::class)
internal class BeregnSaertilskuddControllerMockTest {
    @Autowired
    private val httpHeaderTestRestTemplate: HttpHeaderTestRestTemplate? = null

    @LocalServerPort
    private val port = 0

    @MockkBean
    lateinit var beregnSaertilskuddServiceMock: BeregnSaertilskuddService

    @Test
    @DisplayName("Skal returnere total særtilskudd resultat ved gyldig input")
    fun skalReturnereTotalSaertilskuddResultatVedGyldigInput() {
        every { beregnSaertilskuddServiceMock.beregn(any()) } returns
            HttpResponse.Companion.from(
                HttpStatus.OK,
                BeregnetTotalSaertilskuddResultat(
                    mapFraResultatPeriodeCore(
                        arrayListOf(
                            ResultatPeriodeCore(
                                PeriodeCore(
                                    LocalDate.parse("2020-08-01"),
                                    LocalDate.parse("2020-09-01"),
                                ),
                                1,
                                ResultatBeregningCore(
                                    BigDecimal.valueOf(100),
                                    "SAERTILSKUDD_INNVILGET",
                                ),
                                arrayListOf(
                                    BIDRAGSEVNE_REFERANSE,
                                    BPS_ANDEL_SAERTILSKUDD_REFERANSE,
                                    SAMVAERSFRADRAG_REFERANSE,
                                ),
                            ),
                        ),
                    ),
                    arrayListOf(
                        TestUtil.dummyBidragsevneResultat(),
                        TestUtil.dummyBPsAndelSaertilskuddResultat(),
                        TestUtil.dummySamvaersfradragResultat(),
                    ),
                ),
            )

        val url = "http://localhost:$port/beregn/saertilskudd"
        val request = initHttpEntity(TestUtil.byggTotalSaertilskuddGrunnlag())
        val responseEntity =
            httpHeaderTestRestTemplate!!.exchange(
                url,
                HttpMethod.POST,
                request,
                BeregnetTotalSaertilskuddResultat::class.java,
            )
        val totalSaertilskuddResultat = responseEntity.body
        val saertilskuddDelberegningResultat = totalSaertilskuddResultat?.let { SaertilskuddDelberegningResultat(it) }
        assertAll(
            Executable { assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(totalSaertilskuddResultat).isNotNull() },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe).hasSize(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].datoFom).isEqualTo(LocalDate.parse("2020-08-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].datoTil).isEqualTo(LocalDate.parse("2020-09-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bidragsevneListe[0].belop).isEqualByComparingTo(BigDecimal.valueOf(100))
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe).hasSize(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].datoFom)
                    .isEqualTo(LocalDate.parse("2020-08-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].datoTil)
                    .isEqualTo(LocalDate.parse("2020-09-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].prosent)
                    .isEqualByComparingTo(BigDecimal.valueOf(10))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.bpsAndelSaertilskuddListe[0].belop)
                    .isEqualByComparingTo(BigDecimal.valueOf(100))
            },
            Executable { assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe).hasSize(1) },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].datoFom).isEqualTo(LocalDate.parse("2020-08-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].datoTil).isEqualTo(LocalDate.parse("2020-09-01"))
            },
            Executable {
                assertThat(saertilskuddDelberegningResultat!!.samvaersfradragListe[0].belop).isEqualByComparingTo(BigDecimal.valueOf(100))
            },
            Executable { assertThat(totalSaertilskuddResultat!!.beregnetSaertilskuddPeriodeListe).hasSize(1) },
            Executable {
                assertThat(totalSaertilskuddResultat!!.beregnetSaertilskuddPeriodeListe[0].periode.datoFom).isEqualTo(
                    LocalDate.parse("2020-08-01"),
                )
            },
            Executable {
                assertThat(totalSaertilskuddResultat!!.beregnetSaertilskuddPeriodeListe[0].periode.datoTil).isEqualTo(
                    LocalDate.parse("2020-09-01"),
                )
            },
            Executable {
                assertThat(totalSaertilskuddResultat!!.beregnetSaertilskuddPeriodeListe[0].resultat.belop).isEqualByComparingTo(
                    BigDecimal.valueOf(100),
                )
            },
            Executable {
                assertThat(
                    totalSaertilskuddResultat!!.beregnetSaertilskuddPeriodeListe[0].resultat.kode,
                ).isEqualTo(ResultatKodeSaertilskudd.SAERTILSKUDD_INNVILGET)
            },
        )
    }

    private fun mapFraResultatPeriodeCore(resultatPeriodeCoreListe: List<ResultatPeriodeCore>): List<ResultatPeriode> {
        return resultatPeriodeCoreListe.map {
            ResultatPeriode(
                barn = it.soknadsbarnPersonId,
                periode = Periode(it.periode.datoFom, it.periode.datoTil),
                resultat = ResultatBeregning(it.resultatBeregning.belop, ResultatKodeSaertilskudd.valueOf(it.resultatBeregning.kode)),
                grunnlagReferanseListe = it.grunnlagReferanseListe,
            )
        }.toList()
    }

    @Test
    @DisplayName("Skal returnere 400 Bad Request når input data mangler")
    fun skalReturnere400BadRequestNaarInputDataMangler() {
        every { beregnSaertilskuddServiceMock.beregn(any()) } returns
            HttpResponse.Companion.from(
                BAD_REQUEST,
                BeregnetTotalSaertilskuddResultat(),
            )

        val url = "http://localhost:$port/beregn/saertilskudd"
        val request = initHttpEntity(BeregnGrunnlag(LocalDate.parse("2021-08-18"), LocalDate.parse("2021-08-18"), emptyList()))
        val responseEntity =
            httpHeaderTestRestTemplate?.exchange(
                url,
                HttpMethod.POST,
                request,
                BeregnetTotalSaertilskuddResultat::class.java,
            )
        val totalSaertilskuddResultat = responseEntity?.body
        assertAll(
            Executable { assertThat(responseEntity?.statusCode).isEqualTo(BAD_REQUEST) },
            Executable { assertThat(totalSaertilskuddResultat).isEqualTo(BeregnetTotalSaertilskuddResultat()) },
        )
    }

    @Test
    @DisplayName("Skal returnere 500 Internal Server Error når kall til servicen feiler")
    fun skalReturnere500InternalServerErrorNaarKallTilServicenFeiler() {
        every { beregnSaertilskuddServiceMock.beregn(any()) } returns
            HttpResponse.Companion.from(
                INTERNAL_SERVER_ERROR,
                BeregnetTotalSaertilskuddResultat(),
            )

        val url = "http://localhost:$port/beregn/saertilskudd"
        val request = initHttpEntity(BeregnGrunnlag(LocalDate.parse("2021-08-18"), LocalDate.parse("2021-08-18"), emptyList()))
        val responseEntity =
            httpHeaderTestRestTemplate?.exchange(
                url,
                HttpMethod.POST,
                request,
                BeregnetTotalSaertilskuddResultat::class.java,
            )
        val totalSaertilskuddResultat = responseEntity?.body
        assertAll(
            Executable { assertThat(responseEntity?.statusCode).isEqualTo(INTERNAL_SERVER_ERROR) },
            Executable { assertThat(totalSaertilskuddResultat).isEqualTo(BeregnetTotalSaertilskuddResultat()) },
        )
    }

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        return HttpEntity(body, httpHeaders)
    }
}
