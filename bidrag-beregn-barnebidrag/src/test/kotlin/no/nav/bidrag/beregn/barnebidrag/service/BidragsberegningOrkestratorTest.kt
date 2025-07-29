package no.nav.bidrag.beregn.barnebidrag.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningBeløpshistorikkConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
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
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
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
@Disabled
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
    private lateinit var identUtils: IdentUtils

    @MockK(relaxed = true)
    private lateinit var aldersjusteringOrchestrator: AldersjusteringOrchestrator

    @MockK(relaxed = true)
    private lateinit var beregnIndeksreguleringApi: BeregnIndeksreguleringApi

    private lateinit var barnebidragApi: BeregnBarnebidragApi
    private lateinit var klageOrkestrator: KlageOrkestrator
    private lateinit var bidragsberegningOrkestrator: BidragsberegningOrkestrator
    private lateinit var vedtakService: VedtakService

    @BeforeEach
    fun init() {
        every { identUtils.hentNyesteIdent(any()) }.answers {
            val ident = firstArg<Personident>()
            ident
        }
        barnebidragApi = BeregnBarnebidragApi()
        vedtakService = VedtakService(
            vedtakConsumer = vedtakConsumer,
            stønadConsumer = stønadConsumer,
            vedtakFilter = Vedtaksfiltrering(),
            identUtils = identUtils,
        )
        klageOrkestrator = KlageOrkestrator(vedtakService, aldersjusteringOrchestrator, beregnIndeksreguleringApi, identUtils)
        bidragsberegningOrkestrator = BidragsberegningOrkestrator(
            barnebidragApi = barnebidragApi,
            klageOrkestrator = klageOrkestrator,
        )
        stubSjablonProvider()
    }

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
            resultatVedtakListe[0].klagevedtak shouldBe false
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    @Test
    @DisplayName("Beregn klage")
    fun test02_BeregnKlage() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test02_klage_beregning_grunnlag.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.KLAGE)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 1
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].klagevedtak shouldBe true
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 77353 i Q1
    @Test
    @DisplayName("Beregn klage endelig A")
    fun test03A_BeregnKlageEndeligOver12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_etterfølgende_vedtak.json"
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

        val klageOrkestratorGrunnlag = KlageOrkestratorGrunnlag(
            stønad = stønad,
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.KLAGE_ENDELIG,
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
            resultatVedtakListe[0].klagevedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe true
            resultatVedtakListe[1].klagevedtak shouldBe false
            resultatVedtakListe[2].delvedtak shouldBe false
            resultatVedtakListe[2].klagevedtak shouldBe false
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 4
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp shouldBe BigDecimal.valueOf(4260)
        }

        assertSoftly { endringErOverGrense shouldBe true }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 217310 i Q1
    @Test
    @DisplayName("Beregn klage endelig B")
    fun test03B_BeregnKlageEndeligOver12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_beløpshistorikk_nå.json"
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

        val klageOrkestratorGrunnlag = KlageOrkestratorGrunnlag(
            stønad = stønad,
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.KLAGE_ENDELIG,
            )

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)
        printJson(beregningResultat)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 2
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].klagevedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe false
            resultatVedtakListe[1].klagevedtak shouldBe false
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }

    // Stønad 77353 i Q1 modifisert
    @Test
    @DisplayName("Beregn klage endelig C")
    fun test03C_BeregnKlageEndeligUnder12Prosent() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_påklaget_vedtak.json"
        filnavnBeløpshistorikkNå = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03C_beløpshistorikk_nå.json"
        filnavnBeløpshistorikkKlage = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03C_beløpshistorikk_klage.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_etterfølgende_vedtak.json"
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

        val klageOrkestratorGrunnlag = KlageOrkestratorGrunnlag(
            stønad = stønad,
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.KLAGE_ENDELIG,
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
            resultatVedtakListe[0].klagevedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe true
            resultatVedtakListe[1].klagevedtak shouldBe false
            resultatVedtakListe[2].delvedtak shouldBe false
            resultatVedtakListe[2].klagevedtak shouldBe false
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 4
            resultatVedtakListe[2].resultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp shouldBe BigDecimal.valueOf(4300)
        }

        assertSoftly { endringErOverGrense shouldBe false }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(beregningResultat.resultatVedtakListe)
    }
}
