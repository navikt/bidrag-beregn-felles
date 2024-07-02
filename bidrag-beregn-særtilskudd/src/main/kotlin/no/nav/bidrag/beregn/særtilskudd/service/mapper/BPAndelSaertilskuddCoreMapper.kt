package no.nav.bidrag.beregn.særtilskudd.service.mapper

object BPAndelSaertilskuddCoreMapper : CoreMapper() {
//    fun mapBPsAndelSaertilskuddGrunnlagTilCore(
//        beregnGrunnlag: BeregnGrunnlag,
//        sjablontallMap: Map<String, SjablonTallNavn>,
//        sjablonListe: SjablonListe,
//    ): BeregnBPsAndelSaertilskuddGrunnlagCore {
//        val nettoSaertilskuddPeriodeListe = ArrayList<NettoSaertilskuddPeriodeCore>()
//        val inntektBPPeriodeListe = ArrayList<InntektPeriodeCore>()
//        val inntektBMPeriodeListe = ArrayList<InntektPeriodeCore>()
//        val inntektBBPeriodeListe = ArrayList<InntektPeriodeCore>()
//
//        // Løper gjennom alle grunnlagene og identifiserer de som skal mappes til bidragsevne core
//        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
//            when (grunnlag.type) {
//                Grunnlagstype.INNTEKT -> {
//                    val (rolle) = grunnlagTilObjekt(grunnlag, InntektRolle::class.java)
//                    if (rolle == Rolle.BIDRAGSPLIKTIG) {
//                        val bpInntekt = grunnlagTilObjekt(grunnlag, BPInntekt::class.java)
//                        inntektBPPeriodeListe.add(bpInntekt.tilInntektPeriodeCoreBPsAndelSaertilskudd(grunnlag.referanse!!))
//                    } else if (rolle == Rolle.BIDRAGSMOTTAKER) {
//                        val bmInntekt = grunnlagTilObjekt(grunnlag, BMInntekt::class.java)
//                        inntektBMPeriodeListe.add(bmInntekt.tilCore(grunnlag.referanse!!))
//                    } else if (rolle == Rolle.SØKNADSBARN) {
//                        val sbInntekt = grunnlagTilObjekt(grunnlag, SBInntekt::class.java)
//                        inntektBBPeriodeListe.add(sbInntekt.tilCore(grunnlag.referanse!!))
//                    }
//                }
//
//                Grunnlagstype.NETTO_SAERTILSKUDD -> {
//                    val nettoSaertilskudd = grunnlagTilObjekt(grunnlag, NettoSaertilskudd::class.java)
//                    nettoSaertilskuddPeriodeListe.add(nettoSaertilskudd.tilCore(grunnlag.referanse!!))
//                }
//
//                else -> {}
//            }
//        }
//
//        // Hent aktuelle sjabloner
//        val sjablonPeriodeCoreListe =
//            mapSjablonSjablontall(
//                sjablonListe.sjablontallResponse,
//                BP_ANDEL_SAERTILSKUDD,
//                beregnGrunnlag,
//                sjablontallMap,
//            )
//        return BeregnBPsAndelSaertilskuddGrunnlagCore(
//            beregnGrunnlag.beregnDatoFra!!,
//            beregnGrunnlag.beregnDatoTil!!,
//            nettoSaertilskuddPeriodeListe,
//            inntektBPPeriodeListe,
//            inntektBMPeriodeListe,
//            inntektBBPeriodeListe,
//            sjablonPeriodeCoreListe,
//        )
//    }
}

/*fun BasePeriode.tilPeriodeCore(): PeriodeCore {
    valider()
    return PeriodeCore(datoFom!!, datoTil!!)
}

fun BasePeriode.valider() {
    if (datoFom == null) throw UgyldigInputException("datoFom kan ikke være null")
    if (datoTil == null) throw UgyldigInputException("datoTil kan ikke være null")
}*/

/*fun InntektBase.tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse: String): InntektPeriodeCore {
    validerInntekt()
    return InntektPeriodeCore(
        referanse,
        tilPeriodeCore(),
        inntektType!!,
        belop!!,
        deltFordel = false,
        skatteklasse2 = false
    )
}

fun InntektBase.validerInntekt() {
    if (inntektType == null) throw UgyldigInputException("inntektType kan ikke være null")
    if (belop == null) throw UgyldigInputException("belop kan ikke være null")
}*/

/*fun BMInntekt.tilCore(referanse: String): InntektPeriodeCore {
    valider()
    return InntektPeriodeCore(
        referanse,
        tilPeriodeCore(),
        inntektType!!,
        belop!!,
        deltFordel!!,
        skatteklasse2!!
    )
}*/

/*fun SBInntekt.tilCore(referanse: String): InntektPeriodeCore {
    valider()
    return tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)
}*/

/*fun NettoSaertilskudd.tilCore(referanse: String): NettoSaertilskuddPeriodeCore {
    valider()
    return NettoSaertilskuddPeriodeCore(
        referanse,
        tilPeriodeCore(),
        nettoSaertilskuddBelop!!
    )
}*/
