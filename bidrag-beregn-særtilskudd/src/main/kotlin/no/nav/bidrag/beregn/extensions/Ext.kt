package no.nav.bidrag.beregn.extensions

import no.nav.bidrag.beregn.core.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.SaerfradragPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.SkatteklassePeriodeCore
import no.nav.bidrag.beregn.core.bpsandelsaertilskudd.dto.NettoSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.core.samvaersfradrag.dto.SamvaersklassePeriodeCore
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.exception.UgyldigInputException
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.felles.Grunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.BMInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.BPInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.BarnIHusstand
import no.nav.bidrag.transport.beregning.saertilskudd.BasePeriode
import no.nav.bidrag.transport.beregning.saertilskudd.Bostatus
import no.nav.bidrag.transport.beregning.saertilskudd.InntektBase
import no.nav.bidrag.transport.beregning.saertilskudd.LopendeBidrag
import no.nav.bidrag.transport.beregning.saertilskudd.NettoSaertilskudd
import no.nav.bidrag.transport.beregning.saertilskudd.SBInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.Saerfradrag
import no.nav.bidrag.transport.beregning.saertilskudd.Samvaersklasse
import no.nav.bidrag.transport.beregning.saertilskudd.Skatteklasse

fun BeregnGrunnlag.valider() {
    if (beregnDatoFra == null) throw UgyldigInputException("beregnDatoFra kan ikke være null")
    if (beregnDatoTil == null) throw UgyldigInputException("beregnDatoTil kan ikke være null")
    grunnlagListe?.map { it.valider() } ?: throw UgyldigInputException("grunnlagListe kan ikke være null")
}

fun Grunnlag.valider() {
    if (referanse == null) throw UgyldigInputException("referanse kan ikke være null")
    if (type == null) throw UgyldigInputException("type kan ikke være null")
    if (innhold == null) throw UgyldigInputException("innhold kan ikke være null")
}

fun InntektBase.validerInntekt() {
    if (inntektType == null) throw UgyldigInputException("inntektType kan ikke være null")
    if (belop == null) throw UgyldigInputException("belop kan ikke være null")
}

fun InntektBase.tilInntektPeriodeCore(referanse: String): InntektPeriodeCore {
    validerInntekt()
    return InntektPeriodeCore(
        referanse,
        tilPeriodeCore(),
        inntektType!!,
        belop!!,
    )
}

fun InntektBase.tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse: String): no.nav.bidrag.beregn.core.bpsandelsaertilskudd.dto.InntektPeriodeCore {
    validerInntekt()
    return no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore(
        referanse,
        tilPeriodeCore(),
        inntektType!!,
        belop!!,
        deltFordel = false,
        skatteklasse2 = false,
    )
}

fun BasePeriode.valider() {
    if (datoFom == null) throw UgyldigInputException("datoFom kan ikke være null")
    if (datoTil == null) throw UgyldigInputException("datoTil kan ikke være null")
}

fun BasePeriode.tilPeriodeCore(): PeriodeCore {
    valider()
    return PeriodeCore(datoFom!!, datoTil!!)
}

fun BPInntekt.tilCore(referanse: String): InntektPeriodeCore = tilInntektPeriodeCore(referanse)

fun BPInntekt.tilBPsAndelSaertilskuddCore(referanse: String) = tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)

fun BMInntekt.valider() {
    validerInntekt()
    if (deltFordel == null) throw UgyldigInputException("deltFordel kan ikke være null")
    if (skatteklasse2 == null) throw UgyldigInputException("skatteklasse2 kan ikke være null")
}

fun BMInntekt.tilCore(referanse: String): no.nav.bidrag.beregn.core.bpsandelsaertilskudd.dto.InntektPeriodeCore {
    valider()
    return no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore(
        referanse,
        tilPeriodeCore(),
        inntektType!!,
        belop!!,
        deltFordel!!,
        skatteklasse2!!,
    )
}

fun SBInntekt.valider() {
    validerInntekt()
    if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
}

fun SBInntekt.tilCore(referanse: String): no.nav.bidrag.beregn.core.bpsandelsaertilskudd.dto.InntektPeriodeCore {
    valider()
    return tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)
}

/*fun SBInntekt.tilCore(referanse: String): no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore {
        valider()
        return tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)
    }*/

fun BarnIHusstand.valider() {
    if (antall == null) throw UgyldigInputException("antall kan ikke være null")
}

fun BarnIHusstand.tilCore(referanse: String): AntallBarnIEgetHusholdPeriodeCore {
    valider()
    return AntallBarnIEgetHusholdPeriodeCore(
        referanse,
        tilPeriodeCore(),
        antall!!,
    )
}

fun Bostatus.valider() {
    if (bostatusKode == null) throw UgyldigInputException("bostatusKode kan ikke være null")
}

fun Bostatus.tilCore(referanse: String): BostatusPeriodeCore {
    valider()
    return BostatusPeriodeCore(
        referanse,
        tilPeriodeCore(),
        bostatusKode!!,
    )
}

fun Saerfradrag.valider() {
    if (saerfradragKode == null) throw UgyldigInputException("saerfradragKode kan ikke være null")
}

fun Saerfradrag.tilCore(referanse: String): SaerfradragPeriodeCore {
    valider()
    return SaerfradragPeriodeCore(
        referanse,
        tilPeriodeCore(),
        saerfradragKode!!,
    )
}

fun Skatteklasse.valider() {
    if (skatteklasseId == null) throw UgyldigInputException("skatteklasseId kan ikke være null")
}

fun Skatteklasse.tilCore(referanse: String): SkatteklassePeriodeCore {
    valider()
    return SkatteklassePeriodeCore(
        referanse,
        tilPeriodeCore(),
        skatteklasseId!!,
    )
}

fun NettoSaertilskudd.valider() {
    if (nettoSaertilskuddBelop == null) throw UgyldigInputException("nettoSaertilskuddBelop kan ikke være null")
}

fun NettoSaertilskudd.tilCore(referanse: String): NettoSaertilskuddPeriodeCore {
    valider()
    return NettoSaertilskuddPeriodeCore(
        referanse,
        tilPeriodeCore(),
        nettoSaertilskuddBelop!!,
    )
}

fun Samvaersklasse.valider() {
    if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
    if (soknadsbarnFodselsdato == null) throw UgyldigInputException("soknadsbarnFodselsdato kan ikke være null")
    if (samvaersklasseId == null) throw UgyldigInputException("samvaersklasseId kan ikke være null")
}

fun Samvaersklasse.tilCore(referanse: String): SamvaersklassePeriodeCore {
    valider()
    return SamvaersklassePeriodeCore(
        referanse,
        tilPeriodeCore(),
        soknadsbarnId!!,
        soknadsbarnFodselsdato!!,
        samvaersklasseId!!,
    )
}

fun LopendeBidrag.valider() {
    if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
    if (belop == null) throw UgyldigInputException("belop kan ikke være null")
    if (opprinneligBPAndelUnderholdskostnadBelop == null) {
        throw UgyldigInputException(
            "opprinneligBPAndelUnderholdskostnadBelop kan ikke være null",
        )
    }
    if (opprinneligBidragBelop == null) throw UgyldigInputException("opprinneligBidragBelop kan ikke være null")
    if (opprinneligSamvaersfradragBelop == null) throw UgyldigInputException("opprinneligSamvaersfradragBelop kan ikke være null")
}
