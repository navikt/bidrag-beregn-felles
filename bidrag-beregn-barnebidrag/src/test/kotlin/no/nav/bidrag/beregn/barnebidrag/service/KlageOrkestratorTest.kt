package no.nav.bidrag.beregn.barnebidrag.service

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.KlageOrkestratorGrunnlag
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.YearMonth

@ExtendWith(MockKExtension::class)
internal class KlageOrkestratorTest : FellesTest() {
    private lateinit var filnavnKlageberegningResultat: String
    private lateinit var filnavnPåklagetVedtak: String
    private lateinit var filnavnLøpendeStønad: String
    private lateinit var filnavnEtterfølgendeVedtak1: String
    private lateinit var filnavnEtterfølgendeVedtak2: String

    private lateinit var orkestrator: KlageOrkestrator

    @MockK(relaxed = true)
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

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak

        val klageResultat = orkestrator.utførKlageEndelig(
            klageberegningResultat = klageberegningResultat,
            grunnlag = KlageOrkestratorGrunnlag(
                stønad = stønad,
                klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 8)),
                påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
            ),
        )

        printJson(klageResultat)

        assertSoftly(klageResultat) {
            it shouldHaveSize 2
            it[0].delvedtak shouldBe true
            it[0].klagevedtak shouldBe true
            it[0].resultat shouldBe klageberegningResultat
            it[1].delvedtak shouldBe false
            it[1].klagevedtak shouldBe false
            it[1].resultat shouldBe klageberegningResultat
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(klageResultat)
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

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak
        every { vedtakService.hentVedtak(4703679) } returns etterfølgendeVedtak1
        every { vedtakService.hentVedtak(4703680) } returns etterfølgendeVedtak2
        every { vedtakService.hentLøpendeStønad(stønad) } returns løpendeStønad

        val klageResultat = orkestrator.utførKlageEndelig(
            klageberegningResultat = klageberegningResultat,
            grunnlag = KlageOrkestratorGrunnlag(
                stønad = stønad,
                klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 3)),
                påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
            ),
        )

        printJson(klageResultat)

        assertSoftly(klageResultat) {
            it shouldHaveSize 4

            it[0].delvedtak shouldBe true
            it[0].klagevedtak shouldBe true
            it[0].resultat shouldBe klageberegningResultat
            it[0].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 1
            it[0].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2024, 8), null)

            it[1].delvedtak shouldBe true
            it[1].klagevedtak shouldBe false
            it[1].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 1
            it[1].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2025, 2), null)
            it[1].resultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe shouldHaveSize 1
            it[1].resultat.grunnlagListe shouldHaveSize 1
            it[1].resultat.grunnlagListe[0].type shouldBe Grunnlagstype.RESULTAT_FRA_VEDTAK

            it[2].delvedtak shouldBe true
            it[2].klagevedtak shouldBe false
            it[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 1
            it[2].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2025, 6), null)
            it[1].resultat.beregnetBarnebidragPeriodeListe[0].grunnlagsreferanseListe shouldHaveSize 1
            it[1].resultat.grunnlagListe shouldHaveSize 1
            it[1].resultat.grunnlagListe[0].type shouldBe Grunnlagstype.RESULTAT_FRA_VEDTAK

            it[3].delvedtak shouldBe false
            it[3].klagevedtak shouldBe false
            it[3].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 3
            it[3].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2025, 2))
            it[3].resultat.beregnetBarnebidragPeriodeListe[1].periode shouldBe ÅrMånedsperiode(YearMonth.of(2025, 2), YearMonth.of(2025, 6))
            it[3].resultat.beregnetBarnebidragPeriodeListe[2].periode shouldBe ÅrMånedsperiode(YearMonth.of(2025, 6), null)
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(klageResultat)
    }

    @Test
    @DisplayName("Virkningstidspunkt for klage er flyttet fram - skal opphøre tidligere perioder")
    fun test03_VirkningstidspunktForKlageErFlyttetFramSkalOpphøreTidligerePerioder() {
        filnavnKlageberegningResultat = "src/test/resources/testfiler/klage_orkestrator/test03_klageberegningResultat.json"
        filnavnPåklagetVedtak = "src/test/resources/testfiler/klage_orkestrator/test03_påklagetVedtak.json"
        val klageberegningResultat = lesFilOgByggRequest<BeregnetBarnebidragResultat>(filnavnKlageberegningResultat)
        val påklagetVedtak = lesFilOgByggRequest<VedtakDto>(filnavnPåklagetVedtak)

        val stønad = Stønadsid(
            type = Stønadstype.BIDRAG,
            kravhaver = Personident("33333333333"),
            skyldner = Personident("11111111111"),
            sak = Saksnummer("2500292"),
        )

        every { vedtakService.hentVedtak(påklagetVedtak.vedtaksid.toInt()) } returns påklagetVedtak

        val klageResultat = orkestrator.utførKlageEndelig(
            klageberegningResultat = klageberegningResultat,
            grunnlag = KlageOrkestratorGrunnlag(
                stønad = stønad,
                klageperiode = ÅrMånedsperiode(YearMonth.of(2024, 12), YearMonth.of(2025, 8)),
                påklagetVedtakId = påklagetVedtak.vedtaksid.toInt(),
            ),
        )

        printJson(klageResultat)

        assertSoftly(klageResultat) {
            it shouldHaveSize 3

            it[0].delvedtak shouldBe true
            it[0].klagevedtak shouldBe false
            it[0].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 1
            it[0].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2024, 12))
            it[0].resultat.beregnetBarnebidragPeriodeListe[0].resultat.beløp shouldBe null
            it[0].resultat.grunnlagListe shouldHaveSize 0

            it[1].delvedtak shouldBe true
            it[1].klagevedtak shouldBe true
            it[1].resultat shouldBe klageberegningResultat

            it[2].delvedtak shouldBe false
            it[2].klagevedtak shouldBe false
            it[2].resultat.beregnetBarnebidragPeriodeListe shouldHaveSize 2
            it[2].resultat.beregnetBarnebidragPeriodeListe[0].periode shouldBe ÅrMånedsperiode(YearMonth.of(2024, 8), YearMonth.of(2024, 12))
            it[2].resultat.beregnetBarnebidragPeriodeListe[1].periode shouldBe ÅrMånedsperiode(YearMonth.of(2024, 12), null)
        }

        // Sjekk at alle referanser er med i resultatet
        sjekkReferanser(klageResultat)
    }

    private fun sjekkReferanser(klageResultat: List<ResultatVedtak>) {
        val alleReferanser = hentAlleReferanser(klageResultat.last().resultat.grunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = klageResultat.last().resultat.grunnlagListe,
            barnebidragResultat = klageResultat.last().resultat,
        )

        assertSoftly {
            alleReferanser.containsAll(alleRefererteReferanser)
            alleRefererteReferanser.containsAll(alleReferanser)
        }
    }
}
