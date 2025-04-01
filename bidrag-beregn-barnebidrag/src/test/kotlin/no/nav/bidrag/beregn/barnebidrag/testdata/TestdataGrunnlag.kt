package no.nav.bidrag.beregn.barnebidrag.testdata

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.AldersgruppeForskudd
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.rolle.SøktAvType
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningForskudd
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SøknadGrunnlag
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

val persongrunnlagBM =
    GrunnlagDto(
        type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        referanse = personIdentBidragsmottaker,
        innhold = POJONode(Person(ident = Personident(personIdentBidragsmottaker))),
    )
val persongrunnlagBA2 =
    GrunnlagDto(
        type = Grunnlagstype.PERSON_SØKNADSBARN,
        referanse = personIdentSøknadsbarn2,
        innhold = POJONode(Person(ident = Personident(personIdentSøknadsbarn2), fødselsdato = LocalDate.now().minusYears(15))),
    )
val persongrunnlagBA =
    GrunnlagDto(
        type = Grunnlagstype.PERSON_SØKNADSBARN,
        referanse = personIdentSøknadsbarn1,
        innhold = POJONode(Person(ident = Personident(personIdentSøknadsbarn1), fødselsdato = LocalDate.now().minusYears(15))),
    )
val persongrunnlagBP =
    GrunnlagDto(
        type = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        referanse = personIdentBidragspliktig,
        innhold = POJONode(Person(ident = Personident(personIdentBidragspliktig))),
    )

fun opprettBostatatusperiode(
    gjelderBarn: GrunnlagDto = persongrunnlagBA,
    referanse: String = "BOSTATUS_PERIODE",
) = GrunnlagDto(
    referanse = referanse,
    type = Grunnlagstype.BOSTATUS_PERIODE,
    grunnlagsreferanseListe = emptyList(),
    gjelderReferanse = persongrunnlagBM.referanse,
    gjelderBarnReferanse = gjelderBarn.referanse,
    innhold =
    POJONode(
        BostatusPeriode(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            bostatus = Bostatuskode.MED_FORELDER,
            manueltRegistrert = false,
            relatertTilPart = gjelderBarn.referanse,
        ),
    ),
)

fun opprettGrunnlagSøknad(referanse: String = "SØKNAD") = GrunnlagDto(
    referanse = referanse,
    type = Grunnlagstype.SØKNAD,
    grunnlagsreferanseListe = emptyList(),
    innhold =
    POJONode(
        SøknadGrunnlag(
            mottattDato = LocalDate.now(),
            søktFraDato = LocalDate.parse("2024-01-01"),
            søktAv = SøktAvType.BIDRAGSMOTTAKER,
        ),
    ),
)

fun opprettDelberegningBarnIHusstand(referanse: String = "DELBEREGNING_BARN_I_HUSSTAND") = GrunnlagDto(
    referanse = referanse,
    type = Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND,
    grunnlagsreferanseListe = listOf(opprettBostatatusperiode().referanse),
    gjelderReferanse = persongrunnlagBM.referanse,
    innhold =
    POJONode(
        DelberegningBarnIHusstand(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            antallBarn = 1.0,
        ),
    ),
)

fun opprettSivilstandPeriode() = GrunnlagDto(
    referanse = "SIVILSTAND_PERIODE",
    type = Grunnlagstype.SIVILSTAND_PERIODE,
    grunnlagsreferanseListe = emptyList(),
    gjelderReferanse = persongrunnlagBM.referanse,
    innhold =
    POJONode(
        SivilstandPeriode(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            sivilstand = Sivilstandskode.BOR_ALENE_MED_BARN,
            manueltRegistrert = false,
        ),
    ),
)

fun opprettInntektsrapportering() = GrunnlagDto(
    referanse = "INNTEKT_RAPPORTERING_PERIODE",
    type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
    grunnlagsreferanseListe = emptyList(),
    gjelderReferanse = persongrunnlagBM.referanse,
    innhold =
    POJONode(
        InntektsrapporteringPeriode(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            beløp = BigDecimal(600000),
            manueltRegistrert = true,
            inntektsrapportering = Inntektsrapportering.LØNN_MANUELT_BEREGNET,
            valgt = true,
        ),
    ),
)

