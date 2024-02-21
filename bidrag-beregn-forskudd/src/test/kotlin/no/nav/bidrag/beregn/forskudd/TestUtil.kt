package no.nav.bidrag.beregn.forskudd

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.forskudd.core.bo.Alder
import no.nav.bidrag.beregn.forskudd.core.bo.BarnIHusstanden
import no.nav.bidrag.beregn.forskudd.core.bo.BarnIHusstandenPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddGrunnlag
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddResultat
import no.nav.bidrag.beregn.forskudd.core.bo.Bostatus
import no.nav.bidrag.beregn.forskudd.core.bo.BostatusPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Inntekt
import no.nav.bidrag.beregn.forskudd.core.bo.InntektPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.Sivilstand
import no.nav.bidrag.beregn.forskudd.core.bo.SivilstandPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.Søknadsbarn
import no.nav.bidrag.beregn.forskudd.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SivilstandPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SøknadsbarnCore
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.AldersgruppeForskudd
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth

object TestUtil {
    private const val INNTEKT_REFERANSE_1 = "INNTEKT_REFERANSE_1"
    private const val INNTEKT_REFERANSE_2 = "INNTEKT_REFERANSE_2"
    private const val INNTEKT_REFERANSE_3 = "INNTEKT_REFERANSE_3"
    private const val SIVILSTAND_REFERANSE_GIFT = "SIVILSTAND_REFERANSE_GIFT"
    private const val SIVILSTAND_REFERANSE_ENSLIG = "SIVILSTAND_REFERANSE_ENSLIG"
    private const val BARN_I_HUSSTANDEN_REFERANSE_1 = "BARN_I_HUSSTANDEN_REFERANSE_1"
    private const val BARN_I_HUSSTANDEN_REFERANSE_2 = "BARN_I_HUSSTANDEN_REFERANSE_2"
    private const val BARN_I_HUSSTANDEN_REFERANSE_3 = "BARN_I_HUSSTANDEN_REFERANSE_3"
    private const val BARN_I_HUSSTANDEN_REFERANSE_4 = "BARN_I_HUSSTANDEN_REFERANSE_4"
    private const val BARN_I_HUSSTANDEN_REFERANSE_5 = "BARN_I_HUSSTANDEN_REFERANSE_5"
    private const val SØKNADSBARN_REFERANSE = "SØKNADSBARN_REFERANSE"
    private const val BOSTATUS_REFERANSE_MED_FORELDRE_1 = "BOSTATUS_REFERANSE_MED_FORELDRE_1"
    private const val BOSTATUS_REFERANSE_MED_FORELDRE_2 = "BOSTATUS_REFERANSE_MED_FORELDRE_2"
    private const val BOSTATUS_REFERANSE_MED_ANDRE_ENN_FORELDRE = "BOSTATUS_REFERANSE_MED_ANDRE_ENN_FORELDRE"
    private const val BARN_REFERANSE_1 = "BARN_REFERANSE_1"

    // CORE

