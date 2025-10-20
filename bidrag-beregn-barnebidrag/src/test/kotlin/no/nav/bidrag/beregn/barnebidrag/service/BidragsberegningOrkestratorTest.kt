package no.nav.bidrag.beregn.barnebidrag.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBBMConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettBidragBeregningResponsDto
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
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragssak
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.OmgjøringOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
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

    @MockK(relaxed = true)
    private lateinit var bbmConsumer: BeregningBBMConsumer

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
        omgjøringOrkestrator = OmgjøringOrkestrator(
            vedtakService = vedtakService,
            aldersjusteringOrchestrator = aldersjusteringOrchestrator,
            beregnIndeksreguleringApi = beregnIndeksreguleringApi,
            omgjøringOrkestratorHelpers = omgjøringOrkestratorHelpers,
        )
        bidragsberegningOrkestrator = BidragsberegningOrkestrator(
            barnebidragApi = barnebidragApi,
            omgjøringOrkestrator = omgjøringOrkestrator,
            vedtakService = vedtakService,
        )
        stubSjablonProvider()
    }

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, ingen andre løpende bidrag")
    fun beregnBidrag_test01() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/bidrag_test01_grunnlag.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(
            beregnGrunnlagListe = listOf(beregnGrunnlag),
        )

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

    @Test
    @DisplayName("Beregn bidrag - 2 søknadsbarn, ingen andre løpende bidrag, BP har  full evne")
    fun beregnBidrag_test02() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/bidrag_test02_grunnlag.json"
        val beregnGrunnlag: List<BeregnGrunnlag> = lesFilOgByggRequestListe(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(
            beregnGrunnlagListe = beregnGrunnlag,
        )

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

    @Test
    @DisplayName("Beregn bidrag - 2 søknadsbarn, ingen andre løpende bidrag, BP har ikke full evne")
    @Disabled
    fun beregnBidrag_test03() {
        // TODO
    }

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag i bidrag-behandling, BP har full evne")
    fun beregnBidrag_test04A() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/bidrag_test04_grunnlag.json"

        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(
            beregnGrunnlagListe = listOf(beregnGrunnlag),
        )

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

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag i BBM, BP har full evne")
    @Disabled
    fun beregnBidrag_test04B() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/bidrag_test04_grunnlag.json"

        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(
            beregnGrunnlagListe = listOf(beregnGrunnlag),
        )

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

        every { vedtakService.finnSisteManuelleVedtakForEvnevurdering(any()) }.answers {
            opprettVedtakForStønadBidragsberegning(
                skyldner = SKYLDNER,
                kravhaver = KRAVHAVER_LØPENDE_BIDRAG,
                mottaker = MOTTAKER,
                sak = SAK_LØPENDE_BIDRAG,
                beregnetBeløp = LØPENDE_BELØP,
            )
        }

        every { vedtakService.hentBeregningFraBBM(any()) }.answers {
            BidragBeregningResponsDto(
                opprettBidragBeregningResponsDto(
                    kravhaver = KRAVHAVER_LØPENDE_BIDRAG,
                    sak = SAK_LØPENDE_BIDRAG,
                    beregnetBeløp = LØPENDE_BELØP,
                ),
            )
        }

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

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag i bidrag-behandling, BP har ikke full evne")
    @Disabled
    fun beregnBidrag_test05A() {
        // TODO
    }

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag i BBM, BP har ikke full evne")
    @Disabled
    fun beregnBidrag_test05B() {
        // TODO
    }

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag eksisterer, komplette grunnlag mottatt, BP har full evne")
    @Disabled
    fun beregnBidrag_test06() {
        // TODO
    }

    @Test
    @DisplayName("Beregn bidrag - 1 søknadsbarn, løpende bidrag eksisterer, komplette grunnlag mottatt, BP har ikke full evne")
    @Disabled
    fun beregnBidrag_test07() {
        // TODO
    }

    @Test
    @DisplayName("Beregn klage")
    fun beregnKlage_test01() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test01_grunnlag.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(
            beregnGrunnlagListe = listOf(beregnGrunnlag),
            beregningstype = Beregningstype.OMGJØRING,
        )

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
    @Test
    @DisplayName("Beregn klage endelig over 12 prosent")
    @Disabled
    fun beregnKlage_test02() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_etterfølgende_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequest<StønadDto>(filnavnBeløpshistorikkNå)
        val beløpshistorikkKlage = lesFilOgByggRequest<StønadDto>(filnavnBeløpshistorikkKlage)
        val etterfølgendeVedtak = lesFilOgByggRequest<VedtakDto>(filnavnEtterfølgendeVedtak)

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

        every { vedtakConsumer.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakConsumer.hentVedtak(4934258) } returns etterfølgendeVedtak
        every { stønadConsumer.hentLøpendeStønad(hentStønadRequest) } returns beløpshistorikkNå
        every { stønadConsumer.hentHistoriskeStønader(hentStønadHistoriskRequest) } returns beløpshistorikkKlage

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid.toInt(),
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlagListe = listOf(beregnGrunnlag),
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
    @Test
    @DisplayName("Beregn klage endelig over 12 prosent")
    @Disabled
    fun beregnKlage_test03() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test03_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test03_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test03_beløpshistorikk_nå.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequest<StønadDto>(filnavnBeløpshistorikkNå)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2300960"),
        )

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakService.hentLøpendeStønad(stønad) } returns beløpshistorikkNå

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid.toInt(),
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlagListe = listOf(beregnGrunnlag),
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
    @Test
    @DisplayName("Beregn klage endelig under 12 prosent")
    @Disabled
    fun beregnKlage_test04() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test04_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test04_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_test02_etterfølgende_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val beløpshistorikkNå = lesFilOgByggRequest<StønadDto>(filnavnBeløpshistorikkNå)
        val beløpshistorikkKlage = lesFilOgByggRequest<StønadDto>(filnavnBeløpshistorikkKlage)
        val etterfølgendeVedtak = lesFilOgByggRequest<VedtakDto>(filnavnEtterfølgendeVedtak)

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

        every { vedtakConsumer.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakConsumer.hentVedtak(4934258) } returns etterfølgendeVedtak
        every { stønadConsumer.hentLøpendeStønad(hentStønadRequest) } returns beløpshistorikkNå
        every { stønadConsumer.hentHistoriskeStønader(hentStønadHistoriskRequest) } returns beløpshistorikkKlage

        val klageOrkestratorGrunnlag = OmgjøringOrkestratorGrunnlag(
            stønad = stønad,
            omgjørVedtakId = påklagetVedtak.vedtaksid.toInt(),
            skalInnkreves = false,
            erBeregningsperiodeLøpende = false,
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlagListe = listOf(beregnGrunnlag),
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

    private fun finnBPIdent(beregnGrunnlag: BeregnGrunnlag): Personident = beregnGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_BIDRAGSPLIKTIG)
        .first()
        .innhold.ident ?: throw NoSuchElementException("BP mangler i input")

    private fun finnSøknadsbarnIdent(beregnGrunnlag: BeregnGrunnlag): Personident = beregnGrunnlag.grunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<Person>(Grunnlagstype.PERSON_SØKNADSBARN)
        .first()
        .innhold.ident ?: throw NoSuchElementException("Søknadsbarn mangler i input")

    companion object {
        private const val KRAVHAVER = "11111111110"
        private const val KRAVHAVER_LØPENDE_BIDRAG = "11111111111"
        private const val MOTTAKER = "22222222222"
        private const val SKYLDNER = "22222222222"
        private const val SAK_LØPENDE_BIDRAG = "1"
        private val LØPENDE_BELØP = BigDecimal.valueOf(5000)
    }
}
