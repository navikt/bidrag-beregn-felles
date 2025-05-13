package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningPersonConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningSakConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningStønadConsumer
import no.nav.bidrag.beregn.barnebidrag.service.external.BeregningVedtakConsumer
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettSakRespons
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettStønadDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettStønadPeriodeDto
import no.nav.bidrag.beregn.barnebidrag.testdata.opprettVedtakForStønad
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragsmottaker
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentBidragspliktig
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn1
import no.nav.bidrag.beregn.barnebidrag.testdata.personIdentSøknadsbarn2
import no.nav.bidrag.beregn.barnebidrag.testdata.saksnummer
import no.nav.bidrag.beregn.vedtak.Vedtaksfiltrering
import no.nav.bidrag.commons.web.mock.hentFil
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.commons.web.mock.stubSjablonService
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.sak.Sakskategori
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.felles.enhet_utland
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningBarnebidragGrunnlagIReferanser
import no.nav.bidrag.transport.behandling.vedtak.response.HentVedtakForStønadResponse
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.felles.commonObjectmapper
import no.nav.bidrag.transport.sak.RolleDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

val vedtaksidBidrag = 2

@ExtendWith(MockKExtension::class)
class AldersjusteringOrchestratorTest {

    @MockK
    lateinit var bidragSakConsumer: BeregningSakConsumer

    @MockK
    lateinit var beregningVedtakConsumer: BeregningVedtakConsumer

    @MockK
    lateinit var beregningStønadConsumer: BeregningStønadConsumer

    @MockK
    lateinit var personConsumer: BeregningPersonConsumer

    lateinit var bidragVedtakService: VedtakService
    lateinit var aldersjusteringOrchestrator: AldersjusteringOrchestrator

