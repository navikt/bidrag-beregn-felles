package no.nav.bidrag.beregn.core.util

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import java.time.LocalDate

object PeriodeUtil {
    // Validerer at datoer er gyldige
    @JvmStatic
    fun validerInputDatoer(
        beregnDatoFom: LocalDate,
        beregnDatoTil: LocalDate,
        dataElement: String,
        periodeListe: List<Periode>,
        sjekkOverlappendePerioder: Boolean,
        sjekkOppholdMellomPerioder: Boolean,
        sjekkDatoTilNull: Boolean,
        sjekkDatoStartSluttAvPerioden: Boolean,
        sjekkBeregnPeriode: Boolean,
    ): List<Avvik> {
        var indeks = 0
        var forrigePeriode: Periode? = null
        val avvikListe = mutableListOf<Avvik>()

        // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
        if (sjekkBeregnPeriode) {
            avvikListe.addAll(
                sjekkBeregnPeriode(
                    beregnDatoFom = beregnDatoFom,
                    beregnDatoTil = beregnDatoTil,
                    dataElement = dataElement,
                    periodeListe = periodeListe,
                ),
            )
        }
        periodeListe.forEach {
            indeks++

            // Sjekk om perioder overlapper
            if (sjekkOverlappendePerioder && it.overlapper(forrigePeriode)) {
                val feilmelding = "Overlappende perioder i $dataElement: datoTil=${forrigePeriode!!.datoTil}, datoFom=${it.datoFom}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.PERIODER_OVERLAPPER))
            }

            // Sjekk om det er opphold mellom perioder
            if (sjekkOppholdMellomPerioder && it.harOpphold(forrigePeriode)) {
                val feilmelding = "Opphold mellom perioder i $dataElement: datoTil=${forrigePeriode!!.datoTil}, datoFom=${it.datoFom}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.PERIODER_HAR_OPPHOLD))
            }

            // Sjekk om datoTil er null (bortsett fra siste element)
            if (sjekkDatoTilNull && indeks != periodeListe.size && it.datoTil == null) {
                val feilmelding = "datoTil kan ikke være null i $dataElement: datoFom=${it.datoFom}, datoTil=${it.datoTil}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.NULL_VERDI_I_DATO))
            }

            // Sjekk om datoFom og datoTil alltid er første dag i en måned
            if (sjekkDatoStartSluttAvPerioden && it.datoFom.dayOfMonth != 1) {
                val feilmelding = "datoFom i $dataElement må være den første dagen i måneden: datoFom=${it.datoFom}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.DAG_ER_IKKE_FØRSTE_DAG_I_MND))
            }
            if (sjekkDatoStartSluttAvPerioden && it.datoTil != null && it.datoTil.dayOfMonth != 1) {
                val feilmelding = "datoTil i $dataElement må være den første dagen i måneden: datoTil=${it.datoTil}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.DAG_ER_IKKE_FØRSTE_DAG_I_MND))
            }

            // Sjekk om datoFom er etter datoTil
            if (!it.datoTilErEtterDatoFom()) {
                val feilmelding = "datoTil må være etter datoFom i $dataElement: datoFom=${it.datoFom}, datoTil=${it.datoTil}"
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL))
            }

            forrigePeriode = Periode(datoFom = it.datoFom, datoTil = it.datoTil)
        }

        return avvikListe
    }

    // Validerer at beregningsperiode fra/til er gyldig
    @JvmStatic
    fun validerBeregnPeriodeInput(
        beregnDatoFom: LocalDate?,
        beregnDatoTil: LocalDate?,
    ): List<Avvik> {
        val avvikListe = mutableListOf<Avvik>()
        if (beregnDatoFom == null) {
            avvikListe.add(Avvik("beregnDatoFom kan ikke være null", Avvikstype.NULL_VERDI_I_DATO))
        }
        if (beregnDatoTil == null) {
            avvikListe.add(Avvik("beregnDatoTil kan ikke være null", Avvikstype.NULL_VERDI_I_DATO))
        }
        if (beregnDatoFom != null && !Periode(beregnDatoFom, beregnDatoTil).datoTilErEtterDatoFom()) {
            avvikListe.add(Avvik("beregnDatoTil må være etter beregnDatoFom", Avvikstype.DATO_FOM_ETTER_DATO_TIL))
        }
        return avvikListe
    }

    // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
    private fun sjekkBeregnPeriode(
        beregnDatoFom: LocalDate,
        beregnDatoTil: LocalDate,
        dataElement: String,
        periodeListe: List<Periode>,
    ): List<Avvik> {
        val avvikListe = mutableListOf<Avvik>()
        if (periodeListe.isEmpty()) {
            return avvikListe
        }

        // Sjekk at første dato i periodelisten ikke er etter start-dato i perioden det skal beregnes for
        val startDatoIPeriodeListe = periodeListe.first().datoFom
        if (startDatoIPeriodeListe.isAfter(beregnDatoFom)) {
            val feilmelding = "Første dato i $dataElement ($startDatoIPeriodeListe) er etter beregnDatoFom ($beregnDatoFom)"
            avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.PERIODE_MANGLER_DATA))
        }

        // Sjekk at siste dato i periodelisten ikke er før slutt-dato i perioden det skal beregnes for
        val sluttDatoPeriodeListe = periodeListe.map { it.datoTil }.sortedWith(nullsLast(naturalOrder())).toList()
        val sluttDatoIPeriodeListe = sluttDatoPeriodeListe[sluttDatoPeriodeListe.size - 1]
        if (sluttDatoIPeriodeListe != null && sluttDatoIPeriodeListe.isBefore(beregnDatoTil)) {
            val feilmelding = "Siste dato i $dataElement ($sluttDatoIPeriodeListe) er før beregnDatoTil ($beregnDatoTil)"
            avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = Avvikstype.PERIODE_MANGLER_DATA))
        }
        return avvikListe
    }
}
