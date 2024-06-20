package no.nav.bidrag.beregn.særtilskudd.service.mapper

import no.nav.bidrag.beregn.core.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.SkatteklassePeriodeCore
import no.nav.bidrag.beregn.core.bidragsevne.dto.SærfradragPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.særtilskudd.bo.Bidragsevne
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.SkatteklassePeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.SærfradragPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.særtilskudd.extensions.tilCore
import no.nav.bidrag.beregn.særtilskudd.service.mapper.BPAndelSaertilskuddCoreMapper.akkumulerOgPeriodiser
import no.nav.bidrag.beregn.særtilskudd.service.mapper.CoreMapper.mapInntekt
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.rolle.Rolle
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.BPInntekt
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.BarnIHusstand
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.Bostatus
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.InntektRolle
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.Saerfradrag
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.Skatteklasse
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal
import java.util.*

object BidragsevneCoreMapper : CoreMapper() {
    fun mapBidragsevneGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBidragsevneGrunnlagCore {
        val inntektBPPeriodeCoreListe = ArrayList<InntektPeriodeCore>()
        val skatteklassePeriodeCoreListe = ArrayList<SkatteklassePeriodeCore>()
        val bostatusPeriodeCoreListe = ArrayList<BostatusPeriodeCore>()
        val antallBarnIEgetHusholdPeriodeCoreListe = ArrayList<AntallBarnIEgetHusholdPeriodeCore>()
        val særfradragPeriodeCoreListe = ArrayList<SærfradragPeriodeCore>()
        val sjablonPeriodeCoreListe = ArrayList<SjablonPeriodeCore>()

        // Henter sjablonverdi for kapitalinntekt
        // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
        val innslagKapitalinntektSjablonverdi =
            sjablonListe.sjablontallResponse.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi
                ?: BigDecimal.ZERO

//        val referanseBidragsmottaker = beregnGrunnlag.grunnlagListe
//            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSMOTTAKER }
//            .map { it.referanse }
//            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_BIDRAGSMOTTAKER mangler i input")

        val referanseBidragspliktig = beregnGrunnlag.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_BIDRAGSPLIKTIG mangler i input")

//        val referanseSøknadsbarn = beregnGrunnlag.grunnlagListe
//            .filter { it.type == Grunnlagstype.PERSON_SØKNADSBARN }
//            .map { it.referanse }
//            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_SØKNADSBARN mangler i input")

        val inntektPeriodeCoreListe = mapInntekt(beregnGrunnlag, referanseBidragspliktig, innslagKapitalinntektSjablonverdi)

        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnForskuddGrunnlag)

