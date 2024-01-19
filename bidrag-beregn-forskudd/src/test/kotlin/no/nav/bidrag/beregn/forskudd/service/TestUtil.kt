package no.nav.bidrag.beregn.forskudd.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.beregning.ResultatkodeForskudd
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.Grunnlag
import no.nav.bidrag.transport.behandling.beregning.forskudd.BeregnetForskuddResultat
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.forskudd.ResultatPeriode
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth

object TestUtil {
    private const val INNTEKT_REFERANSE_1 = "INNTEKT_REFERANSE_1"
    private const val SIVILSTAND_REFERANSE_ENSLIG = "SIVILSTAND_REFERANSE_ENSLIG"
    private const val BARN_REFERANSE_1 = "BARN_REFERANSE_1"
    private const val SOKNADBARN_REFERANSE = "SOKNADBARN_REFERANSE"
    private const val BOSTATUS_REFERANSE_MED_FORELDRE_1 = "BOSTATUS_REFERANSE_MED_FORELDRE_1"

    fun byggDummyForskuddGrunnlag(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("")
    }

    fun byggForskuddGrunnlagUtenBeregningsperiodeTil(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("beregningsperiodeTil")
    }

    fun byggForskuddGrunnlagUtenGrunnlagListe(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("grunnlagListe")
    }

    fun byggForskuddGrunnlagUtenReferanse(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("referanse")
    }

    fun byggForskuddGrunnlagUtenType(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("type")
    }

    fun byggForskuddGrunnlagUtenInnhold(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("innhold")
    }

    // Bygger opp BeregnGrunnlag
    private fun byggDummyForskuddGrunnlag(nullVerdi: String): BeregnGrunnlag {
        val mapper = ObjectMapper()
        val beregningsperiodeFom = YearMonth.parse("2017-01")
        val beregningsperiodeTil = if (nullVerdi == "beregningsperiodeTil") null else YearMonth.parse("2020-01")
        val referanse = if (nullVerdi == "referanse") null else "Mottatt_BM_Inntekt_AG_20201201"
        val type = if (nullVerdi == "type") null else Grunnlagstype.INNTEKT
        val innhold =
            if (nullVerdi == "innhold") {
                null
            } else {
                mapper.valueToTree<JsonNode>(
                    mapOf(
                        "rolle" to "BM",
                        "datoFom" to "2017-01",
                        "datoTil" to "2020-01",
                        "inntektsrapportering" to "INNTEKT_RAPPORTERING",
                        "belop" to 290000,
                        "manueltRegistrert" to false,
                        "valgt" to true,
                    ),
                )
            }
        val grunnlagListe =
            if (nullVerdi == "grunnlagListe") {
                null
            } else {
                listOf(
                    Grunnlag(
                        referanse = referanse,
                        type = type,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innhold,
                    ),
                )
            }

        return BeregnGrunnlag(
            periode = ÅrMånedsperiode(fom = beregningsperiodeFom, til = beregningsperiodeTil),
            søknadsbarnReferanse = "1",
            grunnlagListe = grunnlagListe,
        )
    }

    // Bygger opp fullt BeregnGrunnlag
    @JvmOverloads
    fun byggForskuddGrunnlag(
        periodeFom: String = "2017-01",
        periodeTil: String = "2020-01",
        fødselsdato: String = "2006-12-01",
        beløp: String = "290000",
    ): BeregnGrunnlag {
        val mapper = ObjectMapper()
        val personSøknadsbarnInnhold =
            mapper.valueToTree<JsonNode>(
                mapOf(
                    "ident" to "11111111111",
                    "navn" to "Søknadsbarn",
                    "fødselsdato" to fødselsdato,
                ),
            )
        val bostatusInnhold =
            mapper.valueToTree<JsonNode>(
                mapOf(
                    "periode" to
                        mapOf(
                            "fom" to periodeFom,
                            "til" to periodeTil,
                        ),
                    "bostatus" to Bostatuskode.MED_FORELDER.name,
                    "manueltRegistrert" to false,
                ),
            )
        val inntektInnhold =
            mapper.valueToTree<JsonNode>(
                mapOf(
                    "periode" to
                        mapOf(
                            "fom" to periodeFom,
                            "til" to periodeTil,
                        ),
                    "inntektsrapportering" to Inntektsrapportering.AINNTEKT.name,
                    "gjelderBarn" to null,
                    "beløp" to beløp,
                    "manueltRegistrert" to false,
                    "valgt" to true,
                ),
            )
        val sivilstandInnhold =
            mapper.valueToTree<JsonNode>(
                mapOf(
                    "periode" to
                        mapOf(
                            "fom" to periodeFom,
                            "til" to periodeTil,
                        ),
                    "sivilstand" to Sivilstandskode.GIFT_SAMBOER.name,
                ),
            )

        val beregningsperiodeFom = YearMonth.parse(periodeFom)
        val beregningsperiodeTil = YearMonth.parse(periodeTil)
        val grunnlagListe = mutableListOf<Grunnlag>()

        grunnlagListe.add(
            Grunnlag(
                referanse = "Person_Søknadsbarn",
                type = Grunnlagstype.PERSON,
                grunnlagsreferanseListe = emptyList(),
                innhold = personSøknadsbarnInnhold,
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                referanse = "Bostatus_20170101",
                type = Grunnlagstype.BOSTATUS_PERIODE,
                grunnlagsreferanseListe = listOf("Person_Søknadsbarn"),
                innhold = bostatusInnhold,
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                referanse = "BeregningInntektRapportering_Ainntekt_20170101",
                type = Grunnlagstype.BEREGNING_INNTEKT_RAPPORTERING_PERIODE,
                grunnlagsreferanseListe = emptyList(),
                innhold = inntektInnhold,
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                referanse = "Sivilstand_20170101",
                type = Grunnlagstype.SIVILSTAND_PERIODE,
                grunnlagsreferanseListe = emptyList(),
                innhold = sivilstandInnhold,
            ),
        )

        return BeregnGrunnlag(
            periode = ÅrMånedsperiode(fom = beregningsperiodeFom, til = beregningsperiodeTil),
            søknadsbarnReferanse = "Person_Søknadsbarn",
            grunnlagListe = grunnlagListe,
        )
    }

