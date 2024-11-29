package no.nav.bidrag.beregn.barnebidrag.beregning

import com.fasterxml.jackson.databind.node.POJONode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.bidrag.commons.web.mock.stubSjablonService
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningInnteksbasertGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.ManueltOverstyrtGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnGrunnlagSomErReferertAv
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertAv
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
val bmReferanse = "bmReferanse"
val bpReferanse = "bpReferanse"
val barn1Referanse = "barn1Referanse"
val barn2Referanse = "barn2Referanse"

class BeregnGebyrServiceTest {

    val gebyrBeregningApi = BeregnGebyrService(stubSjablonService())

    @Test
    fun `skal beregne gebyr`() {
        val grunnlagInput = opprettGrunnlagDelberegningInntekter()

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 6

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        val sjablonGrunnlag = resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag)
        sjablonGrunnlag shouldHaveSize 1
        sjablonGrunnlag.first().innholdTilObjekt<SjablonSjablontallPeriode>().sjablon shouldBe SjablonTallNavn.FASTSETTELSESGEBYR_BELØP

        sluttberegning.innhold.ilagtGebyr shouldBe true

        val delberegningInnteksbasertGebyrListe = resultat.finnOgKonverterGrunnlagSomErReferertAv<DelberegningInnteksbasertGebyr>(
            Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR,
            sluttberegning.grunnlag,
        )
        delberegningInnteksbasertGebyrListe.shouldHaveSize(1)
        val delberegningInnteksbasertGebyr = delberegningInnteksbasertGebyrListe.first()
        delberegningInnteksbasertGebyr.grunnlag.grunnlagsreferanseListe shouldHaveSize 3
        delberegningInnteksbasertGebyr.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_SUM_INNTEKT, delberegningInnteksbasertGebyr.grunnlag) shouldHaveSize 2
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, delberegningInnteksbasertGebyr.grunnlag) shouldHaveSize 1
        assertSoftly(delberegningInnteksbasertGebyr.innhold) {
            it.ileggesGebyr shouldBe true
            it.sumInntekt shouldBe BigDecimal(1004000)
        }
    }

    @Test
    fun `skal beregne gebyr med bare en barnetillegg`() {
        val grunnlagInput = listOf(
            opprettDelberegningSumInntektGrunnlag(
                "ref1",
                bmReferanse,
                barn1Referanse,
                BigDecimal(900000),
                BigDecimal(3000),
                LocalDate.parse("2024-01-01"),
            ),
        )

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 5

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        val sjablonGrunnlag = resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag)
        sjablonGrunnlag shouldHaveSize 1
        sjablonGrunnlag.first().innholdTilObjekt<SjablonSjablontallPeriode>().sjablon shouldBe SjablonTallNavn.FASTSETTELSESGEBYR_BELØP

        sluttberegning.innhold.ilagtGebyr shouldBe true

        val delberegningInnteksbasertGebyrListe = resultat.finnOgKonverterGrunnlagSomErReferertAv<DelberegningInnteksbasertGebyr>(
            Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR,
            sluttberegning.grunnlag,
        )
        delberegningInnteksbasertGebyrListe.shouldHaveSize(1)
        val delberegningInnteksbasertGebyr = delberegningInnteksbasertGebyrListe.first()
        delberegningInnteksbasertGebyr.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        delberegningInnteksbasertGebyr.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_SUM_INNTEKT, delberegningInnteksbasertGebyr.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, delberegningInnteksbasertGebyr.grunnlag) shouldHaveSize 1
        assertSoftly(delberegningInnteksbasertGebyr.innhold) {
            it.ileggesGebyr shouldBe true
            it.sumInntekt shouldBe BigDecimal(903000)
        }
    }

    @Test
    fun `skal beregne med manuelt overstyrt gebyr`() {
        val grunnlagInput = opprettGrunnlagDelberegningInntekter()
        grunnlagInput.add(
            GrunnlagDto(
                type = Grunnlagstype.MANUELT_OVERSTYRT_GEBYR,
                referanse = "manueltilagtgebyr",
                gjelderReferanse = bmReferanse,
                innhold = POJONode(
                    ManueltOverstyrtGebyr(
                        ilagtGebyr = false,
                        begrunnelse = "En begrunnelse",
                    ),
                ),
            ),
        )

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 7

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 3
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.MANUELT_OVERSTYRT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag) shouldHaveSize 1
        sluttberegning.innhold.ilagtGebyr shouldBe false
    }

    @Test
    fun `skal beregne med manuelt overstyrt gebyr uten grunnlag`() {
        val grunnlagInput = listOf(
            GrunnlagDto(
                type = Grunnlagstype.MANUELT_OVERSTYRT_GEBYR,
                referanse = "manueltilagtgebyr",
                gjelderReferanse = bmReferanse,
                innhold = POJONode(
                    ManueltOverstyrtGebyr(
                        ilagtGebyr = false,
                        begrunnelse = "En begrunnelse",
                    ),
                ),
            ),
        )

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 3

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.MANUELT_OVERSTYRT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag) shouldHaveSize 1
        sluttberegning.innhold.ilagtGebyr shouldBe false
    }

    @Test
    fun `skal beregne gebyr og ikke ilegge gebyr ved lav inntekt`() {
        val grunnlagInput = listOf(
            opprettDelberegningSumInntektGrunnlag(
                "ref3",
                bmReferanse,
                barn1Referanse,
                BigDecimal(10000),
                BigDecimal(2000),
                LocalDate.parse("2024-06-01"),
            ),
        )

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 5

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag) shouldHaveSize 1
        sluttberegning.innhold.ilagtGebyr shouldBe false

        val delberegningInnteksbasertGebyrListe = resultat.finnOgKonverterGrunnlagSomErReferertAv<DelberegningInnteksbasertGebyr>(
            Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR,
            sluttberegning.grunnlag,
        )
        val delberegningInnteksbasertGebyr = delberegningInnteksbasertGebyrListe.first()
        assertSoftly(delberegningInnteksbasertGebyr.innhold) {
            it.ileggesGebyr shouldBe false
            it.sumInntekt shouldBe BigDecimal(12000)
        }
    }

    @Test
    fun `skal beregne gebyr med ingen barnetillegg`() {
        val grunnlagInput = listOf(
            opprettDelberegningSumInntektGrunnlag(
                "ref3",
                bmReferanse,
                barn1Referanse,
                BigDecimal(10000000),
                null,
                LocalDate.parse("2024-06-01"),
            ),
        )

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        resultat shouldHaveSize 5

        val sluttberegningListe = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SluttberegningGebyr>(Grunnlagstype.SLUTTBEREGNING_GEBYR)
        sluttberegningListe shouldHaveSize 1
        val sluttberegning = sluttberegningListe.first()
        sluttberegning.grunnlag.grunnlagsreferanseListe shouldHaveSize 2
        sluttberegning.gjelderReferanse shouldBe bmReferanse
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR, sluttberegning.grunnlag) shouldHaveSize 1
        resultat.finnGrunnlagSomErReferertAv(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag) shouldHaveSize 1
        sluttberegning.innhold.ilagtGebyr shouldBe true
    }

    @Test
    fun `skal legge til grunnlag sjablon`() {
        val grunnlagInput = opprettGrunnlagDelberegningInntekter()

        val resultat = gebyrBeregningApi.beregnGebyr(grunnlagInput, bmReferanse)

        val sjablonGrunnlag = resultat.filtrerOgKonverterBasertPåFremmedReferanse<SjablonSjablontallPeriode>(Grunnlagstype.SJABLON_SJABLONTALL)
        sjablonGrunnlag shouldHaveSize 2

        val nedreInntektsgrenseGebyrSjablon = sjablonGrunnlag.find { it.innhold.sjablon == SjablonTallNavn.NEDRE_INNTEKTSGRENSE_GEBYR_BELØP }!!
        nedreInntektsgrenseGebyrSjablon.innhold.verdi shouldBe BigDecimal(331200)

        val fastsettelsegebyrSjablon = sjablonGrunnlag.find { it.innhold.sjablon == SjablonTallNavn.FASTSETTELSESGEBYR_BELØP }!!
        fastsettelsegebyrSjablon.innhold.verdi shouldBe BigDecimal(1277)
    }
}

