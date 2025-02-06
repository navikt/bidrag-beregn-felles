package no.nav.bidrag.beregn.core.util

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.inntekt.InntektPeriodeGrunnlag
import no.nav.bidrag.beregn.core.inntekt.InntektPeriodeGrunnlagUtenInntektType
import no.nav.bidrag.beregn.core.inntekt.PeriodisertInntekt
import no.nav.bidrag.beregn.core.periode.Periodiserer
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.rolle.Rolle
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO Vurdere om disse metodene trengs, evt flytte dem til bidrag-behandling eller bidrag-inntekt. Pt i bruk i beregn-barnebidrag-core, men
//      fjernet (midlertidig?) fra beregn-forskudd-core og beregn-saertilskudd-core
object InntektUtil {
    private val FOM_DATO_FORDEL_SKATTEKLASSE2 = LocalDate.MIN
    private val TIL_DATO_FORDEL_SKATTEKLASSE2 = LocalDate.parse("2013-01-01")
    private val FOM_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER = LocalDate.parse("2013-01-01")
    private val TIL_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER = LocalDate.MAX

    // Validerer inntekt
    @JvmStatic
    fun validerInntekter(inntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlag>, formaal: Formål, rolle: Rolle): List<Avvik> {
        val avvikListe = mutableListOf<Avvik>()

        // Validerer formaal, rolle og fra- /til-dato for en inntektstype
//        inntektPeriodeGrunnlagListe.forEach {
//            avvikListe.addAll(validerSoknadstypeOgRolle(it.type, formaal, rolle))
//            avvikListe.addAll(validerPeriode(it))
//        }

        // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
//        avvikListe.addAll(validerDatoFomPerInntektsgruppe(inntektPeriodeGrunnlagListe))

        return avvikListe
    }

    // Justerer inntekt
//    @JvmStatic
//    fun justerInntekter(inntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlag>) =
//        justerPerioder(inntektPeriodeGrunnlagListe)

    // Validerer at formaal og rolle er gyldig for en inntektstype
//    private fun validerSoknadstypeOgRolle(inntektType: InntektType, formaal: Formaal, rolle: Rolle): List<Avvik> {
//        val soknadstypeOgRolleErGyldig = when (formaal) {
//            Formaal.BIDRAG -> when (rolle) {
//                Rolle.BIDRAGSPLIKTIG -> inntektType.bidrag && inntektType.bidragspliktig
//                Rolle.BIDRAGSMOTTAKER -> inntektType.bidrag && inntektType.bidragsmottaker
//                Rolle.SOKNADSBARN -> inntektType.bidrag && inntektType.soknadsbarn
//            }
//
//            Formaal.SAERTILSKUDD -> when (rolle) {
//                Rolle.BIDRAGSPLIKTIG -> inntektType.saertilskudd && inntektType.bidragspliktig
//                Rolle.BIDRAGSMOTTAKER -> inntektType.saertilskudd && inntektType.bidragsmottaker
//                Rolle.SOKNADSBARN -> inntektType.saertilskudd && inntektType.soknadsbarn
//            }
//
//            Formaal.FORSKUDD -> when (rolle) {
//                Rolle.BIDRAGSPLIKTIG -> inntektType.forskudd && inntektType.bidragspliktig
//                Rolle.BIDRAGSMOTTAKER -> inntektType.forskudd && inntektType.bidragsmottaker
//                Rolle.SOKNADSBARN -> inntektType.forskudd && inntektType.soknadsbarn
//            }
//        }
//        return if (!soknadstypeOgRolleErGyldig) {
//            listOf(
//                Avvik(
//                    avvikTekst = "inntektType $inntektType er ugyldig for formaal $formaal og rolle $rolle",
//                    avvikType = Avvikstype.UGYLDIG_INNTEKT_TYPE
//                )
//            )
//        } else {
//            emptyList()
//        }
//    }

