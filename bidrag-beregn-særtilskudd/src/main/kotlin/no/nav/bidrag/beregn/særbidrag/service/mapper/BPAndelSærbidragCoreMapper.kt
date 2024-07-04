package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse

internal object BPAndelSærbidragCoreMapper : CoreMapper() {
    fun mapBPsAndelSaertilskuddGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBPsAndelSærtilskuddGrunnlagCore {

        val innslagKapitalinntektSjablonverdi = finnInnslagKapitalinntekt(sjablonListe.sjablonSjablontallResponse)

        // Mapper grunnlagstyper til input for core
        val inntektBPPeriodeCoreListe =
            mapInntekt(
                beregnSærtilskuddGrunnlag = beregnGrunnlag,
                referanseBidragspliktig = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi
            )

        val inntektBMPeriodeCoreListe =
            mapInntekt(
                beregnSærtilskuddGrunnlag = beregnGrunnlag,
                referanseBidragspliktig = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi
            )

        val inntektSBPeriodeCoreListe =
            mapInntekt(
                beregnSærtilskuddGrunnlag = beregnGrunnlag,
                referanseBidragspliktig = finnReferanseTilRolle(
                    grunnlagListe = beregnGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN
                ),
                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi
            )

        val utgiftPeriodeCoreListe = mapUtgift(beregnGrunnlag)

        // Henter aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            mapSjablonSjablontall(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablonListe.sjablonSjablontallResponse,
                sjablontallMap = sjablontallMap,
                criteria = { it.bpAndelSaertilskudd }
            )

        return BeregnBPsAndelSærtilskuddGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            utgiftPeriodeListe = utgiftPeriodeCoreListe,
            inntektBPPeriodeListe = inntektBPPeriodeCoreListe,
            inntektBMPeriodeListe = inntektBMPeriodeCoreListe,
            inntektBBPeriodeListe = inntektSBPeriodeCoreListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }

    private fun mapUtgift(
        beregnSærtilskuddGrunnlag: BeregnGrunnlag,
    ): List<UtgiftPeriodeCore> {
        try {
            return beregnSærtilskuddGrunnlag.grunnlagListe
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
