package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.FaktiskUtgiftPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsperiodeGrunnlag
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

val fødselsdatoBarn1 = LocalDate.now().minusYears(8)
val referanseBP = "Person_Bidragspliktig"
val referanseBM = "Person_Bidragsmottaker"
val referanseBA = "Person_Søknadsbarn"
fun opprettGrunnlagBM() = GrunnlagDto(
    referanse = referanseBM,
    type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
    innhold = POJONode(
        Person(
            Personident("22222222222"),
            "Søkende Bidragsmottaker",
            LocalDate.parse("1982-05-05"),
        ),
    ),
    grunnlagsreferanseListe = emptyList(),
    gjelderReferanse = null,
)

fun opprettGrunnlagBP() = // Person Bidragspliktig
    GrunnlagDto(
        referanse = referanseBP,
        type = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        innhold = POJONode(
            Person(
                Personident("33333333333"),
                "Motvillig Bidragspliktig",
                LocalDate.parse("1981-07-13"),
            ),
        ),
        grunnlagsreferanseListe = emptyList(),
        gjelderReferanse = null,
    )

fun opprettGrunnlagBarn1() = // Person Bidragspliktig
    GrunnlagDto(
        referanse = referanseBA,
        type = Grunnlagstype.PERSON_SØKNADSBARN,
        innhold = POJONode(
            Person(
                Personident("11111111110"),
                "Trengende Søknadsbarn",
                fødselsdatoBarn1,
            ),
        ),
        grunnlagsreferanseListe = emptyList(),
        gjelderReferanse = null,
    )
fun opprettTestdata(periodeFra: YearMonth, periodeTil: YearMonth?, opphørsdato: YearMonth?) = BeregnGrunnlag(
    periode = ÅrMånedsperiode(periodeFra, periodeTil),
    søknadsbarnReferanse = referanseBA,
    opphørsdato = opphørsdato,
    grunnlagListe = listOf(
        opprettGrunnlagBarn1(),
        opprettGrunnlagBM(),
        opprettGrunnlagBP(),

        // Inntekt Bidragsmottaker
        GrunnlagDto(
            referanse = "Mottatt_InntektRapportering_Ainntekt_Bidragsmottaker_202008",
            type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
            innhold = POJONode(
                InntektsrapporteringPeriode(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    inntektsrapportering = Inntektsrapportering.AINNTEKT,
                    gjelderBarn = null,
                    beløp = BigDecimal(300000),
                    manueltRegistrert = false,
                    valgt = true,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderReferanse = referanseBM,
        ),

        // Inntekt Søknadsbarn
        GrunnlagDto(
            referanse = "Mottatt_InntektRapportering_Ainntekt_Søknadsbarn_202008",
            type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
            innhold = POJONode(
                InntektsrapporteringPeriode(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    inntektsrapportering = Inntektsrapportering.AINNTEKT,
                    gjelderBarn = null,
                    beløp = BigDecimal(3000),
                    manueltRegistrert = false,
                    valgt = true,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderReferanse = referanseBA,
        ),

        // Bostatus Søknadsbarn
        GrunnlagDto(
            referanse = "Bostatus_Søknadsbarn_202008",
            type = Grunnlagstype.BOSTATUS_PERIODE,
            innhold = POJONode(
                BostatusPeriode(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    bostatus = Bostatuskode.IKKE_MED_FORELDER,
                    manueltRegistrert = false,
                    relatertTilPart = referanseBP,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderBarnReferanse = referanseBA,
            gjelderReferanse = referanseBP,
        ),

        // Bostatus Bidragspliktig
        GrunnlagDto(
            referanse = "Bostatus_Bidragspliktig_202008",
            type = Grunnlagstype.BOSTATUS_PERIODE,
            innhold = POJONode(
                BostatusPeriode(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    bostatus = Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE,
                    manueltRegistrert = false,
                    relatertTilPart = referanseBP,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderReferanse = referanseBP,
        ),

        // Samværsperiode
        GrunnlagDto(
            referanse = "Mottatt_Samværsperiode_202008",
            type = Grunnlagstype.SAMVÆRSPERIODE,
            innhold = POJONode(
                SamværsperiodeGrunnlag(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
                    manueltRegistrert = false,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderBarnReferanse = referanseBA,
            gjelderReferanse = referanseBP,
        ),
        GrunnlagDto(
            referanse = "Mottatt_Samværsperiode_202008",
            type = Grunnlagstype.SAMVÆRSPERIODE,
            innhold = POJONode(
                FaktiskUtgiftPeriode(
                    ÅrMånedsperiode(periodeFra, periodeTil),
                    faktiskUtgiftBeløp = BigDecimal(1000),
                    kostpengerBeløp = BigDecimal(0),
                    fødselsdatoBarn = fødselsdatoBarn1,
                    manueltRegistrert = false,
                ),
            ),
            grunnlagsreferanseListe = emptyList(),
            gjelderBarnReferanse = referanseBA,
            gjelderReferanse = referanseBP,
        ),
    ),
)
