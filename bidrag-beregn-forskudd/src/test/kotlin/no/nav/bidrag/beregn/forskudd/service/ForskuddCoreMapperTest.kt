package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.service.ForskuddCoreMapper.mapInntekt
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.Method
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

internal class ForskuddCoreMapperTest {
    @Test
    @DisplayName("Skal kaste UgyldigInputException når PERSON-objekt inneholder ugyldige data")
    fun mapPersonUgyldig() {
        val mapper = ObjectMapper()
        val innholdSøknadsbarn = innholdSøknadsbarnMedFeil(mapper)
        val innholdBidragsmottaker = innholdBidragsmottakerOK(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                listOf(
                    GrunnlagDto(
                        referanse = "Person_Søknadsbarn",
                        type = Grunnlagstype.PERSON_SØKNADSBARN,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdSøknadsbarn,
                    ),
                    GrunnlagDto(
                        referanse = "Person_Bidragsmottaker",
                        type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdBidragsmottaker,
                    ),
                ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ForskuddCoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.PERSON_SØKNADSBARN er ikke gyldig")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når BOSTATUS_PERIODE-objekt inneholder ugyldige data")
    fun mapBostatusPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdSøknadsbarn = innholdSøknadsbarnOK(mapper)
        val innholdBidragsmottaker = innholdBidragsmottakerOK(mapper)
        val innholdBostatusMedFeil = innholdBostatusMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                listOf(
                    GrunnlagDto(
                        referanse = "Person_Søknadsbarn",
                        type = Grunnlagstype.PERSON_SØKNADSBARN,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdSøknadsbarn,
                    ),
                    GrunnlagDto(
                        referanse = "Person_Bidragsmottaker",
                        type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdBidragsmottaker,
                    ),
                    GrunnlagDto(
                        referanse = "Bostatus_Søknadsbarn",
                        type = Grunnlagstype.BOSTATUS_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Søknadsbarn",
                        innhold = innholdBostatusMedFeil,
                    ),
                ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ForskuddCoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når INNTEKT_RAPPORTERING_PERIODE-objekt inneholder ugyldige data")
    fun mapInntektPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdSøknadsbarn = innholdSøknadsbarnOK(mapper)
        val innholdBidragsmottaker = innholdBidragsmottakerOK(mapper)
        val innholdBostatus = innholdBostatusOK(mapper)
        val innholdInntektMedFeil = innholdInntektMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                listOf(
                    GrunnlagDto(
                        referanse = "Person_Søknadsbarn",
                        type = Grunnlagstype.PERSON_SØKNADSBARN,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdSøknadsbarn,
                    ),
                    GrunnlagDto(
                        referanse = "Person_Bidragsmottaker",
                        type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdBidragsmottaker,
                    ),
                    GrunnlagDto(
                        referanse = "Bostatus_Søknadsbarn",
                        type = Grunnlagstype.BOSTATUS_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Søknadsbarn",
                        innhold = innholdBostatus,
                    ),
                    GrunnlagDto(
                        referanse = "BeregningInntektRapportering_Ainntekt",
                        type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Bidragsmottaker",
                        innhold = innholdInntektMedFeil,
                    ),
                ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ForskuddCoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining(
                "Ugyldig input ved beregning. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig",
            )
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når SIVILSTAND_PERIODE-objekt inneholder ugyldige data")
    fun mapSivilstandPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdSøknadsbarn = innholdSøknadsbarnOK(mapper)
        val innholdBidragsmottaker = innholdBidragsmottakerOK(mapper)
        val innholdBostatus = innholdBostatusOK(mapper)
        val innholdInntekt = innholdInntektOK(mapper)
        val innholdSivilstandMedFeil = innholdSivilstandMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                listOf(
                    GrunnlagDto(
                        referanse = "Person_Søknadsbarn",
                        type = Grunnlagstype.PERSON_SØKNADSBARN,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdSøknadsbarn,
                    ),
                    GrunnlagDto(
                        referanse = "Person_Bidragsmottaker",
                        type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innholdBidragsmottaker,
                    ),
                    GrunnlagDto(
                        referanse = "Bostatus_Søknadsbarn",
                        type = Grunnlagstype.BOSTATUS_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Søknadsbarn",
                        innhold = innholdBostatus,
                    ),
                    GrunnlagDto(
                        referanse = "BeregningInntektRapportering_Ainntekt",
                        type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Bidragsmottaker",
                        innhold = innholdInntekt,
                    ),
                    GrunnlagDto(
                        referanse = "Sivilstand",
                        type = Grunnlagstype.SIVILSTAND_PERIODE,
                        grunnlagsreferanseListe = emptyList(),
                        gjelderReferanse = "Person_Bidragsmottaker",
                        innhold = innholdSivilstandMedFeil,
                    ),
                ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ForskuddCoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.SIVILSTAND_PERIODE er ikke gyldig")
    }

    // Tester også at det bare er BMs inntekt som brukes og bare hvis inntekten er valgt og gjelder alle barn eller søknadsbarnet
    @Test
    @DisplayName("Skal summere og periodisere inntekter")
    fun mapInntektTest() {
        val filnavn = "src/test/resources/testfiler/forskudd_test_inntekt.json"
        val forskuddGrunnlag = lesFilOgByggRequest(filnavn)
        val inntektPeriodeListe = mapInntekt(forskuddGrunnlag, "Person_Bidragsmottaker", BigDecimal.valueOf(10000))

        assertAll(
            { assertThat(inntektPeriodeListe).isNotNull },

            { assertThat(inntektPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2022-01-01")) },
            { assertThat(inntektPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2022-02-01")) },
            { assertThat(inntektPeriodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(100000.00).setScale(2)) },
            { assertThat(inntektPeriodeListe[0].grunnlagsreferanseListe).containsExactly("BeregningInntektRapportering_Ainntekt_BM_202201") },

            { assertThat(inntektPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2022-02-01")) },
            { assertThat(inntektPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2022-03-01")) },
            { assertThat(inntektPeriodeListe[1].beløp).isEqualTo(BigDecimal.valueOf(300000.00).setScale(2)) },
            {
                assertThat(inntektPeriodeListe[1].grunnlagsreferanseListe).containsExactly(
                    "BeregningInntektRapportering_Ainntekt_BM_202201",
                    "BeregningInntektRapportering_Ainntekt_BM_202202",
                )
            },

            { assertThat(inntektPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2022-03-01")) },
            { assertThat(inntektPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2022-04-01")) },
            { assertThat(inntektPeriodeListe[2].beløp).isEqualTo(BigDecimal.valueOf(500000.00).setScale(2)) },
            {
                assertThat(inntektPeriodeListe[2].grunnlagsreferanseListe).containsExactly(
                    "BeregningInntektRapportering_Ainntekt_BM_202202",
                    "BeregningInntektRapportering_Ainntekt_BM_202203",
                )
            },

            { assertThat(inntektPeriodeListe[3].periode.datoFom).isEqualTo(LocalDate.parse("2022-04-01")) },
            { assertThat(inntektPeriodeListe[3].periode.datoTil).isEqualTo(LocalDate.parse("2022-05-01")) },
            { assertThat(inntektPeriodeListe[3].beløp).isEqualTo(BigDecimal.valueOf(900000.00).setScale(2)) },
            {
                assertThat(inntektPeriodeListe[3].grunnlagsreferanseListe).containsExactly(
                    "BeregningInntektRapportering_Ainntekt_BM_202202",
                    "BeregningInntektRapportering_Ainntekt_BM_202203",
                    "BeregningInntektRapportering_Ainntekt_BM_202204",
                )
            },

            { assertThat(inntektPeriodeListe[4].periode.datoFom).isEqualTo(LocalDate.parse("2022-05-01")) },
            { assertThat(inntektPeriodeListe[4].periode.datoTil).isNull() },
            { assertThat(inntektPeriodeListe[4].beløp).isEqualTo(BigDecimal.valueOf(700000.00).setScale(2)) },
            {
                assertThat(inntektPeriodeListe[4].grunnlagsreferanseListe).containsExactly(
                    "BeregningInntektRapportering_Ainntekt_BM_202203",
                    "BeregningInntektRapportering_Ainntekt_BM_202204",
                )
            },
        )
    }

    @Test
    @DisplayName("Skal telle og periodisere antall barn i husstanden")
    fun mapAntallBarnIHusstandTest() {
        val filnavn = "src/test/resources/testfiler/forskudd_test_barnihusstand.json"
        val forskuddGrunnlag = lesFilOgByggRequest(filnavn)

        val inntektPeriodeListe =
            invokePrivateMethod(
                ForskuddCoreMapper,
                "mapBarnIHusstanden",
                forskuddGrunnlag,
                "Referanse_Bidragsmottaker",
            ) as List<BarnIHusstandenPeriodeCore>

        assertAll(
            { assertThat(inntektPeriodeListe).isNotNull },

            { assertThat(inntektPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2022-01-01")) },
            { assertThat(inntektPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2022-02-01")) },
            { assertThat(inntektPeriodeListe[0].antall).isEqualTo(2.0) },
            {
                assertThat(inntektPeriodeListe[0].grunnlagsreferanseListe).contains(
                    "Bostatus_Søknadsbarn_202201",
                    "Bostatus_Husstandsbarn_01_202201",
                )
            },

            { assertThat(inntektPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2022-02-01")) },
            { assertThat(inntektPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2022-03-01")) },
            { assertThat(inntektPeriodeListe[1].antall).isEqualTo(3.0) },
            {
                assertThat(inntektPeriodeListe[1].grunnlagsreferanseListe).contains(
                    "Bostatus_Søknadsbarn_202201",
                    "Bostatus_Husstandsbarn_01_202201",
                    "Bostatus_Husstandsbarn_02_202202",
                )
            },

            { assertThat(inntektPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2022-03-01")) },
            { assertThat(inntektPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2022-04-01")) },
            { assertThat(inntektPeriodeListe[2].antall).isEqualTo(3.0) },
            {
                assertThat(inntektPeriodeListe[2].grunnlagsreferanseListe).contains(
                    "Bostatus_Søknadsbarn_202201",
                    "Bostatus_Husstandsbarn_02_202202",
                    "Bostatus_Husstandsbarn_03_202203",
                )
            },

            { assertThat(inntektPeriodeListe[3].periode.datoFom).isEqualTo(LocalDate.parse("2022-04-01")) },
            { assertThat(inntektPeriodeListe[3].periode.datoTil).isEqualTo(LocalDate.parse("2022-05-01")) },
            { assertThat(inntektPeriodeListe[3].antall).isEqualTo(4.0) },
            {
                assertThat(inntektPeriodeListe[3].grunnlagsreferanseListe).contains(
                    "Bostatus_Søknadsbarn_202201",
                    "Bostatus_Husstandsbarn_02_202202",
                    "Bostatus_Husstandsbarn_03_202203",
                    "Bostatus_Husstandsbarn_04_202204",
                )
            },

            { assertThat(inntektPeriodeListe[4].periode.datoFom).isEqualTo(LocalDate.parse("2022-05-01")) },
            { assertThat(inntektPeriodeListe[4].periode.datoTil).isNull() },
            { assertThat(inntektPeriodeListe[4].antall).isEqualTo(3.0) },
            {
                assertThat(inntektPeriodeListe[4].grunnlagsreferanseListe).contains(
                    "Bostatus_Søknadsbarn_202201",
                    "Bostatus_Husstandsbarn_03_202203",
                    "Bostatus_Husstandsbarn_04_202204",
                )
            },
        )
    }

    // Bruker reflection for å kalle private metoder
    private fun invokePrivateMethod(klasse: Any, metode: String, vararg args: Any): Any? {
        val method: Method = klasse.javaClass.getDeclaredMethod(metode, *args.map { it.javaClass }.toTypedArray())
        method.isAccessible = true
        return method.invoke(klasse, *args)
    }

    private fun lesFilOgByggRequest(filnavn: String?): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn!!))
        } catch (e: Exception) {
            Assertions.fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    private fun innholdSøknadsbarnMedFeil(mapper: ObjectMapper) =
        mapper.readTree("{\"ident\": \"11111111111\"," + "\"navn\": \"Søknadsbarn\"," + "\"fødselsdato\": null}")

    private fun innholdSøknadsbarnOK(mapper: ObjectMapper) =
        mapper.readTree("{\"ident\": \"11111111111\"," + "\"navn\": \"Søknadsbarn\"," + "\"fødselsdato\": \"2010-01-01\"}")

    private fun innholdBidragsmottakerOK(mapper: ObjectMapper) =
        mapper.readTree("{\"ident\": \"11111111111\"," + "\"navn\": \"Bidragsmottaker\"," + "\"fødselsdato\": \"1982-01-01\"}")

    private fun innholdBostatusMedFeil(mapper: ObjectMapper) = mapper.readTree(
        "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"bostatus\": \"MED_BESTEMOR\"," +
            "\"manueltRegistrert\": false}",
    )

    private fun innholdBostatusOK(mapper: ObjectMapper) = mapper.readTree(
        "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"bostatus\": \"MED_FORELDER\"," +
            "\"manueltRegistrert\": false, \"relatertTilPart\": \"Person_Bidragsmottaker\"}",
    )

    private fun innholdInntektMedFeil(mapper: ObjectMapper) = mapper.readTree(
        "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"inntektsrapportering\": \"AINNTEKT\"," +
            "\"gjelderBarn\": null," + "\"beløp\": \"29x000\"," + "\"manueltRegistrert\": false," + "\"valgt\": true}",
    )

    private fun innholdInntektOK(mapper: ObjectMapper) = mapper.readTree(
        "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"inntektsrapportering\": \"AINNTEKT\"," +
            "\"gjelderBarn\": null," + "\"beløp\": 290000," + "\"manueltRegistrert\": false," + "\"valgt\": true}",
    )

    private fun innholdSivilstandMedFeil(mapper: ObjectMapper) =
        mapper.readTree("{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"sivilstand\": \"UGIFT\"}")
}
