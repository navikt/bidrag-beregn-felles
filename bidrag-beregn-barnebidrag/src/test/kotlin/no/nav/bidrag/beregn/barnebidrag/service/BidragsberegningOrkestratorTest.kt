package no.nav.bidrag.beregn.barnebidrag.service

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BidragsberegningOrkestratorTest : FellesTest() {
    private lateinit var filnavnBeregnGrunnlag: String
    private lateinit var filnavnKlageberegningResultat: String
    private lateinit var filnavnPåklagetVedtak: String

    @Mock
    private lateinit var bidragsberegningOrkestrator: BidragsberegningOrkestrator

    @Mock
    private lateinit var barnebidragApi: BeregnBarnebidragApi

    @Mock
    private lateinit var klageOrkestrator: KlageOrkestrator

    @Mock
    private lateinit var vedtakService: VedtakService

    @BeforeEach
    fun initMock() {
        klageOrkestrator = KlageOrkestrator(vedtakService)
        barnebidragApi = BeregnBarnebidragApi()
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

        assertAll(
            { assertThat(beregningResultat.resultatVedtakListe).isNotEmpty },
            { assertThat(beregningResultat.resultatVedtakListe).hasSize(1) },
            { assertThat(beregningResultat.resultatVedtakListe[0].delvedtak).isFalse() },
            { assertThat(beregningResultat.resultatVedtakListe[0].klagevedtak).isFalse() },
        )
    }

    @Test
    @DisplayName("Beregn klage")
    fun test02_BeregnKlage() {
        filnavnBeregnGrunnlag = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_beregning.json"
        val beregnGrunnlag: BeregnGrunnlag = lesFilOgByggRequest(filnavnBeregnGrunnlag)
        val request = BidragsberegningOrkestratorRequest(beregnGrunnlag = beregnGrunnlag, beregningstype = Beregningstype.KLAGE)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)

        assertAll(
            { assertThat(beregningResultat.resultatVedtakListe).isNotEmpty },
            { assertThat(beregningResultat.resultatVedtakListe).hasSize(1) },
            { assertThat(beregningResultat.resultatVedtakListe[0].delvedtak).isTrue() },
            { assertThat(beregningResultat.resultatVedtakListe[0].klagevedtak).isTrue() },
        )
    }

    @Test
    @DisplayName("Beregn klage endelig")
    fun test03_BeregnKlageEndelig() {
        filnavnKlageberegningResultat = "src/test/resources/testfiler/bidragsberegning_orkestrator/klage_beregning_resultat.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/bidragsberegning_orkestrator/påklaget_vedtak.json"
        val klageberegningResultat = lesFilOgByggRequest<BeregnetBarnebidragResultat>(filnavnKlageberegningResultat)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        val klageOrkestratorGrunnlag = KlageOrkestratorGrunnlag(
            stønad = stønad,
            klageberegningResultat = klageberegningResultat,
            klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 8)),
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        )

        Mockito.`when`(vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()))
            .thenReturn(påklagetVedtak)

        val request =
            BidragsberegningOrkestratorRequest(klageOrkestratorGrunnlag = klageOrkestratorGrunnlag, beregningstype = Beregningstype.KLAGE_ENDELIG)

        val beregningResultat = bidragsberegningOrkestrator.utførBidragsberegning(request)

        assertAll(
            { assertThat(beregningResultat.resultatVedtakListe).isNotEmpty },
            { assertThat(beregningResultat.resultatVedtakListe).hasSize(2) },
            { assertThat(beregningResultat.resultatVedtakListe[0].delvedtak).isTrue() },
            { assertThat(beregningResultat.resultatVedtakListe[0].klagevedtak).isTrue() },
            { assertThat(beregningResultat.resultatVedtakListe[1].delvedtak).isFalse() },
            { assertThat(beregningResultat.resultatVedtakListe[1].klagevedtak).isFalse() },
        )
    }
}