    // Bygger opp BeregnForskuddResultatCore
    fun dummyForskuddResultatCore(): BeregnetForskuddResultatCore {
        val beregnetForskuddPeriodeListe = mutableListOf<ResultatPeriodeCore>()
        beregnetForskuddPeriodeListe.add(
            ResultatPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                resultat =
                ResultatBeregningCore(
                    belop = BigDecimal.valueOf(100),
                    kode = ResultatkodeForskudd.FORHØYET_FORSKUDD_100_PROSENT.name,
                    regel = "REGEL 1",
                ),
                grunnlagsreferanseListe =
                listOf(
                    INNTEKT_REFERANSE_1,
                    SIVILSTAND_REFERANSE_ENSLIG,
                    BARN_REFERANSE_1,
                    SOKNADBARN_REFERANSE,
                    BOSTATUS_REFERANSE_MED_FORELDRE_1,
                ),
            ),
        )

        return BeregnetForskuddResultatCore(
            beregnetForskuddPeriodeListe = beregnetForskuddPeriodeListe,
            sjablonListe = emptyList(),
            avvikListe = emptyList(),
        )
    }

    // Bygger opp BeregnForskuddResultatCore med avvik
    fun dummyForskuddResultatCoreMedAvvik(): BeregnetForskuddResultatCore {
        val avvikListe = mutableListOf<AvvikCore>()
        avvikListe.add(AvvikCore(avvikTekst = "beregnDatoFra kan ikke være null", avvikType = "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                avvikTekst =
                "periodeDatoTil må være etter periodeDatoFra i bidragMottakInntektPeriodeListe: periodeDatoFra=2018-04-01, " +
                    "periodeDatoTil=2018-03-01",
                avvikType = "DATO_FRA_ETTER_DATO_TIL",
            ),
        )

        return BeregnetForskuddResultatCore(beregnetForskuddPeriodeListe = emptyList(), sjablonListe = emptyList(), avvikListe = avvikListe)
    }

    // Bygger opp BeregnForskuddResultat
    fun dummyForskuddResultat(): BeregnetForskuddResultat {
        val beregnetForskuddPeriodeListe = mutableListOf<ResultatPeriode>()
        beregnetForskuddPeriodeListe.add(
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = LocalDate.parse("2017-01-01"), til = LocalDate.parse("2019-01-01")),
                resultat =
                ResultatBeregning(
                    belop = BigDecimal.valueOf(100),
                    kode = ResultatkodeForskudd.FORHØYET_FORSKUDD_100_PROSENT,
                    regel = "REGEL 1",
                ),
                grunnlagsreferanseListe =
                listOf(
                    INNTEKT_REFERANSE_1,
                    SIVILSTAND_REFERANSE_ENSLIG,
                    BARN_REFERANSE_1,
                    SOKNADBARN_REFERANSE,
                    BOSTATUS_REFERANSE_MED_FORELDRE_1,
                ),
            ),
        )

        return BeregnetForskuddResultat(beregnetForskuddPeriodeListe = beregnetForskuddPeriodeListe, grunnlagListe = emptyList())
    }

    // Bygger opp liste av sjablonverdier
    fun dummySjablonSjablontallListe(): List<Sjablontall> {
        val sjablonSjablontallListe = mutableListOf<Sjablontall>()
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(1490),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(1530),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(1570),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(1600),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(1640),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0005",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(1670),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0013",
                datoFom = LocalDate.parse("2003-01-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(320),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(241600),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(264200),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(271000),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(270200),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(277600),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0033",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(297500),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(370200),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(399100),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(408200),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(419700),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(430000),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0034",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(468500),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(314800),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(328700),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(335900),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(336500),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(344900),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0035",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(360800),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(58400),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(60200),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(61100),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(61700),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(62700),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0036",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(69100),
            ),
        )

        // Ikke i bruk for forskudd
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2015-07-01"),
                datoTom = LocalDate.parse("2016-06-30"),
                verdi = BigDecimal.valueOf(74250),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2016-07-01"),
                datoTom = LocalDate.parse("2017-06-30"),
                verdi = BigDecimal.valueOf(76250),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2017-07-01"),
                datoTom = LocalDate.parse("2018-06-30"),
                verdi = BigDecimal.valueOf(78300),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2018-07-01"),
                datoTom = LocalDate.parse("2019-06-30"),
                verdi = BigDecimal.valueOf(54750),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2019-07-01"),
                datoTom = LocalDate.parse("2020-06-30"),
                verdi = BigDecimal.valueOf(56550),
            ),
        )
        sjablonSjablontallListe.add(
            Sjablontall(
                typeSjablon = "0028",
                datoFom = LocalDate.parse("2020-07-01"),
                datoTom = LocalDate.parse("9999-12-31"),
                verdi = BigDecimal.valueOf(51300),
            ),
        )

        return sjablonSjablontallListe
    }

    fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
