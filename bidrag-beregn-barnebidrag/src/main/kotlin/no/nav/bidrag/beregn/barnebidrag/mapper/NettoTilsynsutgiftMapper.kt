package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.FaktiskTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.util.*

internal object NettoTilsynsutgiftMapper : CoreMapper() {
    fun mapNettoTilsynsutgiftGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): NettoTilsynsutgiftPeriodeGrunnlag {
        val referanseBM = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        val resultat = NettoTilsynsutgiftPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            faktiskTilsynsutgiftListe = mapFaktiskUtgift(mottattGrunnlag, referanseBM),
            kostpengerListe = mapKostpenger(mottattGrunnlag),
            tilleggsstønadListe = mapTilleggsstønad(mottattGrunnlag),
            sjablonMaksTilsynsbeløpPeriodeGrunnlagListe = mapSjablonMaksTilsynsbeløp(sjablonGrunnlag),
            sjablonMaksFradragsbeløpPeriodeGrunnlagListe = mapSjablonMaksFradragsbeløp(sjablonGrunnlag),

        )
        return resultat
    }

    private fun mapSøknadsbarn(beregnGrunnlag: BeregnGrunnlag): SøknadsbarnPeriodeGrunnlag {
        try {
            val søknadsbarnGrunnlag =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<Person>(referanse = beregnGrunnlag.søknadsbarnReferanse)

            return SøknadsbarnPeriodeGrunnlag(referanse = søknadsbarnGrunnlag[0].referanse, fødselsdato = søknadsbarnGrunnlag[0].innhold.fødselsdato)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Feil i grunnlag som inneholder søknadsbarn: " + e.message,
            )
        }
    }
    private fun mapFaktiskUtgift(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse): List<FaktiskkTilsynsutgiftPeriodeCore> {
        try {
            val faktiskTilsynsutgiftListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<FaktiskTilsynsutgift>(Grunnlagstype.FAKTISK_UTGIFT)
                    .map {
                        FaktiskTilsynsutgiftPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            borMedAndre = it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                                it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                            grunnlagsreferanseListe = Collections.emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                faktiskTilsynsutgiftListe,
                beregnGrunnlag.søknadsbarnReferanse,
                referanseTilRolle,
                FaktiskTilsynsutgiftPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av netto tilsynsutgifter. Innhold i Grunnlagstype.FAKTISK_UTGIFT er ikke gyldig: " + e.message,
            )
        }
    }
}
