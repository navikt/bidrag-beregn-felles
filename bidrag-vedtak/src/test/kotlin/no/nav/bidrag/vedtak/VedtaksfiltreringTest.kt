package no.nav.bidrag.vedtak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.vedtak.Årstall.R10
import no.nav.bidrag.vedtak.Årstall.R12
import no.nav.bidrag.vedtak.Årstall.Y2K14
import no.nav.bidrag.vedtak.Årstall.Y2K16
import no.nav.bidrag.vedtak.Årstall.Y2K18
import no.nav.bidrag.vedtak.Årstall.Y2K19
import no.nav.bidrag.vedtak.Årstall.Y2K20
import no.nav.bidrag.vedtak.Årstall.Y2K22
import no.nav.bidrag.vedtak.Årstall.Y2K23
import no.nav.bidrag.vedtak.Årstall.Y2K24
import no.nav.bidrag.vedtak.Beløp.B800
import no.nav.bidrag.vedtak.Beløp.B1000
import no.nav.bidrag.vedtak.Beløp.B1070
import no.nav.bidrag.vedtak.Beløp.B1200
import no.nav.bidrag.vedtak.Beløp.B1300
import no.nav.bidrag.vedtak.Beløp.B5000
import java.time.Year
import kotlin.test.Test

class VedtaksfiltreringTest {

    val vedtaksfiltrering: Vedtaksfiltrering = Vedtaksfiltrering()

    @Test
    fun `skal hoppe over indeksregulering`() {

        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, beslutningsårsak = Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, beslutningsårsak = Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendringListe.shouldNotBeEmpty()
            vedtak.stønadsendringListe.first().periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendringListe.first().periodeListe.first().beløp shouldBe B1200.verdi
        }
    }

    @Test
    fun `skal hoppe over ved 12 prosen ingen endring`() {

        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, beslutningsårsak = Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
                OppretteVedtakRequest(Y2K24, null, B1200, beslutningsårsak = Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendringListe.shouldNotBeEmpty()
            vedtak.stønadsendringListe.first().periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendringListe.first().periodeListe.first().beløp shouldBe B1200.verdi
        }
    }

    @Test
    fun `skal hoppe over originalt vedtak ved klage og 12-prosent-ingen-endring`() {

        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K14, Y2K16, B800, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K16, Y2K18, B1000, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K18, Y2K19, B1070, beslutningsårsak = Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K19, Y2K20, B800, beslutningsårsak = Beslutningsårsak.INGEN_ENDRING_12_PROSENT, omgjørVedtak = 2)
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendringListe.shouldNotBeEmpty()
            vedtak.stønadsendringListe.first().periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Y2K14.år.atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal hoppe til opprinnelig vedtak hvis resultat fra annet vedtak`() {

        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K14, Y2K16, B800),
                OppretteVedtakRequest(Y2K16, Y2K18, B1200),
                OppretteVedtakRequest(Y2K18, Y2K19, B1300),
                OppretteVedtakRequest(Y2K19, Y2K20, B1200, beslutningsårsak = Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK),
                OppretteVedtakRequest(Y2K20, Y2K22, B5000, kilde = Vedtakskilde.AUTOMATISK)
            )
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendringListe.shouldNotBeEmpty()
            vedtak.stønadsendringListe.first().periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Year.of(2016).atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal hente første manuelle vedtak i vedtaksrekke`() {

        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(R12, R10, B1000, beslutningsårsak = Beslutningsårsak.INNVILGETT_VEDTAK),
                OppretteVedtakRequest(R10, null, B1200, Vedtakskilde.AUTOMATISK, Beslutningsårsak.INNVILGETT_VEDTAK),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneManueltVedtakTilEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak != null
            vedtak!!.kilde shouldBe Vedtakskilde.MANUELT
            vedtak.stønadsendringListe.shouldNotBeEmpty()
            vedtak.stønadsendringListe.first().periodeListe.first().delytelseId shouldBe "10000"
        }
    }
}



