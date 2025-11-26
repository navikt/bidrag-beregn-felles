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
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.VedtakService
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.AldersjusteringOrchestrator
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.BidragsberegningOrkestrator
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.HentLøpendeBidragService
import no.nav.bidrag.beregn.barnebidrag.service.orkestrering.OmgjøringOrkestrator
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakDtoForBidragsberegning
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønadBidragsberegning
import no.nav.bidrag.beregn.barnebidrag.utils.OmgjøringOrkestratorHelpers
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidrag
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragPeriodeResponse
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragssak
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequestV2
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.OmgjøringOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class BidragsberegningOrkestratorTest : FellesTest() {
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
    private lateinit var bidragsberegningOrkestrator: BidragsberegningOrkestrator
    private lateinit var hentLøpendeBidragService: HentLøpendeBidragService

    @MockK(relaxed = true)
    private lateinit var personConsumer: BeregningPersonConsumer

    @BeforeEach
    fun init() {
        every { identUtils.hentNyesteIdent(any()) }.answers {
            val ident = firstArg<Personident>()
            ident
        }
        barnebidragApi = BeregnBarnebidragApi()
//        vedtakService = VedtakService(
//            vedtakConsumer = vedtakConsumer,
//            stønadConsumer = stønadConsumer,
//            bbmConsumer = bbmConsumer,
//            vedtakFilter = Vedtaksfiltrering(),
//            identUtils = identUtils,
//        )
        val omgjøringOrkestratorHelpers = OmgjøringOrkestratorHelpers(vedtakService, identUtils)
        omgjøringOrkestrator =
            OmgjøringOrkestrator(vedtakService, aldersjusteringOrchestrator, beregnIndeksreguleringApi, omgjøringOrkestratorHelpers)
        hentLøpendeBidragService = HentLøpendeBidragService(vedtakService = vedtakService)
        bidragsberegningOrkestrator = BidragsberegningOrkestrator(
            barnebidragApi = barnebidragApi,
            omgjøringOrkestrator = omgjøringOrkestrator,
            hentLøpendeBidragService = hentLøpendeBidragService,
            personConsumer = personConsumer,
        )
        stubSjablonProvider()
    }

    @Disabled
    @Test
    fun `gi direkte avslag`() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test01_v3_direkte_avslag_bidrag_grunnlag.json"
        val beregnRequest = lesFilOgByggRequestGenerisk<BidragsberegningOrkestratorRequestV2>(filnavnBeregnGrunnlag)

        val beregnResponse = bidragsberegningOrkestrator.utførBidragsberegningV3(beregnRequest)
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

    @Test
    fun `beregn bidrag v3 - 1 BM, 2 søknadsbarn - ingen løpende stønader`() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test01_v3_beregn_bidrag_grunnlag.json"
        val beregnRequest = lesFilOgByggRequestGenerisk<BidragsberegningOrkestratorRequestV2>(filnavnBeregnGrunnlag)

        val beregnResponse = bidragsberegningOrkestrator.utførBidragsberegningV3(beregnRequest)
        printJson(beregnResponse)

        assertSoftly(beregnResponse) {
            grunnlagListe shouldHaveAtLeastSize 1
            resultat shouldHaveAtLeastSize 1
        }
    }

    @Test
    fun `beregn bidrag v3 - 1 BM, 1 søknadsbarn - 1 løpende stønad i bidrag-behandling med annen BM`() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test02_v3_beregn_bidrag_grunnlag.json"
        val beregnRequest = lesFilOgByggRequestGenerisk<BidragsberegningOrkestratorRequestV2>(filnavnBeregnGrunnlag)

        every { vedtakService.hentSisteLøpendeStønader(any()) }.answers {
            listOf(
                LøpendeBidragssak(
                    sak = Saksnummer(SAK_LØPENDE_BIDRAG),
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(KRAVHAVER_LØPENDE_BIDRAG),
                    løpendeBeløp = LØPENDE_BELØP,
                ),
            )
        }

        every { vedtakService.hentAlleStønaderForBidragspliktig(any()) }.answers {
            LøpendeBidragPeriodeResponse(
                listOf(
                    LøpendeBidrag(
                        sak = Saksnummer(SAK_LØPENDE_BIDRAG),
                        type = Stønadstype.BIDRAG,
                        kravhaver = Personident(KRAVHAVER_LØPENDE_BIDRAG),
                        periodeListe = emptyList(),
                    ),
                ),
            )
        }

        every { vedtakService.finnSisteManuelleVedtakForEvnevurdering(any()) }.answers {
            opprettVedtakForStønadBidragsberegning(
                skyldner = SKYLDNER,
                kravhaver = KRAVHAVER_LØPENDE_BIDRAG,
                mottaker = MOTTAKER,
                sak = SAK_LØPENDE_BIDRAG,
                beregnetBeløp = LØPENDE_BELØP,
            )
        }

        every { vedtakService.hentVedtak(any()) }.answers {
            opprettVedtakDtoForBidragsberegning(
                skyldner = SKYLDNER,
                kravhaver = KRAVHAVER_LØPENDE_BIDRAG,
                mottaker = MOTTAKER,
                sak = SAK_LØPENDE_BIDRAG,
                beregnetBeløp = LØPENDE_BELØP,
            )
        }

        val beregnResponse = bidragsberegningOrkestrator.utførBidragsberegningV3(beregnRequest)
        printJson(beregnResponse)

        assertSoftly(beregnResponse) {
            grunnlagListe shouldHaveAtLeastSize 1
            resultat shouldHaveAtLeastSize 1
        }
    }

    @Disabled
    @Test
    @DisplayName("Beregn bidrag")
    fun test01_BeregnBidrag() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test01_barnebidrag_beregning_grunnlag.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.BIDRAG)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 1
            resultatVedtakListe[0].delvedtak shouldBe false
            resultatVedtakListe[0].omgjøringsvedtak shouldBe false
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    @Disabled
    @Test
    @DisplayName("Beregn klage")
    fun test02_BeregnKlage() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test02_klage_beregning_grunnlag.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.OMGJØRING)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 1
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].omgjøringsvedtak shouldBe true
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 77353 i Q1
    @Disabled
    @Test
    @DisplayName("Beregn klage endelig A")
    fun test03A_BeregnKlageEndeligOver12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_etterfølgende_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequestGenerisk<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequestGenerisk<StønadDto>(filnavnBeløpshistorikkNå)
        val beløpshistorikkKlage = lesFilOgByggRequestGenerisk<StønadDto>(filnavnBeløpshistorikkKlage)
        val etterfølgendeVedtak = lesFilOgByggRequestGenerisk<VedtakDto>(filnavnEtterfølgendeVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
        )

        val hentStønadRequest = HentStønadRequest(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
        )

        val hentStønadHistoriskRequest = HentStønadHistoriskRequest(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
            gyldigTidspunkt = påklagetVedtak.vedtakstidspunkt!!.minusSeconds(1),
        )

        every { vedtakConsumer.hentVedtak(påklagetVedtak.vedtaksid) } returns påklagetVedtak
        every { vedtakConsumer.hentVedtak(4934258) } returns etterfølgendeVedtak
        every { stønadConsumer.hentLøpendeStønad(hentStønadRequest) } returns beløpshistorikkNå
        every { stønadConsumer.hentHistoriskeStønader(hentStønadHistoriskRequest) } returns beløpshistorikkKlage

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid,
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                omgjøringOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.OMGJØRING_ENDELIG,
            )

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        val endringErOverGrense = beregningResultat.resultatVedtakListe[0].resultat.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map { it.innhold.endringErOverGrense }
            .first()

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 3
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].omgjøringsvedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe true
            resultatVedtakListe[1].omgjøringsvedtak shouldBe false
            resultatVedtakListe[2].delvedtak shouldBe false
            resultatVedtakListe[2].omgjøringsvedtak shouldBe false
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 4
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp shouldBe BigDecimal.valueOf(4260)
        }

        assertSoftly { endringErOverGrense shouldBe true }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 217310 i Q1
    @Disabled
    @Test
    @DisplayName("Beregn klage endelig B")
    fun test03B_BeregnKlageEndeligOver12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_beløpshistorikk_nå.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequestGenerisk<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequestGenerisk<StønadDto>(filnavnBeløpshistorikkNå)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2300960"),
        )

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid) } returns påklagetVedtak
        every { vedtakService.hentLøpendeStønad(stønad) } returns beløpshistorikkNå

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid,
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                omgjøringOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.OMGJØRING_ENDELIG,
            )

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 2
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].omgjøringsvedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe false
            resultatVedtakListe[1].omgjøringsvedtak shouldBe false
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 77353 i Q1 modifisert
    @Disabled
    @Test
    @DisplayName("Beregn klage endelig C")
    fun test03C_BeregnKlageEndeligUnder12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03C_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03C_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_etterfølgende_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequestGenerisk<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequestGenerisk<StønadDto>(filnavnBeløpshistorikkNå)
        val beløpshistorikkKlage = lesFilOgByggRequestGenerisk<StønadDto>(filnavnBeløpshistorikkKlage)
        val etterfølgendeVedtak = lesFilOgByggRequestGenerisk<VedtakDto>(filnavnEtterfølgendeVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
        )

        val hentStønadRequest = HentStønadRequest(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
        )

        val hentStønadHistoriskRequest = HentStønadHistoriskRequest(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
            gyldigTidspunkt = påklagetVedtak.vedtakstidspunkt!!.minusSeconds(1),
        )

        every { vedtakConsumer.hentVedtak(påklagetVedtak.vedtaksid) } returns påklagetVedtak
        every { vedtakConsumer.hentVedtak(4934258) } returns etterfølgendeVedtak
        every { stønadConsumer.hentLøpendeStønad(hentStønadRequest) } returns beløpshistorikkNå
        every { stønadConsumer.hentHistoriskeStønader(hentStønadHistoriskRequest) } returns beløpshistorikkKlage

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid,
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                omgjøringOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.OMGJØRING_ENDELIG,
            )

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        val endringErOverGrense = beregningResultat.resultatVedtakListe[0].resultat.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map { it.innhold.endringErOverGrense }
            .first()

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 3
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].omgjøringsvedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe true
            resultatVedtakListe[1].omgjøringsvedtak shouldBe false
            resultatVedtakListe[2].delvedtak shouldBe false
            resultatVedtakListe[2].omgjøringsvedtak shouldBe false
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 4
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp shouldBe BigDecimal.valueOf(4300)
        }

        assertSoftly { endringErOverGrense shouldBe false }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    companion object {
        private const val KRAVHAVER_LØPENDE_BIDRAG = "11111111111"
        private const val MOTTAKER = "22222222221"
        private const val SKYLDNER = "33333333330"
        private const val SAK_LØPENDE_BIDRAG = "2"
        private val LØPENDE_BELØP = BigDecimal.valueOf(5000)
    }
}
