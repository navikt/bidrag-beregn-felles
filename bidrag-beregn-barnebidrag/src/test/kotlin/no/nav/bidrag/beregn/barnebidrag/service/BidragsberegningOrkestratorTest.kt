package no.nav.bidrag.beregn.barnebidrag.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.beregning.Beregningstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class BidragsberegningOrkestratorTest : FellesTest() {
    private lateinit var filnavnBeregnGrunnlag: String
    private lateinit var filnavnPåklagetVedtak: String
    private lateinit var filnavnLøpendeStønad: String
    private lateinit var filnavnEtterfølgendeVedtak: String

    @MockK(relaxed = true)
    private lateinit var vedtakService: VedtakService

    private lateinit var barnebidragApi: BeregnBarnebidragApi
    private lateinit var klageOrkestrator: KlageOrkestrator
    private lateinit var bidragsberegningOrkestrator: BidragsberegningOrkestrator

    @BeforeEach
    fun initMock() {
        barnebidragApi = BeregnBarnebidragApi()
        klageOrkestrator = KlageOrkestrator(vedtakService)
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
    }

    @Test
    @DisplayName("Beregn klage endelig A")
    fun test03A_BeregnKlageEndelig() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_påklaget_vedtak.json"
        filnavnLøpendeStønad = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_løpende_stønad.json"
        filnavnEtterfølgendeVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03A_etterfølgende_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val løpendeStønad = lesFilOgByggRequest<StønadDto>(filnavnLøpendeStønad)
        val etterfølgendeVedtak = lesFilOgByggRequest<VedtakDto>(filnavnEtterfølgendeVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("1912673"),
        )

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakService.hentVedtak(4934258) } returns etterfølgendeVedtak
        every { vedtakService.hentLøpendeStønad(stønad) } returns løpendeStønad

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
            resultatVedtakListe shouldHaveSize 3
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].klagevedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe true
            resultatVedtakListe[1].klagevedtak shouldBe false
            resultatVedtakListe[2].delvedtak shouldBe false
            resultatVedtakListe[2].klagevedtak shouldBe false
        }
    }

    @Test
    @DisplayName("Beregn klage endelig B")
    fun test03B_BeregnKlageEndelig() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_klage_beregning_grunnlag.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_påklaget_vedtak.json"
        filnavnLøpendeStønad = "src/test/resources/testfiler/bidragsberegning_orkestrator/test03B_løpende_stønad.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val løpendeStønad = lesFilOgByggRequest<StønadDto>(filnavnLøpendeStønad)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2300960"),
        )

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakService.hentLøpendeStønad(stønad) } returns løpendeStønad

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
    }
}
