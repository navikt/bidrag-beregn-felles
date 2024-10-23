package no.nav.bidrag.beregn.vedtak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.beregn.vedtak.Beløp.B1000
import no.nav.bidrag.beregn.vedtak.Beløp.B1070
import no.nav.bidrag.beregn.vedtak.Beløp.B1200
import no.nav.bidrag.beregn.vedtak.Beløp.B1300
import no.nav.bidrag.beregn.vedtak.Beløp.B5000
import no.nav.bidrag.beregn.vedtak.Beløp.B800
import no.nav.bidrag.beregn.vedtak.Årstall.R10
import no.nav.bidrag.beregn.vedtak.Årstall.R12
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K14
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K16
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K18
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K19
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K20
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K22
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K23
import no.nav.bidrag.beregn.vedtak.Årstall.Y2K24
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import java.time.Year
import kotlin.test.Test

class VedtaksfiltreringTest {

    val vedtaksfiltrering: Vedtaksfiltrering = Vedtaksfiltrering()

    @Test
    fun `skal returnere null hvis ingen manuelle vedtak`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        vedtak.shouldBeNull()
    }

    @Test
    fun `skal hente ut oppfostringsbidrag`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, Stønadstype.OPPFOSTRINGSBIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.OPPFOSTRINGSBIDRAG
        }
    }

    @Test
    fun `skal hente ut vedtak om aldersjustering`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.ALDERSJUSTERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal hente ut vedtak om aldersopphør`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.ALDERSOPPHØR),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal hente ut vedtak om opphør`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.OPPHØR),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal returnere null hvis siste manuelle vedtak gjelder stadfestelse`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, Stønadstype.OPPFOSTRINGSBIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.OPPFOSTRINGSBIDRAG
        }
    }

    @Test
    fun `skal hente ut 18-årsbidrag`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(
                    Y2K22,
                    null,
                    B1200,
                    beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG,
                    stønadstype = Stønadstype.BIDRAG18AAR,
                ),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG18AAR
        }
    }

    @Test
    fun `skal hoppe over indeksregulering`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal ikke hoppe over ved 12 prosen ingen endring`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K20, Y2K22, B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K22, null, B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K23, Y2K24, B1200, Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
                OppretteVedtakRequest(Y2K24, null, B1200, Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10003"
            vedtak.vedtakstidspunkt shouldBe Y2K24.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe B1200.verdi
        }
    }

    @Test
    fun `skal hoppe over klage`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K14, Y2K16, B800, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K16, Y2K18, B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Y2K18, Y2K19, B1070, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Y2K19, Y2K20, B800, Beslutningsårsak.INGEN_ENDRING_12_PROSENT, omgjørVedtak = 2),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Y2K16.år.atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal returnere null hvis referert vedtak ikke finnes`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K16, Y2K18, B1000, Beslutningsårsak.STADFESTELSE, beslutningstype = Beslutningstype.STADFESTELSE),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        vedtak.shouldBeNull()
    }

    @Test
    fun `skal hoppe til opprinnelig vedtak hvis resultat fra annet vedtak`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Y2K14, Y2K16, B800),
                OppretteVedtakRequest(Y2K16, Y2K18, B1200),
                OppretteVedtakRequest(Y2K18, Y2K19, B1300),
                OppretteVedtakRequest(Y2K19, Y2K20, B1200, Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK),
                OppretteVedtakRequest(Y2K20, Y2K22, B5000, kilde = Vedtakskilde.AUTOMATISK),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Year.of(2016).atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal hente første manuelle vedtak i vedtaksrekke`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(R12, R10, B1000, beslutningsårsak = Beslutningsårsak.INNVILGETT_VEDTAK),
                OppretteVedtakRequest(R10, null, B1200, Beslutningsårsak.INNVILGETT_VEDTAK, kilde = Vedtakskilde.AUTOMATISK),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneSisteManuelleVedtak(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak != null
            vedtak!!.kilde shouldBe Vedtakskilde.MANUELT
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
        }
    }
}
