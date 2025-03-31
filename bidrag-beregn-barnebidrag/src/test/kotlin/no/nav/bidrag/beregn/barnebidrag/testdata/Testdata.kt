package no.nav.bidrag.beregn.barnebidrag.testdata

import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.sak.Bidragssakstatus
import no.nav.bidrag.domene.enums.sak.Sakskategori
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.felles.personidentNav
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.stonad.response.StønadDto
import no.nav.bidrag.transport.behandling.stonad.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.sak.BidragssakDto
import no.nav.bidrag.transport.sak.RolleDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

val SOKNAD_ID = 12412421414L
val saksnummer = "21321312321"
val personIdentSøknadsbarn1 = "213213213213"
val personIdentSøknadsbarn2 = "213213333213213"
val personIdentBidragsmottaker = "123213333"
val personIdentBidragspliktig = "1345235325325"
val SAKSBEHANDLER_IDENT = "Z999999"

fun opprettStønadDto(
    periodeListe: List<StønadPeriodeDto>,
    stønadstype: Stønadstype = Stønadstype.BIDRAG,
    opprettetTidspunkt: LocalDateTime = LocalDateTime.parse("2025-01-01T00:00:00"),
) = StønadDto(
    sak = Saksnummer(saksnummer),
    skyldner = if (stønadstype == Stønadstype.BIDRAG) Personident(personIdentBidragspliktig) else personidentNav,
    kravhaver = Personident(personIdentSøknadsbarn1),
    mottaker = Personident(personIdentBidragsmottaker),
    førsteIndeksreguleringsår = 2025,
    innkreving = Innkrevingstype.MED_INNKREVING,
    opprettetAv = "",
    opprettetTidspunkt = opprettetTidspunkt,
    endretAv = null,
    endretTidspunkt = null,
    stønadsid = 1,
    type = stønadstype,
    periodeListe = periodeListe,
)

fun opprettStønadPeriodeDto(
    periode: ÅrMånedsperiode = ÅrMånedsperiode(LocalDate.parse("2024-08-01"), null),
    beløp: BigDecimal? = BigDecimal.ONE,
    valutakode: String = "NOK",
    vedtakId: Int = 1,
) = StønadPeriodeDto(
    stønadsid = 1,
    periodeid = 1,
    periodeGjortUgyldigAvVedtaksid = null,
    vedtaksid = vedtakId,
    gyldigFra = LocalDateTime.parse("2024-01-01T00:00:00"),
    gyldigTil = null,
    periode = periode,
    beløp = beløp,
    valutakode = valutakode,
    resultatkode = "OK",
)
fun opprettVedtakForStønad(
    kravhaver: String,
    stønadstype: Stønadstype,
) = VedtakForStønad(
    vedtaksid = 1,
    type = Vedtakstype.FASTSETTELSE,
    kilde = Vedtakskilde.MANUELT,
    vedtakstidspunkt = LocalDateTime.parse("2024-01-01T00:00:00"),
    behandlingsreferanser =
    listOf(
        BehandlingsreferanseDto(
            kilde = BehandlingsrefKilde.BISYS_SØKNAD,
            referanse =
            if (kravhaver == personIdentSøknadsbarn1) {
                SOKNAD_ID.toString()
            } else if (kravhaver == personIdentSøknadsbarn2) {
                124124231414L.toString()
            } else {
                12412435521414L.toString()
            },
        ),
    ),
    stønadsendring =
    StønadsendringDto(
        type = stønadstype,
        sak = Saksnummer(saksnummer),
        skyldner = Personident(personIdentBidragspliktig),
        kravhaver = Personident(kravhaver),
        mottaker = Personident(personIdentBidragsmottaker),
        førsteIndeksreguleringsår = 0,
        innkreving = Innkrevingstype.MED_INNKREVING,
        beslutning = Beslutningstype.ENDRING,
        omgjørVedtakId = null,
        eksternReferanse = "123456",
        grunnlagReferanseListe = emptyList(),
        sisteVedtaksid = null,
        periodeListe =
        listOf(
            VedtakPeriodeDto(
                periode = ÅrMånedsperiode(LocalDate.parse("2024-07-01"), null),
                beløp = BigDecimal(5160),
                valutakode = "NOK",
                resultatkode = "KBB",
                delytelseId = null,
                grunnlagReferanseListe = emptyList(),
            ),
        ),
    ),
)
fun opprettVedtakDto() = VedtakDto(
    kilde = Vedtakskilde.MANUELT,
    fastsattILand = "",
    type = Vedtakstype.ENDRING,
    opprettetAv = "",
    opprettetAvNavn = "",
    kildeapplikasjon = "bisys",
    vedtakstidspunkt = LocalDateTime.now(),
    enhetsnummer = Enhetsnummer("4444"),
    innkrevingUtsattTilDato = null,
    opprettetTidspunkt = LocalDateTime.now(),
    engangsbeløpListe = emptyList(),
    behandlingsreferanseListe = emptyList(),
    grunnlagListe = emptyList(),
    unikReferanse = "",
    stønadsendringListe = emptyList(),
)
fun opprettStønadsendringBidrag() = StønadsendringDto(
    type = Stønadstype.BIDRAG,
    kravhaver = Personident(personIdentSøknadsbarn1),
    mottaker = Personident(personIdentBidragsmottaker),
    skyldner = Personident(personIdentBidragspliktig),
    sak = Saksnummer(saksnummer),
    førsteIndeksreguleringsår = null,
    innkreving = Innkrevingstype.MED_INNKREVING,
    beslutning = Beslutningstype.ENDRING,
    omgjørVedtakId = null,
    eksternReferanse = null,
    grunnlagReferanseListe = emptyList(),
    sisteVedtaksid = null,
    periodeListe =
    listOf(
        VedtakPeriodeDto(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            resultatkode = Resultatkode.BEREGNET_BIDRAG.name,
            beløp = BigDecimal(1000),
            delytelseId = null,
            valutakode = "NOK",
            grunnlagReferanseListe = listOf(opprettGrunnlagSluttberegningBidrag().referanse),
        ),
    ),
)
fun opprettSakRespons() = BidragssakDto(
    eierfogd = Enhetsnummer("4806"),
    saksnummer = Saksnummer("123213"),
    saksstatus = Bidragssakstatus.IN,
    kategori = Sakskategori.N,
    opprettetDato = LocalDate.now(),
    levdeAdskilt = false,
    ukjentPart = false,
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
            Personident(personIdentSøknadsbarn1),
            type = Rolletype.BARN,
        ),
    ),
)
