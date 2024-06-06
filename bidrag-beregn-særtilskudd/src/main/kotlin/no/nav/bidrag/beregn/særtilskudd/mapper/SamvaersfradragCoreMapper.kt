package no.nav.bidrag.beregn.særtilskudd.mapper

import no.nav.bidrag.beregn.saertilskudd.rest.consumer.SjablonListe
import no.nav.bidrag.beregn.saertilskudd.rest.exception.UgyldigInputException
import no.nav.bidrag.beregn.saertilskudd.rest.extensions.tilPeriodeCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.BeregnSamvaersfradragGrunnlagCore
import no.nav.bidrag.beregn.samvaersfradrag.dto.SamvaersklassePeriodeCore
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.Samvaersklasse

object SamvaersfradragCoreMapper : CoreMapper() {
    fun mapSamvaersfradragGrunnlagTilCore(beregnGrunnlag: BeregnGrunnlag, sjablonListe: SjablonListe): BeregnSamvaersfradragGrunnlagCore {
        val samvaersklassePeriodeCoreListe = ArrayList<SamvaersklassePeriodeCore>()

        // Løper gjennom alle grunnlagene og identifiserer de som skal mappes til samværsfradrag core
        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
            if (GrunnlagType.SAMVAERSKLASSE == grunnlag.type) {
                val samvaersklasse = grunnlagTilObjekt(grunnlag, Samvaersklasse::class.java)
                samvaersklassePeriodeCoreListe.add(samvaersklasse.tilCore(grunnlag.referanse!!))
            }
        }

        // Henter aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            ArrayList(
                mapSjablonSamvaersfradrag(
                    sjablonListe.sjablonSamvaersfradragResponse,
                    beregnGrunnlag,
                ),
            )
        return BeregnSamvaersfradragGrunnlagCore(
            beregnGrunnlag.beregnDatoFra!!,
            beregnGrunnlag.beregnDatoTil!!,
            samvaersklassePeriodeCoreListe,
            sjablonPeriodeCoreListe,
        )
    }
}

private fun Samvaersklasse.valider() {
    if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
    if (soknadsbarnFodselsdato == null) throw UgyldigInputException("soknadsbarnFodselsdato kan ikke være null")
    if (samvaersklasseId == null) throw UgyldigInputException("samvaersklasseId kan ikke være null")
}

private fun Samvaersklasse.tilCore(referanse: String): SamvaersklassePeriodeCore {
    valider()
    return SamvaersklassePeriodeCore(
        referanse,
        tilPeriodeCore(),
        soknadsbarnId!!,
        soknadsbarnFodselsdato!!,
        samvaersklasseId!!,
    )
}
