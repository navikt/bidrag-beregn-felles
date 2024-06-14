package no.nav.bidrag.beregn.core.samvaersfradrag.beregning

import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.felles.FellesBeregning
import no.nav.bidrag.beregn.core.samvaersfradrag.bo.GrunnlagBeregningPeriodisert
import no.nav.bidrag.beregn.core.samvaersfradrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import java.math.BigDecimal

class SamvaersfradragBeregning : FellesBeregning() {

    fun beregn(grunnlag: GrunnlagBeregningPeriodisert): List<ResultatBeregning> {
        val resultatBeregningListe = mutableListOf<ResultatBeregning>()

        grunnlag.samvaersfradragGrunnlagPerBarnListe.forEach {
            // Henter sjablonverdier
            val sjablonNavnVerdiMap = hentSjablonVerdier(
                sjablonPeriodeListe = grunnlag.sjablonListe,
                samvaersklasse = it.samvaersklasse,
                soknadBarnAlder = it.barnAlder,
            )

            resultatBeregningListe.add(
                ResultatBeregning(
                    barnPersonId = it.barnPersonId,
                    belop = sjablonNavnVerdiMap[SjablonNavn.SAMVÆRSFRADRAG.navn]!!,
                    sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonListe),
                ),
            )
        }

        return resultatBeregningListe
    }

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>, samvaersklasse: String, soknadBarnAlder: Int): Map<String, BigDecimal> {
        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()
        val sjablonListe = sjablonPeriodeListe
            .map { it.sjablon }

        // Samværsfradrag
        sjablonNavnVerdiMap[SjablonNavn.SAMVÆRSFRADRAG.navn] = SjablonUtil.hentSjablonverdi(
            sjablonListe = sjablonListe,
            sjablonNavn = SjablonNavn.SAMVÆRSFRADRAG,
            sjablonNokkelListe = listOf(SjablonNokkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = samvaersklasse)),
            sjablonNokkelNavn = SjablonNøkkelNavn.ALDER_TOM,
            sjablonNokkelVerdi = soknadBarnAlder,
            sjablonInnholdNavn = SjablonInnholdNavn.FRADRAG_BELØP,
        )

        return sjablonNavnVerdiMap
    }
}
