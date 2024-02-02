package no.nav.bidrag.beregn.forskudd.core

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.forskudd.core.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Sjablonverdier
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class ForskuddBeregning {
    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        val sjablonverdier = hentSjablonVerdier(grunnlag.sjablonListe)

        val maksInntektsgrense =
            sjablonverdier.forskuddssats100ProsentBelop.multiply(
                sjablonverdier.maksInntektForskuddMottakerMultiplikator,
            )

        // Legger sammen antall barn i husstanden
        val antallBarnIHusstanden = grunnlag.barnIHusstandenListe.count()

        // Inntektsintervall regnes ut med antall barn utover ett
        var inntektsIntervallTotal = sjablonverdier.inntektsintervallForskuddBelop * BigDecimal(antallBarnIHusstanden - 1)
        if (inntektsIntervallTotal < BigDecimal.ZERO) {
            inntektsIntervallTotal = BigDecimal.ZERO
        }

        val resultatKode: Resultatkode
        val regel: String

        // Legger sammen inntektene
        val bidragMottakerInntekt =
            grunnlag.inntektListe
                .map { it.belop }
                .fold(BigDecimal.ZERO, BigDecimal::add)

        when {
            // Søknadsbarn er over 18 år (REGEL 1)
            grunnlag.soknadBarnAlder.alder >= 18 -> {
                resultatKode = Resultatkode.AVSLAG
                regel = "REGEL 1"
            }

            // Søknadsbarn bor alene eller ikke med foreldre (REGEL 2/3)
            grunnlag.soknadBarnBostatus.kode != Bostatuskode.MED_FORELDER &&
                grunnlag.soknadBarnBostatus.kode != Bostatuskode.DOKUMENTERT_SKOLEGANG -> {
                resultatKode =
                    if (grunnlag.soknadBarnAlder.alder >= 11) {
                        Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
                    } else {
                        Resultatkode.FORHØYET_FORSKUDD_100_PROSENT
                    }
                regel = if (resultatKode == Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT) "REGEL 2" else "REGEL 3"
            }

            // Over maks inntektsgrense for forskudd (REGEL 4)
            !erUnderInntektsGrense(maksInntektsgrense, bidragMottakerInntekt) -> {
                resultatKode = Resultatkode.AVSLAG
                regel = "REGEL 4"
            }

            // Under maks inntektsgrense for fullt forskudd (REGEL 5/6)
            erUnderInntektsGrense(sjablonverdier.inntektsgrense100ProsentForskuddBelop, bidragMottakerInntekt) -> {
                resultatKode =
                    if (grunnlag.soknadBarnAlder.alder >= 11) {
                        Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
                    } else {
                        Resultatkode.FORHØYET_FORSKUDD_100_PROSENT
                    }
                regel = if (resultatKode == Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT) "REGEL 5" else "REGEL 6"
            }

            // Resterende regler (gift/enslig) (REGEL 7/8/9/10/11/12/13/14)
            else -> {
                resultatKode =
                    if (erUnderInntektsGrense(
                            inntektsgrense =
                            finnInntektsgrense(
                                sivilstandKode = grunnlag.sivilstand.kode,
                                inntektsgrenseEnslig75Prosent = sjablonverdier.inntektsgrenseEnslig75ProsentForskuddBelop,
                                inntektsgrenseGift75Prosent = sjablonverdier.inntektsgrenseGiftSamboer75ProsentForskuddBelop,
                            ).add(inntektsIntervallTotal),
                            inntekt = bidragMottakerInntekt,
                        )
                    ) {
                        Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
                    } else {
                        Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
                    }

                regel =
                    if (grunnlag.sivilstand.kode == Sivilstandskode.BOR_ALENE_MED_BARN) {
                        if (antallBarnIHusstanden == 1) {
                            if (resultatKode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 7" else "REGEL 8"
                        } else {
                            if (resultatKode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 9" else "REGEL 10"
                        }
                    } else {
                        if (antallBarnIHusstanden == 1) {
                            if (resultatKode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 11" else "REGEL 12"
                        } else {
                            if (resultatKode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 13" else "REGEL 14"
                        }
                    }
            }
        }

        return ResultatBeregning(
            belop = beregnForskudd(resultatKode = resultatKode, forskuddssats75ProsentBelop = sjablonverdier.forskuddssats75ProsentBelop),
            kode = resultatKode,
            regel = regel,
            sjablonListe = byggSjablonListe(sjablonPeriodeListe = grunnlag.sjablonListe, sjablonverdier = sjablonverdier),
        )
    }

    // Mapper ut sjablonverdier til ResultatBeregning (dette for å sikre at kun sjabloner som faktisk er brukt legges ut i grunnlaget for beregning)
    private fun byggSjablonListe(sjablonPeriodeListe: List<SjablonPeriode>, sjablonverdier: Sjablonverdier) = listOf(
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
            ),
            navn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
            verdi = sjablonverdier.forskuddssats75ProsentBelop,
        ),
        SjablonPeriodeNavnVerdi(
            periode = hentPeriode(sjablonPeriodeListe = sjablonPeriodeListe, sjablonNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn),
            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
            verdi = sjablonverdier.forskuddssats100ProsentBelop,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR.navn,
            ),
            navn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR.navn,
            verdi = sjablonverdier.maksInntektForskuddMottakerMultiplikator,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP.navn,
            ),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP.navn,
            verdi = sjablonverdier.inntektsgrense100ProsentForskuddBelop,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
            ),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
            verdi = sjablonverdier.inntektsgrenseEnslig75ProsentForskuddBelop,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
            ),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
            verdi = sjablonverdier.inntektsgrenseGiftSamboer75ProsentForskuddBelop,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
            ),
            navn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
            verdi = sjablonverdier.inntektsintervallForskuddBelop,
        ),
    )

    private fun hentPeriode(sjablonPeriodeListe: List<SjablonPeriode>, sjablonNavn: String): Periode {
        val sjablonPeriode = sjablonPeriodeListe.find { it.sjablon.navn == sjablonNavn }
        return sjablonPeriode?.getPeriode() ?: Periode(LocalDate.MIN, LocalDate.MAX)
    }

    // Beregner forskuddsbeløp basert på resultatkode
    // Forskudd 50%  = Sjablon 0038 * 2/3
    // Forskudd 75%  = Sjablon 0038
    // Forskudd 100% = Sjablon 0038 * 4/3
    // Forskudd 125% = Sjablon 0038 * 5/3
    private fun beregnForskudd(resultatKode: Resultatkode, forskuddssats75ProsentBelop: BigDecimal) = when (resultatKode) {
        Resultatkode.REDUSERT_FORSKUDD_50_PROSENT ->
            forskuddssats75ProsentBelop.multiply(BigDecimal.valueOf(2))
                .divide(BigDecimal.valueOf(3), -1, RoundingMode.HALF_UP)

        Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT -> forskuddssats75ProsentBelop
        Resultatkode.FORHØYET_FORSKUDD_100_PROSENT ->
            forskuddssats75ProsentBelop.multiply(BigDecimal.valueOf(4))
                .divide(BigDecimal.valueOf(3), -1, RoundingMode.HALF_UP)

        Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT ->
            forskuddssats75ProsentBelop.multiply(BigDecimal.valueOf(5))
                .divide(BigDecimal.valueOf(3), -1, RoundingMode.HALF_UP)

        else -> BigDecimal.ZERO
    }

    private fun erUnderInntektsGrense(inntektsgrense: BigDecimal, inntekt: BigDecimal) = inntekt.compareTo(inntektsgrense) < 1

    private fun finnInntektsgrense(
        sivilstandKode: Sivilstandskode,
        inntektsgrenseEnslig75Prosent: BigDecimal,
        inntektsgrenseGift75Prosent: BigDecimal,
    ) = if (sivilstandKode == Sivilstandskode.BOR_ALENE_MED_BARN) inntektsgrenseEnslig75Prosent else inntektsgrenseGift75Prosent

    // Henter sjablonverdier
    private fun hentSjablonVerdier(sjablonPeriodeListe: List<SjablonPeriode>): Sjablonverdier {
        val sjablonListe =
            sjablonPeriodeListe
                .map { it.sjablon }
        val forskuddssats75ProsentBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP,
            )
        val forskuddssats100ProsentBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP,
            )
        val maksInntektForskuddMottakerMultiplikator =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR,
            )
        val inntektsgrense100ProsentForskuddBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP,
            )
        val inntektsgrenseEnslig75ProsentForskuddBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
            )
        val inntektsgrenseGiftSamboer75ProsentForskuddBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
            )
        val inntektsintervallForskuddBelop =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonListe,
                sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
            )

        return Sjablonverdier(
            forskuddssats75ProsentBelop = forskuddssats75ProsentBelop,
            forskuddssats100ProsentBelop = forskuddssats100ProsentBelop,
            maksInntektForskuddMottakerMultiplikator = maksInntektForskuddMottakerMultiplikator,
            inntektsgrense100ProsentForskuddBelop = inntektsgrense100ProsentForskuddBelop,
            inntektsgrenseEnslig75ProsentForskuddBelop = inntektsgrenseEnslig75ProsentForskuddBelop,
            inntektsgrenseGiftSamboer75ProsentForskuddBelop = inntektsgrenseGiftSamboer75ProsentForskuddBelop,
            inntektsintervallForskuddBelop = inntektsintervallForskuddBelop,
        )
    }
}
