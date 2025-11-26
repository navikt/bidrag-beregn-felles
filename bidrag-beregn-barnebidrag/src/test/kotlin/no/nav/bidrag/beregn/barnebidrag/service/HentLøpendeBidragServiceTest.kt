package no.nav.bidrag.beregn.barnebidrag.service.orkestrering

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.bidrag.beregn.barnebidrag.service.external.VedtakService
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettBidragBeregningResponsDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakDtoForLøpendeBidrag
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønad
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønadBidragsberegning
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentAnnetbarn
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragsmottaker
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragspliktig
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn1
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn2
import no.nav.bidrag.beregn.barnebidrag.testdata.saksnummer
import no.nav.bidrag.beregn.barnebidrag.testdata.saksnummer2
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
import kotlin.collections.get
import kotlin.text.contains

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

        every {
            vedtakService.hentAlleStønaderForBidragspliktig(any())
        } returns LøpendeBidragPeriodeResponse(emptyList())

        val resultat = hentLøpendeBidragService.hentLøpendeBidragForBehandling(
            bidragspliktigIdent,
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
    fun `skal konvertere løpende bidrag til beregngrunnlag for søknadsbarn`() {
        val bpReferanse = "person_PERSON_BIDRAGSPLIKTIG_12345"
        val søknadsbarnIdent = Personident(personIdentSøknadsbarn1)
        val søknadsbarnReferanse = "person_PERSON_SØKNADSBARN_${søknadsbarnIdent.verdi}"
        val søknadsbarnIdentMap = mapOf(søknadsbarnIdent to søknadsbarnReferanse)
        val løpendeBarnFødselsdatoMap = emptyMap<Personident, LocalDate?>()

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = søknadsbarnIdent,
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
                    løpendeBeløp = BigDecimal.valueOf(5000),
                    valutakode = "NOK",
                ),
            ),
        )

        val beregning = BidragBeregningResponsDto.BidragBeregning(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
            saksnummer = saksnummer,
            personidentBarn = søknadsbarnIdent,
            datoSøknad = LocalDate.now(),
            beregnetBeløp = BigDecimal.valueOf(5160),
            faktiskBeløp = BigDecimal.valueOf(5000),
            beløpSamvær = BigDecimal.ZERO,
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
        )

        val løpendeBidragOgBeregninger = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(listOf(beregning)),
            løpendeBidragListe = listOf(løpendeBidrag),
        )

        val resultat = løpendeBidragOgBeregninger.tilBeregnGrunnlag(
            bpReferanse,
            søknadsbarnIdentMap,
            løpendeBarnFødselsdatoMap,
        )

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].søknadsbarnReferanse).isEqualTo(søknadsbarnReferanse) },
            { assertThat(resultat[0].stønadstype).isEqualTo(Stønadstype.BIDRAG) },
            { assertThat(resultat[0].grunnlagListe).hasSize(1) },
            { assertThat(resultat[0].periode.fom).isEqualTo(YearMonth.of(2024, 1)) },
            { assertThat(resultat[0].periode.til).isEqualTo(YearMonth.of(2024, 12)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal konvertere løpende bidrag til beregngrunnlag for barn som ikke er søknadsbarn`() {
        val bpReferanse = "person_PERSON_BIDRAGSPLIKTIG_12345"
        val løpendeBarnIdent = Personident(personIdentSøknadsbarn2)
        val søknadsbarnIdentMap = emptyMap<Personident, String>()
        val løpendeBarnFødselsdatoMap = mapOf(løpendeBarnIdent to LocalDate.of(2010, 5, 15))

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = løpendeBarnIdent,
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
                    løpendeBeløp = BigDecimal.valueOf(3000),
                    valutakode = "NOK",
                ),
            ),
        )

        val beregning = BidragBeregningResponsDto.BidragBeregning(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
            saksnummer = saksnummer,
            personidentBarn = løpendeBarnIdent,
            datoSøknad = LocalDate.now(),
            beregnetBeløp = BigDecimal.valueOf(3200),
            faktiskBeløp = BigDecimal.valueOf(3000),
            beløpSamvær = BigDecimal.ZERO,
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = Samværsklasse.SAMVÆRSKLASSE_0,
        )

        val løpendeBidragOgBeregninger = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(listOf(beregning)),
            løpendeBidragListe = listOf(løpendeBidrag),
        )

        val resultat = løpendeBidragOgBeregninger.tilBeregnGrunnlag(
            bpReferanse,
            søknadsbarnIdentMap,
            løpendeBarnFødselsdatoMap,
        )

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].søknadsbarnReferanse).contains("20100515_innhentet") },
            { assertThat(resultat[0].stønadstype).isEqualTo(Stønadstype.BIDRAG) },
            { assertThat(resultat[0].grunnlagListe).hasSize(1) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere flere perioder for samme kravhaver`() {
        val bpReferanse = "person_PERSON_BIDRAGSPLIKTIG_12345"
        val søknadsbarnIdent = Personident(personIdentSøknadsbarn1)
        val søknadsbarnReferanse = "person_PERSON_SØKNADSBARN_${søknadsbarnIdent.verdi}"
        val søknadsbarnIdentMap = mapOf(søknadsbarnIdent to søknadsbarnReferanse)
        val løpendeBarnFødselsdatoMap = emptyMap<Personident, LocalDate?>()

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = søknadsbarnIdent,
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1)),
                    løpendeBeløp = BigDecimal.valueOf(5000),
                    valutakode = "NOK",
                ),
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 1)),
                    løpendeBeløp = BigDecimal.valueOf(5500),
                    valutakode = "NOK",
                ),
            ),
        )

        val beregninger = listOf(
            BidragBeregningResponsDto.BidragBeregning(
                periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1)),
                saksnummer = saksnummer,
                personidentBarn = søknadsbarnIdent,
                datoSøknad = LocalDate.now(),
                beregnetBeløp = BigDecimal.valueOf(5160),
                faktiskBeløp = BigDecimal.valueOf(5000),
                beløpSamvær = BigDecimal.ZERO,
                stønadstype = Stønadstype.BIDRAG,
                samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
            ),
            BidragBeregningResponsDto.BidragBeregning(
                periode = ÅrMånedsperiode(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 12, 1)),
                saksnummer = saksnummer,
                personidentBarn = søknadsbarnIdent,
                datoSøknad = LocalDate.now(),
                beregnetBeløp = BigDecimal.valueOf(5660),
                faktiskBeløp = BigDecimal.valueOf(5500),
                beløpSamvær = BigDecimal.ZERO,
                stønadstype = Stønadstype.BIDRAG,
                samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
            ),
        )

        val løpendeBidragOgBeregninger = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(beregninger),
            løpendeBidragListe = listOf(løpendeBidrag),
        )

        val resultat = løpendeBidragOgBeregninger.tilBeregnGrunnlag(
            bpReferanse,
            søknadsbarnIdentMap,
            løpendeBarnFødselsdatoMap,
        )

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].grunnlagListe).hasSize(2) },
            { assertThat(resultat[0].periode.fom).isEqualTo(YearMonth.of(2024, 1)) },
            { assertThat(resultat[0].periode.til).isEqualTo(YearMonth.of(2024, 12)) },
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere flere kravhavere`() {
        val bpReferanse = "person_PERSON_BIDRAGSPLIKTIG_12345"
        val søknadsbarn1Ident = Personident(personIdentSøknadsbarn1)
        val søknadsbarn2Ident = Personident(personIdentSøknadsbarn2)
        val annetBarnIdent = Personident(personIdentAnnetbarn)
        val søknadsbarnIdentMap = mapOf(
            søknadsbarn1Ident to "person_PERSON_SØKNADSBARN_${søknadsbarn1Ident.verdi}",
            søknadsbarn2Ident to "person_PERSON_SØKNADSBARN_${søknadsbarn2Ident.verdi}",
        )
        val løpendeBarnFødselsdatoMap = emptyMap<Personident, LocalDate?>()

        val løpendeBidragListe = listOf(
            LøpendeBidrag(
                sak = Saksnummer(saksnummer),
                type = Stønadstype.BIDRAG,
                kravhaver = søknadsbarn1Ident,
                periodeListe = listOf(
                    BidragPeriode(
                        periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
                        løpendeBeløp = BigDecimal.valueOf(5000),
                        valutakode = "NOK",
                    ),
                ),
            ),
            LøpendeBidrag(
                sak = Saksnummer(saksnummer),
                type = Stønadstype.BIDRAG,
                kravhaver = søknadsbarn2Ident,
                periodeListe = listOf(
                    BidragPeriode(
                        periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 1)),
                        løpendeBeløp = BigDecimal.valueOf(3000),
                        valutakode = "NOK",
                    ),
                ),
            ),
            LøpendeBidrag(
                sak = Saksnummer(saksnummer2),
                type = Stønadstype.BIDRAG18AAR,
                kravhaver = annetBarnIdent,
                periodeListe = listOf(
                    BidragPeriode(
                        periode = ÅrMånedsperiode(LocalDate.of(2023, 1, 1), null),
                        løpendeBeløp = BigDecimal.valueOf(1700),
                        valutakode = "NOK",
                    ),
                ),
            ),
        )

        val beregninger = listOf(
            BidragBeregningResponsDto.BidragBeregning(
                periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), null),
                saksnummer = saksnummer,
                personidentBarn = søknadsbarn1Ident,
                datoSøknad = LocalDate.now(),
                beregnetBeløp = BigDecimal.valueOf(5160),
                faktiskBeløp = BigDecimal.valueOf(5000),
                beløpSamvær = BigDecimal.ZERO,
                stønadstype = Stønadstype.BIDRAG,
                samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
            ),
            BidragBeregningResponsDto.BidragBeregning(
                periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), null),
                saksnummer = saksnummer,
                personidentBarn = søknadsbarn2Ident,
                datoSøknad = LocalDate.now(),
                beregnetBeløp = BigDecimal.valueOf(3200),
                faktiskBeløp = BigDecimal.valueOf(3000),
                beløpSamvær = BigDecimal.ZERO,
                stønadstype = Stønadstype.BIDRAG,
                samværsklasse = Samværsklasse.SAMVÆRSKLASSE_0,
            ),
            BidragBeregningResponsDto.BidragBeregning(
                periode = ÅrMånedsperiode(LocalDate.of(2022, 6, 1), null),
                saksnummer = saksnummer,
                personidentBarn = annetBarnIdent,
                datoSøknad = LocalDate.now(),
                beregnetBeløp = BigDecimal.valueOf(1500),
                faktiskBeløp = BigDecimal.valueOf(1600),
                beløpSamvær = BigDecimal.valueOf(1000),
                stønadstype = Stønadstype.BIDRAG18AAR,
                samværsklasse = Samværsklasse.SAMVÆRSKLASSE_2,
            ),
        )

        val løpendeBidragOgBeregninger = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(beregninger),
            løpendeBidragListe = løpendeBidragListe,
        )

        val resultat = løpendeBidragOgBeregninger.tilBeregnGrunnlag(
            bpReferanse,
            søknadsbarnIdentMap,
            løpendeBarnFødselsdatoMap,
        )

        assertSoftly {
            resultat shouldHaveSize 3
//            resultat[0].søknadsbarnReferanse shouldContain annetBarnIdent.verdi
            resultat[1].søknadsbarnReferanse shouldContain søknadsbarn1Ident.verdi
            resultat[2].søknadsbarnReferanse shouldContain søknadsbarn2Ident.verdi

            resultat[0].stønadstype shouldBe Stønadstype.BIDRAG18AAR
            resultat[1].stønadstype shouldBe Stønadstype.BIDRAG
            resultat[2].stønadstype shouldBe Stønadstype.BIDRAG

            resultat[0].grunnlagListe shouldHaveSize 1
            resultat[1].grunnlagListe shouldHaveSize 1
            resultat[2].grunnlagListe shouldHaveSize 1
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal håndtere periode uten til-dato`() {
        val bpReferanse = "person_PERSON_BIDRAGSPLIKTIG_12345"
        val søknadsbarnIdent = Personident(personIdentSøknadsbarn1)
        val søknadsbarnReferanse = "person_PERSON_SØKNADSBARN_${søknadsbarnIdent.verdi}"
        val søknadsbarnIdentMap = mapOf(søknadsbarnIdent to søknadsbarnReferanse)
        val løpendeBarnFødselsdatoMap = emptyMap<Personident, LocalDate?>()

        val løpendeBidrag = LøpendeBidrag(
            sak = Saksnummer(saksnummer),
            type = Stønadstype.BIDRAG,
            kravhaver = søknadsbarnIdent,
            periodeListe = listOf(
                BidragPeriode(
                    periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), null),
                    løpendeBeløp = BigDecimal.valueOf(5000),
                    valutakode = "NOK",
                ),
            ),
        )

        val beregning = BidragBeregningResponsDto.BidragBeregning(
            periode = ÅrMånedsperiode(LocalDate.of(2024, 1, 1), null),
            saksnummer = saksnummer,
            personidentBarn = søknadsbarnIdent,
            datoSøknad = LocalDate.now(),
            beregnetBeløp = BigDecimal.valueOf(5160),
            faktiskBeløp = BigDecimal.valueOf(5000),
            beløpSamvær = BigDecimal.ZERO,
            stønadstype = Stønadstype.BIDRAG,
            samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
        )

        val løpendeBidragOgBeregninger = LøpendeBidragOgBeregninger(
            beregnetBeløpListe = BidragBeregningResponsDto(listOf(beregning)),
            løpendeBidragListe = listOf(løpendeBidrag),
        )

        val resultat = løpendeBidragOgBeregninger.tilBeregnGrunnlag(
            bpReferanse,
            søknadsbarnIdentMap,
            løpendeBarnFødselsdatoMap,
        )

        assertAll(
            { assertThat(resultat).hasSize(1) },
            { assertThat(resultat[0].periode.til).isNull() },
            { assertThat(resultat[0].grunnlagListe).hasSize(1) },
        )
    }
}