    // Validerer at inntektstypen er gyldig innenfor den angitte tidsperioden
//    private fun validerPeriode(inntektPeriodeGrunnlag: InntektPeriodeGrunnlag): List<Avvik> {
//        val inntektDatoFom = inntektPeriodeGrunnlag.getPeriode().datoFom
//        val inntektType = inntektPeriodeGrunnlag.type
//
//        // Åpen eller uendelig slutt-dato skal ikke ryke ut på dato-test (?). Setter datoTil lik siste dato i året til datoFom
//        val inntektDatoTil =
//            if ((inntektPeriodeGrunnlag.getPeriode().datoTil == null) || (inntektPeriodeGrunnlag.getPeriode().datoTil == LocalDate.MAX) ||
//                (inntektPeriodeGrunnlag.getPeriode().datoTil == LocalDate.parse("9999-12-31"))
//            ) {
//                inntektDatoFom.withMonth(12).withDayOfMonth(31)
//            } else {
//                inntektPeriodeGrunnlag.getPeriode().datoTil
//            }
//
//        return if ((inntektDatoFom < inntektType.gyldigFom) || (inntektDatoTil!! > inntektType.gyldigTil)) {
//            listOf(
//                Avvik(
//                    avvikTekst = (
//                        "inntektType " + inntektType + " er kun gyldig fom. " + inntektType.gyldigFom.toString() +
//                            " tom. " + inntektType.gyldigTil.toString()
//                        ),
//                    avvikType = Avvikstype.UGYLDIG_INNTEKT_PERIODE
//                )
//            )
//        } else {
//            emptyList()
//        }
//    }

    // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
//    private fun validerDatoFomPerInntektsgruppe(inntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlag>): List<Avvik> {
//        val avvikListe = mutableListOf<Avvik>()
//        val kriterie = comparing { inntektPeriodeGrunnlag: InntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.type.gruppe }
//            .thenComparing { inntektPeriodeGrunnlag: InntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().datoFom }
//        val inntektGrunnlagListeSortert = inntektPeriodeGrunnlagListe.stream()
//            .sorted(kriterie)
//            .toList()
//        var inntektGrunnlagForrige = InntektPeriodeGrunnlag(
//            referanse = "",
//            inntektPeriode = Periode(LocalDate.MIN, LocalDate.MAX),
//            type = InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG,
//            belop = BigDecimal.ZERO,
//            deltFordel = false,
//            skatteklasse2 = false
//        )
//        inntektGrunnlagListeSortert.forEach {
//            val inntektGruppe = it.type.gruppe
//            val inntektGruppeForrige = inntektGrunnlagForrige.type.gruppe
//            val datoFom = it.getPeriode().datoFom
//            val datoFomForrige = inntektGrunnlagForrige.getPeriode().datoFom
//            if (inntektGruppe.isNotBlank() && inntektGruppe == inntektGruppeForrige && datoFom == datoFomForrige) {
//                avvikListe.add(
//                    Avvik(
//                        avvikTekst = "inntektType " + it.type + " og inntektType " + inntektGrunnlagForrige.type +
//                            " tilhører samme inntektsgruppe og har samme datoFom (" + datoFom + ")",
//                        avvikType = Avvikstype.OVERLAPPENDE_INNTEKT
//                    )
//                )
//            }
//            inntektGrunnlagForrige = it
//        }
//        return avvikListe
//    }

