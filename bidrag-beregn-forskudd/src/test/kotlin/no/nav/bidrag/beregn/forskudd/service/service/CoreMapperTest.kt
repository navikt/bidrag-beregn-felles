package no.nav.bidrag.beregn.forskudd.service.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.forskudd.service.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.Grunnlag
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class CoreMapperTest {
    @Test
    @DisplayName("Skal kaste UgyldigInputException når PERSON-objekt inneholder ugyldige data")
    fun mapPersonUgyldig() {
        val mapper = ObjectMapper()
        val innholdPersonMedFeil = innholdPersonMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                    listOf(
                        Grunnlag(
                            referanse = "Person_Søknadsbarn",
                            type = Grunnlagstype.PERSON,
                            grunnlagsreferanseListe = emptyList(),
                            innhold = innholdPersonMedFeil,
                        ),
                    ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.PERSON er ikke gyldig")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når BOSTATUS_PERIODE-objekt inneholder ugyldige data")
    fun mapBostatusPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdPerson = innholdPersonOK(mapper)
        val innholdBostatusMedFeil = innholdBostatusMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                    listOf(
                        Grunnlag(
                            referanse = "Person_Søknadsbarn",
                            type = Grunnlagstype.PERSON,
                            grunnlagsreferanseListe = emptyList(),
                            innhold = innholdPerson,
                        ),
                        Grunnlag(
                            referanse = "Bostatus_Søknadsbarn",
                            type = Grunnlagstype.BOSTATUS_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Søknadsbarn"),
                            innhold = innholdBostatusMedFeil,
                        ),
                    ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig")
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når BEREGNING_INNTEKT_RAPPORTERING_PERIODE-objekt inneholder ugyldige data")
    fun mapInntektPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdPerson = innholdPersonOK(mapper)
        val innholdBostatus = innholdBostatusOK(mapper)
        val innholdInntektMedFeil = innholdInntektMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                    listOf(
                        Grunnlag(
                            referanse = "Person_Søknadsbarn",
                            type = Grunnlagstype.PERSON,
                            grunnlagsreferanseListe = emptyList(),
                            innhold = innholdPerson,
                        ),
                        Grunnlag(
                            referanse = "Bostatus_Søknadsbarn",
                            type = Grunnlagstype.BOSTATUS_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Søknadsbarn"),
                            innhold = innholdBostatus,
                        ),
                        Grunnlag(
                            referanse = "BeregningInntektRapportering_Ainntekt",
                            type = Grunnlagstype.BEREGNING_INNTEKT_RAPPORTERING_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Bidragsmottaker"),
                            innhold = innholdInntektMedFeil,
                        ),
                    ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BEREGNING_INNTEKT_RAPPORTERING_PERIODE er ikke gyldig",
            )
    }

    @Test
    @DisplayName("Skal kaste UgyldigInputException når SIVILSTAND_PERIODE-objekt inneholder ugyldige data")
    fun mapSivilstandPeriodeUgyldig() {
        val mapper = ObjectMapper()
        val innholdPerson = innholdPersonOK(mapper)
        val innholdBostatus = innholdBostatusOK(mapper)
        val innholdInntekt = innholdInntektOK(mapper)
        val innholdSivilstandMedFeil = innholdSivilstandMedFeil(mapper)
        val beregnForskuddGrunnlag =
            BeregnGrunnlag(
                periode = ÅrMånedsperiode(fom = "2020-12", til = "2021-01"),
                søknadsbarnReferanse = "Person_Søknadsbarn",
                grunnlagListe =
                    listOf(
                        Grunnlag(
                            referanse = "Person_Søknadsbarn",
                            type = Grunnlagstype.PERSON,
                            grunnlagsreferanseListe = emptyList(),
                            innhold = innholdPerson,
                        ),
                        Grunnlag(
                            referanse = "Bostatus_Søknadsbarn",
                            type = Grunnlagstype.BOSTATUS_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Søknadsbarn"),
                            innhold = innholdBostatus,
                        ),
                        Grunnlag(
                            referanse = "BeregningInntektRapportering_Ainntekt",
                            type = Grunnlagstype.BEREGNING_INNTEKT_RAPPORTERING_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Bidragsmottaker"),
                            innhold = innholdInntekt,
                        ),
                        Grunnlag(
                            referanse = "Sivilstand",
                            type = Grunnlagstype.SIVILSTAND_PERIODE,
                            grunnlagsreferanseListe = listOf("Person_Bidragsmottaker"),
                            innhold = innholdSivilstandMedFeil,
                        ),
                    ),
            )

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.SIVILSTAND_PERIODE er ikke gyldig")
    }

    private fun innholdPersonMedFeil(mapper: ObjectMapper) =
        mapper.readTree("{\"ident\": \"11111111111\"," + "\"navn\": \"Søknadsbarn\"," + "\"fødselsdato\": null}")

    private fun innholdPersonOK(mapper: ObjectMapper) =
        mapper.readTree("{\"ident\": \"11111111111\"," + "\"navn\": \"Søknadsbarn\"," + "\"fødselsdato\": \"2010-01-01\"}")

    private fun innholdBostatusMedFeil(mapper: ObjectMapper) =
        mapper.readTree(
            "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"bostatus\": \"MED_BESTEMOR\"," +
                "\"manueltRegistrert\": false}",
        )

    private fun innholdBostatusOK(mapper: ObjectMapper) =
        mapper.readTree(
            "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"bostatus\": \"MED_FORELDER\"," +
                "\"manueltRegistrert\": false}",
        )

    private fun innholdInntektMedFeil(mapper: ObjectMapper) =
        mapper.readTree(
            "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"inntektsrapportering\": \"AINNTEKT\"," +
                "\"gjelderBarn\": null," + "\"beløp\": \"29x000\"," + "\"manueltRegistrert\": false," + "\"valgt\": true}",
        )

    private fun innholdInntektOK(mapper: ObjectMapper) =
        mapper.readTree(
            "{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"inntektsrapportering\": \"AINNTEKT\"," +
                "\"gjelderBarn\": null," + "\"beløp\": 290000," + "\"manueltRegistrert\": false," + "\"valgt\": true}",
        )

    private fun innholdSivilstandMedFeil(mapper: ObjectMapper) =
        mapper.readTree("{\"periode\":{" + "\"fom\": \"2020-12\"," + "\"til\": \"2021-01\"}," + "\"sivilstand\": \"UGIFT\"}")
}