private fun opprettGrunnlagDelberegningInntekter(): MutableList<GrunnlagDto> {
    val grunnlagBarn1 = listOf(
        opprettDelberegningSumInntektGrunnlag(
            "ref1",
            bmReferanse,
            barn1Referanse,
            BigDecimal(1000),
            BigDecimal(3000),
            LocalDate.parse("2024-01-01"),
        ),
        opprettDelberegningSumInntektGrunnlag(
            "ref2",
            bmReferanse,
            barn1Referanse,
            BigDecimal(1000),
            BigDecimal(5000),
            LocalDate.parse("2024-04-01"),
        ),
        opprettDelberegningSumInntektGrunnlag(
            "ref3",
            bmReferanse,
            barn1Referanse,
            BigDecimal(1000000),
            BigDecimal(2000),
            LocalDate.parse("2024-06-01"),
        ),
    )
    val grunnlagBarn2 = listOf(
        opprettDelberegningSumInntektGrunnlag(
            "ref1",
            bmReferanse,
            barn2Referanse,
            BigDecimal(1000),
            BigDecimal(3000),
            LocalDate.parse("2024-01-01"),
        ),
        opprettDelberegningSumInntektGrunnlag(
            "ref2",
            bmReferanse,
            barn2Referanse,
            BigDecimal(1000),
            BigDecimal(5000),
            LocalDate.parse("2024-04-01"),
        ),
        opprettDelberegningSumInntektGrunnlag(
            "ref3",
            bmReferanse,
            barn2Referanse,
            BigDecimal(1000000),
            BigDecimal(4000),
            LocalDate.parse("2024-06-01"),
        ),
    )

    return (grunnlagBarn1 + grunnlagBarn2).toMutableList()
}

