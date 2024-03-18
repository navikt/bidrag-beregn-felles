package no.nav.bidrag.beregn.forskudd.core

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlag
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlagMedAvvik
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlagMedFlereInntekterISammePeriode
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlagUtenAndreBarn
import no.nav.bidrag.beregn.forskudd.TestUtil.byggForskuddGrunnlagUtenSivilstand
import no.nav.bidrag.beregn.forskudd.TestUtil.byggSjablonPeriodeListe
import no.nav.bidrag.beregn.forskudd.TestUtil.byggSjablonPeriodeNavnVerdiListe
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddResultat
import no.nav.bidrag.beregn.forskudd.core.bo.InntektPeriode
import no.nav.bidrag.beregn.forskudd.core.periode.ForskuddPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.math.BigDecimal
import java.time.LocalDate

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ForskuddPeriodeTest {
    private val forskuddBeregning = ForskuddBeregning()
    private val forskuddPeriode = ForskuddPeriode(forskuddBeregning)
    private val beregnForskuddGrunnlag = byggForskuddGrunnlag()
    private val beregnForskuddGrunnlagMedAvvik = byggForskuddGrunnlagMedAvvik()
    private val beregnForskuddGrunnlagUtenBarn = byggForskuddGrunnlagUtenAndreBarn()
    private val beregnForskuddGrunnlagUtenSivilstand = byggForskuddGrunnlagUtenSivilstand()

    @Test
    @DisplayName("Test utvidet grunnlag")
    fun testUtvidetGrunnlag() {
        val resultat = forskuddPeriode.beregnPerioder(beregnForskuddGrunnlag)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beregnetForskuddPeriodeListe).isNotEmpty() },
            { assertThat(resultat.beregnetForskuddPeriodeListe).hasSize(9) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2017-12-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].resultat.kode,
                ).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_100_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].resultat.regel).isEqualTo("REGEL 6") },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].resultat.sjablonListe,
                ).isEqualTo(byggSjablonPeriodeNavnVerdiListe())
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe).hasSize(1) },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe[0].type)
                    .isEqualTo("INNTEKTSOPPLYSNINGER_ARBEIDSGIVER")
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe[0].beløp,
                ).isEqualTo(BigDecimal.valueOf(250000))
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe[0].referanse,
                ).isEqualTo(INNTEKT_REFERANSE_1)
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.sivilstand.kode,
                ).isEqualTo(Sivilstandskode.GIFT_SAMBOER)
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.sivilstand.referanse,
                ).isEqualTo(SIVILSTAND_REFERANSE_GIFT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.barnIHusstandenListe.count()).isEqualTo(3) },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.barnIHusstandenListe[0].referanse)
                    .isEqualTo(BARN_I_HUSSTANDEN_REFERANSE_1)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnAlder.alder).isZero() },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnAlder.referanse,
                ).isEqualTo(SØKNADSBARN_REFERANSE)
            },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnBostatus.kode)
                    .isEqualTo(Bostatuskode.MED_FORELDER)
            },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnBostatus.referanse)
                    .isEqualTo(BOSTATUS_REFERANSE_MED_FORELDRE_1)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.sjablonListe).isEqualTo(byggSjablonPeriodeListe()) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2017-12-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[1].resultat.kode)
                    .isEqualTo(Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[1].resultat.regel).isEqualTo("REGEL 5") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2018-05-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[2].resultat.kode,
                ).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[2].resultat.regel).isEqualTo("REGEL 13") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[3].periode.datoFom).isEqualTo(LocalDate.parse("2018-05-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[3].periode.datoTil).isEqualTo(LocalDate.parse("2018-07-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[3].resultat.kode,
                ).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[3].resultat.regel).isEqualTo("REGEL 9") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[4].periode.datoFom).isEqualTo(LocalDate.parse("2018-07-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[4].periode.datoTil).isEqualTo(LocalDate.parse("2018-09-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[4].resultat.kode,
                ).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[4].resultat.regel).isEqualTo("REGEL 9") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[5].periode.datoFom).isEqualTo(LocalDate.parse("2018-09-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[5].periode.datoTil).isEqualTo(LocalDate.parse("2018-12-01")) },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[5].resultat.kode)
                    .isEqualTo(Resultatkode.AVSLAG)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[5].resultat.regel).isEqualTo("REGEL 2") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[6].periode.datoFom).isEqualTo(LocalDate.parse("2018-12-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[6].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[6].resultat.kode,
                ).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[6].resultat.regel).isEqualTo("REGEL 9") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[7].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[7].periode.datoTil).isEqualTo(LocalDate.parse("2019-04-01")) },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[7].resultat.kode,
                ).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[7].resultat.regel).isEqualTo("REGEL 10") },
            { assertThat(resultat.beregnetForskuddPeriodeListe[8].periode.datoFom).isEqualTo(LocalDate.parse("2019-04-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[8].periode.datoTil).isNull() },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[8].resultat.kode,
                ).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[8].resultat.regel).isEqualTo("REGEL 9") },
        )

        printGrunnlagResultat(resultat)
    }

    @Test
    @DisplayName("Test utvidet grunnlag med avvik")
    fun testUtvidetGrunnlagMedAvvik() {
        val avvikListe = forskuddPeriode.validerInput(beregnForskuddGrunnlagMedAvvik)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(4) },
            { assertThat(avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFom") },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL) },
            {
                assertThat(avvikListe[1].avvikTekst)
                    .isEqualTo("Overlappende perioder i bidragMottakerSivilstandPeriodeListe: datoTil=2018-04-01, datoFom=2018-03-01")
            },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODER_OVERLAPPER) },
            {
                assertThat(avvikListe[2].avvikTekst)
                    .isEqualTo("datoTil kan ikke være null i søknadsbarnBostatusPeriodeListe: datoFom=2018-09-01, datoTil=null")
            },
            { assertThat(avvikListe[2].avvikType).isEqualTo(Avvikstype.NULL_VERDI_I_DATO) },
            {
                assertThat(avvikListe[3].avvikTekst)
                    .isEqualTo("datoTil må være etter datoFom i bidragMottakerBarnPeriodeListe: datoFom=2019-04-01, datoTil=2018-07-01")
            },
            { assertThat(avvikListe[3].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL) },
        )

        printAvvikListe(avvikListe)
    }

    @Test
    @DisplayName("Test utvidet grunnlag med avvik periode mangler data")
    fun testUtvidetGrunnlagMedAvvikPeriodeManglerData() {
        val grunnlag = byggForskuddGrunnlag("2016-01-01", "2020-01-01")
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertAll(
            { assertThat(avvikListe).isNotEmpty() },
            { assertThat(avvikListe).hasSize(3) },
            {
                assertThat(avvikListe[0].avvikTekst)
                    .isEqualTo("Første dato i bidragMottakerSivilstandPeriodeListe (2017-01-01) er etter beregnDatoFom (2016-01-01)")
            },
            { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(avvikListe[1].avvikTekst)
                    .isEqualTo("Siste dato i bidragMottakerSivilstandPeriodeListe (2019-08-01) er før beregnDatoTil (2020-01-01)")
            },
            { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            {
                assertThat(avvikListe[2].avvikTekst)
                    .isEqualTo("Første dato i sjablonPeriodeListe (2017-01-01) er etter beregnDatoFom (2016-01-01)")
            },
            { assertThat(avvikListe[2].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
        )

        printAvvikListe(avvikListe)
    }

    @Test
    @DisplayName("Test grunnlag uten andre barn")
    fun testGrunnlagUtenBarn() {
        val resultat = forskuddPeriode.beregnPerioder(beregnForskuddGrunnlagUtenBarn)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beregnetForskuddPeriodeListe).isNotEmpty() },
            { assertThat(resultat.beregnetForskuddPeriodeListe).hasSize(1) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].periode.datoTil).isNull() },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].resultat.kode,
                ).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_100_PROSENT)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].resultat.regel).isEqualTo("REGEL 6") },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].resultat.sjablonListe,
                ).isEqualTo(byggSjablonPeriodeNavnVerdiListe())
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe).hasSize(1) },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe[0].type)
                    .isEqualTo("INNTEKTSOPPLYSNINGER_ARBEIDSGIVER")
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.inntektListe[0].beløp,
                ).isEqualTo(BigDecimal.valueOf(250000))
            },
            {
                assertThat(
                    resultat.beregnetForskuddPeriodeListe[0].grunnlag.sivilstand.kode,
                ).isEqualTo(Sivilstandskode.GIFT_SAMBOER)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.barnIHusstandenListe.count()).isEqualTo(1) },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnAlder.alder).isZero() },
            {
                assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.søknadsbarnBostatus.kode)
                    .isEqualTo(Bostatuskode.MED_FORELDER)
            },
            { assertThat(resultat.beregnetForskuddPeriodeListe[0].grunnlag.sjablonListe).isEqualTo(byggSjablonPeriodeListe()) },
        )
    }

    @Test
    @DisplayName("Test grunnlag uten sivilstandperioder")
    fun testGrunnlagUtenSivilstandperioder() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            forskuddPeriode.beregnPerioder(beregnForskuddGrunnlagUtenSivilstand)
        }.withMessageContaining("Grunnlagsobjekt SIVILSTAND mangler data for periode")
    }

    @Test
    @DisplayName("Test valider input - grunnlag med flere inntekter i samme periode - test 1")
    fun testValiderInputGrunnlagMedFlereInntekterISammePeriodeTest1() {
        val bmInntektListe = mutableListOf<InntektPeriode>()
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_1,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_2,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2017-06-01")),
                type = "KAPITALINNTEKT_EGNE_OPPLYSNINGER",
                beløp = BigDecimal.valueOf(10000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_3,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-04-01"), datoTil = LocalDate.parse("2017-09-01")),
                type = "UTVIDET_BARNETRYGD",
                beløp = BigDecimal.valueOf(15000),
            ),
        )

        val grunnlag = byggForskuddGrunnlagMedFlereInntekterISammePeriode(bmInntektListe)
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertThat(avvikListe).isEmpty()
    }

    @Test
    @DisplayName("Test valider input - grunnlag med flere inntekter i samme periode - test 2")
    fun testValiderInputGrunnlagMedFlereInntekterISammePeriodeTest2() {
        val bmInntektListe = mutableListOf<InntektPeriode>()
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_1,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_2,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2017-06-01")),
                type = "KAPITALINNTEKT_EGNE_OPPLYSNINGER",
                beløp = BigDecimal.valueOf(10000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_3,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-04-01"), datoTil = LocalDate.parse("2017-09-01")),
                type = "UTVIDET_BARNETRYGD",
                beløp = BigDecimal.valueOf(15000),
            ),
        )

        val grunnlag = byggForskuddGrunnlagMedFlereInntekterISammePeriode(bmInntektListe)
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertThat(avvikListe).isEmpty()
    }

    @Test
    @DisplayName("Test valider input - grunnlag med flere inntekter i samme periode - test 4")
    fun testValiderInputGrunnlagMedFlereInntekterISammePeriodeTest4() {
        val bmInntektListe = mutableListOf<InntektPeriode>()
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_1,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_2,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                type = "KAPITALINNTEKT_EGNE_OPPLYSNINGER",
                beløp = BigDecimal.valueOf(10000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_3,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                type = "UTVIDET_BARNETRYGD",
                beløp = BigDecimal.valueOf(15000),
            ),
        )

        val grunnlag = byggForskuddGrunnlagMedFlereInntekterISammePeriode(bmInntektListe)
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertThat(avvikListe).isEmpty()
    }

    @Test
    @DisplayName("Test valider input - grunnlag med flere inntekter i samme periode - test 5")
    fun testValiderInputGrunnlagMedFlereInntekterISammePeriodeTest5() {
        val bmInntektListe = mutableListOf<InntektPeriode>()
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_1,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_2,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-03-01"), datoTil = LocalDate.parse("2017-06-01")),
                type = "KAPITALINNTEKT_EGNE_OPPLYSNINGER",
                beløp = BigDecimal.valueOf(10000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_3,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-03-01"), datoTil = LocalDate.parse("2017-06-01")),
                type = "UTVIDET_BARNETRYGD",
                beløp = BigDecimal.valueOf(15000),
            ),
        )

        val grunnlag = byggForskuddGrunnlagMedFlereInntekterISammePeriode(bmInntektListe)
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertThat(avvikListe).isEmpty()
    }

    @Test
    @DisplayName("Test valider input - grunnlag med flere inntekter i samme periode - test 6")
    fun testValiderInputGrunnlagMedFlereInntekterISammePeriodeTest6() {
        val bmInntektListe = mutableListOf<InntektPeriode>()
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_1,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2017-06-01")),
                type = "KAPITALINNTEKT_EGNE_OPPLYSNINGER",
                beløp = BigDecimal.valueOf(10000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_2,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                beløp = BigDecimal.valueOf(400000),
            ),
        )
        bmInntektListe.add(
            InntektPeriode(
                referanse = INNTEKT_REFERANSE_3,
                inntektPeriode = Periode(datoFom = LocalDate.parse("2017-04-01"), datoTil = LocalDate.parse("2017-09-01")),
                type = "UTVIDET_BARNETRYGD",
                beløp = BigDecimal.valueOf(15000),
            ),
        )

        val grunnlag = byggForskuddGrunnlagMedFlereInntekterISammePeriode(bmInntektListe)
        val avvikListe = forskuddPeriode.validerInput(grunnlag)

        assertThat(avvikListe).isEmpty()
    }

    private fun printGrunnlagResultat(resultat: BeregnForskuddResultat) {
        resultat.beregnetForskuddPeriodeListe
            .sortedBy { it.periode.datoFom }
            .forEach {
                println(
                    "Dato fom: ${it.periode.datoFom}; Dato til: ${it.periode.datoTil}; Beløp: ${it.resultat.beløp.toInt()}; " +
                        "Resultatkode: ${it.resultat.kode}; Regel: ${it.resultat.regel}",
                )
            }
    }

    private fun printAvvikListe(avvikListe: List<Avvik>) {
        avvikListe.forEach { println("Avvik tekst: ${it.avvikTekst}; Avvik type: ${it.avvikType}") }
    }

    companion object {
        private const val INNTEKT_REFERANSE_1 = "INNTEKT_REFERANSE_1"
        private const val INNTEKT_REFERANSE_2 = "INNTEKT_REFERANSE_2"
        private const val INNTEKT_REFERANSE_3 = "INNTEKT_REFERANSE_3"
        private const val SIVILSTAND_REFERANSE_GIFT = "SIVILSTAND_REFERANSE_GIFT"
        private const val BARN_I_HUSSTANDEN_REFERANSE_1 = "BARN_I_HUSSTANDEN_REFERANSE_1"
        private const val SØKNADSBARN_REFERANSE = "SØKNADSBARN_REFERANSE"
        private const val BOSTATUS_REFERANSE_MED_FORELDRE_1 = "BOSTATUS_REFERANSE_MED_FORELDRE_1"
    }
}
