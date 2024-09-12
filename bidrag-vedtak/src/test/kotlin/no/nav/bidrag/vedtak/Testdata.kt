package no.nav.bidrag.vedtak

import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Testperson(
    val navn: String,
    val personident: Personident,
    val fødselsdato: LocalDate,
    val rolletype: Rolletype
)

val bm  = Testperson("Varig Mottaker", Personident("12345612345"), LocalDate.now().minusYears(38), Rolletype.BIDRAGSMOTTAKER)
val bp = Testperson("Pliktig Giver", Personident("23456732165"), LocalDate.now().minusYears(43), Rolletype.BIDRAGSPLIKTIG)
val ba1 = Testperson("Født Først", Personident("01234599999"), LocalDate.now().minusYears(13), Rolletype.BARN)
val ba2 = Testperson("Den Yngste", Personident("01234599999"), LocalDate.now().minusYears(3), Rolletype.BARN)

fun oppretteVedtak(
    stønadsendringer: List<StønadsendringDto>,
    engangsbeløp: List<EngangsbeløpDto>,
    vedtakstype: Vedtakstype = Vedtakstype.FASTSETTELSE,
    kilde: Vedtakskilde = Vedtakskilde.MANUELT,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),

): VedtakDto {
    return VedtakDto(
        stønadsendringListe = stønadsendringer,
        engangsbeløpListe = engangsbeløp,
        behandlingsreferanseListe = emptyList(),
        grunnlagListe = emptyList(),
        enhetsnummer = Enhetsnummer("1234"),
        vedtakstidspunkt = vedtakstidspunkt,
        fastsattILand = "NO",
        innkrevingUtsattTilDato = null,
        kilde = kilde,
        kildeapplikasjon = "bidrag-behandling",
        opprettetAv = "s123456",
        opprettetAvNavn = "Saksbehandlers navn",
        opprettetTidspunkt = vedtakstidspunkt,
        type = vedtakstype,
    )
}

fun oppretteEngangsbeløp() = EngangsbeløpDto(
    type = Engangsbeløptype.SÆRBIDRAG,
    sak = Saksnummer("1234"),
    beløp = BigDecimal(100),
    resultatkode = Resultatkode.AVSLAG.name,
    beslutning = Beslutningstype.ENDRING,
    delytelseId = "DelytelseId",
    eksternReferanse = "EksternReferanse",
    grunnlagReferanseListe = emptyList(),
    innkreving = Innkrevingstype.MED_INNKREVING,
    kravhaver = Personident(""),
    mottaker = Personident(""),
    omgjørVedtakId = null,
    referanse = "Referanse",
    skyldner = Personident(""),
    valutakode = "Valutakode",
)

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
    innkrevingstype: Innkrevingstype = Innkrevingstype.MED_INNKREVING
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
    type = stønadstype,
)


enum class Resultatkode(val kode: String){

}
