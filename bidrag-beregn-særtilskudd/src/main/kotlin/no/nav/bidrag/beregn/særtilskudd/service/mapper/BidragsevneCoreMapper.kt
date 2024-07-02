package no.nav.bidrag.beregn.særtilskudd.service.mapper

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal

internal object BidragsevneCoreMapper : CoreMapper() {
    fun mapBidragsevneGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnBidragsevneGrunnlagCore {
        // Henter sjablonverdi for kapitalinntekt
        // TODO Pt ligger det bare en gyldig sjablonverdi (uforandret siden 2003). Logikken her må utvides hvis det legges inn nye sjablonverdier
        // TODO Legge denne metoden et generisk sted
        val innslagKapitalinntektSjablonverdi =
            sjablonListe.sjablonSjablontallResponse.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }?.verdi
                ?: BigDecimal.ZERO

        val referanseBidragspliktig = beregnGrunnlag.grunnlagListe
            .filter { it.type == Grunnlagstype.PERSON_BIDRAGSPLIKTIG }
            .map { it.referanse }
            .firstOrNull() ?: throw NoSuchElementException("Grunnlagstype PERSON_BIDRAGSPLIKTIG mangler i input")

        // Mapper grunnlagstyper til input for core
        val voksneIHusstandenPeriodeCoreListe = mapVoksneIHusstanden(beregnGrunnlag)
        val inntektBPPeriodeCoreListe = mapInntekt(beregnGrunnlag, referanseBidragspliktig, innslagKapitalinntektSjablonverdi)
        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnGrunnlag)
        val sjablonPeriodeCoreListe = ArrayList<SjablonPeriodeCore>()

        // Validerer at alle nødvendige grunnlag er med
        validerGrunnlag(
            inntektGrunnlag = inntektBPPeriodeCoreListe.isNotEmpty(),
        )

        // Henter aktuelle sjabloner
        sjablonPeriodeCoreListe.addAll(
            mapSjablonSjablontall(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonSjablontallListe = sjablonListe.sjablonSjablontallResponse,
                sjablontallMap = sjablontallMap,
                filter = filtrerDelberegning("bidragsevne"),
            ),
        )
        sjablonPeriodeCoreListe.addAll(
            mapSjablonBidragsevne(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonBidragsevneListe = sjablonListe.sjablonBidragsevneResponse,
            ),
        )
        sjablonPeriodeCoreListe.addAll(
            mapSjablonTrinnvisSkattesats(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonTrinnvisSkattesatsListe = sjablonListe.sjablonTrinnvisSkattesatsResponse,
            ),
        )

        return BeregnBidragsevneGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            inntektPeriodeListe = inntektBPPeriodeCoreListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeCoreListe,
            voksneIHusstandenPeriodeListe = voksneIHusstandenPeriodeCoreListe,
            sjablonPeriodeListe = sjablonPeriodeCoreListe,
        )
    }

    private fun validerGrunnlag(inntektGrunnlag: Boolean) {
        when {
            !inntektGrunnlag -> {
                throw IllegalArgumentException("Inntekt mangler i input")
            }
        }
    }

    private fun mapBarnIHusstanden(beregnGrunnlag: BeregnGrunnlag): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter {
                        it.innhold.bostatus == Bostatuskode.MED_FORELDER || it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG ||
                            it.innhold.bostatus == Bostatuskode.DELT_BOSTED
                    }
                    .map {
                        BarnIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            antall = if (it.innhold.bostatus == Bostatuskode.DELT_BOSTED) 0.5 else 1.0,
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
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapVoksneIHusstanden(beregnGrunnlag: BeregnGrunnlag): List<VoksneIHusstandenPeriodeCore> {
        try {
            val voksneIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
//                    .filter {
//                        it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN || it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE
//                    }
                    .map {
                        VoksneIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            borMedAndre = it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                                it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }
            return akkumulerOgPeriodiser(
                voksneIHusstandenGrunnlagListe,
                beregnGrunnlag.søknadsbarnReferanse,
                VoksneIHusstandenPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
}
