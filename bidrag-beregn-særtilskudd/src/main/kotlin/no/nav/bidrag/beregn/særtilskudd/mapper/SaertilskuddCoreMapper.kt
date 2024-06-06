package no.nav.bidrag.beregn.særtilskudd.mapper

import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.bidragsevne.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddResultatCore
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.BPsAndelSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.saertilskudd.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.LopendeBidragPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.SamvaersfradragPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.SjablonListe
import no.nav.bidrag.beregn.saertilskudd.rest.extensions.tilPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.extensions.valider
import no.nav.bidrag.beregn.samvaersfradrag.dto.BeregnSamvaersfradragResultatCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatBeregningCore
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.LopendeBidrag
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SaertilskuddCoreMapper : CoreMapper() {
    fun mapSaertilskuddGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        beregnBidragsevneResultatCore: BeregnBidragsevneResultatCore,
        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
        beregnSamvaersfradragResultatCore: BeregnSamvaersfradragResultatCore,
        soknadsBarnId: Int?,
        sjablonListe: SjablonListe,
    ): BeregnSaertilskuddGrunnlagCore {
        // Løp gjennom output fra beregning av bidragsevne og bygg opp ny input-liste til core
        val bidragsevnePeriodeCoreListe =
            beregnBidragsevneResultatCore.resultatPeriodeListe
                .stream()
                .map { (periode, resultatBeregning): ResultatPeriodeCore ->
                    BidragsevnePeriodeCore(
                        byggReferanseForDelberegning("Delberegning_BP_Bidragsevne", periode.datoFom),
                        PeriodeCore(periode.datoFom, periode.datoTil),
                        resultatBeregning.belop,
                    )
                }
                .toList()

        // Løp gjennom output fra beregning av BPs andel særtilskudd og bygg opp ny input-liste til core
        val bpAndelSaertilskuddPeriodeCoreListe =
            beregnBPsAndelSaertilskuddResultatCore.resultatPeriodeListe
                .stream()
                .map { (periode, resultatBeregning): no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatPeriodeCore ->
                    BPsAndelSaertilskuddPeriodeCore(
                        byggReferanseForDelberegning("Delberegning_BP_AndelSaertilskudd", periode.datoFom),
                        PeriodeCore(periode.datoFom, periode.datoTil),
                        resultatBeregning.resultatAndelProsent,
                        resultatBeregning.resultatAndelBelop,
                        resultatBeregning.barnetErSelvforsorget,
                    )
                }
                .toList()

        // Løp gjennom output fra beregning av samværsfradrag og bygg opp ny input-liste til core
        val samvaersfradragPeriodeCoreListe =
            beregnSamvaersfradragResultatCore.resultatPeriodeListe
                .stream()
                .flatMap { (periode, resultatBeregningListe): no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatPeriodeCore ->
                    resultatBeregningListe
                        .stream()
                        .map { (barnPersonId, resultatSamvaersfradragBelop): ResultatBeregningCore ->
                            SamvaersfradragPeriodeCore(
                                byggReferanseForDelberegning("Delberegning_BP_Samvaersfradrag", periode.datoFom),
                                PeriodeCore(
                                    periode.datoFom,
                                    periode.datoTil,
                                ),
                                barnPersonId,
                                resultatSamvaersfradragBelop,
                            )
                        }
                }
                .toList()
        val andreLopendeBidragListe = ArrayList<LopendeBidragPeriodeCore>()
        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
            if (GrunnlagType.LOPENDE_BIDRAG == grunnlag.type) {
                val lopendeBidrag = grunnlagTilObjekt(grunnlag, LopendeBidrag::class.java)
                andreLopendeBidragListe.add(lopendeBidrag.tilCore(grunnlag.referanse!!))
            }
        }

        // Henter aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            ArrayList(
                mapSjablonSjablontall(
                    sjablonListe.sjablonSjablontallResponse,
                    SAERTILSKUDD,
                    beregnGrunnlag,
                    mapSjablontall(),
                ),
            )
        return BeregnSaertilskuddGrunnlagCore(
            beregnGrunnlag.beregnDatoFra!!,
            beregnGrunnlag.beregnDatoTil!!,
            soknadsBarnId!!,
            bidragsevnePeriodeCoreListe,
            bpAndelSaertilskuddPeriodeCoreListe,
            andreLopendeBidragListe,
            samvaersfradragPeriodeCoreListe,
            sjablonPeriodeCoreListe,
        )
    }

    // Bygger referanse for delberegning
    private fun byggReferanseForDelberegning(delberegning: String, dato: LocalDate): String {
        return delberegning + "_" + dato.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }
}

fun LopendeBidrag.tilCore(referanse: String): LopendeBidragPeriodeCore {
    valider()
    return LopendeBidragPeriodeCore(
        referanse,
        tilPeriodeCore(),
        soknadsbarnId!!,
        belop!!,
        opprinneligBPAndelUnderholdskostnadBelop!!,
        opprinneligBidragBelop!!,
        opprinneligSamvaersfradragBelop!!,
    )
}