        // Løper gjennom alle grunnlagene og identifiserer de som skal mappes til bidragsevne core
        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
            when (grunnlag.type) {
                Grunnlagstype.INNTEKT -> {
                    val (rolle) = grunnlagTilObjekt(grunnlag, InntektRolle::class.java)
                    if (rolle == Rolle.BIDRAGSPLIKTIG) {
                        val bpInntekt = grunnlagTilObjekt(grunnlag, BPInntekt::class.java)
                        inntektBPPeriodeCoreListe.add(bpInntekt.tilCore(grunnlag.referanse!!))
                    }
                }

                Grunnlagstype.BARN_I_HUSSTAND -> {
                    val barnIHusstand = grunnlagTilObjekt(grunnlag, BarnIHusstand::class.java)
                    antallBarnIEgetHusholdPeriodeCoreListe.add(barnIHusstand.tilCore(grunnlag.referanse!!))
                }

                Grunnlagstype.BOSTATUS -> {
                    val bostatus = grunnlagTilObjekt(grunnlag, Bostatus::class.java)
                    bostatusPeriodeCoreListe.add(bostatus.tilCore(grunnlag.referanse!!))
                }

                Grunnlagstype.SAERFRADRAG -> {
                    val saerfradrag = grunnlagTilObjekt(grunnlag, Saerfradrag::class.java)
                    særfradragPeriodeCoreListe.add(saerfradrag.tilCore(grunnlag.referanse!!))
                }

                Grunnlagstype.SKATTEKLASSE -> {
                    val skatteklasse = grunnlagTilObjekt(grunnlag, Skatteklasse::class.java)
                    skatteklassePeriodeCoreListe.add(skatteklasse.tilCore(grunnlag.referanse!!))
                }

                else -> {}
            }
        }
        // Hent aktuelle sjabloner
        sjablonPeriodeCoreListe.addAll(
            mapSjablonSjablontall(sjablonListe.sjablonSjablontallResponse, BIDRAGSEVNE, beregnGrunnlag, sjablontallMap),
        )
        sjablonPeriodeCoreListe.addAll(mapSjablonBidragsevne(sjablonListe.sjablonBidragsevneResponse, beregnGrunnlag))
        sjablonPeriodeCoreListe
            .addAll(mapSjablonTrinnvisSkattesats(sjablonListe.sjablonTrinnvisSkattesatsResponse, beregnGrunnlag))
        return BeregnBidragsevneGrunnlagCore(
            beregnGrunnlag.beregnDatoFra!!,
            beregnGrunnlag.beregnDatoTil!!,
            inntektBPPeriodeCoreListe,
            skatteklassePeriodeCoreListe,
            bostatusPeriodeCoreListe,
            antallBarnIEgetHusholdPeriodeCoreListe,
            særfradragPeriodeCoreListe,
            sjablonPeriodeCoreListe,
        )
    }

    private fun mapSjablonBidragsevne(sjablonBidragsevneListe: List<Bidragsevne>, beregnGrunnlag: BeregnGrunnlag): List<SjablonPeriodeCore> {
        val beregnDatoFra = beregnGrunnlag.beregnDatoFra
        val beregnDatoTil = beregnGrunnlag.beregnDatoTil
        return sjablonBidragsevneListe
            .stream()
            .filter { (_, datoFom, datoTom): Bidragsevne -> datoFom!!.isBefore(beregnDatoTil) && !datoTom!!.isBefore(beregnDatoFra) }
            .map { (bostatus, datoFom, datoTom, belopBoutgift, belopUnderhold): Bidragsevne ->
                SjablonPeriodeCore(
                    PeriodeCore(datoFom!!, datoTom),
                    SjablonNavn.BIDRAGSEVNE.navn,
                    listOf(SjablonNokkelCore(SjablonNokkelNavn.BOSTATUS.navn, bostatus!!)),
                    Arrays.asList(
                        SjablonInnholdCore(SjablonInnholdNavn.BOUTGIFT_BELØP.navn, belopBoutgift!!),
                        SjablonInnholdCore(SjablonInnholdNavn.UNDERHOLD_BELØP.navn, belopUnderhold!!),
                    ),
                )
            }
            .toList()
    }

    private fun mapBarnIHusstanden(beregnGrunnlag: BeregnGrunnlag): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter { it.innhold.bostatus == Bostatuskode.MED_FORELDER || it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG }
                    .map {
                        BarnIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            antall = 1,
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }
            return akkumulerOgPeriodiser(
                barnIHusstandenGrunnlagListe,
                beregnGrunnlag.søknadsbarnReferanse,
                BarnIHusstandenPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av forskudd. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
}

// fun BPInntekt.tilCore(referanse: String): InntektPeriodeCore = tilInntektPeriodeCore(referanse)

/*fun BasePeriode.valider() {
        if (datoFom == null) throw UgyldigInputException("datoFom kan ikke være null")
        if (datoTil == null) throw UgyldigInputException("datoTil kan ikke være null")
    }

fun BasePeriode.tilPeriodeCore(): PeriodeCore {
        valider()
        return PeriodeCore(datoFom!!, datoTil!!)
    }


fun InntektBase.validerInntekt() {
        if (inntektType == null) throw UgyldigInputException("inntektType kan ikke være null")
        if (belop == null) throw UgyldigInputException("belop kan ikke være null")
    }*/

/*fun InntektBase.tilInntektPeriodeCore(referanse: String): InntektPeriodeCore {
        validerInntekt()
        return InntektPeriodeCore(
            referanse,
            tilPeriodeCore(),
            inntektType!!,
            belop!!,
        )
    }*/

/*
fun InntektBase.tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse: String): no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore {
        validerInntekt()
        return no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore(
            referanse,
            tilPeriodeCore(),
            inntektType!!,
            belop!!,
            deltFordel = false,
            skatteklasse2 = false
        )
    }
*/

