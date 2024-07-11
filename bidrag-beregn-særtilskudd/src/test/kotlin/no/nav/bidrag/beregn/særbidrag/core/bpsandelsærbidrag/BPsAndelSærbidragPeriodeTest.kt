package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning.BPsAndelSærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.UtgiftPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class BPsAndelSærbidragPeriodeTest {

    private val bPsAndelSærbidragBeregning = BPsAndelSærbidragBeregning()
    private val bPsAndelSærbidragPeriode = BPsAndelSærbidragPeriode(bPsAndelSærbidragBeregning)

    @Test
    @DisplayName("Test av periodisering. Periodene i grunnlaget skal gjenspeiles i resultatperiodene")
    fun testPeriodisering() {
        val grunnlag = lagGrunnlag("2018-07-01", "2020-08-01")
        val resultat = bPsAndelSærbidragPeriode.beregnPerioder(grunnlag)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultat.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2018-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(35.2)) },
            { assertThat(resultat.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2020-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2020-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].periode.datoTil).isNull() },
        )
    }

    @Test
    @DisplayName("Test med feil i grunnlag som skal resultere i avvik")
    fun testGrunnlagMedAvvik() {
        val grunnlag = lagGrunnlag("2016-01-01", "2021-01-01")
        val avvikListe = bPsAndelSærbidragPeriode.validerInput(grunnlag)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(8) },
            {
                assertThat(avvikListe[0].avvikTekst)
                    .isEqualTo("Første dato i utgiftPeriodeListe (2018-01-01) er etter beregnDatoFom (2016-01-01)")
            },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(
                    avvikListe[1].avvikTekst,
                ).isEqualTo("Siste dato i utgiftPeriodeListe (2020-08-01) er før beregnDatoTil (2021-01-01)")
            },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
        )
    }

    private fun lagGrunnlag(beregnDatoFra: String, beregnDatoTil: String): BeregnBPsAndelSærbidragGrunnlag {
        val utgiftPeriodeListe = listOf(
            UtgiftPeriode(
                referanse = TestUtil.UTGIFT_REFERANSE,
                periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                beløp = BigDecimal.valueOf(1000),
            ),
        )
        val inntektBPPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(217666),
            ),
        )
        val inntektBMPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        val inntektSBPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(40000),
            ),
        )

        return BeregnBPsAndelSærbidragGrunnlag(
            beregnDatoFra = LocalDate.parse(beregnDatoFra),
            beregnDatoTil = LocalDate.parse(beregnDatoTil),
            utgiftPeriodeListe = utgiftPeriodeListe,
            inntektBPPeriodeListe = inntektBPPeriodeListe,
            inntektBMPeriodeListe = inntektBMPeriodeListe,
            inntektSBPeriodeListe = inntektSBPeriodeListe,
            sjablonPeriodeListe = lagSjablonGrunnlag(),
        )
    }

    private fun lagSjablonGrunnlag(): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = LocalDate.parse("2020-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1640))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1670))),
                ),
            ),
        )

        return sjablonPeriodeListe
    }
}
