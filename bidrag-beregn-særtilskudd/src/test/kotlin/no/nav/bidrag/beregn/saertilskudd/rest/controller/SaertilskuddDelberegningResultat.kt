package no.nav.bidrag.beregn.saertilskudd.rest.controller

import no.nav.bidrag.beregn.saertilskudd.rest.mapper.CoreMapper.Companion.grunnlagTilObjekt
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.transport.beregning.felles.Grunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.BPsAndelSaertilskuddResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
import no.nav.bidrag.transport.beregning.saertilskudd.BidragsevneResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.SamvaersfradragResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.SjablonResultatPeriode

class SaertilskuddDelberegningResultat(beregnetTotalSaertilskuddResultat: BeregnetTotalSaertilskuddResultat) {
    var bidragsevneListe: MutableList<BidragsevneResultatPeriode> = ArrayList()
    var bpsAndelSaertilskuddListe: MutableList<BPsAndelSaertilskuddResultatPeriode> = ArrayList()
    var samvaersfradragListe: MutableList<SamvaersfradragResultatPeriode> = ArrayList()
    var sjablonPeriodeListe: MutableList<SjablonResultatPeriode> = ArrayList()

    init {
        for ((_, _, _, grunnlagReferanseListe) in beregnetTotalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe) {
            for (referanse in grunnlagReferanseListe) {
                val resultatGrunnlag =
                    beregnetTotalSaertilskuddResultat.grunnlagListe.stream()
                        .filter { (referanse1): Grunnlag -> referanse1 == referanse }
                        .findFirst()
                        .orElse(null)
                if (resultatGrunnlag != null) {
                    when (resultatGrunnlag.type) {
                        GrunnlagType.BIDRAGSEVNE ->
                            bidragsevneListe.add(
                                grunnlagTilObjekt(resultatGrunnlag, BidragsevneResultatPeriode::class.java),
                            )
                        GrunnlagType.BPS_ANDEL_SAERTILSKUDD ->
                            bpsAndelSaertilskuddListe.add(
                                grunnlagTilObjekt(
                                    resultatGrunnlag,
                                    BPsAndelSaertilskuddResultatPeriode::class.java,
                                ),
                            )

                        GrunnlagType.SAMVAERSFRADRAG ->
                            samvaersfradragListe.add(
                                grunnlagTilObjekt(
                                    resultatGrunnlag,
                                    SamvaersfradragResultatPeriode::class.java,
                                ),
                            )

                        GrunnlagType.SJABLON ->
                            sjablonPeriodeListe.add(
                                grunnlagTilObjekt(resultatGrunnlag, SjablonResultatPeriode::class.java),
                            )
                        else -> {}
                    }
                }
            }
        }
    }
}
