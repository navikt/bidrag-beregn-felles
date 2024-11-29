package no.nav.bidrag.beregn.barnebidrag.beregning

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.mapping.tilGrunnlagsobjekt
import no.nav.bidrag.commons.service.sjablon.SjablonService
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningInnteksbasertGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InnholdMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.ManueltOverstyrtGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningGebyr
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import java.math.BigDecimal

internal data class DelberegningMaksInntektIntern(val maksInntekt: BigDecimal?, val grunnlagsliste: Set<InnholdMedReferanse<*>>)
internal data class DelberegningInntektsbasertGebyrIntern(
    val skalIleggesGebyr: Boolean,
    val grunnlagsliste: Set<BaseGrunnlag>,
    val referanseTilDelberegning: Grunnlagsreferanse?,
)

class BeregnGebyrService(private val sjablonService: SjablonService) {

    fun beregnGebyr(grunnlagsliste: List<GrunnlagDto>, referanseTilRolle: Grunnlagsreferanse): List<BaseGrunnlag> {
        val sjablonListe = sjablonService.hentSjablontall()
        val gebyrBeregning = grunnlagsliste.beregnGebyrForRolle(referanseTilRolle)
        val manueltOverstyrGebyrGrunnlag = grunnlagsliste.finnManueltOverstyrtGebyr(referanseTilRolle)
        val manueltOverstyrGebyrGrunnlagInnhold = manueltOverstyrGebyrGrunnlag?.innholdTilObjekt<ManueltOverstyrtGebyr>()
        val fastsettelsegebyrBeløpSjablon = sjablonListe.finnSjablonFastsettelsegebyr()
        val grunnlagFastsettelsegebyrBeløpSjablon = fastsettelsegebyrBeløpSjablon.tilGrunnlagsobjekt(SjablonTallNavn.FASTSETTELSESGEBYR_BELØP)

        val sluttberegning = GrunnlagDto(
            type = Grunnlagstype.SLUTTBEREGNING_GEBYR,
            referanse = "${Grunnlagstype.SLUTTBEREGNING_GEBYR.name}_$referanseTilRolle",
            innhold = POJONode(
                SluttberegningGebyr(
                    ilagtGebyr = manueltOverstyrGebyrGrunnlagInnhold?.ilagtGebyr ?: gebyrBeregning.skalIleggesGebyr,
                ),
            ),
            gjelderReferanse = referanseTilRolle,
            grunnlagsreferanseListe = listOfNotNull(
                gebyrBeregning.referanseTilDelberegning,
                manueltOverstyrGebyrGrunnlag?.referanse,
                grunnlagFastsettelsegebyrBeløpSjablon.referanse,
            ),
        )
        return (
            setOfNotNull(
                sluttberegning,
                manueltOverstyrGebyrGrunnlag,
                grunnlagFastsettelsegebyrBeløpSjablon,
            ) + gebyrBeregning.grunnlagsliste
            ).toList()
    }