    // Sjablontall
    fun byggSjablonPeriodeNavnVerdiListe() = listOf(
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
            verdi = BigDecimal.valueOf(1280),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
            verdi = BigDecimal.valueOf(1710),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR.navn,
            verdi = BigDecimal.valueOf(320),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP.navn,
            verdi = BigDecimal.valueOf(270200),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
            verdi = BigDecimal.valueOf(419700),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
            verdi = BigDecimal.valueOf(336500),
        ),
        SjablonPeriodeNavnVerdi(
            periode = Periode(LocalDate.parse("2017-01-01"), null),
            navn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
            verdi = BigDecimal.valueOf(61700),
        ),
    )

    fun byggSjablonPeriodeListe() = listOf(
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1710))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(320))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(270200))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(419700))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(336500))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(61700))),
            ),
        ),
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1280))),
            ),
        ),
    )

    fun byggForskuddGrunnlagCore(): BeregnForskuddGrunnlagCore {
        return byggForskuddGrunnlagCore(Bostatuskode.MED_FORELDER.toString())
    }

    fun byggForskuddGrunnlagCore(bostatus: String): BeregnForskuddGrunnlagCore {
        val søknadsbarn = SøknadsbarnCore(referanse = SØKNADSBARN_REFERANSE, fødselsdato = LocalDate.parse("2006-05-12"))

        val bostatusPeriodeListe =
            listOf(
                BostatusPeriodeCore(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                    kode = bostatus,
                ),
            )

        val inntektPeriodeListe =
            listOf(
                InntektPeriodeCore(
                    referanse = INNTEKT_REFERANSE_1,
                    periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    beløp = BigDecimal.ZERO,
                    grunnlagsreferanseListe = emptyList(),
                ),
            )

        val bidragsmottakerSivilstandPeriodeListe =
            listOf(
                SivilstandPeriodeCore(
                    referanse = SIVILSTAND_REFERANSE_GIFT,
                    periode = PeriodeCore(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                    kode = Sivilstandskode.GIFT_SAMBOER.toString(),
                ),
                SivilstandPeriodeCore(
                    referanse = SIVILSTAND_REFERANSE_ENSLIG,
                    periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                    kode = Sivilstandskode.BOR_ALENE_MED_BARN.toString(),
                ),
            )

        val bidragsmottakerBarnPeriodeListe =
            listOf(
                BarnIHusstandenPeriodeCore(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                    antall = 1,
                    grunnlagsreferanseListe = emptyList(),
                ),
            )

        return BeregnForskuddGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusPeriodeListe,
            inntektPeriodeListe = inntektPeriodeListe,
            sivilstandPeriodeListe = bidragsmottakerSivilstandPeriodeListe,
            barnIHusstandenPeriodeListe = bidragsmottakerBarnPeriodeListe,
            sjablonPeriodeListe = emptyList(),
        )
    }

    fun byggForskuddResultat(): BeregnForskuddResultat {
        val periodeResultatListe =
            listOf(
                ResultatPeriode(
                    periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                    resultat = ResultatBeregning(
                        beløp = BigDecimal.valueOf(1600),
                        kode = Resultatkode.FORHØYET_FORSKUDD_100_PROSENT,
                        regel = "REGEL 1",
                        sjablonListe = byggSjablonPeriodeNavnVerdiListe(),
                    ),
                    grunnlag = GrunnlagBeregning(
                        inntektListe = listOf(
                            Inntekt(referanse = INNTEKT_REFERANSE_1, type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER", beløp = BigDecimal.valueOf(500000)),
                        ),
                        sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE_ENSLIG, kode = Sivilstandskode.BOR_ALENE_MED_BARN),
                        barnIHusstandenListe = listOf(
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
                        ),
                        søknadsbarnAlder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 10),
                        søknadsbarnBostatus = Bostatus(referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1, kode = Bostatuskode.MED_FORELDER),
                        sjablonListe = byggSjablonPeriodeListe(),
                    ),
                ),
                ResultatPeriode(
                    periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                    resultat = ResultatBeregning(
                        beløp = BigDecimal.valueOf(1200),
                        kode = Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT,
                        regel = "REGEL 2",
                        sjablonListe = byggSjablonPeriodeNavnVerdiListe(),
                    ),
                    grunnlag = GrunnlagBeregning(
                        inntektListe = listOf(
                            Inntekt(referanse = INNTEKT_REFERANSE_2, type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER", beløp = BigDecimal.valueOf(500000)),
                        ),
                        sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE_ENSLIG, kode = Sivilstandskode.BOR_ALENE_MED_BARN),
                        barnIHusstandenListe = listOf(
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
                        ),
                        søknadsbarnAlder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 10),
                        søknadsbarnBostatus = Bostatus(referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1, kode = Bostatuskode.MED_FORELDER),
                        sjablonListe = byggSjablonPeriodeListe(),
                    ),
                ),
                ResultatPeriode(
                    periode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                    resultat = ResultatBeregning(
                        beløp = BigDecimal.valueOf(0),
                        kode = Resultatkode.AVSLAG,
                        regel = "REGEL 11",
                        sjablonListe = byggSjablonPeriodeNavnVerdiListe(),
                    ),
                    grunnlag = GrunnlagBeregning(
                        inntektListe = listOf(
                            Inntekt(referanse = INNTEKT_REFERANSE_3, type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER", beløp = BigDecimal.valueOf(500000)),
                        ),
                        sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE_ENSLIG, kode = Sivilstandskode.BOR_ALENE_MED_BARN),
                        barnIHusstandenListe = listOf(
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                            BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
                        ),
                        søknadsbarnAlder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 10),
                        søknadsbarnBostatus = Bostatus(referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1, kode = Bostatuskode.MED_FORELDER),
                        sjablonListe = byggSjablonPeriodeListe(),
                    ),
                ),
            )
        return BeregnForskuddResultat(periodeResultatListe)
    }

    fun byggAvvikListe(): List<Avvik> {
        return listOf(Avvik(avvikTekst = "beregnDatoTil må være etter beregnDatoFra", avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL))
    }

    fun byggForskuddGrunnlag(): BeregnForskuddGrunnlag {
        return byggForskuddGrunnlag(beregnDatoFra = "2017-01-01", beregnDatoTil = "2019-08-01")
    }

    fun byggForskuddGrunnlag(beregnDatoFra: String, beregnDatoTil: String): BeregnForskuddGrunnlag {
        val fodselsdato = LocalDate.parse("2006-12-19")

        val bostatusListe =
            listOf(
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = LocalDate.parse("2018-09-01")),
                    kode = Bostatuskode.MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_ANDRE_ENN_FORELDRE,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-09-01"), datoTil = LocalDate.parse("2018-12-01")),
                    kode = Bostatuskode.IKKE_MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_2,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-12-01"), datoTil = null),
                    kode = Bostatuskode.MED_FORELDER,
                ),
            )

        val søknadsbarn = Søknadsbarn(referanse = SØKNADSBARN_REFERANSE, fødselsdato = fodselsdato)

        val inntektListe =
            listOf(
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_1,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(250000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_2,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(400000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_3,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = null),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(500000),
                ),
            )

        val sivilstandListe =
            listOf(
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_GIFT,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-05-01")),
                    kode = Sivilstandskode.GIFT_SAMBOER,
                ),
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_ENSLIG,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2018-05-01"), datoTil = LocalDate.parse("2019-08-01")),
                    kode = Sivilstandskode.BOR_ALENE_MED_BARN,
                ),
            )

        val barnIHusstandenListe =
            listOf(
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = null),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_2,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-09-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_3,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-07-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_4,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2018-12-01"), datoTil = null),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_5,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2019-04-01"), datoTil = null),
                    antall = 1,
                ),
            )

        return BeregnForskuddGrunnlag(
            beregnDatoFra = LocalDate.parse(beregnDatoFra),
            beregnDatoTil = LocalDate.parse(beregnDatoTil),
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusListe,
            inntektPeriodeListe = inntektListe,
            sivilstandPeriodeListe = sivilstandListe,
            barnIHusstandenPeriodeListe = barnIHusstandenListe,
            sjablonPeriodeListe = byggSjablonPeriodeListe(),
        )
    }

    fun byggForskuddGrunnlagUtenSivilstand(): BeregnForskuddGrunnlag {
        val beregnDatoFra = LocalDate.parse("2017-01-01")
        val beregnDatoTil = LocalDate.parse("2017-02-01")
        val fødselsdato = LocalDate.parse("2006-12-19")

        val bostatusListe =
            listOf(
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = LocalDate.parse("2018-09-01")),
                    kode = Bostatuskode.MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_ANDRE_ENN_FORELDRE,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-09-01"), datoTil = LocalDate.parse("2018-12-01")),
                    kode = Bostatuskode.IKKE_MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_2,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-12-01"), datoTil = null),
                    kode = Bostatuskode.MED_FORELDER,
                ),
            )

        val søknadsbarn = Søknadsbarn(referanse = SØKNADSBARN_REFERANSE, fødselsdato = fødselsdato)

        val inntektListe =
            listOf(
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_1,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(250000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_2,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(400000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_3,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = null),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(500000),
                ),
            )

        val barnIHusstandenListe =
            listOf(
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = LocalDate.parse("2019-04-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_2,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-09-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_3,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-07-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_4,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2018-12-01"), datoTil = LocalDate.parse("2019-04-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_5,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2019-04-01"), datoTil = LocalDate.parse("2019-04-01")),
                    antall = 1,
                ),
            )

        return BeregnForskuddGrunnlag(
            beregnDatoFra = beregnDatoFra,
            beregnDatoTil = beregnDatoTil,
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusListe,
            inntektPeriodeListe = inntektListe,
            sivilstandPeriodeListe = emptyList(),
            barnIHusstandenPeriodeListe = barnIHusstandenListe,
            sjablonPeriodeListe = byggSjablonPeriodeListe(),
        )
    }

    fun byggForskuddGrunnlagMedAvvik(): BeregnForskuddGrunnlag {
        val beregnDatoFra = LocalDate.parse("2017-01-01")
        val beregnDatoTil = LocalDate.parse("2017-01-01")
        val fødselsdato = LocalDate.parse("2006-12-19")

        val bostatusListe =
            listOf(
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = LocalDate.parse("2018-09-01")),
                    kode = Bostatuskode.MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_ANDRE_ENN_FORELDRE,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-09-01"), datoTil = null),
                    kode = Bostatuskode.IKKE_MED_FORELDER,
                ),
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_2,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2018-12-01"), datoTil = null),
                    kode = Bostatuskode.MED_FORELDER,
                ),
            )

        val søknadsbarn = Søknadsbarn(referanse = SØKNADSBARN_REFERANSE, fødselsdato = fødselsdato)

        val inntektListe =
            listOf(
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_1,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(250000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_2,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2018-02-01"), datoTil = LocalDate.parse("2019-01-01")),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(400000),
                ),
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_3,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = null),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(500000),
                ),
            )

        val sivilstandListe =
            listOf(
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_GIFT,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-04-01")),
                    kode = Sivilstandskode.GIFT_SAMBOER,
                ),
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_ENSLIG,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2018-03-01"), datoTil = LocalDate.parse("2019-07-01")),
                    kode = Sivilstandskode.BOR_ALENE_MED_BARN,
                ),
            )

        val barnIHusstandenListe =
            listOf(
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_2,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-07-01")),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_3,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2019-04-01"), datoTil = LocalDate.parse("2018-07-01")),
                    antall = 1,
                ),
            )

        return BeregnForskuddGrunnlag(
            beregnDatoFra = beregnDatoFra,
            beregnDatoTil = beregnDatoTil,
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusListe,
            inntektPeriodeListe = inntektListe,
            sivilstandPeriodeListe = sivilstandListe,
            barnIHusstandenPeriodeListe = barnIHusstandenListe,
            sjablonPeriodeListe = byggSjablonPeriodeListe(),
        )
    }

    fun byggForskuddGrunnlagUtenAndreBarn(): BeregnForskuddGrunnlag {
        val beregnDatoFra = LocalDate.parse("2017-01-01")
        val beregnDatoTil = LocalDate.parse("2017-02-01")
        val fødselsdato = LocalDate.parse("2006-12-19")

        val bostatusListe =
            listOf(
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2007-01-01"), datoTil = null),
                    kode = Bostatuskode.MED_FORELDER,
                ),
            )

        val søknadsbarn = Søknadsbarn(referanse = SØKNADSBARN_REFERANSE, fødselsdato = fødselsdato)

        val inntektListe =
            listOf(
                InntektPeriode(
                    referanse = INNTEKT_REFERANSE_1,
                    inntektPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.valueOf(250000),
                ),
            )

        val sivilstandListe =
            listOf(
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_GIFT,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    kode = Sivilstandskode.GIFT_SAMBOER,
                ),
            )

        val barnIHusstandenListe =
            listOf(
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    antall = 1,
                ),
            )

        return BeregnForskuddGrunnlag(
            beregnDatoFra = beregnDatoFra,
            beregnDatoTil = beregnDatoTil,
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusListe,
            inntektPeriodeListe = inntektListe,
            sivilstandPeriodeListe = sivilstandListe,
            barnIHusstandenPeriodeListe = barnIHusstandenListe,
            sjablonPeriodeListe = byggSjablonPeriodeListe(),
        )
    }

    fun byggForskuddGrunnlagMedFlereInntekterISammePeriode(inntektListe: List<InntektPeriode>): BeregnForskuddGrunnlag {
        val beregnDatoFra = LocalDate.parse("2017-01-01")
        val beregnDatoTil = LocalDate.parse("2018-01-01")
        val fødselsdato = LocalDate.parse("2007-12-19")

        val bostatusListe =
            listOf(
                BostatusPeriode(
                    referanse = BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    bostatusPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    kode = Bostatuskode.MED_FORELDER,
                ),
            )

        val søknadsbarn = Søknadsbarn(referanse = SØKNADSBARN_REFERANSE, fødselsdato = fødselsdato)

        val sivilstandListe =
            listOf(
                SivilstandPeriode(
                    referanse = SIVILSTAND_REFERANSE_ENSLIG,
                    sivilstandPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    kode = Sivilstandskode.BOR_ALENE_MED_BARN,
                ),
            )

        val barnIHusstandenListe =
            listOf(
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_1,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    antall = 1,
                ),
                BarnIHusstandenPeriode(
                    referanse = BARN_I_HUSSTANDEN_REFERANSE_2,
                    barnIHusstandenPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                    antall = 1,
                ),
            )

        return BeregnForskuddGrunnlag(
            beregnDatoFra = beregnDatoFra,
            beregnDatoTil = beregnDatoTil,
            søknadsbarn = søknadsbarn,
            bostatusPeriodeListe = bostatusListe,
            inntektPeriodeListe = inntektListe,
            sivilstandPeriodeListe = sivilstandListe,
            barnIHusstandenPeriodeListe = barnIHusstandenListe,
            sjablonPeriodeListe = byggSjablonPeriodeListe(),
        )
    }

    // SERVICE

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

    fun byggForskuddGrunnlagUtenInnhold(): BeregnGrunnlag {
        return byggDummyForskuddGrunnlag("innhold")
    }

    // Bygger opp BeregnGrunnlag
    private fun byggDummyForskuddGrunnlag(nullVerdi: String): BeregnGrunnlag {
        val mapper = ObjectMapper()
        val beregningsperiodeFom = YearMonth.parse("2017-01")
        val beregningsperiodeTil = if (nullVerdi == "beregningsperiodeTil") null else YearMonth.parse("2020-01")
        val referanse = if (nullVerdi == "referanse") "" else "Mottatt_BM_Inntekt_AG_20201201"
        val type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE
        val periodeFom = "2017-01"
        val periodeTil = "2020-01"
        val innhold =
            if (nullVerdi == "innhold") {
                POJONode(null)
            } else {
                mapper.valueToTree<JsonNode>(
                    mapOf(
                        "periode" to
                            mapOf(
                                "fom" to periodeFom,
                                "til" to periodeTil,
                            ),
                        "inntektsrapportering" to Inntektsrapportering.AINNTEKT.name,
                        "gelderBarn" to null,
                        "beløp" to 290000,
                        "manueltRegistrert" to false,
                        "valgt" to true,
                    ),
                )
            }
        val grunnlagListe =
            if (nullVerdi == "grunnlagListe") {
                emptyList()
            } else {
                listOf(
                    GrunnlagDto(
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
    fun byggForskuddBeregnGrunnlag(
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
        val personBidragsmottakerInnhold =
            mapper.valueToTree<JsonNode>(
                mapOf(
                    "ident" to "22222222222",
                    "navn" to "Bidragsmottaker",
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
                    "relatertTilPart" to "Person_Bidragsmottaker",
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
        val grunnlagListe = mutableListOf<GrunnlagDto>()

        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Person_Søknadsbarn",
                type = Grunnlagstype.PERSON_SØKNADSBARN,
                grunnlagsreferanseListe = emptyList(),
                innhold = personSøknadsbarnInnhold,
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Person_Bidragsmottaker",
                type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                grunnlagsreferanseListe = emptyList(),
                innhold = personBidragsmottakerInnhold,
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Bostatus_20170101",
                type = Grunnlagstype.BOSTATUS_PERIODE,
                grunnlagsreferanseListe = listOf("Person_Søknadsbarn"),
                innhold = bostatusInnhold,
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "BeregningInntektRapportering_Ainntekt_20170101",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragsmottaker",
                innhold = inntektInnhold,
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
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
                    beløp = BigDecimal.valueOf(100),
                    kode = Resultatkode.FORHØYET_FORSKUDD_100_PROSENT,
                    regel = "REGEL 3",
                    aldersgruppe = AldersgruppeForskudd.ALDER_0_10_ÅR,
                ),
                grunnlagsreferanseListe =
                listOf(
                    INNTEKT_REFERANSE_1,
                    SIVILSTAND_REFERANSE_ENSLIG,
                    BARN_REFERANSE_1,
                    SØKNADSBARN_REFERANSE,
                    BOSTATUS_REFERANSE_MED_FORELDRE_1,
                    "Person_Søknadsbarn",
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
