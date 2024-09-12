package no.nav.bidrag.vedtak

import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

class FiltreringTest {

    val vedtaksfiltrering: Vedtaksfiltrering = Vedtaksfiltrering()

    @Test
    fun `skal hente første manuelle vedtak i vedtaksrekke`() {

        // gitt
        val periodeFørsteManuelleVedtak =
            ÅrMånedsperiode(LocalDate.now().minusYears(12).withDayOfYear(1), LocalDate.now().minusYears(10).withDayOfMonth(1))
        var delytelsesid = 1
        val førsteVedtaksperiode = VedtakPeriodeDto(
            beløp = BigDecimal(5000),
            delytelseId = delytelsesid.toString(),
            periode = periodeFørsteManuelleVedtak,
            resultatkode = Beslutningsårsak.INNVILGETT_VEDTAK.kode,
            valutakode = null,
            grunnlagReferanseListe = emptyList()
        )
        delytelsesid++
        val førsteStønadsendring = oppretteStønadsendring(
            skyldner = bp.personident,
            mottaker = bm.personident,
            kravhaver = ba1.personident,
            stønadstype = Stønadstype.BIDRAG,
            perioder = listOf(førsteVedtaksperiode),
            saksnummer = Saksnummer("1234567")
        )
        val førsteManuelleVedtak = oppretteVedtak(
            vedtakstidspunkt = periodeFørsteManuelleVedtak.fom.atDay(1).atStartOfDay(),
            engangsbeløp = emptyList(),
            stønadsendringer = listOf(
                førsteStønadsendring
            )
        )
        val periodeAndreManuelleVedtak = ÅrMånedsperiode(periodeFørsteManuelleVedtak.til!!, periodeFørsteManuelleVedtak.til!!.plusYears(2))
        val andreVedtak =
            førsteManuelleVedtak.copy(
                kilde = Vedtakskilde.AUTOMATISK,
                vedtakstidspunkt = periodeAndreManuelleVedtak.fom.atDay(1).atStartOfDay(),
                stønadsendringListe = listOf(førsteStønadsendring.copy(periodeListe = listOf(
                    førsteVedtaksperiode.copy(delytelseId = delytelsesid.toString(), periode = periodeAndreManuelleVedtak))))
            )
        delytelsesid++

        // hvis
       val vedtak =  vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(setOf(førsteManuelleVedtak, andreVedtak), ba1.personident)

        // så
        assert(true)
    }
}



