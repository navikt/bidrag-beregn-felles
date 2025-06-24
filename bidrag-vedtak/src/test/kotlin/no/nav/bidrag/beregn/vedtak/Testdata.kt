package no.nav.bidrag.beregn.vedtak

import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth

data class Testperson(val navn: String, val personident: Personident, val fødselsdato: LocalDate, val rolletype: Rolletype)

data class OppretteVedtakRequest(
    val fom: Årstall,
    val til: Årstall? = null,
    val beløp: Beløp = Beløp.B1000,
    val beslutningsårsak: Beslutningsårsak = Beslutningsårsak.INNVILGETT_VEDTAK,
    val stønadstype: Stønadstype = Stønadstype.BIDRAG,
    val vedtakstype: Vedtakstype = Vedtakstype.FASTSETTELSE,
    val omgjørVedtak: Int? = null,
    val kilde: Vedtakskilde = Vedtakskilde.MANUELT,
    val beslutningstype: Beslutningstype = Beslutningstype.ENDRING,
)

val bm = Testperson("Varig Mottaker", Personident("12345612345"), LocalDate.now().minusYears(38), Rolletype.BIDRAGSMOTTAKER)
val bp = Testperson("Pliktig Giver", Personident("23456732165"), LocalDate.now().minusYears(43), Rolletype.BIDRAGSPLIKTIG)
val ba1 = Testperson("Født Først", Personident("01234599999"), LocalDate.now().minusYears(13), Rolletype.BARN)
val ba2 = Testperson("Den Yngste", Personident("01234599999"), LocalDate.now().minusYears(3), Rolletype.BARN)
val saksnummer: Saksnummer = Saksnummer("1234567")

val y12 = Year.now().minusYears(12)
val y10 = Year.now().minusYears(10)

enum class Årstall(val år: Year) {
    Y2K14(Year.of(2014)),
    Y2K16(Year.of(2016)),
    Y2K18(Year.of(2018)),
    Y2K19(Year.of(2019)),
    Y2K20(Year.of(2020)),
    Y2K21(Year.of(2021)),
    Y2K22(Year.of(2022)),
    Y2K23(Year.of(2023)),
    Y2K24(Year.of(2024)),
    R10(Year.now().minusYears(10)),
    R12(Year.now().minusYears(12)),
}

enum class Beløp(val verdi: BigDecimal) {
    B800(BigDecimal.valueOf(800)),
    B1000(BigDecimal.valueOf(100)),
    B1070(BigDecimal.valueOf(1070)),
    B1200(BigDecimal.valueOf(1200)),
    B1300(
        BigDecimal.valueOf(
            1300,
        ),
    ),
    B5000(BigDecimal.valueOf(5000)),
}

fun oppretteVedtakssett(requests: Set<OppretteVedtakRequest>): Set<VedtakForStønad> {
    var vedtaksid: Long = 1
    var delytelsesid = 10000

    return requests.map { request ->
        val vedtakstype = when (request.beslutningsårsak) {
            Beslutningsårsak.INDEKSREGULERING -> Vedtakstype.INDEKSREGULERING
            else -> request.vedtakstype
        }

        oppretteVedtak(
            id = vedtaksid++,
            vedtakstype = request.omgjørVedtak?.let { Vedtakstype.KLAGE } ?: vedtakstype,
            stønadsendring =
            oppretteStønadsendring(
                beslutningstype = request.beslutningstype,
                skyldner = bp.personident,
                mottaker = bm.personident,
                kravhaver = ba1.personident,
                omgjørVedtaksid = request.omgjørVedtak,
                stønadstype = request.stønadstype,
                perioder = listOf(
                    oppretteVedtaksperiode(
                        delytelsesid++.toString(),
                        request.fom.år.atMonth(1),
                        request.til?.år?.atMonth(1),
                        request.beløp.verdi,
                        request.beslutningsårsak,
                    ),
                ),
                saksnummer = saksnummer,
            ),
            kilde = request.kilde,
            vedtakstidspunkt = request.fom.år.atDay(1).atStartOfDay(),
        )
    }.toSet()
}

fun oppretteVedtaksperiode(
    delytelsesid: String,
    fom: YearMonth,
    tom: YearMonth?,
    beløp: BigDecimal,
    beslutningsårsak: Beslutningsårsak = Beslutningsårsak.INNVILGETT_VEDTAK,
): VedtakPeriodeDto = VedtakPeriodeDto(
    beløp = beløp,
    delytelseId = delytelsesid,
    periode = ÅrMånedsperiode(fom, tom),
    resultatkode = beslutningsårsak.kode,
    valutakode = null,
    grunnlagReferanseListe = emptyList(),
)

fun oppretteVedtak(
    id: Long,
    stønadsendring: StønadsendringDto,
    vedtakstidspunkt: LocalDateTime,
    vedtakstype: Vedtakstype = Vedtakstype.FASTSETTELSE,
    kilde: Vedtakskilde = Vedtakskilde.MANUELT,
): VedtakForStønad = VedtakForStønad(
    vedtaksid = id,
    stønadsendring = stønadsendring,
    behandlingsreferanser = if (Vedtakstype.KLAGE == vedtakstype) oppretteBehandlingsreferanseForKlage() else emptyList(),
    vedtakstidspunkt = vedtakstidspunkt,
    kilde = kilde,
    type = vedtakstype,
    kildeapplikasjon = "",
)

fun oppretteBehandlingsreferanseForKlage(): List<BehandlingsreferanseDto> {
    val søknadsid = 229190
    return listOf(
        BehandlingsreferanseDto(
            kilde = BehandlingsrefKilde.BISYS_KLAGE_REF_SØKNAD,
            referanse = søknadsid.toString(),
        ),
    )
}

fun oppretteStønadsendring(
    skyldner: Personident,
    kravhaver: Personident,
    mottaker: Personident,
    saksnummer: Saksnummer,
    stønadstype: Stønadstype,
    beslutningstype: Beslutningstype = Beslutningstype.ENDRING,
    eksternReferanse: String? = null,
    indeksår: Int? = null,
    omgjørVedtaksid: Int? = null,
    grunnlagsreferanser: List<Grunnlagsreferanse> = emptyList(),
    perioder: List<VedtakPeriodeDto>,
    innkrevingstype: Innkrevingstype = Innkrevingstype.MED_INNKREVING,
) = StønadsendringDto(
    beslutning = beslutningstype,
    eksternReferanse = eksternReferanse,
    skyldner = skyldner,
    førsteIndeksreguleringsår = indeksår,
    grunnlagReferanseListe = grunnlagsreferanser,
    innkreving = innkrevingstype,
    kravhaver = kravhaver,
    mottaker = mottaker,
    omgjørVedtakId = omgjørVedtaksid,
    periodeListe = perioder,
    sak = saksnummer,
    sisteVedtaksid = null,
    type = stønadstype,
)
