package no.nav.bidrag.beregn.service

import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.NettoSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.SjablonListe
import no.nav.bidrag.beregn.saertilskudd.rest.extensions.tilCore
import no.nav.bidrag.beregn.saertilskudd.rest.extensions.tilInntektPeriodeCoreBPsAndelSaertilskudd
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.domain.enums.Rolle
import no.nav.bidrag.domain.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.BMInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.BPInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.InntektRolle
import no.nav.bidrag.transport.beregning.saertilskudd.NettoSaertilskudd
import no.nav.bidrag.transport.beregning.saertilskudd.SBInntekt

object BPAndelSaertilskuddCoreMapper : CoreMapper() {
    fun mapBPsAndelSaertilskuddGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBPsAndelSaertilskuddGrunnlagCore {
        val nettoSaertilskuddPeriodeListe = ArrayList<NettoSaertilskuddPeriodeCore>()
        val inntektBPPeriodeListe = ArrayList<InntektPeriodeCore>()
        val inntektBMPeriodeListe = ArrayList<InntektPeriodeCore>()
        val inntektBBPeriodeListe = ArrayList<InntektPeriodeCore>()

        // Løper gjennom alle grunnlagene og identifiserer de som skal mappes til bidragsevne core
        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
            when (grunnlag.type) {
                GrunnlagType.INNTEKT -> {
                    val (rolle) = grunnlagTilObjekt(grunnlag, InntektRolle::class.java)
                    if (rolle == Rolle.BIDRAGSPLIKTIG) {
                        val bpInntekt = grunnlagTilObjekt(grunnlag, BPInntekt::class.java)
                        inntektBPPeriodeListe.add(bpInntekt.tilInntektPeriodeCoreBPsAndelSaertilskudd(grunnlag.referanse!!))
                    } else if (rolle == Rolle.BIDRAGSMOTTAKER) {
                        val bmInntekt = grunnlagTilObjekt(grunnlag, BMInntekt::class.java)
                        inntektBMPeriodeListe.add(bmInntekt.tilCore(grunnlag.referanse!!))
                    } else if (rolle == Rolle.SOKNADSBARN) {
                        val sbInntekt = grunnlagTilObjekt(grunnlag, SBInntekt::class.java)
                        inntektBBPeriodeListe.add(sbInntekt.tilCore(grunnlag.referanse!!))
                    }
                }

                GrunnlagType.NETTO_SAERTILSKUDD -> {
                    val nettoSaertilskudd = grunnlagTilObjekt(grunnlag, NettoSaertilskudd::class.java)
                    nettoSaertilskuddPeriodeListe.add(nettoSaertilskudd.tilCore(grunnlag.referanse!!))
                }

                else -> {}
            }
        }

        // Hent aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            mapSjablonSjablontall(
                sjablonListe.sjablonSjablontallResponse,
                BP_ANDEL_SAERTILSKUDD,
                beregnGrunnlag,
                sjablontallMap,
            )
        return BeregnBPsAndelSaertilskuddGrunnlagCore(
            beregnGrunnlag.beregnDatoFra!!,
            beregnGrunnlag.beregnDatoTil!!,
            nettoSaertilskuddPeriodeListe,
            inntektBPPeriodeListe,
            inntektBMPeriodeListe,
            inntektBBPeriodeListe,
            sjablonPeriodeCoreListe,
        )
    }
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
