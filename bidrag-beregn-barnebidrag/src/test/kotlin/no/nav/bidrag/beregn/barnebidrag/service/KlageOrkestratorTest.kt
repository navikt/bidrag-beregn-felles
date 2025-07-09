package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Paths
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class KlageOrkestratorTest {
    private lateinit var filnavnKlageBeregning: String
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
        filnavnKlageBeregning = "src/test/resources/testfiler/klage_orkestrator/test01_klageBeregning.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/klage_orkestrator/test01_påklagetVedtak.json"
        val klageBeregning: BeregnetBarnebidragResultat = lesFilOgByggRequest(filnavnKlageBeregning)
        val påklagetVedtak: VedtakDto = lesFilOgByggRequest(filnavnPåklagetVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        Mockito.`when`(vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()))
            .thenReturn(påklagetVedtak)

        val klageResultat = orkestrator.utførKlageEndelig(
            stønad = stønad,
            klageBeregning = klageBeregning,
            klagePeriode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 8)),
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        ).sortedByDescending { it.delvedtak }

        assertAll(
            { assertThat(klageResultat).isNotEmpty },
            { assertThat(klageResultat).hasSize(2) },
            { assertThat(klageResultat[0].delvedtak).isTrue() },
            { assertThat(klageResultat[0].klagevedtak).isTrue() },
            { assertThat(klageResultat[0].resultat).isEqualTo(klageBeregning) },
            { assertThat(klageResultat[1].delvedtak).isFalse() },
            { assertThat(klageResultat[1].klagevedtak).isFalse() },
            { assertThat(klageResultat[1].resultat).isEqualTo(klageBeregning) },
        )
    }

    @Test
    @DisplayName("Klagevedtak dekker opprinnelig beregningsperiode for påklaget vedtak - legge til etterfølgende vedtak")
    fun test02_KlagevedtakDekkerOpprinneligBeregningsperiodeLeggTilEtterfølgendeVedtak() {
        filnavnKlageBeregning = "src/test/resources/testfiler/klage_orkestrator/test02_klageBeregning.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/klage_orkestrator/test02_påklagetVedtak.json"
        filnavnLøpendeStønad = "src/test/resources/testfiler/klage_orkestrator/test02_løpendeStønad.json"
        filnavnEtterfølgendeVedtak1 = "src/test/resources/testfiler/klage_orkestrator/test02_etterfølgendeVedtak1.json"
        filnavnEtterfølgendeVedtak2 = "src/test/resources/testfiler/klage_orkestrator/test02_etterfølgendeVedtak2.json"
        val klageBeregning: BeregnetBarnebidragResultat = lesFilOgByggRequest(filnavnKlageBeregning)
        val påklagetVedtak: VedtakDto = lesFilOgByggRequest(filnavnPåklagetVedtak)
        val løpendeStønad: StønadDto = lesFilOgByggRequest(filnavnLøpendeStønad)
        val etterfølgendeVedtak1: VedtakDto = lesFilOgByggRequest(filnavnEtterfølgendeVedtak1)
        val etterfølgendeVedtak2: VedtakDto = lesFilOgByggRequest(filnavnEtterfølgendeVedtak2)

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
            stønad = stønad,
            klageBeregning = klageBeregning,
            klagePeriode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 3)),
            påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
        ).sortedByDescending { it.delvedtak }

        assertAll(
            { assertThat(klageResultat).isNotEmpty },
            { assertThat(klageResultat).hasSize(4) },
            { assertThat(klageResultat[0].delvedtak).isTrue() },
            { assertThat(klageResultat[0].klagevedtak).isTrue() },
            { assertThat(klageResultat[0].resultat).isEqualTo(klageBeregning) },
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

    inline fun <reified T> lesFilOgByggRequest(filnavn: String): T {
        val json = try {
            Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn", e)
        }

        return jacksonObjectMapper().findAndRegisterModules().readValue(json, T::class.java)
    }
}