    // Justerer perioder for å unngå overlapp innefor samme inntektsgruppe.
    // Sorterer inntektGrunnlagListe på gruppe og datoFom.
    // datoTil (forrige forekomst) settes lik datoFom - 1 dag (denne forekomst) hvis de tilhører samme gruppe
//    private fun justerPerioder(inntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlag>): List<InntektPeriodeGrunnlag> {
//        val kriterie = comparing { inntektPeriodeGrunnlag: InntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.type.gruppe }
//            .thenComparing { inntektPeriodeGrunnlag: InntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().datoFom }
//        val inntektGrunnlagListeSortert = inntektPeriodeGrunnlagListe.stream()
//            .sorted(kriterie)
//            .toList()
//        val inntektGrunnlagListeJustert = mutableListOf<InntektPeriodeGrunnlag>()
//        var inntektPeriodeGrunnlagForrige: InntektPeriodeGrunnlag? = null
//        var hoppOverInntekt = true
//        var inntektGruppe: String
//        var inntektGruppeForrige: String
//        var datoFom: LocalDate
//        var datoFomForrige: LocalDate
//        var nyDatoTilForrige: LocalDate?
//
//        inntektGrunnlagListeSortert.forEach {
//            if (hoppOverInntekt) {
//                hoppOverInntekt = false
//                inntektPeriodeGrunnlagForrige = it
//                return@forEach
//            }
//            inntektGruppe = it.type.gruppe
//            inntektGruppeForrige = inntektPeriodeGrunnlagForrige!!.type.gruppe
//            datoFom = it.getPeriode().datoFom
//            datoFomForrige = inntektPeriodeGrunnlagForrige!!.getPeriode().datoFom
//            if (inntektGruppe.isNotBlank() && (inntektGruppe == inntektGruppeForrige) && (datoFom.isAfter(datoFomForrige))) {
//                nyDatoTilForrige = datoFom.minusDays(1)
//                inntektGrunnlagListeJustert
//                    .add(
//                        InntektPeriodeGrunnlag(
//                            inntektPeriodeGrunnlagForrige!!.referanse,
//                            Periode(datoFomForrige, nyDatoTilForrige),
//                            inntektPeriodeGrunnlagForrige!!.type,
//                            inntektPeriodeGrunnlagForrige!!.belop,
//                            inntektPeriodeGrunnlagForrige!!.deltFordel,
//                            inntektPeriodeGrunnlagForrige!!.skatteklasse2
//                        )
//                    )
//            } else {
//                inntektGrunnlagListeJustert.add(inntektPeriodeGrunnlagForrige!!)
//            }
//            inntektPeriodeGrunnlagForrige = it
//        }
//
//        // Legg til siste forekomst (skal aldri justeres)
//        inntektGrunnlagListeJustert.add(inntektPeriodeGrunnlagForrige!!)
//
//        return inntektGrunnlagListeJustert
//    }

