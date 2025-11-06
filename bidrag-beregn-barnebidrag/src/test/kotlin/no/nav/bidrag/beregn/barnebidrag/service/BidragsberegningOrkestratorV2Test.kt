package no.nav.bidrag.beregn.barnebidrag.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.VedtakService
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.AldersjusteringOrchestrator
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.BidragsberegningOrkestratorV2
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.HentLøpendeBidragService
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.OmgjøringOrkestrator
import no.nav.bidrag.beregn.barnebidrag.utils.OmgjøringOrkestratorHelpers
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequestV2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class BidragsberegningOrkestratorV2Test : FellesTest() {
    private lateinit var filnavnBeregnGrunnlag: String
    private lateinit var filnavnPåklagetVedtak: String
    private lateinit var filnavnBeløpshistorikkNå: String
    private lateinit var filnavnBeløpshistorikkKlage: String
    private lateinit var filnavnEtterfølgendeVedtak: String

    @MockK(relaxed = true)
    private lateinit var vedtakConsumer: BeregningVedtakConsumer

    @MockK(relaxed = true)
    private lateinit var stønadConsumer: BeregningBeløpshistorikkConsumer

//    @MockK(relaxed = true)
//    private lateinit var bbmConsumer: BeregningBBMConsumer

    @MockK(relaxed = true)
    private lateinit var identUtils: IdentUtils

    @MockK(relaxed = true)
    private lateinit var aldersjusteringOrchestrator: AldersjusteringOrchestrator

    @MockK(relaxed = true)
    private lateinit var beregnIndeksreguleringApi: BeregnIndeksreguleringApi

    @MockK(relaxed = true)
    private lateinit var vedtakService: VedtakService

    private lateinit var barnebidragApi: BeregnBarnebidragApi
    private lateinit var omgjøringOrkestrator: OmgjøringOrkestrator
    private lateinit var bidragsberegningOrkestratorV2: BidragsberegningOrkestratorV2
    private lateinit var hentLøpendeBidragService: HentLøpendeBidragService

    @BeforeEach
    fun init() {
        every { identUtils.hentNyesteIdent(any()) }.answers {
            val ident = firstArg<Personident>()
            ident
        }
        barnebidragApi = BeregnBarnebidragApi()

        val omgjøringOrkestratorHelpers = OmgjøringOrkestratorHelpers(vedtakService, identUtils)
        omgjøringOrkestrator =
            OmgjøringOrkestrator(vedtakService, aldersjusteringOrchestrator, beregnIndeksreguleringApi, omgjøringOrkestratorHelpers)
        hentLøpendeBidragService = HentLøpendeBidragService(vedtakService = vedtakService)
        bidragsberegningOrkestratorV2 = BidragsberegningOrkestratorV2(
            barnebidragApi = barnebidragApi,
            omgjøringOrkestrator = omgjøringOrkestrator,
            hentLøpendeBidragService = hentLøpendeBidragService,
        )
        stubSjablonProvider()
    }

    @Test
    fun `beregn bidrag v3 - 1 BM, 2 søknadsbarn - ingen løpende stønader`() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test01_v3_beregn_bidrag_grunnlag.json"
        val beregnRequest = lesFilOgByggRequestGenerisk<BidragsberegningOrkestratorRequestV2>(filnavnBeregnGrunnlag)

        val beregnResponse = bidragsberegningOrkestratorV2.utførBidragsberegningV3(beregnRequest)
        printJson(beregnResponse)

        assertSoftly(beregnResponse) {
            grunnlagListe shouldHaveAtLeastSize 1
            resultat shouldHaveSize 2
        }
    }

    @Test
    fun `gi direkte avslag`() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test01_v3_direkte_avslag_bidrag_grunnlag.json"
        val beregnRequest = lesFilOgByggRequestGenerisk<BidragsberegningOrkestratorRequestV2>(filnavnBeregnGrunnlag)

        val beregnResponse = bidragsberegningOrkestratorV2.utførBidragsberegningV3(beregnRequest)
        printJson(beregnResponse)

        assertSoftly(beregnResponse) {
            grunnlagListe shouldHaveAtLeastSize 1
            resultat shouldHaveSize 2
            resultat.all { resultatVedtak ->
                resultatVedtak.resultatVedtakListe.all { vedtak ->
                    vedtak.periodeListe.shouldHaveSize(1)
                    vedtak.periodeListe.all { periode -> periode.resultat.beløp == null }
                }
            } shouldBe true
        }
    }

    companion object {
        private const val KRAVHAVER_LØPENDE_BIDRAG = "11111111111"
        private const val MOTTAKER = "22222222221"
        private const val SKYLDNER = "33333333330"
        private const val SAK_LØPENDE_BIDRAG = "2"
        private val LØPENDE_BELØP = BigDecimal.valueOf(5000)
    }
}