    private fun List<GrunnlagDto>.beregnGebyrForRolle(referanseTilRolle: Grunnlagsreferanse): DelberegningInntektsbasertGebyrIntern {
        val sjablonListe = sjablonService.hentSjablontall()

        val inntektBeregning = beregnMaksInntektSistePeriode(referanseTilRolle)

        val nedreInntektsgrenseGebyrSjablon = sjablonListe.finnSjablonNedreInntektsgrense()

        val skalIleggesGebyr = nedreInntektsgrenseGebyrSjablon.verdi!! <= (inntektBeregning.maksInntekt ?: BigDecimal.ZERO)

        val grunnlagNedreInntektsgrenseGebyrSjablon = nedreInntektsgrenseGebyrSjablon.tilGrunnlagsobjekt(
            SjablonTallNavn.NEDRE_INNTEKTSGRENSE_GEBYR_BELØP,
        )

        val grunnlagDelberegning = inntektBeregning.maksInntekt?.let {
            GrunnlagDto(
                referanse = "${Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR.name}_$referanseTilRolle",
                type = Grunnlagstype.DELBEREGNING_INNTEKTSBASERT_GEBYR,
                innhold = POJONode(
                    DelberegningInnteksbasertGebyr(
                        ileggesGebyr = skalIleggesGebyr,
                        sumInntekt = inntektBeregning.maksInntekt,
                    ),
                ),
                grunnlagsreferanseListe =
                inntektBeregning.grunnlagsliste.map { it.referanse } +
                    listOf(grunnlagNedreInntektsgrenseGebyrSjablon.referanse),
                gjelderReferanse = referanseTilRolle,
            )
        }

        val grunnlaglisteDelberegning = grunnlagDelberegning?.let { setOf(it, grunnlagNedreInntektsgrenseGebyrSjablon) } ?: emptySet()
        val inntektGrunnlagsliste = inntektBeregning.grunnlagsliste.map { it.grunnlag }
        return DelberegningInntektsbasertGebyrIntern(
            skalIleggesGebyr,
            grunnlaglisteDelberegning + inntektGrunnlagsliste,
            grunnlagDelberegning?.referanse,
        )
    }

    private fun List<GrunnlagDto>.beregnMaksInntektSistePeriode(referanseTilRolle: Grunnlagsreferanse): DelberegningMaksInntektIntern {
        val årsinntektSistePeriode = finnÅrsinntektSistePeriode(referanseTilRolle)
        val barnetilleggSistePeriode = finnMaksBarnetilleggSistePeriode(referanseTilRolle)
        val inntektsgrunnlagForGebyrBeregning = if (årsinntektSistePeriode == null) {
            null
        } else {
            (årsinntektSistePeriode.innhold.skattepliktigInntekt ?: BigDecimal.ZERO) +
                (barnetilleggSistePeriode?.innhold?.barnetillegg ?: BigDecimal.ZERO)
        }

        return DelberegningMaksInntektIntern(inntektsgrunnlagForGebyrBeregning, setOfNotNull(barnetilleggSistePeriode, årsinntektSistePeriode))
    }
}

private fun List<GrunnlagDto>.finnMaksBarnetilleggSistePeriode(referanseTilRolle: Grunnlagsreferanse) =
    finnDelberegningSumInntektSistePeriode(referanseTilRolle).maxByOrNull { it.innhold.barnetillegg ?: BigDecimal.ZERO }

private fun List<GrunnlagDto>.finnÅrsinntektSistePeriode(referanseTilRolle: Grunnlagsreferanse) =
    finnDelberegningSumInntektSistePeriode(referanseTilRolle).maxByOrNull { it.innhold.skattepliktigInntekt ?: BigDecimal.ZERO }

private fun List<GrunnlagDto>.finnDelberegningSumInntektSistePeriode(referanseTilRolle: Grunnlagsreferanse) =
    filtrerOgKonverterBasertPåFremmedReferanse<DelberegningSumInntekt>(
        grunnlagType = Grunnlagstype.DELBEREGNING_SUM_INNTEKT,
        referanse = referanseTilRolle,
    ).groupBy { it.gjelderBarnReferanse }.map { (_, inntekt) -> inntekt.maxBy { it.innhold.periode.fom } }

private fun List<GrunnlagDto>.finnManueltOverstyrtGebyr(referanseTilRolle: Grunnlagsreferanse) =
    find { it.type == Grunnlagstype.MANUELT_OVERSTYRT_GEBYR && it.gjelderReferanse == referanseTilRolle }
private fun List<Sjablontall>.finnSjablonFastsettelsegebyr() = filter { it.typeSjablon == SjablonTallNavn.FASTSETTELSESGEBYR_BELØP.id }
    .maxBy { it.datoFom!! }
private fun List<Sjablontall>.finnSjablonNedreInntektsgrense() = filter { it.typeSjablon == SjablonTallNavn.NEDRE_INNTEKTSGRENSE_GEBYR_BELØP.id }
    .maxBy { it.datoFom!! }
