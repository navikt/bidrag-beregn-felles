package no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.beregning.SamvaersfradragBeregning
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.BeregnSamvaersfradragGrunnlag
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.SamvaersfradragGrunnlagPeriode
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.periode.SamvaersfradragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class SamvaersfradragPeriodeTest {

    private val samvaersfradragBeregning = SamvaersfradragBeregning()
    private val samvaersfradragPeriode = SamvaersfradragPeriode(samvaersfradragBeregning)

    @Test
    @DisplayName("Test av periodisering. Resultatperioden skal være lik beregnDatoFra -> beregnDatoTil")
    fun testPeriodisering() {
        // Lag samværsinfo
        val samvaersfradragGrunnlagPeriodeListe = mutableListOf<SamvaersfradragGrunnlagPeriode>()
        samvaersfradragGrunnlagPeriodeListe.add(
            SamvaersfradragGrunnlagPeriode(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                samvaersfradragDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-10-01")),
                barnPersonId = 1,
                barnFodselsdato = LocalDate.parse("2016-03-17"),
                samvaersklasse = "02",
            ),
        )
        samvaersfradragGrunnlagPeriodeListe.add(
            SamvaersfradragGrunnlagPeriode(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                samvaersfradragDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-10-01")),
                barnPersonId = 2,
                barnFodselsdato = LocalDate.parse("2017-05-17"),
                samvaersklasse = "02",
            ),
        )

        // Lag sjabloner
        val sjablonPeriodeListe = listOf(
            SjablonPeriode(
                Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2020-06-30")),
                Sjablon(
                    navn = SjablonNavn.SAMVÆRSFRADRAG.navn,
                    nokkelListe = listOf(
                        SjablonNokkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = "02"),
                        SjablonNokkel(navn = SjablonNøkkelNavn.ALDER_TOM.navn, verdi = "5"),
                    ),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, verdi = BigDecimal.ZERO),
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, verdi = BigDecimal.valueOf(8)),
                        SjablonInnhold(navn = SjablonInnholdNavn.FRADRAG_BELØP.navn, verdi = BigDecimal.valueOf(727)),
                    ),
                ),
            ),
        )

        val beregnSamvaersfradragGrunnlag = BeregnSamvaersfradragGrunnlag(
            beregnDatoFra = LocalDate.parse("2020-06-01"),
            beregnDatoTil = LocalDate.parse("2020-07-01"),
            samvaersfradragGrunnlagPeriodeListe = samvaersfradragGrunnlagPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )

        val resultat = samvaersfradragPeriode.beregnPerioder(beregnSamvaersfradragGrunnlag)

        assertAll(
            { assertThat(resultat.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatBeregningListe).hasSize(2) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatDatoFraTil.datoFom).isEqualTo(LocalDate.parse("2020-06-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatDatoFraTil.datoTil).isEqualTo(LocalDate.parse("2020-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatBeregningListe[0].belop).isEqualByComparingTo(BigDecimal.valueOf(727)) },
            { assertThat(resultat.resultatPeriodeListe[0].resultatBeregningListe[1].belop).isEqualByComparingTo(BigDecimal.valueOf(727)) },
        )
    }

    @Test
    @DisplayName("Test med feil i grunnlag som skal resultere i avvik")
    fun testGrunnlagMedAvvik() {
        // Lag samværsinfo
        val samvaersklassePeriodeListe = listOf(
            SamvaersfradragGrunnlagPeriode(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                samvaersfradragDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-07-01")),
                barnPersonId = 1,
                barnFodselsdato = LocalDate.parse("2014-03-17"),
                samvaersklasse = "02",
            ),
        )

        // Lag sjabloner
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2020-06-30")),
                sjablon = Sjablon(
                    navn = SjablonNavn.SAMVÆRSFRADRAG.navn,
                    nokkelListe = listOf(
                        SjablonNokkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = "02"),
                        SjablonNokkel(navn = SjablonNøkkelNavn.ALDER_TOM.navn, verdi = "5"),
                    ),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, verdi = BigDecimal.ZERO),
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, verdi = BigDecimal.valueOf(8)),
                        SjablonInnhold(navn = SjablonInnholdNavn.FRADRAG_BELØP.navn, verdi = BigDecimal.valueOf(727)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = null),
                Sjablon(
                    navn = SjablonNavn.SAMVÆRSFRADRAG.navn,
                    nokkelListe = listOf(
                        SjablonNokkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = "02"),
                        SjablonNokkel(navn = SjablonNøkkelNavn.ALDER_TOM.navn, verdi = "10"),
                    ),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, verdi = BigDecimal.ZERO),
                        SjablonInnhold(navn = SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, verdi = BigDecimal.valueOf(8)),
                        SjablonInnhold(navn = SjablonInnholdNavn.FRADRAG_BELØP.navn, verdi = BigDecimal.valueOf(1052)),
                    ),
                ),
            ),
        )

        val beregnSamvaersfradragGrunnlag = BeregnSamvaersfradragGrunnlag(
            beregnDatoFra = LocalDate.parse("2018-07-01"),
            beregnDatoTil = LocalDate.parse("2021-01-01"),
            samvaersfradragGrunnlagPeriodeListe = samvaersklassePeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )

        val avvikListe = samvaersfradragPeriode.validerInput(beregnSamvaersfradragGrunnlag)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(2) },
            {
                assertThat(avvikListe[0].avvikTekst)
                    .isEqualTo("Første dato i samvaersfradragGrunnlagPeriodeListe (2019-01-01) er etter beregnDatoFom (2018-07-01)")
            },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(avvikListe[1].avvikTekst)
                    .isEqualTo("Siste dato i samvaersfradragGrunnlagPeriodeListe (2020-07-01) er før beregnDatoTil (2021-01-01)")
            },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
        )
    }
}
