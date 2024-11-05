package no.nav.bidrag.beregn.forskudd.core

import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonVerdiGrunnlag
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.beregn.forskudd.TestUtil.byggSjablonPeriodeListe
import no.nav.bidrag.beregn.forskudd.TestUtil.byggSjablonPeriodeNavnVerdiListe
import no.nav.bidrag.beregn.forskudd.core.bo.Alder
import no.nav.bidrag.beregn.forskudd.core.bo.BarnIHusstanden
import no.nav.bidrag.beregn.forskudd.core.bo.Bostatus
import no.nav.bidrag.beregn.forskudd.core.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Inntekt
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatBeregning
import no.nav.bidrag.beregn.forskudd.core.bo.Sivilstand
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.math.BigDecimal
import java.math.RoundingMode

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ForskuddBeregningTest {
    private var grunnlag: GrunnlagBeregning? = null
    private val forskuddBeregning = ForskuddBeregning()
    private val sjablonPeriodeListe = byggSjablonPeriodeListe()
    private val sjablonPeriodeNavnVerdiListe = byggSjablonPeriodeNavnVerdiListe()
    private val forventetResultatBeløp50Prosent = BigDecimal.valueOf(850)
    private val forventetResultatBeløp75Prosent = BigDecimal.valueOf(1280)
    private val forventetResultatBeløp100Prosent = BigDecimal.valueOf(1710)
    private val forventetResultatBeløp125Prosent = BigDecimal.valueOf(2140)

    @Test
    @Order(1)
    @DisplayName("Regel 1: Søknadsbarn alder er høyere enn eller lik 18 år")
    fun skalGiAvslagBarnOver18År() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.ZERO,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 18)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isZero() },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.AVSLAG_OVER_18_ÅR) },
            { assertThat(resultat.regel).isEqualTo("REGEL 1") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "   * ")
    }

    @Test
    @Order(2)
    @DisplayName("Regel 2: Bostedsstatus er IKKE_MED_FORELDER")
    fun skalGi125ProsentBorIkkeMedForelder() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp = BigDecimal.ZERO,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.IKKE_MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isZero },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.AVSLAG_IKKE_REGISTRERT_PÅ_ADRESSE) },
            { assertThat(resultat.regel).isEqualTo("REGEL 2") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "   **")
    }

    @Test
    @Order(4)
    @DisplayName("Regel 4: BM inntekt er over maksgrense")
    fun skalGiAvslagOverMaksGrense() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(sjablonPeriodeListe = sjablonPeriodeListe, sjablonTallNavn = SjablonTallNavn.FORSKUDDSSATS_BELØP).verdi
                        .multiply(
                            finnSjablonVerdi(
                                sjablonPeriodeListe = sjablonPeriodeListe,
                                sjablonTallNavn = SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR,
                            ).verdi,
                        ).add(BigDecimal.ONE),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isZero() },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.AVSLAG_HØY_INNTEKT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 4") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "*    ")
    }

    @Test
    @Order(5)
    @DisplayName("Regel 5: BM inntekt er lavere eller lik sats for fullt forskudd og søknadsbarn alder er høyere enn eller lik 11 år")
    fun skalGi125ProsentLavInntekt() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP,
                    ).verdi,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp125Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_11_ÅR_125_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 5") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "*  * ")
    }

    @Test
    @Order(6)
    @DisplayName("Regel 6: BM inntekt er lavere eller lik sats for fullt forskudd og søknadsbarn alder er lavere enn 11 år")
    fun skalGi100ProsentLavInntekt() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP,
                    ).verdi,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 10)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp100Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.FORHØYET_FORSKUDD_100_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 6") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "*  * ")
    }

    @Test
    @Order(7)
    @DisplayName("Regel 7: BM inntekt er lavere eller lik sats for 75% forskudd enslig og antall barn i husstand er 1")
    fun skalGi75ProsentEnsligEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
                    ).verdi,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp75Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 7") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(8)
    @DisplayName("Regel 8: BM inntekt er høyere enn sats for 75% forskudd enslig og antall barn i husstand er 1")
    fun skalGi50ProsentEnsligEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
                    ).verdi.add(BigDecimal.ONE),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp50Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 8") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(9)
    @DisplayName("Regel 9: BM inntekt er lavere eller lik sats for 75% forskudd enslig ++ og antall barn i husstand er mer enn 1")
    fun skalGi75ProsentEnsligMerEnnEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
                    ).verdi.add(
                        finnSjablonVerdi(
                            sjablonPeriodeListe = sjablonPeriodeListe,
                            sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                        ).verdi,
                    ),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe =
            listOf(
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
            )
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp75Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 9") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(10)
    @DisplayName("Regel 10: BM inntekt er høyere enn sats for 75% forskudd enslig ++ og antall barn i husstand er mer enn 1")
    fun skalGi50ProsentEnsligMerEnnEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
                    ).verdi.add(
                        finnSjablonVerdi(
                            sjablonPeriodeListe = sjablonPeriodeListe,
                            sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                        ).verdi,
                    ).add(BigDecimal.ONE),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.BOR_ALENE_MED_BARN)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe =
            listOf(
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
            )
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp50Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 10") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(11)
    @DisplayName("Regel 11: BM inntekt er lavere eller lik sats for 75% forskudd gift og antall barn i husstand er 1")
    fun skalGi75ProsentGiftEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                    ).verdi,
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.GIFT_SAMBOER)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp75Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 11") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(12)
    @DisplayName("Regel 12: BM inntekt er høyere enn sats for 75% forskudd gift og antall barn i husstand er 1")
    fun skalGi50ProsentGiftEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                    ).verdi.add(BigDecimal.ONE),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.GIFT_SAMBOER)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe = listOf(BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1))
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp50Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 12") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(13)
    @DisplayName("Regel 13: BM inntekt er lavere eller lik sats for 75% forskudd gift ++ og antall barn i husstand er mer enn 1")
    fun skalGi75ProsentGiftMerEnnEttBarn() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                    ).verdi.add(
                        finnSjablonVerdi(
                            sjablonPeriodeListe = sjablonPeriodeListe,
                            sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                        ).verdi,
                    ),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.GIFT_SAMBOER)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe =
            listOf(
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
            )
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp75Prosent) },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.ORDINÆRT_FORSKUDD_75_PROSENT) },
            { assertThat(resultat.regel).isEqualTo("REGEL 13") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(14)
    @DisplayName("Regel 14: BM inntekt er høyere enn sats for 75% forskudd gift ++ og antall barn i husstand er mer enn 1 (1 inntekt)")
    fun skalGi50ProsentGiftMerEnnEttBarn_EnInntekt() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                    ).verdi.add(
                        finnSjablonVerdi(
                            sjablonPeriodeListe = sjablonPeriodeListe,
                            sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                        ).verdi.add(BigDecimal.ONE),
                    ),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.GIFT_SAMBOER)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe =
            listOf(
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
            )
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp50Prosent) },
            { assertThat(resultat.regel).isEqualTo("REGEL 14") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(15)
    @DisplayName("Regel 14: BM inntekt er høyere enn sats for 75% forskudd gift ++ og antall barn i husstand er mer enn 1 (2 inntekter)")
    fun skalGi50ProsentGiftMerEnnEttBarn_ToInntekter() {
        val inntektListe =
            listOf(
                Inntekt(
                    referanse = INNTEKT_REFERANSE_1,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                    ).verdi,
                ),
                Inntekt(
                    referanse = INNTEKT_REFERANSE_2,
                    type = "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                    beløp =
                    finnSjablonVerdi(
                        sjablonPeriodeListe = sjablonPeriodeListe,
                        sjablonTallNavn = SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                    ).verdi.add(BigDecimal.ONE),
                ),
            )
        val sivilstand = Sivilstand(referanse = SIVILSTAND_REFERANSE, kode = Sivilstandskode.GIFT_SAMBOER)
        // Søknadsbarnet er med i grunnlag antall barn i husstanden
        val barnIHusstandenListe =
            listOf(
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_1, antall = 1),
                BarnIHusstanden(referanse = BARN_I_HUSSTANDEN_REFERANSE_2, antall = 1),
            )
        val alder = Alder(referanse = SØKNADSBARN_REFERANSE, alder = 11)
        val bostatus = Bostatus(referanse = BOSTATUS_REFERANSE, kode = Bostatuskode.MED_FORELDER)
        lagGrunnlag(
            inntekt = inntektListe,
            sivilstand = sivilstand,
            barnIHusstanden = barnIHusstandenListe,
            alder = alder,
            bostatus = bostatus,
        )
        val resultat = forskuddBeregning.beregn(grunnlag!!)

        assertAll(
            { assertThat(resultat).isNotNull() },
            { assertThat(resultat.kode).isEqualTo(Resultatkode.REDUSERT_FORSKUDD_50_PROSENT) },
            { assertThat(resultat.beløp).isEqualByComparingTo(forventetResultatBeløp50Prosent) },
            { assertThat(resultat.regel).isEqualTo("REGEL 14") },
            { assertThat(resultat.sjablonListe).isEqualTo(sjablonPeriodeNavnVerdiListe) },
        )

        printGrunnlagResultat(resultat, "***  ")
    }

    @Test
    @Order(16)
    @DisplayName("Tester at satser for 2024 beregnes riktig basert på verdi av sjablon 0038")
    fun testAvSatser2024() {
        val forventetRedusertForskudd = BigDecimal.valueOf(990)
        val forventetOrdinærtForskudd = BigDecimal.valueOf(1480)
        val forventetForhøyetForskudd = BigDecimal.valueOf(1970)
        val forventetForhøyetForskudd11År = BigDecimal.valueOf(2460)

        val prosent50 = BigDecimal.valueOf(50, 2)
        val prosent75 = BigDecimal.valueOf(75, 2)
        val prosent125 = BigDecimal.valueOf(125, 2)

        val sjablon0038 = BigDecimal.valueOf(1480)

        val ordinærtForskudd = sjablon0038
        val uavrundetForskudd100Prosent = ordinærtForskudd.divide(prosent75, 5, RoundingMode.HALF_UP)
        val forhøyetForskudd = avrund(uavrundetForskudd100Prosent)
        val redusertForskudd = avrund(uavrundetForskudd100Prosent.multiply(prosent50))
        val forhøyetForskudd11År = avrund(forhøyetForskudd.multiply(prosent125))

        assertAll(
            { assertThat(redusertForskudd).isEqualTo(forventetRedusertForskudd) },
            { assertThat(ordinærtForskudd).isEqualTo(forventetOrdinærtForskudd) },
            { assertThat(forhøyetForskudd).isEqualTo(forventetForhøyetForskudd) },
            { assertThat(forhøyetForskudd11År).isEqualTo(forventetForhøyetForskudd11År) },
        )
    }

    private fun avrund(beløp: BigDecimal): BigDecimal = beløp.divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP).multiply(BigDecimal.TEN)

    private fun lagGrunnlag(
        inntekt: List<Inntekt>,
        sivilstand: Sivilstand,
        barnIHusstanden: List<BarnIHusstanden>,
        alder: Alder,
        bostatus: Bostatus,
    ) {
        grunnlag =
            GrunnlagBeregning(
                inntektListe = inntekt,
                sivilstand = sivilstand,
                barnIHusstandenListe = barnIHusstanden,
                søknadsbarnAlder = alder,
                søknadsbarnBostatus = bostatus,
                sjablonListe = sjablonPeriodeListe,
            )
    }

    private fun finnSjablonVerdi(sjablonPeriodeListe: List<SjablonPeriode>, sjablonTallNavn: SjablonTallNavn): SjablonVerdiGrunnlag {
        val sjablonListe =
            sjablonPeriodeListe
                .map { it.sjablon }
        return SjablonUtil.hentSjablonverdi(sjablonListe = sjablonListe, sjablonTallNavn = sjablonTallNavn)
    }

    private fun printGrunnlagResultat(resultat: ResultatBeregning?, betydning: String) {
        println()
        println()
        println("SJABLONVERDIER:")
        println("---------------")
        println(
            "0005 Forskuddssats 100%:                             " +
                finnSjablonVerdi(sjablonPeriodeListe, SjablonTallNavn.FORSKUDDSSATS_BELØP),
        )
        println(
            "0006 Innslag kapitalinntekt:                         " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP,
                ),
        )
        println(
            "0013 Multiplikator:                                  " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR,
                ),
        )
        println(
            "0033 Inntektsgrense 100%:                            " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.ØVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELØP,
                ),
        )
        println(
            "0034 Inntektsgrense 75% enslig:                      " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELØP,
                ),
        )
        println(
            "0035 Inntektsgrense 75% gift:                        " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.ØVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELØP,
                ),
        )
        println(
            "0036 Inntektsintervall:                              " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                ),
        )
        println(
            "0038 Forskuddssats 75%:                              " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.FORSKUDDSSATS_75PROSENT_BELØP,
                ),
        )
        println(
            "0005x0013 Maks inntektsgrense:                       " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.FORSKUDDSSATS_BELØP,
                ).verdi.multiply(
                    finnSjablonVerdi(sjablonPeriodeListe, SjablonTallNavn.MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR).verdi,
                ),
        )
        println()
        println("GRUNNLAG:")
        println("---------")
        println(
            "BM inntekt:                                        " + betydning[0] + " " + grunnlag!!.inntektListe,
        )
        println(
            "BM sivilstand:                                     " + betydning[1] + " " +
                grunnlag!!.sivilstand.kode.name,
        )
        println("Antall barn i husstand:                            " + betydning[2] + " " + grunnlag!!.barnIHusstandenListe.count())
        println("Alder på søknadsbarn:                              " + betydning[3] + " " + grunnlag!!.søknadsbarnAlder)
        println(
            "Bostedsstatus søknadsbarn:                         " + betydning[4] + " " + grunnlag!!.søknadsbarnBostatus.kode.name,
        )
        println()
        println(
            "Inntektsintervall totalt (0036 x (antall barn - 1)): " +
                finnSjablonVerdi(
                    sjablonPeriodeListe,
                    SjablonTallNavn.INNTEKTSINTERVALL_FORSKUDD_BELØP,
                ).verdi.multiply(BigDecimal(grunnlag!!.barnIHusstandenListe.count() - 1)),
        )
        println()
        println("RESULTAT:")
        println("---------")
        println("Beregnet beløp:                                      " + (resultat?.beløp?.toInt() ?: "null"))
        println("Resultatkode:                                        " + (resultat?.kode?.name ?: "null"))
        println("Regel brukt i beregning:                             " + (resultat?.regel ?: "null"))
    }

    companion object {
        private const val INNTEKT_REFERANSE_1 = "INNTEKT_REFERANSE_1"
        private const val INNTEKT_REFERANSE_2 = "INNTEKT_REFERANSE_2"
        private const val SIVILSTAND_REFERANSE = "SIVILSTAND_REFERANSE"
        private const val BARN_I_HUSSTANDEN_REFERANSE_1 = "BARN_I_HUSSTANDEN_REFERANSE_1"
        private const val BARN_I_HUSSTANDEN_REFERANSE_2 = "BARN_I_HUSSTANDEN_REFERANSE_2"
        private const val SØKNADSBARN_REFERANSE = "SØKNADSBARN_REFERANSE"
        private const val BOSTATUS_REFERANSE = "BOSTATUS_REFERANSE"
    }
}