    @BeforeEach
    fun init() {
        stubSjablonService()
        stubSjablonProvider()
        bidragVedtakService = VedtakService(
            vedtakConsumer = beregningVedtakConsumer,
            stønadConsumer = beregningStønadConsumer,
            vedtakFilter = Vedtaksfiltrering(),
        )
        aldersjusteringOrchestrator = AldersjusteringOrchestrator(
            vedtakService = bidragVedtakService,
            sakConsumer = bidragSakConsumer,
            personConsumer = personConsumer,
            barnebidragApi = BeregnBarnebidragApi(),
        )
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns konverterVedtakFraFil()
        every { personConsumer.hentFødselsdatoForPerson(any()) } returns LocalDate.now().minusYears(11)
        every { beregningVedtakConsumer.hentVedtakForStønad(any()) } returns
            HentVedtakForStønadResponse(
                vedtakListe =
                listOf(
                    opprettVedtakForStønad(personIdentBidragspliktig, stønadstype = Stønadstype.FORSKUDD).copy(
                        vedtaksid = 55,
                        type = Vedtakstype.ALDERSJUSTERING,
                        kilde = Vedtakskilde.AUTOMATISK,
                    ),
                    opprettVedtakForStønad(personIdentBidragspliktig, stønadstype = Stønadstype.FORSKUDD).copy(
                        vedtaksid = vedtaksidBidrag.toLong(),
                        type = Vedtakstype.ENDRING,
                    ),
                ),
            )
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().withMonth(9).minusYears(1), null),
                        beløp = BigDecimal("4800"),
                        vedtakId = vedtaksidBidrag,
                    ),
                ),
            )

        every { bidragSakConsumer.hentSak(any()) } returns
            opprettSakRespons()
                .copy(
                    roller =
                    listOf(
                        RolleDto(
                            Personident(personIdentBidragsmottaker),
                            type = Rolletype.BIDRAGSMOTTAKER,
                        ),
                        RolleDto(
                            Personident(personIdentBidragspliktig),
                            type = Rolletype.BIDRAGSPLIKTIG,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn2),
                            type = Rolletype.BARN,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn1),
                            type = Rolletype.BARN,
                        ),
                    ),
                )
    }

    @Test
    fun test() {
        val periods = listOf(
            ÅrMånedsperiode(YearMonth.of(2023, 1), YearMonth.of(2023, 3)),
            ÅrMånedsperiode(YearMonth.of(2023, 4), null), // `til` is null here
            ÅrMånedsperiode(YearMonth.of(2023, 6), YearMonth.of(2023, 8)),
        )

        val result = periods.map { period ->
            period.til ?: YearMonth.now().plusYears(10000) // Handle null `til`
        }

        println(result)
    }

    @Test
    fun `skal beregne aldersjustering`() {
        val fødselsdatoBarn = LocalDate.now().minusYears(11)
        every { personConsumer.hentFødselsdatoForPerson(eq(Personident(personIdentSøknadsbarn1))) } returns fødselsdatoBarn
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().withMonth(9).minusYears(1), null),
                        beløp = BigDecimal("4800"),
                        vedtakId = vedtaksidBidrag,
                    ),
                ),
            )
        val (vedtaksid, løpendeBeløp, resultat) = aldersjusteringOrchestrator.utførAldersjustering(
            stønad = Stønadsid(
                type = Stønadstype.BIDRAG,
                kravhaver = Personident(personIdentSøknadsbarn1),
                skyldner = Personident(personIdentBidragspliktig),
                sak = Saksnummer(saksnummer),
            ),
        )
        resultat.shouldNotBeNull()

        assertSoftly(resultat) {
            it.beregnetBarnebidragPeriodeListe.shouldHaveSize(1)
            it.beregnetBarnebidragPeriodeListe.first().resultat.beløp shouldBe BigDecimal(5300)
            it.beregnetBarnebidragPeriodeListe.first().periode.fom shouldBe YearMonth.parse("2025-07")
            it.beregnetBarnebidragPeriodeListe.first().periode.til shouldBe null
        }
    }

    @Test
    fun `skal beregne aldersjustering når barnet skal aldersjusteres for år i input`() {
        val fødselsdatoBarn = LocalDate.parse("2018-03-01")
        every { personConsumer.hentFødselsdatoForPerson(eq(Personident(personIdentSøknadsbarn1))) } returns fødselsdatoBarn

        val (vedtaksid, løpendeBeløp, resultat) = aldersjusteringOrchestrator.utførAldersjustering(
            stønad = Stønadsid(
                type = Stønadstype.BIDRAG,
                kravhaver = Personident(personIdentSøknadsbarn1),
                skyldner = Personident(personIdentBidragspliktig),
                sak = Saksnummer(saksnummer),
            ),
            aldersjusteresForÅr = fødselsdatoBarn.year + 6,
        )
        resultat.shouldNotBeNull()

        assertSoftly(resultat) {
            it.beregnetBarnebidragPeriodeListe.shouldHaveSize(1)
            it.beregnetBarnebidragPeriodeListe.first().resultat.beløp shouldBe BigDecimal(5300)
            it.beregnetBarnebidragPeriodeListe.first().periode.fom shouldBe YearMonth.parse("2024-07")
            it.beregnetBarnebidragPeriodeListe.first().periode.til shouldBe null
        }
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis barnet ikke er i aldersjusteringsalder`() {
        val fødselsdatoBarn = LocalDate.parse("2018-03-01")
        every { personConsumer.hentFødselsdatoForPerson(eq(Personident(personIdentSøknadsbarn1))) } returns fødselsdatoBarn

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = fødselsdatoBarn.year + 7,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING"
        exception.begrunnelser.shouldHaveSize(1)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.IKKE_ALDERSGRUPPE_FOR_ALDERSJUSTERING
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis siste manuelle vedtak resultat var redusert av evne`() {
        val vedtak = konverterVedtakFraFil()
        val sistePeriode = vedtak.stønadsendringListe.find { it.kravhaver.verdi == personIdentSøknadsbarn1 }!!.periodeListe.maxBy { it.periode.fom }
        val sluttberegningBarnebidrag = vedtak.grunnlagListe.finnSluttberegningBarnebidragGrunnlagIReferanser(sistePeriode.grunnlagReferanseListe)!!
        val sluttberegningGrunnlag = sluttberegningBarnebidrag.grunnlag as GrunnlagDto
        val nyGrunnlagsliste = vedtak.grunnlagListe.filter { it.referanse != sluttberegningBarnebidrag.referanse } + sluttberegningGrunnlag.copy(
            innhold = POJONode(
                sluttberegningBarnebidrag.innhold.copy(
                    bidragJustertNedTilEvne = true,
                ),
            ),
        )
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns vedtak.copy(
            grunnlagListe = nyGrunnlagsliste,
        )

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse JUSTERT_PÅ_GRUNN_AV_EVNE"
        exception.begrunnelser.shouldHaveSize(1)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.JUSTERT_PÅ_GRUNN_AV_EVNE
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis siste manuelle vedtak har resultat som ikke skal aldersjusteres`() {
        val vedtak = konverterVedtakFraFil()
        val sistePeriode = vedtak.stønadsendringListe.find { it.kravhaver.verdi == personIdentSøknadsbarn1 }!!.periodeListe.maxBy { it.periode.fom }
        val sluttberegningBarnebidrag = vedtak.grunnlagListe.finnSluttberegningBarnebidragGrunnlagIReferanser(sistePeriode.grunnlagReferanseListe)!!
        val sluttberegningGrunnlag = sluttberegningBarnebidrag.grunnlag as GrunnlagDto
        val nyGrunnlagsliste = vedtak.grunnlagListe.filter { it.referanse != sluttberegningBarnebidrag.referanse } + sluttberegningGrunnlag.copy(
            innhold = POJONode(
                sluttberegningBarnebidrag.innhold.copy(
                    bidragJustertNedTilEvne = true,
                    bidragJustertNedTil25ProsentAvInntekt = true,
                    bidragJustertForNettoBarnetilleggBP = true,
                    bidragJustertForNettoBarnetilleggBM = true,
                ),
            ),
        )
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns vedtak.copy(
            grunnlagListe = nyGrunnlagsliste,
        )

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse JUSTERT_FOR_BARNETILLEGG_BM,JUSTERT_FOR_BARNETILLEGG_BP,JUSTERT_PÅ_GRUNN_AV_EVNE,JUSTERT_PÅ_GRUNN_AV_25_PROSENT"
        exception.begrunnelser.shouldHaveSize(4)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.JUSTERT_PÅ_GRUNN_AV_EVNE
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.JUSTERT_PÅ_GRUNN_AV_25_PROSENT
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.JUSTERT_FOR_BARNETILLEGG_BP
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.JUSTERT_FOR_BARNETILLEGG_BM
    }

    @Test
    fun `skal kaste skal aldersjusteres manuelt hvis utenlandsk vedtak`() {
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().withMonth(9).minusYears(1), null),
                        beløp = BigDecimal("4800"),
                        vedtakId = vedtaksidBidrag,
                        valutakode = "NOK",
                    ),
                ),
            )
        every { bidragSakConsumer.hentSak(any()) } returns
            opprettSakRespons()
                .copy(
                    eierfogd = Enhetsnummer(enhet_utland),
                    kategori = Sakskategori.U,
                    roller =
                    listOf(
                        RolleDto(
                            Personident(personIdentBidragsmottaker),
                            type = Rolletype.BIDRAGSMOTTAKER,
                        ),
                        RolleDto(
                            Personident(personIdentBidragspliktig),
                            type = Rolletype.BIDRAGSPLIKTIG,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn2),
                            type = Rolletype.BARN,
                        ),
                        RolleDto(
                            Personident(personIdentSøknadsbarn1),
                            type = Rolletype.BARN,
                        ),
                    ),
                )
        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse UTENLANDSSAK"
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.UTENLANDSSAK
    }

    @Test
    fun `skal kaste skal aldersjusteres manielt hvis mangler grunnlag`() {
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns konverterVedtakFraFil().copy(
            grunnlagListe = emptyList(),
        )

        val exception = shouldThrow<RuntimeException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Aldersjustering kunne ikke utføres fordi vedtak 2 mangler grunnlag"
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis ingen løpende bidrag`() {
        val vedtak = konverterVedtakFraFil()
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns vedtak.copy(
            stønadsendringListe = listOf(
                vedtak.stønadsendringListe.first()
                    .copy(
                        periodeListe = emptyList(),
                    ),
            ),
        )

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse INGEN_LØPENDE_PERIODE"
        exception.begrunnelser.shouldHaveSize(1)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis siste periode er opphør`() {
        val vedtak = konverterVedtakFraFil()
        every { beregningVedtakConsumer.hentVedtak(eq(vedtaksidBidrag)) } returns vedtak.copy(
            stønadsendringListe = listOf(
                vedtak.stønadsendringListe.first()
                    .copy(
                        periodeListe = listOf(
                            VedtakPeriodeDto(
                                beløp = null,
                                delytelseId = "1L",
                                periode = ÅrMånedsperiode(LocalDate.parse("2025-01-01"), null),
                                resultatkode = Resultatkode.OPPHØR.name,
                                valutakode = null,
                                grunnlagReferanseListe = emptyList(),
                            ),
                        ),
                    ),
            ),
        )

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse INGEN_LØPENDE_PERIODE"
        exception.begrunnelser.shouldHaveSize(1)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.INGEN_LØPENDE_PERIODE
    }

    @Test
    fun `skal kaste skal ikke aldersjusteringsfeil hvis bidrag løper med utenlandsk valuta`() {
        every { beregningStønadConsumer.hentHistoriskeStønader(any()) } returns
            opprettStønadDto(
                stønadstype = Stønadstype.BIDRAG,
                periodeListe =
                listOf(
                    opprettStønadPeriodeDto(
                        ÅrMånedsperiode(LocalDate.now().withMonth(9).minusYears(1), null),
                        beløp = BigDecimal("100"),
                        vedtakId = vedtaksidBidrag,
                        valutakode = "USD",
                    ),
                ),
            )

        val exception = shouldThrow<SkalIkkeAldersjusteresException> {
            aldersjusteringOrchestrator.utførAldersjustering(
                stønad = Stønadsid(
                    type = Stønadstype.BIDRAG,
                    kravhaver = Personident(personIdentSøknadsbarn1),
                    skyldner = Personident(personIdentBidragspliktig),
                    sak = Saksnummer(saksnummer),
                ),
                aldersjusteresForÅr = 2025,
            )
        }
        exception.message shouldBe "Skal ikke aldersjusteres med begrunnelse LØPER_MED_UTENLANDSK_VALUTA"
        exception.begrunnelser.shouldHaveSize(1)
        exception.begrunnelser shouldContain SkalIkkeAldersjusteresBegrunnelse.LØPER_MED_UTENLANDSK_VALUTA
    }
}

fun konverterVedtakFraFil(filnavn: String = "vedtak_aldersjustering_1.json") = commonObjectmapper.readValue<VedtakDto>(hentFil("/testfiler/aldersjustering_orkestrator/$filnavn"))
