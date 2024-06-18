package no.nav.bidrag.beregn.mapper

import no.nav.bidrag.beregn.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.core.samvaersfradrag.dto.BeregnSamvaersfradragGrunnlagCore
import no.nav.bidrag.beregn.core.samvaersfradrag.dto.SamvaersklassePeriodeCore
import no.nav.bidrag.beregn.core.samvaersfradrag.tilCore
import no.nav.bidrag.beregn.exception.UgyldigInputException
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.Samvaersklasse

object SamvaersfradragCoreMapper : CoreMapper() {
    fun mapSamvaersfradragGrunnlagTilCore(beregnGrunnlag: BeregnGrunnlag, sjablonListe: SjablonListe): BeregnSamvaersfradragGrunnlagCore {
        val samvaersklassePeriodeCoreListe = ArrayList<SamvaersklassePeriodeCore>()

        // Løper gjennom alle grunnlagene og identifiserer de som skal mappes til samværsfradrag core
        for (grunnlag in beregnGrunnlag.grunnlagListe!!) {
            if (Grunnlagstype.SAMVÆRSKLASSE == grunnlag.type) {
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
