package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.InntektPeriodeCore
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class CoreMapperTest {
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
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
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
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
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
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE er ikke gyldig",
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
            .isThrownBy { CoreMapper.mapGrunnlagTilCore(beregnForskuddGrunnlag = beregnForskuddGrunnlag, sjablontallListe = emptyList()) }
            .withMessageContaining("Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.SIVILSTAND_PERIODE er ikke gyldig")
    }

    @Test
    @DisplayName("Skal summere og periodisere inntekter")
    fun summerOgPeriodiserInntekterTest() {
        val inputList = listOf(
            InntektPeriodeCore(
                referanse = "ref1",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 1), datoTil = LocalDate.of(2022, 1, 10)),
                belop = BigDecimal(100),
                grunnlagsreferanseListe = listOf("gr1"),
            ),
            InntektPeriodeCore(
                referanse = "ref2",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 5), datoTil = LocalDate.of(2022, 1, 18)),
                belop = BigDecimal(200),
                grunnlagsreferanseListe = listOf("gr2"),
            ),
            InntektPeriodeCore(
                referanse = "ref3",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 10), datoTil = null),
                belop = BigDecimal(300),
                grunnlagsreferanseListe = listOf("gr3"),
            ),
            InntektPeriodeCore(
                referanse = "ref4",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 15), datoTil = null),
                belop = BigDecimal(400),
                grunnlagsreferanseListe = listOf("gr4"),
            ),
        )

        val outputList = CoreMapper.akkumulerOgPeriodiser(inputList, InntektPeriodeCore::class.java)

        assertThat(outputList).isNotNull
    }

    @Test
    @DisplayName("Skal telle og periodisere antall barn i husstanden")
    fun summerOgPeriodiserAntallBarnIHusstandenTest() {
        val inputList = listOf(
            BarnIHusstandenPeriodeCore(
                referanse = "ref1",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 1), datoTil = LocalDate.of(2022, 1, 10)),
                antall = 1,
                grunnlagsreferanseListe = listOf("gr1"),
            ),
            BarnIHusstandenPeriodeCore(
                referanse = "ref2",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 5), datoTil = LocalDate.of(2022, 1, 18)),
                antall = 1,
                grunnlagsreferanseListe = listOf("gr2"),
            ),
            BarnIHusstandenPeriodeCore(
                referanse = "ref3",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 10), datoTil = null),
                antall = 1,
                grunnlagsreferanseListe = listOf("gr3"),
            ),
            BarnIHusstandenPeriodeCore(
                referanse = "ref4",
                periode = PeriodeCore(datoFom = LocalDate.of(2022, 1, 15), datoTil = null),
                antall = 1,
                grunnlagsreferanseListe = listOf("gr4"),
            ),
        )

        val outputList = CoreMapper.akkumulerOgPeriodiser(inputList, BarnIHusstandenPeriodeCore::class.java)

        assertThat(outputList).isNotNull
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
            "\"manueltRegistrert\": false}",
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
