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
import java.time.LocalDate
import kotlin.math.roundToInt

class ForskuddBeregning {
    fun beregn(grunnlag: GrunnlagBeregning): ResultatBeregning {
        val sjablonverdier = hentSjablonverdier(grunnlag.sjablonListe)

        val maksInntektsgrense = sjablonverdier.forskuddssats100ProsentBeløp.multiply(sjablonverdier.maksInntektForskuddMottakerMultiplikator)

        // Legger sammen antall barn i husstanden
        val antallBarnIHusstanden = grunnlag.barnIHusstandenListe.sumOf { it.antall }

        // Inntektsintervall regnes ut med antall barn utover ett
        val inntektsintervallTotal = maxOf(sjablonverdier.inntektsintervallForskuddBeløp * BigDecimal(antallBarnIHusstanden - 1), BigDecimal.ZERO)

        val resultatkode: Resultatkode
        val regel: String

        // Legger sammen inntektene
        val bidragsmottakerInntekt = grunnlag.inntektListe.sumOf { it.beløp }

        when {
            // Søknadsbarn er over 18 år (REGEL 1)
            grunnlag.søknadsbarnAlder.alder >= 18 -> {
                resultatkode = Resultatkode.AVSLAG
                regel = "REGEL 1"
            }

            // Søknadsbarn bor alene eller ikke med foreldre (REGEL 2/3)
            grunnlag.søknadsbarnBostatus.kode != Bostatuskode.MED_FORELDER &&
                grunnlag.søknadsbarnBostatus.kode != Bostatuskode.DOKUMENTERT_SKOLEGANG -> {
                resultatkode =
                    if (grunnlag.søknadsbarnAlder.alder >= 11) {
                        Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
                    } else {
                        Resultatkode.FORHØYET_FORSKUDD_100_PROSENT
                    }
                regel = if (resultatkode == Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT) "REGEL 2" else "REGEL 3"
            }

            // Over maks inntektsgrense for forskudd (REGEL 4)
            !erUnderInntektsgrense(maksInntektsgrense, bidragsmottakerInntekt) -> {
                resultatkode = Resultatkode.AVSLAG
                regel = "REGEL 4"
            }

            // Under maks inntektsgrense for fullt forskudd (REGEL 5/6)
            erUnderInntektsgrense(sjablonverdier.inntektsgrense100ProsentForskuddBeløp, bidragsmottakerInntekt) -> {
                resultatkode =
                    if (grunnlag.søknadsbarnAlder.alder >= 11) {
                        Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT
                    } else {
                        Resultatkode.FORHØYET_FORSKUDD_100_PROSENT
                    }
                regel = if (resultatkode == Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT) "REGEL 5" else "REGEL 6"
            }

            // Resterende regler (gift/enslig) (REGEL 7/8/9/10/11/12/13/14)
            else -> {
                resultatkode =
                    if (erUnderInntektsgrense(
                            inntektsgrense =
                            finnInntektsgrense(
                                sivilstandKode = grunnlag.sivilstand.kode,
                                inntektsgrenseEnslig75Prosent = sjablonverdier.inntektsgrenseEnslig75ProsentForskuddBeløp,
                                inntektsgrenseGift75Prosent = sjablonverdier.inntektsgrenseGiftSamboer75ProsentForskuddBeløp,
                            ).add(inntektsintervallTotal),
                            inntekt = bidragsmottakerInntekt,
                        )
                    ) {
                        Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT
                    } else {
                        Resultatkode.REDUSERT_FORSKUDD_50_PROSENT
                    }

                regel =
                    if (grunnlag.sivilstand.kode == Sivilstandskode.BOR_ALENE_MED_BARN) {
                        if (antallBarnIHusstanden == 1) {
                            if (resultatkode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 7" else "REGEL 8"
                        } else {
                            if (resultatkode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 9" else "REGEL 10"
                        }
                    } else {
                        if (antallBarnIHusstanden == 1) {
                            if (resultatkode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 11" else "REGEL 12"
                        } else {
                            if (resultatkode == Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) "REGEL 13" else "REGEL 14"
                        }
                    }
            }
        }

        return ResultatBeregning(
            beløp = beregnForskudd(resultatkode = resultatkode, forskuddssats75ProsentBelop = sjablonverdier.forskuddssats75ProsentBeløp),
            kode = resultatkode,
            regel = regel,
            sjablonListe = byggSjablonliste(sjablonPeriodeListe = grunnlag.sjablonListe, sjablonverdier = sjablonverdier),
        )
    }

    // Mapper ut sjablonverdier til ResultatBeregning (dette for å sikre at kun sjabloner som faktisk er brukt legges ut i grunnlaget for beregning)
    private fun byggSjablonliste(sjablonPeriodeListe: List<SjablonPeriode>, sjablonverdier: Sjablonverdier) = listOf(
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
            ),
            navn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP.navn,
            verdi = sjablonverdier.forskuddssats75ProsentBeløp,
        ),
        SjablonPeriodeNavnVerdi(
            periode = hentPeriode(sjablonPeriodeListe = sjablonPeriodeListe, sjablonNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn),
            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
            verdi = sjablonverdier.forskuddssats100ProsentBeløp,
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
            verdi = sjablonverdier.inntektsgrense100ProsentForskuddBeløp,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
            ),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP.navn,
            verdi = sjablonverdier.inntektsgrenseEnslig75ProsentForskuddBeløp,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
            ),
            navn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP.navn,
            verdi = sjablonverdier.inntektsgrenseGiftSamboer75ProsentForskuddBeløp,
        ),
        SjablonPeriodeNavnVerdi(
            periode =
            hentPeriode(
                sjablonPeriodeListe = sjablonPeriodeListe,
                sjablonNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
            ),
            navn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP.navn,
            verdi = sjablonverdier.inntektsintervallForskuddBeløp,
        ),
    )

    private fun hentPeriode(sjablonPeriodeListe: List<SjablonPeriode>, sjablonNavn: String): Periode {
        val sjablonperiode = sjablonPeriodeListe.find { it.sjablon.navn == sjablonNavn }
        return sjablonperiode?.getPeriode() ?: Periode(LocalDate.MIN, LocalDate.MAX)
    }

    // Beregner forskuddsbeløp basert på resultatkode. Resultatet avrundes til nærmeste tikrone.
    // Forskudd 50%  = Sjablon 0038 * 2/3
    // Forskudd 75%  = Sjablon 0038
    // Forskudd 100% = Sjablon 0038 * 4/3
    // Forskudd 125% = Sjablon 0038 * 5/3
    private fun beregnForskudd(resultatkode: Resultatkode, forskuddssats75ProsentBelop: BigDecimal): BigDecimal {
        val kalkulertResultat = when (resultatkode) {
            Resultatkode.REDUSERT_FORSKUDD_50_PROSENT -> forskuddssats75ProsentBelop.toDouble() * 2 / 3
            Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT -> forskuddssats75ProsentBelop.toDouble()
            Resultatkode.FORHØYET_FORSKUDD_100_PROSENT -> forskuddssats75ProsentBelop.toDouble() * 4 / 3
            Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT -> forskuddssats75ProsentBelop.toDouble() * 5 / 3
            else -> 0.0
        }
        return BigDecimal(10 * (kalkulertResultat / 10.0).roundToInt())
    }

    private fun erUnderInntektsgrense(inntektsgrense: BigDecimal, inntekt: BigDecimal) = inntekt.compareTo(inntektsgrense) < 1

    private fun finnInntektsgrense(
        sivilstandKode: Sivilstandskode,
        inntektsgrenseEnslig75Prosent: BigDecimal,
        inntektsgrenseGift75Prosent: BigDecimal,
    ) = if (sivilstandKode == Sivilstandskode.BOR_ALENE_MED_BARN) inntektsgrenseEnslig75Prosent else inntektsgrenseGift75Prosent

    // Henter sjablonverdier
    private fun hentSjablonverdier(sjablonPeriodeListe: List<SjablonPeriode>): Sjablonverdier {
        val sjablonliste =
            sjablonPeriodeListe
                .map { it.sjablon }
        val forskuddssats75ProsentBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP,
            )
        val forskuddssats100ProsentBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP,
            )
        val maksInntektForskuddMottakerMultiplikator =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR,
            )
        val inntektsgrense100ProsentForskuddBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP,
            )
        val inntektsgrenseEnslig75ProsentForskuddBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
            )
        val inntektsgrenseGiftSamboer75ProsentForskuddBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
            )
        val inntektsintervallForskuddBeløp =
            SjablonUtil.hentSjablonverdi(
                sjablonListe = sjablonliste,
                sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
            )

        return Sjablonverdier(
            forskuddssats75ProsentBeløp = forskuddssats75ProsentBeløp,
            forskuddssats100ProsentBeløp = forskuddssats100ProsentBeløp,
            maksInntektForskuddMottakerMultiplikator = maksInntektForskuddMottakerMultiplikator,
            inntektsgrense100ProsentForskuddBeløp = inntektsgrense100ProsentForskuddBeløp,
            inntektsgrenseEnslig75ProsentForskuddBeløp = inntektsgrenseEnslig75ProsentForskuddBeløp,
            inntektsgrenseGiftSamboer75ProsentForskuddBeløp = inntektsgrenseGiftSamboer75ProsentForskuddBeløp,
            inntektsintervallForskuddBeløp = inntektsintervallForskuddBeløp,
        )
    }
}
