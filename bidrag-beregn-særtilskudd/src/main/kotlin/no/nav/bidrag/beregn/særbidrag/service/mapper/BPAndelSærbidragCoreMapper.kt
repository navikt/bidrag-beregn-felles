package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object BPAndelSærbidragCoreMapper : CoreMapper() {
    fun mapBPsAndelSærbidragGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBPsAndelSærbidragGrunnlagCore {
        val innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntekt(sjablonListe.sjablonSjablontallResponse)

        // Mapper grunnlagstyper til input for core
        val inntektBPPeriodeCoreListe =
            mapInntekt(
                beregnSærbidragrunnlag = beregnGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
            )

        val inntektBMPeriodeCoreListe =
            mapInntekt(
                beregnSærbidragrunnlag = beregnGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
            )

        val inntektSBPeriodeCoreListe =
            mapInntekt(
                beregnSærbidragrunnlag = beregnGrunnlag,
                referanseTilRolle = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN,
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
            )

        val utgiftPeriodeCoreListe = mapUtgift(beregnGrunnlag)

        // Henter aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            mapSjablonSjablontall(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablonListe.sjablonSjablontallResponse,
                sjablontallMap = sjablontallMap,
                criteria = { it.bpAndelSærbidrag },
            )

        return BeregnBPsAndelSærbidragGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            utgiftPeriodeListe = utgiftPeriodeCoreListe,
            inntektBPPeriodeListe = inntektBPPeriodeCoreListe,
            inntektBMPeriodeListe = inntektBMPeriodeCoreListe,
            inntektSBPeriodeListe = inntektSBPeriodeCoreListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }

    private fun mapUtgift(beregnSærbidragGrunnlag: BeregnGrunnlag): List<UtgiftPeriodeCore> {
        try {
            return beregnSærbidragGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUtgift>(Grunnlagstype.DELBEREGNING_UTGIFT)
                .map {
                    UtgiftPeriodeCore(
                        referanse = it.referanse,
                        periode =
                        PeriodeCore(
                            datoFom = it.innhold.periode.toDatoperiode().fom,
                            datoTil = it.innhold.periode.toDatoperiode().til,
                        ),
                        beløp = it.innhold.sumGodkjent,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.DELBEREGNING_UTGIFT er ikke gyldig: " + e.message,
            )
        }
    }
}
