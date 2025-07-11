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
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BidragsberegningOrkestratorRequest
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.YearMonth

@ExtendWith(MockKExtension::class)
internal class BidragsberegningOrkestratorTest : FellesTest() {
    private lateinit var filnavnBeregnGrunnlag: String
    private lateinit var filnavnKlageberegningResultat: String
    private lateinit var filnavnPåklagetVedtak: String

    @MockK(relaxed = true)
    private lateinit var barnebidragApi: BeregnBarnebidragApi

    @MockK(relaxed = true)
    private lateinit var vedtakService: VedtakService

    private lateinit var klageOrkestrator: KlageOrkestrator
    private lateinit var bidragsberegningOrkestrator: BidragsberegningOrkestrator

    @BeforeEach
    fun initMock() {
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
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/barnebidrag_beregning.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.BIDRAG)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 1
            resultatVedtakListe[0].delvedtak shouldBe false
            resultatVedtakListe[0].klagevedtak shouldBe false
        }
    }

    @Test
    @DisplayName("Beregn klage")
    fun test02_BeregnKlage() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_beregning.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.KLAGE)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 1
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].klagevedtak shouldBe true
        }
    }

    @Test
    @DisplayName("Beregn klage endelig")
    fun test03_BeregnKlageEndelig() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_beregning.json"
        filnavnKlageberegningResultat = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_beregning_resultat.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/påklaget_vedtak.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val klageberegningResultat = lesFilOgByggRequest<BeregnetBarnebidragResultat>(filnavnKlageberegningResultat)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)

        every { barnebidragApi.beregn(any()) } returns klageberegningResultat
        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        val klageOrkestratorGrunnlag = KlageOrkestratorGrunnlag(
            stønad = stønad,
            klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 8)),
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        )

        val request =
            BidragsberegningOrkestratorRequest(
                beregnGrunnlag = beregnGrunnlag,
                klageOrkestratorGrunnlag = klageOrkestratorGrunnlag,
                beregningstype = Beregningstype.KLAGE_ENDELIG,
            )

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)

        assertSoftly(beregningResultat) {
            resultatVedtakListe shouldHaveSize 2
            resultatVedtakListe[0].delvedtak shouldBe true
            resultatVedtakListe[0].klagevedtak shouldBe true
            resultatVedtakListe[1].delvedtak shouldBe false
            resultatVedtakListe[1].klagevedtak shouldBe false
        }
    }
}
