package no.nav.bidrag.beregn.felles.inntekt

import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagListeDelvisOverlappSammeGruppe
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagListeMedLikDatoFomLikGruppe
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagListeMedLikDatoFomUlikGruppe
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagListeMedLikDatoFomUtenGruppe
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagUtvidetBarnetrygdFull
import no.nav.bidrag.beregn.felles.TestUtil.byggInntektGrunnlagUtvidetBarnetrygdOvergang
import no.nav.bidrag.beregn.felles.TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdFull
import no.nav.bidrag.beregn.felles.TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdOvergang
import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.util.InntektUtil.behandlUtvidetBarnetrygd
import no.nav.bidrag.beregn.felles.util.InntektUtil.justerInntekter
import no.nav.bidrag.beregn.felles.util.InntektUtil.validerInntekter
import no.nav.bidrag.domain.enums.AvvikType
import no.nav.bidrag.domain.enums.Formaal
import no.nav.bidrag.domain.enums.InntektType
import no.nav.bidrag.domain.enums.Rolle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("InntektValidatorTest")
internal class InntektTest {
    private var inntektPeriodeGrunnlagListe = listOf<InntektPeriodeGrunnlag>()
    private var avvikListe = listOf<Avvik>()

    @Test
    @DisplayName("Formål ikke gyldig for inntektstype")
    fun testUgyldigFormaal() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                type = InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(
            inntektPeriodeGrunnlagListe = inntektGrunnlagListe,
            formaal = Formaal.SAERTILSKUDD,
            rolle = Rolle.BIDRAGSMOTTAKER
        )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE) },
            Executable {
                assertThat(avvikListe[0].avvikTekst).isEqualTo(
                    "inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG +
                        " er ugyldig for formaal " + Formaal.SAERTILSKUDD + " og rolle " + Rolle.BIDRAGSMOTTAKER
                )
            }
        )
    }

    @Test
    @DisplayName("Rolle ikke gyldig for inntektstype")
    fun testUgyldigRolle() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                type = InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.BIDRAG, rolle = Rolle.BIDRAGSPLIKTIG)

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE) },
            Executable {
                assertThat(avvikListe[0].avvikTekst).isEqualTo(
                    "inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG +
                        " er ugyldig for formaal " + Formaal.BIDRAG + " og rolle " + Rolle.BIDRAGSPLIKTIG
                )
            }
        )
    }

    @Test
    @DisplayName("datoFom ikke gyldig for inntektstype")
    fun testUgyldigDatoFom() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-12-31")),
                type = InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.FORSKUDD, rolle = Rolle.BIDRAGSMOTTAKER)

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE) },
            Executable {
                assertThat(avvikListe[0].avvikTekst).isEqualTo(
                    "inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG +
                        " er kun gyldig fom. " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.gyldigFom.toString() + " tom. " +
                        InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.gyldigTil.toString()
                )
            }
        )
    }

    @Test
    @DisplayName("datoTil ikke gyldig for inntektstype")
    fun testUgyldigDatoTil() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2016-01-01"), datoTil = LocalDate.parse("2019-12-31")),
                type = InntektType.BARNS_SYKDOM,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.BIDRAG, rolle = Rolle.BIDRAGSMOTTAKER)

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE) },
            Executable {
                assertThat(avvikListe[0].avvikTekst).isEqualTo(
                    "inntektType " + InntektType.BARNS_SYKDOM + " er kun gyldig fom. " +
                        InntektType.BARNS_SYKDOM.gyldigFom.toString() + " tom. " + InntektType.BARNS_SYKDOM.gyldigTil.toString()
                )
            }
        )
    }

    @Test
    @DisplayName("datoTil 9999-12-31 gyldig for inntektstype")
    fun testGyldigDatoTil99991231() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2016-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                type = InntektType.BARNS_SYKDOM,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.BIDRAG, rolle = Rolle.BIDRAGSMOTTAKER)

        assertAll(
            Executable { assertThat(avvikListe).isEmpty() }
        )
    }

    @Test
    @DisplayName("datoTil LocalDate.MAX gyldig for inntektstype")
    fun testGyldigDatoTilMAX() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2016-01-01"), datoTil = LocalDate.MAX),
                type = InntektType.BARNS_SYKDOM,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.BIDRAG, rolle = Rolle.BIDRAGSMOTTAKER)

        assertAll(
            Executable { assertThat(avvikListe).isEmpty() }
        )
    }

    @Test
    @DisplayName("datoTil null gyldig for inntektstype")
    fun testGyldigDatoTilNull() {
        val inntektGrunnlagListe = listOf(
            InntektPeriodeGrunnlag(
                referanse = "REF",
                inntektPeriode = Periode(datoFom = LocalDate.parse("2016-01-01"), datoTil = null),
                type = InntektType.BARNS_SYKDOM,
                belop = BigDecimal.valueOf(200000),
                deltFordel = false,
                skatteklasse2 = false
            )
        )
        avvikListe = validerInntekter(inntektPeriodeGrunnlagListe = inntektGrunnlagListe, formaal = Formaal.BIDRAG, rolle = Rolle.BIDRAGSMOTTAKER)

        assertAll(
            Executable { assertThat(avvikListe).isEmpty() }
        )
    }

    @Test
    @DisplayName("Flere inntekter innenfor samme gruppe med lik datoFom")
    fun testUgyldigSammeGruppeLikDatoFom() {
        avvikListe = validerInntekter(byggInntektGrunnlagListeMedLikDatoFomLikGruppe(), Formaal.BIDRAG, Rolle.BIDRAGSMOTTAKER)
        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable {
                assertThat(avvikListe[0].avvikType).isEqualTo(AvvikType.OVERLAPPENDE_INNTEKT)
            },
            Executable {
                assertThat(avvikListe[0].avvikTekst)
                    .contains("tilhører samme inntektsgruppe og har samme datoFom")
            }
        )
    }

    @Test
    @DisplayName("Flere inntekter fra forskjellige grupper med lik datoFom")
    fun testGyldigUlikGruppeLikDatoFom() {
        avvikListe = validerInntekter(
            inntektPeriodeGrunnlagListe = byggInntektGrunnlagListeMedLikDatoFomUlikGruppe(),
            formaal = Formaal.BIDRAG,
            rolle = Rolle.BIDRAGSMOTTAKER
        )

        assertAll(
            Executable { assertThat(avvikListe).isEmpty() }
        )
    }

    @Test
    @DisplayName("Flere inntekter uten gruppe med lik datoFom")
    fun testGyldigUtenGruppeLikDatoFom() {
        avvikListe = validerInntekter(
            inntektPeriodeGrunnlagListe = byggInntektGrunnlagListeMedLikDatoFomUtenGruppe(),
            formaal = Formaal.BIDRAG,
            rolle = Rolle.BIDRAGSMOTTAKER
        )

        assertAll(
            Executable { assertThat(avvikListe).isEmpty() }
        )
    }

    @Test
    @DisplayName("Juster perioder for inntekter innefor samme gruppe som delvis overlapper")
    fun testJusterDelvisOverlappSammeGruppe() {
        inntektPeriodeGrunnlagListe = justerInntekter(byggInntektGrunnlagListeDelvisOverlappSammeGruppe())

        assertAll(
            Executable { assertThat(inntektPeriodeGrunnlagListe).isNotEmpty() },
            Executable { assertThat(inntektPeriodeGrunnlagListe.size).isEqualTo(5) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[0].type).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[0].belop).isEqualTo(BigDecimal.valueOf(200000)) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[0].getPeriode().datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[0].getPeriode().datoTil).isEqualTo(LocalDate.parse("2018-05-31")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[1].type).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[1].belop).isEqualTo(BigDecimal.valueOf(150000)) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[1].getPeriode().datoFom).isEqualTo(LocalDate.parse("2018-06-01")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[1].getPeriode().datoTil).isEqualTo(LocalDate.parse("2018-12-31")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[2].type).isEqualTo(InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[2].belop).isEqualTo(BigDecimal.valueOf(300000)) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[2].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[2].getPeriode().datoTil).isEqualTo(LocalDate.parse("2019-12-31")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[3].type).isEqualTo(InntektType.ALOYSE) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[3].belop).isEqualTo(BigDecimal.valueOf(250000)) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[3].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-01-01")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[3].getPeriode().datoTil).isEqualTo(LocalDate.MAX) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[4].type).isEqualTo(InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[4].belop).isEqualTo(BigDecimal.valueOf(100000)) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[4].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            Executable { assertThat(inntektPeriodeGrunnlagListe[4].getPeriode().datoTil).isEqualTo(LocalDate.MAX) }
        )
    }

    @Test
    @DisplayName("Utvidet barnetrygd - full test av regelverk")
    fun testUtvidetBarnetrygdFullTest() {
        val nyInntektGrunnlagListe =
            behandlUtvidetBarnetrygd(
                inntektPeriodeGrunnlagListe = byggInntektGrunnlagUtvidetBarnetrygdFull(),
                sjablonPeriodeListe = byggSjablontallGrunnlagUtvidetBarnetrygdFull()
            )

        assertAll(
            Executable { assertThat(nyInntektGrunnlagListe).isNotEmpty() },
            Executable { assertThat(nyInntektGrunnlagListe.size).isEqualTo(14) },
            Executable { assertThat(nyInntektGrunnlagListe[9].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-04-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[9].getPeriode().datoTil).isEqualTo(LocalDate.parse("2019-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[9].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[9].belop).isEqualTo(BigDecimal.valueOf(13000)) },
            Executable { assertThat(nyInntektGrunnlagListe[10].getPeriode().datoFom).isEqualTo(LocalDate.parse("2019-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[10].getPeriode().datoTil).isEqualTo(LocalDate.parse("2019-08-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[10].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[10].belop).isEqualTo(BigDecimal.valueOf(6500)) },
            Executable { assertThat(nyInntektGrunnlagListe[11].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-04-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[11].getPeriode().datoTil).isEqualTo(LocalDate.parse("2020-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[11].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[11].belop).isEqualTo(BigDecimal.valueOf(13000)) },
            Executable { assertThat(nyInntektGrunnlagListe[12].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[12].getPeriode().datoTil).isEqualTo(LocalDate.parse("2020-08-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[12].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[12].belop).isEqualTo(BigDecimal.valueOf(14000)) },
            Executable { assertThat(nyInntektGrunnlagListe[13].getPeriode().datoFom).isEqualTo(LocalDate.parse("2020-08-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[13].getPeriode().datoTil).isEqualTo(LocalDate.parse("2021-01-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[13].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[13].belop).isEqualTo(BigDecimal.valueOf(7000)) }
        )
    }

    @Test
    @DisplayName("Utvidet barnetrygd - test av overgang mellom regelverk for skatteklasse 2 og fordel særfradrag")
    fun testUtvidetBarnetrygdOvergangSkatteklasse2FordelSaerfradrag() {
        val nyInntektGrunnlagListe = behandlUtvidetBarnetrygd(
            inntektPeriodeGrunnlagListe = byggInntektGrunnlagUtvidetBarnetrygdOvergang(),
            sjablonPeriodeListe = byggSjablontallGrunnlagUtvidetBarnetrygdOvergang()
        )

        assertAll(
            Executable { assertThat(nyInntektGrunnlagListe).isNotEmpty() },
            Executable { assertThat(nyInntektGrunnlagListe.size).isEqualTo(5) },
            Executable { assertThat(nyInntektGrunnlagListe[2].getPeriode().datoFom).isEqualTo(LocalDate.parse("2012-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[2].getPeriode().datoTil).isEqualTo(LocalDate.parse("2012-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[2].type).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2.name) },
            Executable { assertThat(nyInntektGrunnlagListe[2].belop).isEqualTo(BigDecimal.valueOf(7500)) },
            Executable { assertThat(nyInntektGrunnlagListe[3].getPeriode().datoFom).isEqualTo(LocalDate.parse("2012-07-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[3].getPeriode().datoTil).isEqualTo(LocalDate.parse("2013-01-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[3].type).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2.name) },
            Executable { assertThat(nyInntektGrunnlagListe[3].belop).isEqualTo(BigDecimal.valueOf(8500)) },
            Executable { assertThat(nyInntektGrunnlagListe[4].getPeriode().datoFom).isEqualTo(LocalDate.parse("2013-01-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[4].getPeriode().datoTil).isEqualTo(LocalDate.parse("2013-06-01")) },
            Executable { assertThat(nyInntektGrunnlagListe[4].type).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.name) },
            Executable { assertThat(nyInntektGrunnlagListe[4].belop).isEqualTo(BigDecimal.valueOf(12500)) }
        )
    }
}
