package no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.beregning

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import java.math.BigDecimal
import java.time.LocalDate

class SumLøpendeBidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: LøpendeBidragGrunnlagCore): ResultatBeregning {
        var totaltLøpendeBidrag = BigDecimal.ZERO
        var totaltBeregnetSamværsfradrag = BigDecimal.ZERO
        var totaltBidragRedusertMedBeløp = BigDecimal.ZERO

        val sjablonliste =
            grunnlag.sjablonPeriodeListe.filter { it.getPeriode().overlapperMed(Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil)) }

        grunnlag.løpendeBidragCoreListe.forEach {
            totaltLøpendeBidrag += it.løpendeBeløp
            totaltBeregnetSamværsfradrag += hentSjablonSamværsfradrag(
                sjablonPeriodeListe = sjablonliste,
                samværsklasse = it.samværsklasse.toString(),
                alderBarn = finnAlder(it.fødselsdatoBarn),
            )
            totaltBidragRedusertMedBeløp += it.beregnetBeløp.minus(it.faktiskBeløp)
        }

        return ResultatBeregning(
            sum = totaltLøpendeBidrag.plus(totaltBeregnetSamværsfradrag).minus(totaltBidragRedusertMedBeløp),
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = emptyMap(), sjablonPeriodeListe = grunnlag.sjablonPeriodeListe),

        )
    }

    private fun finnAlder(fødselsdato: LocalDate): Int {
        val alder = fødselsdato.until(LocalDate.now()).years
        return alder
    }

    // Henter sjablonverdier
    private fun hentSjablonSamværsfradrag(sjablonPeriodeListe: List<SjablonPeriode>, samværsklasse: String, alderBarn: Int): BigDecimal {
        val sjablonListe = sjablonPeriodeListe.map { it.sjablon }.toList()

        // Samværsfradrag
        val samværsfradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonNavn = SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe = listOf(
                    SjablonNøkkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = samværsklasse),
                ),
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdi = alderBarn,
                sjablonInnholdNavn = SjablonInnholdNavn.FRADRAG_BELØP,
            )
        return samværsfradrag
    }
}