    // Regelverk for utvidet barnetrygd: Sjekker om det skal legges til inntekt for fordel særfradrag enslig forsørger og skatteklasse 2
    @JvmStatic
    fun behandlUtvidetBarnetrygd(
        inntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlagUtenInntektType>,
        sjablonPeriodeListe: List<SjablonPeriode>,
    ): List<InntektPeriodeGrunnlagUtenInntektType> {
        // Justerer datoer
        val justertInntektPeriodeGrunnlagListeAlleInntekter =
            inntektPeriodeGrunnlagListe
                .map { InntektPeriodeGrunnlagUtenInntektType(it) }
                .sortedBy { it.getPeriode().datoFom }
                .toList()

        // Danner liste over alle inntekter av type UTVIDET_BARNETRYGD
        val justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd =
            justertInntektPeriodeGrunnlagListeAlleInntekter
                .filter { it.type == Inntektstype.UTVIDET_BARNETRYGD.name }
                .toList()

        // Hvis det ikke finnes inntekter av type UTVIDET_BARNETRYGD, returnerer den samme listen som ble sendt inn
        if (justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd.isEmpty()) {
            return inntektPeriodeGrunnlagListe
        }

        // Finner laveste og høyeste dato i listen over inntekter av type UTVIDET_BARNETRYGD
        val minDato =
            justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd.minOfOrNull { it.getPeriode().datoFom } ?: LocalDate.parse("1900-01-01")
        val maxDato =
            justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd
                .mapNotNull { it.getPeriode().datoTil }
                .maxOrNull() ?: LocalDate.parse("2100-01-01")

        // Lager filter over de sjablonene som skal brukes (0004, 0030, 0031, 0039)
        val sjablonFilter =
            listOf(
                SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP,
                SjablonTallNavn.ØVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELØP,
                SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELØP,
                SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP,
            )

        // Filtrerer sjabloner og justerer datoer
        val justertSjablonListe =
            sjablonPeriodeListe
                .filter {
                    sjablonFilter.any { sjablonTallNavn ->
                        sjablonTallNavn.navn == it.sjablon.navn
                    }
                }
                .map { SjablonPeriode(it) }
                .sortedBy { it.sjablonPeriode.datoFom }
                .toList()

        // Danner bruddperioder for inntekter og sjabloner. Legger til ekstra bruddperioder for gyldigheten til inntektstypene som skal beregnes
        val bruddPeriodeListe =
            Periodiserer()
                .addBruddpunkter(justertInntektPeriodeGrunnlagListeAlleInntekter)
                .addBruddpunkter(justertSjablonListe)
                .addBruddpunkter(Periode(datoFom = FOM_DATO_FORDEL_SKATTEKLASSE2, datoTil = TIL_DATO_FORDEL_SKATTEKLASSE2))
                .addBruddpunkter(
                    Periode(
                        datoFom = FOM_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER,
                        datoTil = TIL_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER,
                    ),
                )
                .finnPerioder(beregnDatoFom = minDato, beregnDatoTil = maxDato!!)

        val periodisertInntektListe = mutableListOf<PeriodisertInntekt>()

        // Løper gjennom bruddperiodene og lager en liste over inntekter, sjablonverdier og andre parametre. Perioder uten inntektstype
        // UTVIDET_BARNETRYGD filtreres bort
        bruddPeriodeListe
            .forEach {
                if (periodeHarUtvidetBarnetrygd(it.getPeriode(), justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd)) {
                    periodisertInntektListe.add(
                        PeriodisertInntekt(
                            periode = it.getPeriode(),
                            summertBeløp =
                            summerInntektPeriode(
                                periode = it.getPeriode(),
                                justertInntektPeriodeGrunnlagListe = justertInntektPeriodeGrunnlagListeAlleInntekter,
                            ),
                            fordelSærfradragBeløp = BigDecimal.ZERO,
                            sjablon0004FordelSkatteklasse2Beløp =
                            finnSjablonverdi(
                                periode = it.getPeriode(),
                                justertsjablonListe = justertSjablonListe,
                                sjablonTallNavn = SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP,
                            ),
                            sjablon0030ØvreInntektsgrenseBeløp =
                            finnSjablonverdi(
                                periode = it.getPeriode(),
                                justertsjablonListe = justertSjablonListe,
                                sjablonTallNavn = SjablonTallNavn.ØVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELØP,
                            ),
                            sjablon0031NedreInntektsgrenseBeløp =
                            finnSjablonverdi(
                                periode = it.getPeriode(),
                                justertsjablonListe = justertSjablonListe,
                                sjablonTallNavn = SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELØP,
                            ),
                            sjablon0039FordelSærfradragBeløp =
                            finnSjablonverdi(
                                periode = it.getPeriode(),
                                justertsjablonListe = justertSjablonListe,
                                sjablonTallNavn = SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP,
                            ),
                            deltFordel =
                            finnDeltFordel(
                                periode = it.getPeriode(),
                                justertInntektPeriodeGrunnlagListe = justertInntektPeriodeGrunnlagListeAlleInntekter,
                            ),
                            skatteklasse2 =
                            finnSkatteklasse2(
                                periode = it.getPeriode(),
                                justertInntektPeriodeGrunnlagListe = justertInntektPeriodeGrunnlagListeAlleInntekter,
                            ),
                        ),
                    )
                }
            }

        // Løper gjennom periodisertInntektListe og beregner fordel særfradrag / fordel skatteklasse 2
        periodisertInntektListe.forEach { it.fordelSærfradragBeløp = beregnFordelSærfradrag(it) }

        // Slår sammen perioder med like beløp og rydder vekk perioder med 0 i beløp. Danner ny InntektPeriodeGrunnlag-liste
        val inntektPeriodeGrunnlagListeSærfradragEnsligForsørger = dannInntektListeSærfradragEnsligForsørger(periodisertInntektListe)

        // Returnerer en sammenslått liste med grunnlagsinntekter og beregnede inntekter
        return (inntektPeriodeGrunnlagListe + inntektPeriodeGrunnlagListeSærfradragEnsligForsørger)
    }

    // Sjekker om en gitt periode har utvidet barnetrygd
    private fun periodeHarUtvidetBarnetrygd(
        periode: Periode,
        justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd: List<InntektPeriodeGrunnlagUtenInntektType>,
    ) = justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd.any {
        it.getPeriode().overlapperMed(periode)
    }

    // Summerer inntektene i en gitt periode (eksklusiv inntekttype utvidet barnetrygd)
    private fun summerInntektPeriode(periode: Periode, justertInntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlagUtenInntektType>) =
        justertInntektPeriodeGrunnlagListe
            .filter { it.getPeriode().overlapperMed(periode) && it.type != Inntektstype.UTVIDET_BARNETRYGD.name }
            .map(InntektPeriodeGrunnlagUtenInntektType::belop)
            .fold(BigDecimal.ZERO) { acc, beløp -> acc + beløp }