fun opprettDelberegningSumInntekt() = GrunnlagDto(
    referanse = "DELBEREGNING_SUM_INNTEKT",
    type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
    grunnlagsreferanseListe = listOf(opprettInntektsrapportering().referanse),
    gjelderReferanse = persongrunnlagBM.referanse,
    innhold =
    POJONode(
        DelberegningSumInntekt(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            totalinntekt = BigDecimal(600000),
        ),
    ),
)
fun opprettGrunnlagDelberegningUnderholdskostnad() = GrunnlagDto(
    referanse = "DELBEREGNING_UNDERHOLDSKOSTNAD",
    type = Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD,
    grunnlagsreferanseListe = listOf(),
    innhold =
    POJONode(
        DelberegningUnderholdskostnad(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            forbruksutgift = BigDecimal(7587),
            boutgift = BigDecimal(3500),
            barnetrygd = BigDecimal(1500),
            underholdskostnad = BigDecimal(9600),
            barnetilsynMedStønad = BigDecimal(150),
            nettoTilsynsutgift = BigDecimal(1000),
        ),
    ),
)
fun opprettGrunnlagSamvær() = GrunnlagDto(
    referanse = "DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL",
    type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
    grunnlagsreferanseListe = listOf(opprettDelberegningSumInntekt().referanse),
    innhold =
    POJONode(
        DelberegningBidragspliktigesAndel(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            endeligAndelFaktor = BigDecimal("0.3"),
            andelBeløp = BigDecimal("0.5"),
            beregnetAndelFaktor = BigDecimal("0.5"),
            barnEndeligInntekt = BigDecimal("0.5"),
            barnetErSelvforsørget = false,
        ),
    ),
)

fun opprettGrunnlagDelberegningAndel() = GrunnlagDto(
    referanse = "DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL",
    type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
    grunnlagsreferanseListe = listOf(opprettDelberegningSumInntekt().referanse),
    innhold =
    POJONode(
        DelberegningBidragspliktigesAndel(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            endeligAndelFaktor = BigDecimal("0.3"),
            andelBeløp = BigDecimal("0.5"),
            beregnetAndelFaktor = BigDecimal("0.5"),
            barnEndeligInntekt = BigDecimal("0.5"),
            barnetErSelvforsørget = false,
        ),
    ),
)

fun opprettGrunnlagSluttberegningForskudd() = GrunnlagDto(
    referanse = "sluttberegning_forskudd",
    type = Grunnlagstype.SLUTTBEREGNING_FORSKUDD,
    grunnlagsreferanseListe =
    listOf(
        opprettSivilstandPeriode().referanse,
        opprettDelberegningBarnIHusstand().referanse,
        opprettDelberegningSumInntekt().referanse,
    ),
    innhold =
    POJONode(
        SluttberegningForskudd(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            beløp = BigDecimal(600000),
            resultatKode = Resultatkode.REDUSERT_FORSKUDD_50_PROSENT,
            aldersgruppe = AldersgruppeForskudd.ALDER_0_10_ÅR,
        ),
    ),
)

fun opprettGrunnlagSluttberegningSærbidrag() = GrunnlagDto(
    referanse = "sluttberegning_særbidrag",
    type = Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG,
    grunnlagsreferanseListe = listOf(opprettGrunnlagDelberegningAndel().referanse),
    innhold =
    POJONode(
        SluttberegningSærbidrag(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            beregnetBeløp = BigDecimal("100"),
            resultatBeløp = BigDecimal("100"),
            resultatKode = Resultatkode.SÆRBIDRAG_INNVILGET,
        ),
    ),
)

fun opprettGrunnlagSluttberegningBidrag() = GrunnlagDto(
    referanse = "sluttberegning_barnebidrag",
    type = Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
    grunnlagsreferanseListe = listOf(
        opprettGrunnlagDelberegningAndel().referanse,
        opprettGrunnlagDelberegningUnderholdskostnad().referanse,
    ),
    innhold =
    POJONode(
        SluttberegningBarnebidrag(
            periode = ÅrMånedsperiode(YearMonth.parse("2024-01"), null),
            beregnetBeløp = BigDecimal("100"),
            resultatBeløp = BigDecimal("100"),
            uMinusNettoBarnetilleggBM = BigDecimal("100"),
            bruttoBidragEtterBarnetilleggBM = BigDecimal("100"),
            nettoBidragEtterBarnetilleggBM = BigDecimal("100"),
            bruttoBidragEtterBarnetilleggBP = BigDecimal("100"),
            nettoBidragEtterSamværsfradrag = BigDecimal("100"),
            bpAndelAvUVedDeltBostedBeløp = BigDecimal("100"),
            bpAndelAvUVedDeltBostedFaktor = BigDecimal("100"),
            bruttoBidragJustertForEvneOg25Prosent = BigDecimal("100"),
        ),
    ),
)

fun opprettGrunnlagslisteBidrag(
    inntekter: List<GrunnlagDto> =
        listOf(
            opprettGrunnlagDelberegningAndel(),
            opprettDelberegningSumInntekt(),
        ),
) = listOf(
    persongrunnlagBA,
    persongrunnlagBM,
    persongrunnlagBP,
    opprettGrunnlagSøknad(),
    opprettGrunnlagSluttberegningBidrag(),
    opprettGrunnlagDelberegningAndel(),
    opprettGrunnlagDelberegningUnderholdskostnad(),
) + inntekter
