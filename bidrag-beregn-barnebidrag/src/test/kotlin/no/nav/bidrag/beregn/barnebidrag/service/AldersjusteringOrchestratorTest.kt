package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningSakConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningStønadConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettSakRespons
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettStønadDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettStønadPeriodeDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønad
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragsmottaker
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragspliktig
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn1
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn2
import no.nav.bidrag.beregn.barnebidrag.testdata.saksnummer
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.web.mock.hentFil
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.commons.web.mock.stubSjablonService
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.vedtak.response.HentVedtakForStønadResponse
import no.nav.bidrag.transport.felles.commonObjectmapper
import no.nav.bidrag.transport.sak.RolleDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

val vedtaksidBidrag = 2
val vedtaksidForskudd = 1
val vedtaksidForskudd2 = 4
val vedtaksidSærbidrag = 3

@ExtendWith(MockKExtension::class)
class AldersjusteringOrchestratorTest {

    @MockK
    lateinit var bidragSakConsumer: BeregningSakConsumer

    @MockK
    lateinit var beregningVedtakConsumer: BeregningVedtakConsumer

    @MockK
    lateinit var beregningStønadConsumer: BeregningStønadConsumer

    @MockK
    lateinit var personConsumer: BeregningPersonConsumer

    lateinit var bidragVedtakService: VedtakService
    lateinit var aldersjusteringOrchestrator: AldersjusteringOrchestrator

    @BeforeEach
    fun init() {
        stubSjablonService()
        stubSjablonProvider()
        bidragVedtakService = VedtakService(
            vedtakConsumer = beregningVedtakConsumer,
            stønadConsumer = beregningStønadConsumer,
            vedtakFilter = Vedtaksfiltrering(),
        )
        aldersjusteringOrchestrator = AldersjusteringOrchestrator(
            vedtakService = bidragVedtakService,
            sakConsumer = bidragSakConsumer,
            personConsumer = personConsumer,
            barnebidragApi = BeregnBarnebidragApi(),
        )
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns commonObjectmapper.readValue(hentFil("/testfiler/aldersjustering_orkestrator/vedtak_aldersjustering_1.json"))
        every { personConsumer.hentFødselsdatoForPerson(any()) } returns LocalDate.now().minusYears(11)
        every { beregningVedtakConsumer.hentVedtakForStønad(any()) } returns
            HentVedtakForStønadResponse(
                vedtakListe =
                listOf(
                    opprettVedtakForStønad(personIdentBidragspliktig, stønadstype = Stønadstype.FORSKUDD).copy(
                        vedtaksid = 55,
                        type = Vedtakstype.ALDERSJUSTERING,
                        kilde = Vedtakskilde.AUTOMATISK,
                    ),
                    opprettVedtakForStønad(personIdentBidragspliktig, stønadstype = Stønadstype.FORSKUDD).copy(
                        vedtaksid = vedtaksidBidrag.toLong(),
                        type = Vedtakstype.ENDRING,
                    ),
                ),
            )
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().minusMonths(4), null),
                        beløp = BigDecimal("5600"),
                        vedtakId = vedtaksidBidrag,
                    ),
                ),
            )

        every { bidragSakConsumer.hentSak(any()) } returns
            opprettSakRespons()
                .copy(
                    roller =
                    listOf(
                        RolleDto(
                            Personident(personIdentBidragsmottaker),
                            type = Rolletype.BIDRAGSMOTTAKER,
                        ),
                        RolleDto(
                            Personident(personIdentBidragspliktig),
                            type = Rolletype.BIDRAGSPLIKTIG,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn2),
                            type = Rolletype.BARN,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn1),
                            type = Rolletype.BARN,
                        ),
                    ),
                )
    }

    @Test
    fun `skal beregne aldersjustering`() {
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().withMonth(9).minusYears(1), null),
                        beløp = BigDecimal("4800"),
                        vedtakId = vedtaksidBidrag,
                    ),
                ),
            )
        val resultat = aldersjusteringOrchestrator.utførAldersjustering(
            stønad = Stønadsid(
                type = Stønadstype.BIDRAG,
                kravhaver = Personident(personIdentSøknadsbarn1),
                skyldner = Personident(personIdentBidragspliktig),
                sak = Saksnummer(saksnummer),
            ),
            aldersjusteresForÅr = 2025,
        )
        resultat.shouldNotBeNull()

        assertSoftly(resultat) {
            it.beregnetBarnebidragPeriodeListe.shouldHaveSize(1)
            it.beregnetBarnebidragPeriodeListe.first().resultat.beløp shouldBe BigDecimal(5300)
            it.beregnetBarnebidragPeriodeListe.first().periode.fom shouldBe YearMonth.parse("2025-07")
            it.beregnetBarnebidragPeriodeListe.first().periode.til shouldBe null
        }
    }
}