    // Finner verdien til en gitt sjablon i en gitt periode
    private fun finnSjablonverdi(periode: Periode, justertsjablonListe: List<SjablonPeriode>, sjablonTallNavn: SjablonTallNavn) = justertsjablonListe
        .filter { it.getPeriode().overlapperMed(periode) && it.sjablon.navn == sjablonTallNavn.navn }
        .map { SjablonUtil.hentSjablonverdi(listOf(it.sjablon), sjablonTallNavn) }
        .firstOrNull() ?: BigDecimal.ZERO

    // Finner verdien til flagget 'Delt fordel' i en gitt periode
    private fun finnDeltFordel(periode: Periode, justertInntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlagUtenInntektType>) =
        justertInntektPeriodeGrunnlagListe.firstOrNull {
            it.getPeriode().overlapperMed(periode) && it.type == Inntektstype.UTVIDET_BARNETRYGD.name
        }?.deltFordel ?: false

    // Finner verdien til flagget 'Skatteklasse 2' i en gitt periode
    private fun finnSkatteklasse2(periode: Periode, justertInntektPeriodeGrunnlagListe: List<InntektPeriodeGrunnlagUtenInntektType>) =
        justertInntektPeriodeGrunnlagListe.firstOrNull {
            it.getPeriode().overlapperMed(periode) && it.type == Inntektstype.UTVIDET_BARNETRYGD.name
        }?.skatteklasse2 ?: false

    // Beregner fordel særfradrag
    private fun beregnFordelSærfradrag(periodisertInntekt: PeriodisertInntekt): BigDecimal {
        if (periodisertInntekt.summertBeløp < periodisertInntekt.sjablon0030ØvreInntektsgrenseBeløp) {
            return BigDecimal.ZERO
        }

        // Fordel skatteklasse 2 (før 2013-01-01)
        if (periodisertInntekt.periode
                .overlapperMed(
                    Periode(
                        datoFom = FOM_DATO_FORDEL_SKATTEKLASSE2,
                        datoTil = TIL_DATO_FORDEL_SKATTEKLASSE2,
                    ),
                ) &&
            periodisertInntekt.skatteklasse2
        ) {
            return if (periodisertInntekt.summertBeløp < periodisertInntekt.sjablon0031NedreInntektsgrenseBeløp) {
                periodisertInntekt.sjablon0004FordelSkatteklasse2Beløp.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
            } else if (periodisertInntekt.deltFordel) {
                periodisertInntekt.sjablon0004FordelSkatteklasse2Beløp.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
            } else {
                periodisertInntekt.sjablon0004FordelSkatteklasse2Beløp
            }
        }

        // Fordel særfradrag (etter 2013-01-01)
        if (periodisertInntekt.periode
                .overlapperMed(
                    Periode(
                        datoFom = FOM_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER,
                        datoTil = TIL_DATO_FORDEL_SAERFRADRAG_ENSLIG_FORSORGER,
                    ),
                )
        ) {
            return if (periodisertInntekt.summertBeløp < periodisertInntekt.sjablon0031NedreInntektsgrenseBeløp) {
                periodisertInntekt.sjablon0039FordelSærfradragBeløp.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
            } else if (periodisertInntekt.deltFordel) {
                periodisertInntekt.sjablon0039FordelSærfradragBeløp.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
            } else {
                periodisertInntekt.sjablon0039FordelSærfradragBeløp
            }
        }

        return BigDecimal.ZERO
    }

