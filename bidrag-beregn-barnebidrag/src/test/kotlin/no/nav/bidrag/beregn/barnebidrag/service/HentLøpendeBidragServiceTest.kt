package no.nav.bidrag.beregn.barnebidrag.service.orkestrering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.bidrag.beregn.barnebidrag.service.external.VedtakService
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettBidragBeregningResponsDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakDtoForLøpendeBidrag
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønad
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønadBidragsberegning
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragsmottaker
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragspliktig
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn1
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn2
import no.nav.bidrag.beregn.barnebidrag.testdata.saksnummer
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.BidragPeriode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidrag
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragPeriodeResponse
import no.nav.bidrag.transport.behandling.beregning.felles.BidragBeregningResponsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class HentLøpendeBidragServiceTest {
    private lateinit var vedtakService: VedtakService
    private lateinit var hentLøpendeBidragService: HentLøpendeBidragService

    @BeforeEach
    fun setup() {
        vedtakService = mockk()
        hentLøpendeBidragService = HentLøpendeBidragService(vedtakService)
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente løpende bidrag for behandling uten manuelle vedtak`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 1), YearMonth.of(2024, 12))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(Personident(personIdentSøknadsbarn1) to "referanse1")

        val bidragPeriode = BidragPeriode(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
            løpendeBeløp = BigDecimal.valueOf(5000),
            valutakode = "NOK",
        )

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = Personident(personIdentSøknadsbarn1),
            periodeListe = listOf(bidragPeriode),
        )

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(listOf(løpendeBidrag))

        every {
            vedtakService.finnAlleManuelleVedtakForEvnevurdering(any())
        } returns emptyList()

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).hasSize(1) },
            { assertThat(resultat.løpendeBidragListe[0].sak).isEqualTo(Saksnummer(saksnummer)) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe).isEmpty() },
        )

        verify { vedtakService.hentAlleStønaderForBidragspliktig(any()) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente løpende bidrag med manuelle vedtak fra BBM`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 7), YearMonth.of(2025, 6))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(Personident(personIdentSøknadsbarn1) to "referanse1")

        val bidragPeriode = BidragPeriode(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 1)),
            løpendeBeløp = BigDecimal.valueOf(5160),
            valutakode = "NOK",
        )

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = Personident(personIdentSøknadsbarn1),
            periodeListe = listOf(bidragPeriode),
        )

        val vedtakForStønad = opprettVedtakForStønad(
            kravhaver = personIdentSøknadsbarn1,
            stønadstype = Stønadstype.BIDRAG,
        )

        val beregningRespons = opprettBidragBeregningResponsDto(
            kravhaver = personIdentSøknadsbarn1,
            sak = saksnummer,
            beregnetBeløp = BigDecimal.valueOf(5160),
        )

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(listOf(løpendeBidrag))

        every {
            vedtakService.finnAlleManuelleVedtakForEvnevurdering(any())
        } returns listOf(vedtakForStønad)

        every {
            vedtakService.hentAlleBeregningerFraBBM(any())
        } returns BidragBeregningResponsDto(beregningRespons)

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).hasSize(1) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe).hasSize(1) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe[0].beregnetBeløp).isEqualTo(BigDecimal.valueOf(5160)) },
        )

        verify { vedtakService.hentAlleBeregningerFraBBM(any()) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente løpende bidrag med manuelle vedtak fra bidrag-vedtak`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 7), YearMonth.of(2025, 6))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(Personident(personIdentSøknadsbarn1) to "referanse1")
        val beregnetBeløp = BigDecimal.valueOf(5160)
        val resultatBeløp = BigDecimal.valueOf(5000)

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = Personident(personIdentSøknadsbarn1),
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), null),
                    løpendeBeløp = beregnetBeløp,
                    valutakode = "NOK",
                ),
            ),
        )

        val vedtakForStønad = opprettVedtakForStønadBidragsberegning(
            skyldner = personIdentBidragspliktig,
            kravhaver = personIdentSøknadsbarn1,
            mottaker = personIdentBidragsmottaker,
            sak = saksnummer,
            beregnetBeløp = beregnetBeløp,
        )

        val vedtakDto = opprettVedtakDtoForLøpendeBidrag(
            skyldner = personIdentBidragspliktig,
            kravhaver = personIdentSøknadsbarn1,
            mottaker = personIdentBidragsmottaker,
            sak = saksnummer,
            beregnetBeløp = beregnetBeløp,
            resultatBeløp = resultatBeløp,
        )

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(listOf(løpendeBidrag))

        every {
            vedtakService.finnAlleManuelleVedtakForEvnevurdering(any())
        } returns listOf(vedtakForStønad)

        every {
            vedtakService.hentVedtak(vedtakForStønad.vedtaksid)
        } returns vedtakDto

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).hasSize(1) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe).hasSize(1) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe[0].beregnetBeløp).isEqualTo(beregnetBeløp) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe[0].faktiskBeløp).isEqualTo(resultatBeløp) },
            { assertThat(resultat.beregnetBeløpListe.beregningListe[0].samværsklasse).isEqualTo(Samværsklasse.SAMVÆRSKLASSE_0) },
        )

        verify { vedtakService.hentVedtak(vedtakForStønad.vedtaksid) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal filtrere bort perioder utenfor beregningsperioden`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 6), YearMonth.of(2024, 12))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(Personident(personIdentSøknadsbarn1) to "referanse1")

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = Personident(personIdentSøknadsbarn1),
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 5, 1)),
                    løpendeBeløp = BigDecimal.valueOf(3000),
                    valutakode = "NOK",
                ),
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 12, 1)),
                    løpendeBeløp = BigDecimal.valueOf(5000),
                    valutakode = "NOK",
                ),
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2025, 1, 1), null),
                    løpendeBeløp = BigDecimal.valueOf(6000),
                    valutakode = "NOK",
                ),
            ),
        )

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(listOf(løpendeBidrag))

        every {
            vedtakService.finnAlleManuelleVedtakForEvnevurdering(any())
        } returns emptyList()

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).hasSize(1) },
            { assertThat(resultat.løpendeBidragListe[0].periodeListe).hasSize(1) },
            { assertThat(resultat.løpendeBidragListe[0].periodeListe[0].periode.fom).isEqualTo(YearMonth.of(2024, 6)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal returnere tom liste når ingen stønader finnes`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 1), YearMonth.of(2024, 12))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(Personident(personIdentSøknadsbarn1) to "referanse1")

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(emptyList())

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).isEmpty() },
            { assertThat(resultat.beregnetBeløpListe.beregningListe).isEmpty() },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere flere kravhavere med forskjellige vedtak`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 7), YearMonth.of(2025, 6))
        val bidragspliktigIdent = Personident(personIdentBidragspliktig)
        val søknadsbarnidentMap = mapOf(
            Personident(personIdentSøknadsbarn1) to "referanse1",
            Personident(personIdentSøknadsbarn2) to "referanse2",
        )

        val løpendeBidragListe = listOf(
            LøpendeBidrag(
                sak = Saksnummer(saksnummer),
                type = Stønadstype.BIDRAG,
                kravhaver = Personident(personIdentSøknadsbarn1),
                periodeListe = listOf(
                    BidragPeriode(
                        periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), null),
                        løpendeBeløp = BigDecimal.valueOf(5000),
                        valutakode = "NOK",
                    ),
                ),
            ),
            LøpendeBidrag(
                sak = Saksnummer(saksnummer),
                type = Stønadstype.BIDRAG,
                kravhaver = Personident(personIdentSøknadsbarn2),
                periodeListe = listOf(
                    BidragPeriode(
                        periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), null),
                        løpendeBeløp = BigDecimal.valueOf(6000),
                        valutakode = "NOK",
                    ),
                ),
            ),
        )

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(løpendeBidragListe)

        every {
            vedtakService.finnAlleManuelleVedtakForEvnevurdering(any())
        } returns emptyList()

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
            søknadsbarnidentMap,
            beregningsperiode,
        )

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.løpendeBidragListe).hasSize(2) },
            { assertThat(resultat.løpendeBidragListe[0].kravhaver).isEqualTo(Personident(personIdentSøknadsbarn1)) },
            { assertThat(resultat.løpendeBidragListe[1].kravhaver).isEqualTo(Personident(personIdentSøknadsbarn2)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `filtrerVedtakMotBeregningsperiode skal returnere vedtak som dekker starten av beregningsperioden`() {
        val beregningsperiode = ÅrMånedsperiode(YearMonth.of(2024, 7), YearMonth.of(2025, 6))
        val vedtakListe = listOf(
            opprettVedtakForStønadBidragsberegning(
                skyldner = personIdentBidragspliktig,
                kravhaver = personIdentSøknadsbarn1,
                mottaker = personIdentBidragsmottaker,
                sak = saksnummer,
                beregnetBeløp = BigDecimal.valueOf(5000),
            ),
        )

        val resultat = vedtakListe.filtrerVedtakMotBeregningsperiode(beregningsperiode)

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].stønadsendring.periodeListe[0].periode.fom).isEqualTo(YearMonth.of(2024, 7)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `tilGrunnlagDto skal konvertere beregningsresultat til grunnlag`() {
        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = Personident(personIdentSøknadsbarn1),
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), null),
                    løpendeBeløp = BigDecimal.valueOf(5000),
                    valutakode = "NOK",
                ),
            ),
        )

        val beregning = BidragBeregningResponsDto.BidragBeregning(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), null),
            saksnummer = saksnummer,
            personidentBarn = Personident(personIdentSøknadsbarn1),
            datoSøknad = LocalDate.now(),
            beregnetBeløp = BigDecimal.valueOf(5160),
            faktiskBeløp = BigDecimal.valueOf(5000),
            beløpSamvær = BigDecimal.ZERO,
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = Samværsklasse.SAMVÆRSKLASSE_0,
        )

        val evnevurderingResultat = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(listOf(beregning)),
            løpendeBidragListe = listOf(løpendeBidrag),
        )

        val grunnlagListe = evnevurderingResultat.tilGrunnlagDto("bp_ref")

        assertAll(
            { assertThat(grunnlagListe).isNotEmpty() },
            { assertThat(grunnlagListe[0].gjelderReferanse).isEqualTo("bp_ref") },
        )
    }
}