private fun opprettDelberegningSumInntektGrunnlag(
    referanse: Grunnlagsreferanse = "referanse1",
    referanseRolle: Grunnlagsreferanse = "referanseRolle1",
    referanseBarn: Grunnlagsreferanse = "referanseBarn1",
    skattepliktigInntekt: BigDecimal = BigDecimal(1000000),
    barnetillegg: BigDecimal? = BigDecimal(3000),
    periodeFom: LocalDate = LocalDate.parse("2024-01-01"),
) = GrunnlagDto(
    type = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
    referanse = Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name + referanse + referanseBarn,
    innhold = POJONode(
        opprettDelberegningSumInntekt(
            skattepliktigInntekt,
            barnetillegg,
            ÅrMånedsperiode(periodeFom, periodeFom.plusMonths(2)),
        ),
    ),
    gjelderReferanse = referanseRolle,
    gjelderBarnReferanse = referanseBarn,
)
private fun opprettDelberegningSumInntekt(
    skattepliktigInntekt: BigDecimal = BigDecimal(1000000),
    barnetillegg: BigDecimal? = BigDecimal(3000),
    periode: ÅrMånedsperiode = ÅrMånedsperiode(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-04-30")),
) = DelberegningSumInntekt(
    skattepliktigInntekt = skattepliktigInntekt,
    totalinntekt = skattepliktigInntekt + (barnetillegg ?: BigDecimal.ZERO),
    barnetillegg = barnetillegg,
    utvidetBarnetrygd = null,
    kontantstøtte = null,
    småbarnstillegg = null,
    periode = periode,
)
