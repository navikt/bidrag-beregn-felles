package no.nav.bidrag.beregn.særtilskudd.core.bpsandelsærtilskudd

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.beregning.BPsAndelSaertilskuddBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddGrunnlag
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.InntektPeriode
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.NettoSaertilskuddPeriode
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.periode.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class BPsAndelSaertilskuddPeriodeTest {

    private val bPsAndelSaertilskuddBeregning = BPsAndelSaertilskuddBeregning()
    private val bPsAndelSaertilskuddPeriode = BPsAndelSaertilskuddPeriode(bPsAndelSaertilskuddBeregning)

    @Test
    @DisplayName("Test av periodisering. Periodene i grunnlaget skal gjenspeiles i resultatperiodene")
    fun testPeriodisering() {
        val grunnlag = lagGrunnlag("2018-07-01", "2020-08-01")
        val resultat = bPsAndelSaertilskuddPeriode.beregnPerioder(grunnlag)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultat.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatDatoFraTil.datoFom).isEqualTo(LocalDate.parse("2018-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatDatoFraTil.datoTil).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatBeregning.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(35.2)) },
            { assertThat(resultat.resultatPeriodeListe[1].resultatDatoFraTil.datoFom).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[1].resultatDatoFraTil.datoTil).isEqualTo(LocalDate.parse("2020-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].resultatDatoFraTil.datoFom).isEqualTo(LocalDate.parse("2020-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].resultatDatoFraTil.datoTil).isNull() },
        )
    }

    @Test
    @DisplayName("Test med feil i grunnlag som skal resultere i avvik")
    fun testGrunnlagMedAvvik() {
        val grunnlag = lagGrunnlag("2016-01-01", "2021-01-01")
        val avvikListe = bPsAndelSaertilskuddPeriode.validerInput(grunnlag)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(8) },
            {
                assertThat(avvikListe[0].avvikTekst)
                    .isEqualTo("Første dato i nettoSaertilskuddPeriodeListe (2018-01-01) er etter beregnDatoFom (2016-01-01)")
            },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(
                    avvikListe[1].avvikTekst,
                ).isEqualTo("Siste dato i nettoSaertilskuddPeriodeListe (2020-08-01) er før beregnDatoTil (2021-01-01)")
            },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
        )
    }

    private fun lagGrunnlag(beregnDatoFra: String, beregnDatoTil: String): BeregnBPsAndelSaertilskuddGrunnlag {
        val nettoSaertilskuddPeriodeListe = listOf(
            NettoSaertilskuddPeriode(
                referanse = TestUtil.NETTO_SÆRTILSKUDD_REFERANSE,
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
            ),
        )
        val inntektBPPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                inntektType = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                inntektBelop = BigDecimal.valueOf(217666),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBMPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                inntektType = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                inntektBelop = BigDecimal.valueOf(400000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBBPeriodeListe = listOf(
            InntektPeriode(
                referanse = "Inntekt_20180101",
                periodeDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                inntektType = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                inntektBelop = BigDecimal.valueOf(40000),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        return BeregnBPsAndelSaertilskuddGrunnlag(
            beregnDatoFra = LocalDate.parse(beregnDatoFra),
            beregnDatoTil = LocalDate.parse(beregnDatoTil),
            nettoSaertilskuddPeriodeListe = nettoSaertilskuddPeriodeListe,
            inntektBPPeriodeListe = inntektBPPeriodeListe,
            inntektBMPeriodeListe = inntektBMPeriodeListe,
            inntektBBPeriodeListe = inntektBBPeriodeListe,
            sjablonPeriodeListe = lagSjablonGrunnlag(),
        )
    }

    private fun lagSjablonGrunnlag(): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-06-30")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = LocalDate.parse("2020-06-30")),
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
