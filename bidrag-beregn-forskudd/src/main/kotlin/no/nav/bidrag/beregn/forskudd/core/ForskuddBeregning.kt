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
    val prosent50 = BigDecimal.valueOf(50, 2)
    val prosent75 = BigDecimal.valueOf(75, 2)
    val prosent125 = BigDecimal.valueOf(125, 2)

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
                resultatkode = Resultatkode.AVSLAG_OVER_18_ÅR
                regel = "REGEL 1"
            }

            // Søknadsbarn bor alene eller ikke med foreldre (REGEL 2)
            // Setter avslagskode (gjelder hvis det er BM som har søkt).
            // Hvis det er andre som har søkt på vegne av barnet vil det resultere i forhøyet forskudd, men dette
            // håndteres pt. på utsiden av denne modulen
            grunnlag.søknadsbarnBostatus.kode != Bostatuskode.MED_FORELDER &&
                grunnlag.søknadsbarnBostatus.kode != Bostatuskode.DOKUMENTERT_SKOLEGANG -> {
                resultatkode = Resultatkode.AVSLAG_IKKE_REGISTRERT_PÅ_ADRESSE
                regel = "REGEL 2"
            }

            // Over maks inntektsgrense for forskudd (REGEL 4)
            !erUnderInntektsgrense(inntektsgrense = maksInntektsgrense, inntekt = bidragsmottakerInntekt) -> {
                resultatkode = Resultatkode.AVSLAG_HØY_INNTEKT
                regel = "REGEL 4"
            }

            // Under maks inntektsgrense for fullt forskudd (REGEL 5/6)
            erUnderInntektsgrense(inntektsgrense = sjablonverdier.inntektsgrense100ProsentForskuddBeløp, inntekt = bidragsmottakerInntekt) -> {
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
            beløp = beregnForskudd(resultatkode = resultatkode, forskuddssats75ProsentBeløp = sjablonverdier.forskuddssats75ProsentBeløp),
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
        return sjablonperiode?.getPeriode() ?: Periode(datoFom = LocalDate.MIN, datoTil = LocalDate.MAX)
    }

    // Beregner forskuddsbeløp basert på resultatkode. Resultatet avrundes til nærmeste tikrone.
    // Utgangspunktet er sjablon 0038 (75% sats), men med utgangspunkt i den simuleres bruk av
    // sjablon 0005 (100% sats) for å få beregningen lik som i Bisys og i samsvar med det som står på nav.no.
    private fun beregnForskudd(resultatkode: Resultatkode, forskuddssats75ProsentBeløp: BigDecimal): BigDecimal {
        val uavrundetForskuddssats100ProsentBeløp = forskuddssats75ProsentBeløp.divide(prosent75, 5, RoundingMode.HALF_UP)
        val avrundetForskuddssats100ProsentBeløp = avrund(uavrundetForskuddssats100ProsentBeløp)

        return when (resultatkode) {
            Resultatkode.REDUSERT_FORSKUDD_50_PROSENT -> avrund(uavrundetForskuddssats100ProsentBeløp.multiply(prosent50))
            Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT -> forskuddssats75ProsentBeløp
            Resultatkode.FORHØYET_FORSKUDD_100_PROSENT -> avrund(uavrundetForskuddssats100ProsentBeløp)
            Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT -> avrund(avrundetForskuddssats100ProsentBeløp.multiply(prosent125))
            else -> BigDecimal.ZERO
        }
    }

    private fun avrund(beløp: BigDecimal): BigDecimal = beløp.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN)

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
            forskuddssats75ProsentBeløp = forskuddssats75ProsentBeløp.verdi,
            forskuddssats100ProsentBeløp = forskuddssats100ProsentBeløp.verdi,
            maksInntektForskuddMottakerMultiplikator = maksInntektForskuddMottakerMultiplikator.verdi,
            inntektsgrense100ProsentForskuddBeløp = inntektsgrense100ProsentForskuddBeløp.verdi,
            inntektsgrenseEnslig75ProsentForskuddBeløp = inntektsgrenseEnslig75ProsentForskuddBeløp.verdi,
            inntektsgrenseGiftSamboer75ProsentForskuddBeløp = inntektsgrenseGiftSamboer75ProsentForskuddBeløp.verdi,
            inntektsintervallForskuddBeløp = inntektsintervallForskuddBeløp.verdi,
        )
    }
}