    // Slår sammen perioder med like beløp og rydder vekk perioder med 0 i beløp. Danner ny InntektPeriodeGrunnlag-liste
    private fun dannInntektListeSærfradragEnsligForsørger(
        periodisertInntektListe: List<PeriodisertInntekt>,
    ): List<InntektPeriodeGrunnlagUtenInntektType> {
        if (periodisertInntektListe.isEmpty()) {
            return emptyList()
        }
        val inntektListeSærfradragEnsligForsørger = mutableListOf<InntektPeriodeGrunnlagUtenInntektType>()
        var forrigeDatoFom = periodisertInntektListe[0].periode.datoFom
        var forrigeDatoTil = periodisertInntektListe[0].periode.datoTil
        var forrigeBeløp = periodisertInntektListe[0].fordelSærfradragBeløp
        var inntektType: Inntektstype
        periodisertInntektListe.forEach {
            if (forrigeBeløp.compareTo(it.fordelSærfradragBeløp) != 0) {
                if (forrigeBeløp.compareTo(BigDecimal.ZERO) != 0) {
// TODO Må enten ha FORDEL_SKATTEKLASSE2 og FORDEL_SAERFRADRAG_ENSLIG_FORSORGER som egne inntektstyper eller forutsette at denne logikken flyttes til bidrag-behandling
// TODO InntetkType.AAP er ikke gyldig her. Har bare satt den for at koden skal kompilere
                    inntektType = Inntektstype.AAP
//                        if (forrigeDatoFom.isBefore(TIL_DATO_FORDEL_SKATTEKLASSE2)) InntektType.FORDEL_SKATTEKLASSE2 else InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER
                    inntektListeSærfradragEnsligForsørger.add(
                        InntektPeriodeGrunnlagUtenInntektType(
                            referanse = lagReferanse(inntektType = inntektType, datoFom = forrigeDatoFom),
                            inntektPeriode = Periode(datoFom = forrigeDatoFom, datoTil = forrigeDatoTil),
                            type = inntektType.name,
                            belop = forrigeBeløp,
                            deltFordel = false,
                            skatteklasse2 = false,
                        ),
                    )
                }
                forrigeDatoFom = it.periode.datoFom
                forrigeBeløp = it.fordelSærfradragBeløp
            }
            forrigeDatoTil = it.periode.datoTil
        }
        if (forrigeBeløp.compareTo(BigDecimal.ZERO) != 0) {
// TODO Må enten ha FORDEL_SKATTEKLASSE2 og FORDEL_SAERFRADRAG_ENSLIG_FORSORGER som egne inntektstyper eller forutsette at denne logikken flyttes til bidrag-behandling
// TODO InntetkType.AAP er ikke gyldig her. Har bare satt den for at koden skal kompilere
            inntektType = Inntektstype.AAP
//                if (forrigeDatoFom.isBefore(TIL_DATO_FORDEL_SKATTEKLASSE2)) InntektType.FORDEL_SKATTEKLASSE2 else InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER
            inntektListeSærfradragEnsligForsørger.add(
                InntektPeriodeGrunnlagUtenInntektType(
                    referanse = lagReferanse(inntektType = inntektType, datoFom = forrigeDatoFom),
                    inntektPeriode = Periode(datoFom = forrigeDatoFom, datoTil = forrigeDatoTil),
                    type = inntektType.name,
                    belop = forrigeBeløp,
                    deltFordel = false,
                    skatteklasse2 = false,
                ),
            )
        }

        return inntektListeSærfradragEnsligForsørger
    }

    private fun lagReferanse(inntektType: Inntektstype, datoFom: LocalDate) =
        "Beregnet_Inntekt_" + inntektType.name + "_" + datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    // Verdi fra sjablon 0006 skal trekkes fra kapitalinntekt. Hvis nettobeløpet blir mindre enn 0, settes det til 0
    fun justerKapitalinntekt(beløp: BigDecimal, innslagKapitalinntektSjablonverdi: BigDecimal): BigDecimal {
        val kapitalinntekt = beløp - innslagKapitalinntektSjablonverdi
        return if (kapitalinntekt < BigDecimal.ZERO) BigDecimal.ZERO else kapitalinntekt
    }

    // Barntillegg tiltakspenger skal ikke være med i inntektsgrunnlaget som skal summeres
    fun justerForBarnetilleggTiltakspenger(inntektsrapporteringPeriode: InntektsrapporteringPeriode) = inntektsrapporteringPeriode.beløp -
        (
            inntektsrapporteringPeriode.inntektspostListe
                .filter { it.inntektstype == Inntektstype.BARNETILLEGG_TILTAKSPENGER }
                .sumOf { it.beløp }
            )

    // Sjekker om inntekten er kapitalinntekt
    fun erKapitalinntekt(inntektsrapportering: Inntektsrapportering) =
        inntektsrapportering in listOf(Inntektsrapportering.KAPITALINNTEKT, Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER)

    // Sjekker om inntekten inneholder barnetillegg tiltakspenger
    fun inneholderBarnetilleggTiltakspenger(inntektsrapporteringPeriode: InntektsrapporteringPeriode) =
        (inntektsrapporteringPeriode.inntektsrapportering == Inntektsrapportering.BARNETILLEGG) &&
            (inntektsrapporteringPeriode.inntektspostListe.any { it.inntektstype == Inntektstype.BARNETILLEGG_TILTAKSPENGER })
}
