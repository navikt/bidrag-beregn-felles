package no.nav.bidrag.beregn.core.vedtak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import org.junit.Test
import org.junit.jupiter.api.Disabled
import java.time.Year

class VedtaksfiltreringTest {

    val vedtaksfiltrering: Vedtaksfiltrering = Vedtaksfiltrering()

    @Test
    fun `skal returnere null hvis ingen manuelle vedtak`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        vedtak.shouldBeNull()
    }

    @Test
    fun `skal hente ut oppfostringsbidrag`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, Stønadstype.OPPFOSTRINGSBIDRAG),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.OPPFOSTRINGSBIDRAG
        }
    }

    @Test
    @Disabled
    fun `skal hente ut vedtak om aldersjustering`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.ALDERSJUSTERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal hente ut vedtak om aldersopphør`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.ALDERSOPPHØR),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal hente ut vedtak om opphør`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, vedtakstype = Vedtakstype.OPPHØR),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal returnere null hvis siste manuelle vedtak gjelder stadfestelse`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG, Stønadstype.OPPFOSTRINGSBIDRAG),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.OPPFOSTRINGSBIDRAG
        }
    }

    @Test
    fun `skal hente ut 18-årsbidrag`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(
                    Årstall.Y2K22,
                    null,
                    Beløp.B1200,
                    beslutningsårsak = Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG,
                    stønadstype = Stønadstype.BIDRAG18AAR,
                ),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG18AAR
        }
    }

    @Test
    fun `skal hoppe over indeksregulering`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K22.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
            vedtak.stønadsendring.type shouldBe Stønadstype.BIDRAG
        }
    }

    @Test
    fun `skal ikke hoppe over ved 12 prosen ingen endring`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K20, Årstall.Y2K22, Beløp.B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K22, null, Beløp.B1200, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K23, Årstall.Y2K24, Beløp.B1200, Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
                OppretteVedtakRequest(Årstall.Y2K24, null, Beløp.B1200, Beslutningsårsak.INGEN_ENDRING_12_PROSENT),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10003"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K24.år.atDay(1).atStartOfDay()
            vedtak.stønadsendring.periodeListe.first().beløp shouldBe Beløp.B1200.verdi
        }
    }

    @Test
    fun `skal hoppe over klage`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K14, Årstall.Y2K16, Beløp.B800, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K16, Årstall.Y2K18, Beløp.B1000, Beslutningsårsak.KOSTNADSBEREGNET_BIDRAG),
                OppretteVedtakRequest(Årstall.Y2K18, Årstall.Y2K19, Beløp.B1070, Beslutningsårsak.INDEKSREGULERING),
                OppretteVedtakRequest(Årstall.Y2K19, Årstall.Y2K20, Beløp.B800, Beslutningsårsak.INGEN_ENDRING_12_PROSENT, omgjørVedtak = 2),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Årstall.Y2K16.år.atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal returnere null hvis referert vedtak ikke finnes`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K16, Årstall.Y2K18, Beløp.B1000, Beslutningsårsak.STADFESTELSE, beslutningstype = Beslutningstype.STADFESTELSE),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        vedtak.shouldBeNull()
    }

    @Test
    fun `skal hoppe til opprinnelig vedtak hvis resultat fra annet vedtak`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.Y2K14, Årstall.Y2K16, Beløp.B800),
                OppretteVedtakRequest(Årstall.Y2K16, Årstall.Y2K18, Beløp.B1200),
                OppretteVedtakRequest(Årstall.Y2K18, Årstall.Y2K19, Beløp.B1300),
                OppretteVedtakRequest(Årstall.Y2K19, Årstall.Y2K20, Beløp.B1200, Beslutningsårsak.RESULTAT_FRA_ANNET_VEDTAK),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak.shouldNotBeNull()
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10001"
            vedtak.vedtakstidspunkt shouldBe Year.of(2016).atDay(1).atStartOfDay()
        }
    }

    @Test
    fun `skal hente første relevante vedtak i vedtaksrekke`() {
        // gitt
        val vedtakssett = oppretteVedtakssett(
            setOf(
                OppretteVedtakRequest(Årstall.R12, Årstall.R10, Beløp.B1000, beslutningsårsak = Beslutningsårsak.INNVILGETT_VEDTAK),
                OppretteVedtakRequest(Årstall.R10, null, Beløp.B1200, Beslutningsårsak.INDEKSREGULERING, kilde = Vedtakskilde.AUTOMATISK),
            ),
        )

        // hvis
        val vedtak = vedtaksfiltrering.finneVedtakForEvnevurdering(vedtakssett, ba1.personident)

        // så
        assertSoftly {
            vedtak != null
            vedtak!!.kilde shouldBe Vedtakskilde.MANUELT
            vedtak.stønadsendring.shouldNotBeNull()
            vedtak.stønadsendring.periodeListe.first().delytelseId shouldBe "10000"
        }
    }
}