// fun BPInntekt.tilCore(referanse: String): InntektPeriodeCore = tilInntektPeriodeCore(referanse)

// fun BPInntekt.tilBPsAndelSaertilskuddCore(referanse: String) = tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)

/*fun BMInntekt.valider() {
        validerInntekt()
        if (deltFordel == null) throw UgyldigInputException("deltFordel kan ikke være null")
        if (skatteklasse2 == null) throw UgyldigInputException("skatteklasse2 kan ikke være null")
    }*/

/*fun BMInntekt.tilCore(referanse: String): no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore {
        valider()
        return no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore(
            referanse,
            tilPeriodeCore(),
            inntektType!!,
            belop!!,
            deltFordel!!,
            skatteklasse2!!
        )
    }*/

/*fun SBInntekt.valider() {
        validerInntekt()
        if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
    }*/

/*fun SBInntekt.tilCore(referanse: String): no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.InntektPeriodeCore {
        valider()
        return tilInntektPeriodeCoreBPsAndelSaertilskudd(referanse)
    }*/

/*fun BarnIHusstand.valider() {
        if (antall == null) throw UgyldigInputException("antall kan ikke være null")
    }*/

/*fun BarnIHusstand.tilCore(referanse: String): AntallBarnIEgetHusholdPeriodeCore {
        valider()
        return AntallBarnIEgetHusholdPeriodeCore(
            referanse,
            tilPeriodeCore(),
            antall!!
        )
    }*/

/*
fun Bostatus.valider() {
        if (bostatusKode == null) throw UgyldigInputException("bostatusKode kan ikke være null")
    }*/

/*fun Bostatus.tilCore(referanse: String): BostatusPeriodeCore {
        valider()
        return BostatusPeriodeCore(
            referanse,
            tilPeriodeCore(),
            bostatusKode!!
        )
    }*/

/*fun Saerfradrag.valider() {
        if (saerfradragKode == null) throw UgyldigInputException("saerfradragKode kan ikke være null")
    }

fun Saerfradrag.tilCore(referanse: String): SaerfradragPeriodeCore {
        valider()
        return SaerfradragPeriodeCore(
            referanse,
            tilPeriodeCore(),
            saerfradragKode!!
        )
    }*/

/*
fun Skatteklasse.valider() {
        if (skatteklasseId == null) throw UgyldigInputException("skatteklasseId kan ikke være null")
    }

fun Skatteklasse.tilCore(referanse: String): SkatteklassePeriodeCore {
        valider()
        return SkatteklassePeriodeCore(
            referanse,
            tilPeriodeCore(),
            skatteklasseId!!
        )
    }*/

/*
fun NettoSaertilskudd.valider() {
        if (nettoSaertilskuddBelop == null) throw UgyldigInputException("nettoSaertilskuddBelop kan ikke være null")
    }*/

/*fun NettoSaertilskudd.tilCore(referanse: String): NettoSaertilskuddPeriodeCore {
        valider()
        return NettoSaertilskuddPeriodeCore(
            referanse,
            tilPeriodeCore(),
            nettoSaertilskuddBelop!!
        )
    }*/

/*fun Samvaersklasse.valider() {
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
            samvaersklasseId!!
        )
}*/

/*fun LopendeBidrag.valider() {
        if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
        if (belop == null) throw UgyldigInputException("belop kan ikke være null")
        if (opprinneligBPAndelUnderholdskostnadBelop == null) throw UgyldigInputException("opprinneligBPAndelUnderholdskostnadBelop kan ikke være null")
        if (opprinneligBidragBelop == null) throw UgyldigInputException("opprinneligBidragBelop kan ikke være null")
        if (opprinneligSamvaersfradragBelop == null) throw UgyldigInputException("opprinneligSamvaersfradragBelop kan ikke være null")
    }*/

/*fun LopendeBidrag.tilCore(referanse: String): LopendeBidragPeriodeCore {
        valider()
        return LopendeBidragPeriodeCore(
            referanse,
            tilPeriodeCore(),
            soknadsbarnId!!,
            belop!!,
            opprinneligBPAndelUnderholdskostnadBelop!!,
            opprinneligBidragBelop!!,
            opprinneligSamvaersfradragBelop!!
        )
    }*/
