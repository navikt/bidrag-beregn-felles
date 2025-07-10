package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
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
internal class KlageOrkestratorTest : FellesTest() {
    private lateinit var filnavnKlageberegningResultat: String
    private lateinit var filnavnPåklagetVedtak: String
    private lateinit var filnavnLøpendeStønad: String
    private lateinit var filnavnEtterfølgendeVedtak1: String
    private lateinit var filnavnEtterfølgendeVedtak2: String

    @Mock
    private lateinit var orkestrator: KlageOrkestrator

    @Mock
    private lateinit var vedtakService: VedtakService

    @BeforeEach
    fun initMock() {
        orkestrator = KlageOrkestrator(vedtakService)
    }

    @Test
    @DisplayName("Klagevedtak dekker perioden fra opprinnelig virkningstidspunkt til inneværende periode - skal overstyre alt")
    fun test01_KlagevedtakFraOpprinneligVedtakstidspunktTilInneværendePeriode() {
        filnavnKlageberegningResultat = "src/test/resources/testfiler/klage_orkestrator/test01_klageberegningResultat.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/klage_orkestrator/test01_påklagetVedtak.json"
        val klageberegningResultat = lesFilOgByggRequest<BeregnetBarnebidragResultat>(filnavnKlageberegningResultat)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        Mockito.`when`(vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()))
            .thenReturn(påklagetVedtak)

        val klageResultat = orkestrator.utførKlageEndelig(
            KlageOrkestratorGrunnlag(
                stønad = stønad,
                klageberegningResultat = klageberegningResultat,
                klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 8)),
                påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
            ),
        )

        assertAll(
            { assertThat(klageResultat).isNotEmpty },
            { assertThat(klageResultat).hasSize(2) },
            { assertThat(klageResultat[0].delvedtak).isTrue() },
            { assertThat(klageResultat[0].klagevedtak).isTrue() },
            { assertThat(klageResultat[0].resultat).isEqualTo(klageberegningResultat) },
            { assertThat(klageResultat[1].delvedtak).isFalse() },
            { assertThat(klageResultat[1].klagevedtak).isFalse() },
            { assertThat(klageResultat[1].resultat).isEqualTo(klageberegningResultat) },
        )
    }

    @Test
    @DisplayName("Klagevedtak dekker opprinnelig beregningsperiode for påklaget vedtak - legge til etterfølgende vedtak")
    fun test02_KlagevedtakDekkerOpprinneligBeregningsperiodeLeggTilEtterfølgendeVedtak() {
        filnavnKlageberegningResultat = "src/test/resources/testfiler/klage_orkestrator/test02_klageberegningResultat.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/klage_orkestrator/test02_påklagetVedtak.json"
        filnavnLøpendeStønad = "src/test/resources/testfiler/klage_orkestrator/test02_løpendeStønad.json"
        filnavnEtterfølgendeVedtak1 = "src/test/resources/testfiler/klage_orkestrator/test02_etterfølgendeVedtak1.json"
        filnavnEtterfølgendeVedtak2 = "src/test/resources/testfiler/klage_orkestrator/test02_etterfølgendeVedtak2.json"
        val klageberegningResultat = lesFilOgByggRequest<BeregnetBarnebidragResultat>(filnavnKlageberegningResultat)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)
        val løpendeStønad = lesFilOgByggRequest<StønadDto>(filnavnLøpendeStønad)
        val etterfølgendeVedtak1 = lesFilOgByggRequest<VedtakDto>(filnavnEtterfølgendeVedtak1)
        val etterfølgendeVedtak2 = lesFilOgByggRequest<VedtakDto>(filnavnEtterfølgendeVedtak2)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        Mockito.`when`(vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()))
            .thenReturn(påklagetVedtak)
        Mockito.`when`(vedtakService.hentVedtak(4703679))
            .thenReturn(etterfølgendeVedtak1)
        Mockito.`when`(vedtakService.hentVedtak(4703680))
            .thenReturn(etterfølgendeVedtak2)
        Mockito.`when`(vedtakService.hentLøpendeStønad(stønad))
            .thenReturn(løpendeStønad)

        val klageResultat = orkestrator.utførKlageEndelig(
            KlageOrkestratorGrunnlag(
                stønad = stønad,
                klageberegningResultat = klageberegningResultat,
                klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 3)),
                påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
            ),
        )

        assertAll(
            { assertThat(klageResultat).isNotEmpty },
            { assertThat(klageResultat).hasSize(4) },
            { assertThat(klageResultat[0].delvedtak).isTrue() },
            { assertThat(klageResultat[0].klagevedtak).isTrue() },
            { assertThat(klageResultat[0].resultat).isEqualTo(klageberegningResultat) },
            { assertThat(klageResultat[1].delvedtak).isTrue() },
            { assertThat(klageResultat[1].klagevedtak).isFalse() },
            { assertThat(klageResultat[2].delvedtak).isTrue() },
            { assertThat(klageResultat[2].klagevedtak).isFalse() },
            { assertThat(klageResultat[3].delvedtak).isFalse() },
            { assertThat(klageResultat[3].klagevedtak).isFalse() },
            { assertThat(klageResultat[3].resultat.beregnetBarnebidragPeriodeListe).isNotEmpty() },
            { assertThat(klageResultat[3].resultat.beregnetBarnebidragPeriodeListe).hasSize(3) },
        )
    }
}
