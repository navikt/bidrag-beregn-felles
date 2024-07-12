package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BarnIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.VoksneIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class BidragsevnePeriodeTest {

    private val bidragsevneBeregning = BidragsevneBeregning()
    private val bidragsevnePeriode = BidragsevnePeriode(bidragsevneBeregning)

    @Test
    @DisplayName("Test med OK grunnlag")
    fun testGrunnlagOk() {
        val grunnlag = lagGrunnlag()
        val resultat = bidragsevnePeriode.beregnPerioder(grunnlag)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultat.resultatPeriodeListe).hasSize(6) },

            { assertThat(resultat.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2018-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultat.resultatPeriodeListe[0].resultat.beløp).isEqualTo(BigDecimal.valueOf(2655)) },
            { assertThat(resultat.resultatPeriodeListe[0].grunnlag.inntektListe[0].inntektBeløp).isEqualTo(BigDecimal.valueOf(444000)) },

            { assertThat(resultat.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultat.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2019-02-01")) },
            { assertThat(resultat.resultatPeriodeListe[1].resultat.beløp).isEqualTo(BigDecimal.valueOf(14523)) },
            { assertThat(resultat.resultatPeriodeListe[1].grunnlag.bostatusVoksneIHusstand.borMedAndre).isFalse() },

            { assertThat(resultat.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2019-02-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2019-04-01")) },
            { assertThat(resultat.resultatPeriodeListe[2].resultat.beløp).isEqualTo(BigDecimal.valueOf(19455)) },

            { assertThat(resultat.resultatPeriodeListe[3].periode.datoFom).isEqualTo(LocalDate.parse("2019-04-01")) },
            { assertThat(resultat.resultatPeriodeListe[3].periode.datoTil).isEqualTo(LocalDate.parse("2019-05-01")) },
            { assertThat(resultat.resultatPeriodeListe[3].resultat.beløp).isEqualTo(BigDecimal.valueOf(19455)) },
            { assertThat(resultat.resultatPeriodeListe[3].grunnlag.inntektListe[0].inntektBeløp).isEqualTo(BigDecimal.valueOf(666001)) },

            { assertThat(resultat.resultatPeriodeListe[4].periode.datoFom).isEqualTo(LocalDate.parse("2019-05-01")) },
            { assertThat(resultat.resultatPeriodeListe[4].periode.datoTil).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[4].resultat.beløp).isEqualTo(BigDecimal.valueOf(19455)) },
            { assertThat(resultat.resultatPeriodeListe[4].grunnlag.inntektListe[0].inntektBeløp).isEqualTo(BigDecimal.valueOf(666001)) },
            { assertThat(resultat.resultatPeriodeListe[4].grunnlag.inntektListe[1].inntektBeløp).isEqualTo(BigDecimal.valueOf(2)) },

            { assertThat(resultat.resultatPeriodeListe[5].periode.datoFom).isEqualTo(LocalDate.parse("2019-07-01")) },
            { assertThat(resultat.resultatPeriodeListe[5].periode.datoTil).isNull() },
            { assertThat(resultat.resultatPeriodeListe[5].resultat.beløp).isEqualTo(BigDecimal.valueOf(18982)) },
            { assertThat(resultat.resultatPeriodeListe[5].grunnlag.inntektListe[0].inntektBeløp).isEqualTo(BigDecimal.valueOf(666001)) },
            { assertThat(resultat.resultatPeriodeListe[5].grunnlag.inntektListe[1].inntektBeløp).isEqualTo(BigDecimal.valueOf(2)) },
            { assertThat(resultat.resultatPeriodeListe[5].grunnlag.inntektListe[2].inntektBeløp).isEqualTo(BigDecimal.valueOf(3)) },
        )
    }

    @Test
    @DisplayName("Test med feil i grunnlag som skal resultere i avvik")
    fun testGrunnlagMedAvvik() {
        val grunnlagMedAvvik = lagGrunnlagMedAvvik()
        val avvikListe = bidragsevnePeriode.validerInput(grunnlagMedAvvik)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(4) },
            { assertThat(avvikListe[0].avvikTekst).isEqualTo("Første dato i inntektPeriodeListe (2003-01-01) er etter beregnDatoFom (2001-07-01)") },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            { assertThat(avvikListe[1].avvikTekst).isEqualTo("Siste dato i inntektPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)") },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(
                    avvikListe[2].avvikTekst,
                ).isEqualTo("Siste dato i barnIHusstandPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)")
            },
            { assertThat(avvikListe[2].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(avvikListe[3].avvikTekst)
                    .isEqualTo("Siste dato i voksneIHusstandPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)")
            },
            { assertThat(avvikListe[3].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
        )
    }

    private fun lagGrunnlag() = BeregnBidragsevneGrunnlag(
        beregnDatoFra = LocalDate.parse("2018-07-01"),
        beregnDatoTil = LocalDate.parse("2020-01-01"),
        inntektPeriodeListe = lagInntektGrunnlag(),
        barnIHusstandPeriodeListe = lagBarnIHusstandGrunnlag(),
        voksneIHusstandPeriodeListe = lagVoksneIHusstandGrunnlag(),
        sjablonPeriodeListe = lagSjablonGrunnlag(),
    )

    private fun lagGrunnlagMedAvvik() = BeregnBidragsevneGrunnlag(
        beregnDatoFra = LocalDate.parse("2001-07-01"),
        beregnDatoTil = LocalDate.parse("2021-01-01"),
        inntektPeriodeListe = lagInntektGrunnlag(),
        barnIHusstandPeriodeListe = lagBarnIHusstandGrunnlag(),
        voksneIHusstandPeriodeListe = lagVoksneIHusstandGrunnlag(),
        sjablonPeriodeListe = lagSjablonGrunnlag(),
    )

    private fun lagInntektGrunnlag(): List<InntektPeriode> {
        val inntektPeriodeListe = mutableListOf<InntektPeriode>()
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "Inntekt_20030101",
                periode = Periode(datoFom = LocalDate.parse("2003-01-01"), datoTil = LocalDate.parse("2004-01-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(666000),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "Inntekt_20040101",
                periode = Periode(datoFom = LocalDate.parse("2004-01-01"), datoTil = LocalDate.parse("2016-01-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(555000),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "Inntekt_20160101",
                periode = Periode(datoFom = LocalDate.parse("2016-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(444000),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "Inntekt_20190101",
                periode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2019-04-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(666000),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "Inntekt_20190401",
                periode = Periode(datoFom = LocalDate.parse("2019-04-01"), datoTil = LocalDate.parse("2020-01-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(666001),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "20190501",
                periode = Periode(datoFom = LocalDate.parse("2019-05-01"), datoTil = LocalDate.parse("2020-01-01")),
                type = "OVERGANGSSTONAD",
                beløp = BigDecimal.valueOf(2),
            ),
        )
        inntektPeriodeListe.add(
            InntektPeriode(
                referanse = "20190701",
                periode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = LocalDate.parse("2020-01-01")),
                type = "KONTANTSTOTTE",
                beløp = BigDecimal.valueOf(3),
            ),
        )

        return inntektPeriodeListe
    }

    private fun lagBarnIHusstandGrunnlag(): List<BarnIHusstandPeriode> {
        val antallBarnIHusstandPeriodeListe = mutableListOf<BarnIHusstandPeriode>()
        antallBarnIHusstandPeriodeListe.add(
            BarnIHusstandPeriode(
                referanse = "BarnIHusstand",
                periode = Periode(datoFom = LocalDate.parse("2001-01-01"), datoTil = LocalDate.parse("2017-01-01")),
                antall = 1.0,
            ),
        )
        antallBarnIHusstandPeriodeListe.add(
            BarnIHusstandPeriode(
                referanse = "BarnIHusstand",
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                antall = 2.0,
            ),
        )

        return antallBarnIHusstandPeriodeListe
    }

    private fun lagVoksneIHusstandGrunnlag(): List<VoksneIHusstandPeriode> {
        val bostatusVoksneIHusstandPeriodeListe = mutableListOf<VoksneIHusstandPeriode>()
        bostatusVoksneIHusstandPeriodeListe.add(
            VoksneIHusstandPeriode(
                referanse = "VoksneIHusstand",
                periode = Periode(datoFom = LocalDate.parse("2001-01-01"), datoTil = LocalDate.parse("2017-01-01")),
                borMedAndre = true,
            ),
        )
        bostatusVoksneIHusstandPeriodeListe.add(
            VoksneIHusstandPeriode(
                referanse = "VoksneIHusstand",
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2019-02-01")),
                borMedAndre = false,
            ),
        )
        bostatusVoksneIHusstandPeriodeListe.add(
            VoksneIHusstandPeriode(
                referanse = "VoksneIHusstand",
                periode = Periode(datoFom = LocalDate.parse("2019-02-01"), datoTil = LocalDate.parse("2020-01-01")),
                borMedAndre = true,
            ),
        )

        return bostatusVoksneIHusstandPeriodeListe
    }

    private fun lagSjablonGrunnlag(): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2003-01-01"), datoTil = LocalDate.parse("2014-01-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(7.8))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2014-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(8.2))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(3417))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(3487))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2005-01-01"), datoTil = LocalDate.parse("2005-06-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(57400))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-07-01"), datoTil = LocalDate.parse("2018-01-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(75000.0))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2018-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(75000))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(83000))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(85050))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(31))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(54750))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(56550))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(23))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(169000)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(1.4)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(237900)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(3.3)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(598050)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(12.4)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(962050)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(15.4)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(174500)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(1.9)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(245650)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(4.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(617500)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(13.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(964800)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(16.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(180800)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(1.9)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(254500)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(4.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(639750)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(13.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    nokkelListe = emptyList(),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, verdi = BigDecimal.valueOf(999550)),
                        SjablonInnhold(navn = SjablonInnholdNavn.SKATTESATS_PROSENT.navn, verdi = BigDecimal.valueOf(16.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.BIDRAGSEVNE.navn,
                    nokkelListe = listOf(SjablonNokkel(SjablonNøkkelNavn.BOSTATUS.navn, "EN")),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.BOUTGIFT_BELØP.navn, verdi = BigDecimal.valueOf(9303)),
                        SjablonInnhold(navn = SjablonInnholdNavn.UNDERHOLD_BELØP.navn, verdi = BigDecimal.valueOf(8657)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2018-07-01"), datoTil = LocalDate.parse("2019-07-01")),
                sjablon = Sjablon(
                    navn = SjablonNavn.BIDRAGSEVNE.navn,
                    nokkelListe = listOf(SjablonNokkel(SjablonNøkkelNavn.BOSTATUS.navn, "GS")),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.BOUTGIFT_BELØP.navn, verdi = BigDecimal.valueOf(5698)),
                        SjablonInnhold(navn = SjablonInnholdNavn.UNDERHOLD_BELØP.navn, verdi = BigDecimal.valueOf(7330)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.BIDRAGSEVNE.navn,
                    nokkelListe = listOf(SjablonNokkel(SjablonNøkkelNavn.BOSTATUS.navn, "EN")),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.BOUTGIFT_BELØP.navn, verdi = BigDecimal.valueOf(9591)),
                        SjablonInnhold(navn = SjablonInnholdNavn.UNDERHOLD_BELØP.navn, verdi = BigDecimal.valueOf(8925)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                sjablonPeriode = Periode(datoFom = LocalDate.parse("2019-07-01"), datoTil = null),
                sjablon = Sjablon(
                    navn = SjablonNavn.BIDRAGSEVNE.navn,
                    nokkelListe = listOf(SjablonNokkel(SjablonNøkkelNavn.BOSTATUS.navn, "GS")),
                    innholdListe = listOf(
                        SjablonInnhold(navn = SjablonInnholdNavn.BOUTGIFT_BELØP.navn, verdi = BigDecimal.valueOf(5875)),
                        SjablonInnhold(navn = SjablonInnholdNavn.UNDERHOLD_BELØP.navn, verdi = BigDecimal.valueOf(7557)),
                    ),
                ),
            ),
        )
        return sjablonPeriodeListe
    }
}
