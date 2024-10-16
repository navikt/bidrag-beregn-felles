package no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.beregning

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.FellesBeregning
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregnetBidragPerBarn
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class BPsBeregnedeTotalbidragBeregning : FellesBeregning() {

    fun beregn(grunnlag: LøpendeBidragGrunnlagCore): ResultatBeregning {
        var bPsBeregnedeTotalbidrag = BigDecimal.ZERO

        val beregnetBidragPerBarnListe = mutableListOf<BeregnetBidragPerBarn>()

        val sjablonNavnVerdiMap = HashMap<String, BigDecimal>()

        val sjablonliste =
            grunnlag.sjablonPeriodeListe.filter { it.getPeriode().overlapperMed(Periode(grunnlag.beregnDatoFra, grunnlag.beregnDatoTil)) }

        grunnlag.løpendeBidragCoreListe.forEach {
            val beregnetBeløpAvrundet = it.beregnetBeløp
                .divide(
                    BigDecimal.TEN,
                    0,
                    RoundingMode.HALF_UP,
                ).multiply(
                    BigDecimal.TEN,
                )

            hentSjablonSamværsfradrag(
                sjablonNavnVerdiMap = sjablonNavnVerdiMap,
                sjablonPeriodeListe = sjablonliste,
                samværsklasse = it.samværsklasse.bisysKode,
                alderBarn = finnAlder(it.fødselsdatoBarn),
                referanseBarn = it.referanseBarn,
            )

            val samværsfradrag = sjablonNavnVerdiMap[SjablonNavn.SAMVÆRSFRADRAG.navn + "_" + it.referanseBarn] ?: BigDecimal.ZERO

            val reduksjonUnderholdskostnad = (beregnetBeløpAvrundet - it.faktiskBeløp).coerceAtLeast(BigDecimal.ZERO)

            val beregnetBidrag = it.løpendeBeløp + samværsfradrag + reduksjonUnderholdskostnad

            beregnetBidragPerBarnListe.add(
                BeregnetBidragPerBarn(
                    personidentBarn = it.personidentBarn,
                    saksnummer = it.saksnummer,
                    løpendeBeløp = it.løpendeBeløp,
                    samværsfradrag = samværsfradrag,
                    beregnetBeløp = beregnetBeløpAvrundet,
                    faktiskBeløp = it.faktiskBeløp,
                    reduksjonUnderholdskostnad = reduksjonUnderholdskostnad,
                    beregnetBidrag = beregnetBidrag,
                ),
            )

            bPsBeregnedeTotalbidrag += beregnetBidrag
        }

        return ResultatBeregning(
            bPsBeregnedeTotalbidrag = bPsBeregnedeTotalbidrag,
            beregnetBidragPerBarn = beregnetBidragPerBarnListe,
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
