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
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregningSumLøpendeBidragPerBarn
import java.math.BigDecimal
import java.time.LocalDate

class SumLøpendeBidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: LøpendeBidragGrunnlagCore): ResultatBeregning {
        var sumLøpendeBidrag = BigDecimal.ZERO

        val beregningPerBarnListe = mutableListOf<BeregningSumLøpendeBidragPerBarn>()

        var sjablonNavnVerdiMap = HashMap<String, BigDecimal>()

        val sjablonliste =
            grunnlag.sjablonPeriodeListe.filter { it.getPeriode().overlapperMed(Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil)) }

        grunnlag.løpendeBidragCoreListe.forEach {
            hentSjablonSamværsfradrag(
                sjablonNavnVerdiMap = sjablonNavnVerdiMap,
                sjablonPeriodeListe = sjablonliste,
                samværsklasse = it.samværsklasse.bisysKode,
                alderBarn = finnAlder(it.fødselsdatoBarn),
                referanseBarn = it.referanseBarn,
            )

            val samværsfradrag = sjablonNavnVerdiMap[SjablonNavn.SAMVÆRSFRADRAG.navn + "_" + it.referanseBarn] ?: BigDecimal.ZERO
            val resultat = it.løpendeBeløp + samværsfradrag + (it.beregnetBeløp - it.faktiskBeløp)

            beregningPerBarnListe.add(
                BeregningSumLøpendeBidragPerBarn(
                    personidentBarn = it.personidentBarn,
                    saksnummer = it.saksnummer,
                    løpendeBeløp = it.løpendeBeløp,
                    samværsfradrag = samværsfradrag,
                    beregnetBeløp = it.beregnetBeløp,
                    faktiskBeløp = it.faktiskBeløp,
                    resultat = resultat,
                ),
            )

            sumLøpendeBidrag += resultat
        }

        return ResultatBeregning(
            sumLøpendeBidrag = sumLøpendeBidrag,
            beregningPerBarn = beregningPerBarnListe,
            sjablonListe = byggSjablonResultatListe(sjablonNavnVerdiMap = sjablonNavnVerdiMap, sjablonPeriodeListe = grunnlag.sjablonPeriodeListe),

        )
    }

    private fun finnAlder(fødselsdato: LocalDate): Int {
        // Fødselsdato skal alltid settes til 1 juli i barnets fødeår
        val justertFødselsdato = fødselsdato.withMonth(7).withDayOfMonth(1)
        val alder = justertFødselsdato.until(LocalDate.now()).years
        return alder
    }

    // Henter sjablonverdier
    private fun hentSjablonSamværsfradrag(
        sjablonNavnVerdiMap: HashMap<String, BigDecimal>,
        sjablonPeriodeListe: List<SjablonPeriode>,
        samværsklasse: String,
        alderBarn: Int,
        referanseBarn: String,
    ): HashMap<String, BigDecimal> {
//        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()
        val sjablonListe = sjablonPeriodeListe.map { it.sjablon }.toList()

        // Samværsfradrag
        sjablonNavnVerdiMap[SjablonNavn.SAMVÆRSFRADRAG.navn + "_" + referanseBarn] =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonNavn = SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe = listOf(
                    SjablonNøkkel(navn = SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, verdi = samværsklasse),
                ),
                sjablonNøkkelNavn = SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdi = alderBarn,
                sjablonInnholdNavn = SjablonInnholdNavn.FRADRAG_BELØP,
            )
        return sjablonNavnVerdiMap
    }
}
