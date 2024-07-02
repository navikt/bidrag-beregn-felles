package no.nav.bidrag.beregn.særtilskudd.service.mapper

object SaertilskuddCoreMapper : CoreMapper() {
//    fun mapSaertilskuddGrunnlagTilCore(
//        beregnGrunnlag: BeregnGrunnlag,
//        beregnBidragsevneResultatCore: BeregnBidragsevneResultatCore,
//        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
//        beregnSamvaersfradragResultatCore: BeregnSamvaersfradragResultatCore,
//        soknadsBarnId: Int?,
//        sjablonListe: SjablonListe,
//    ): BeregnSaertilskuddGrunnlagCore {
//        // Løp gjennom output fra beregning av bidragsevne og bygg opp ny input-liste til core
//        val bidragsevnePeriodeCoreListe =
//            beregnBidragsevneResultatCore.resultatPeriodeListe
//                .stream()
//                .map { (periode, resultatBeregning): ResultatPeriodeCore ->
//                    BidragsevnePeriodeCore(
//                        byggReferanseForDelberegning("Delberegning_BP_Bidragsevne", periode.datoFom),
//                        PeriodeCore(periode.datoFom, periode.datoTil),
//                        resultatBeregning.belop,
//                    )
//                }
//                .toList()
//
//        // Løp gjennom output fra beregning av BPs andel særtilskudd og bygg opp ny input-liste til core
//        val bpAndelSaertilskuddPeriodeCoreListe =
//            beregnBPsAndelSaertilskuddResultatCore.resultatPeriodeListe
//                .stream()
//                .map { (periode, resultatBeregning): no.nav.bidrag.beregn.core.bpsandelsaertilskudd.dto.ResultatPeriodeCore ->
//                    BPsAndelSaertilskuddPeriodeCore(
//                        byggReferanseForDelberegning("Delberegning_BP_AndelSaertilskudd", periode.datoFom),
//                        PeriodeCore(periode.datoFom, periode.datoTil),
//                        resultatBeregning.resultatAndelProsent,
//                        resultatBeregning.resultatAndelBelop,
//                        resultatBeregning.barnetErSelvforsorget,
//                    )
//                }
//                .toList()
//
//        // Løp gjennom output fra beregning av samværsfradrag og bygg opp ny input-liste til core
//        val samvaersfradragPeriodeCoreListe =
//            beregnSamvaersfradragResultatCore.resultatPeriodeListe
//                .stream()
//                .flatMap { (periode, resultatBeregningListe): no.nav.bidrag.beregn.core.samvaersfradrag.dto.ResultatPeriodeCore ->
//                    resultatBeregningListe
//                        .stream()
//                        .map { (barnPersonId, resultatSamvaersfradragBelop): ResultatBeregningCore ->
//                            SamvaersfradragPeriodeCore(
//                                byggReferanseForDelberegning("Delberegning_BP_Samvaersfradrag", periode.datoFom),
//                                PeriodeCore(
//                                    periode.datoFom,
//                                    periode.datoTil,
//                                ),
//                                barnPersonId,
//                                resultatSamvaersfradragBelop,
//                            )
//                        }
//                }
//                .toList()
//        val andreLopendeBidragListe = ArrayList<LopendeBidragPeriodeCore>()
//        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
//            if (Grunnlagstype.LØPENDE_BIDRAG == grunnlag.type) {
//                val lopendeBidrag = grunnlagTilObjekt(grunnlag, LopendeBidrag::class.java)
//                andreLopendeBidragListe.add(lopendeBidrag.tilCore(grunnlag.referanse!!))
//            }
//        }
//
//        // Henter aktuelle sjabloner
//        val sjablonPeriodeCoreListe =
//            ArrayList(
//                mapSjablonSjablontall(
//                    sjablonListe.sjablontallResponse,
//                    SAERTILSKUDD,
//                    beregnGrunnlag,
//                    mapSjablontall(),
//                ),
//            )
//        return BeregnSaertilskuddGrunnlagCore(
//            beregnGrunnlag.beregnDatoFra!!,
//            beregnGrunnlag.beregnDatoTil!!,
//            soknadsBarnId!!,
//            bidragsevnePeriodeCoreListe,
//            bpAndelSaertilskuddPeriodeCoreListe,
//            andreLopendeBidragListe,
//            samvaersfradragPeriodeCoreListe,
//            sjablonPeriodeCoreListe,
//        )
//    }
//
//    // Bygger referanse for delberegning
//    private fun byggReferanseForDelberegning(delberegning: String, dato: LocalDate): String =
//        delberegning + "_" + dato.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}

// fun LopendeBidrag.tilCore(referanse: String): LopendeBidragPeriodeCore {
//    valider()
//    return LopendeBidragPeriodeCore(
//        referanse,
//        tilPeriodeCore(),
//        soknadsbarnId!!,
//        belop!!,
//        opprinneligBPAndelUnderholdskostnadBelop!!,
//        opprinneligBidragBelop!!,
//        opprinneligSamvaersfradragBelop!!,
//    )
// }
