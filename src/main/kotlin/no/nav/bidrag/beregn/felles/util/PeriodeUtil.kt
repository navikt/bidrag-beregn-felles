package no.nav.bidrag.beregn.felles.util

import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.domain.enums.AvvikType
import java.time.LocalDate

object PeriodeUtil {

    // Validerer at datoer er gyldige
    @JvmStatic
    fun validerInputDatoer(
        beregnDatoFom: LocalDate,
        beregnDatoTil: LocalDate,
        dataElement: String,
        periodeListe: List<Periode>,
        sjekkOverlapp: Boolean,
        sjekkOpphold: Boolean,
        sjekkNull: Boolean,
        sjekkBeregnPeriode: Boolean
    ): List<Avvik> {
        var indeks = 0
        var forrigePeriode: Periode? = null
        val avvikListe = mutableListOf<Avvik>()

        // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
        if (sjekkBeregnPeriode) {
            avvikListe.addAll(
                sjekkBeregnPeriode(
                    beregnDatoFra = beregnDatoFom,
                    beregnDatoTil = beregnDatoTil,
                    dataElement = dataElement,
                    periodeListe = periodeListe
                )
            )
        }
        periodeListe.forEach {
            indeks++

            // Sjekk om perioder overlapper
            if (sjekkOverlapp && it.overlapper(forrigePeriode)) {
                val feilmelding = "Overlappende perioder i " + dataElement + ": datoTil=" + forrigePeriode!!.datoTil + ", datoFom=" +
                    it.datoFom
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.PERIODER_OVERLAPPER))
            }

            // Sjekk om det er opphold mellom perioder
            if (sjekkOpphold && it.harOpphold(forrigePeriode)) {
                val feilmelding = "Opphold mellom perioder i " + dataElement + ": datoTil=" + forrigePeriode!!.datoTil + ", datoFom=" +
                    it.datoFom
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.PERIODER_HAR_OPPHOLD))
            }

            // Sjekk om dato er null
            if (sjekkNull && indeks != periodeListe.size && it.datoTil == null) {
                val feilmelding = "datoTil kan ikke være null i " + dataElement + ": datoFom=" + it.datoFom +
                    ", datoTil=" + it.datoTil
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.NULL_VERDI_I_DATO))
            }

            // Sjekk om dato fra er etter dato til
            if (!it.datoTilErEtterDatoFom()) {
                val feilmelding = "datoTil må være etter datoFom i " + dataElement + ": datoFom=" + it.datoFom +
                    ", datoTil=" + it.datoTil
                avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.DATO_FOM_ETTER_DATO_TIL))
            }

            forrigePeriode = Periode(datoFom = it.datoFom, datoTil = it.datoTil)
        }

        return avvikListe
    }

    // Validerer at beregningsperiode fra/til er gyldig
    @JvmStatic
    fun validerBeregnPeriodeInput(beregnDatoFra: LocalDate?, beregnDatoTil: LocalDate?): List<Avvik> {
        val avvikListe = mutableListOf<Avvik>()
        if (beregnDatoFra == null) {
            avvikListe.add(Avvik("beregnDatoFra kan ikke være null", AvvikType.NULL_VERDI_I_DATO))
        }
        if (beregnDatoTil == null) {
            avvikListe.add(Avvik("beregnDatoTil kan ikke være null", AvvikType.NULL_VERDI_I_DATO))
        }
        if (!Periode(beregnDatoFra!!, beregnDatoTil).datoTilErEtterDatoFom()) {
            avvikListe.add(Avvik("beregnDatoTil må være etter beregnDatoFra", AvvikType.DATO_FOM_ETTER_DATO_TIL))
        }
        return avvikListe
    }

    // Validerer at dataene i periodelisten dekker hele perioden det skal beregnes for
    private fun sjekkBeregnPeriode(
        beregnDatoFra: LocalDate,
        beregnDatoTil: LocalDate,
        dataElement: String,
        periodeListe: List<Periode>
    ): List<Avvik> {
        val avvikListe = mutableListOf<Avvik>()
        if (periodeListe.isEmpty()) {
            return avvikListe
        }

        // Sjekk at første dato i periodelisten ikke er etter start-dato i perioden det skal beregnes for
        val startDatoIPeriodeListe = periodeListe.first().datoFom
        if (startDatoIPeriodeListe.isAfter(beregnDatoFra)) {
            val feilmelding = "Første dato i $dataElement ($startDatoIPeriodeListe) er etter beregnDatoFra ($beregnDatoFra)"
            avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.PERIODE_MANGLER_DATA))
        }

        // Sjekk at siste dato i periodelisten ikke er før slutt-dato i perioden det skal beregnes for
        val sluttDatoPeriodeListe = periodeListe.map { it.datoTil }.sortedWith(nullsLast(naturalOrder())).toList()
        val sluttDatoIPeriodeListe = sluttDatoPeriodeListe[sluttDatoPeriodeListe.size - 1]
        if (sluttDatoIPeriodeListe != null && sluttDatoIPeriodeListe.isBefore(beregnDatoTil)) {
            val feilmelding = "Siste dato i $dataElement ($sluttDatoIPeriodeListe) er før beregnDatoTil ($beregnDatoTil)"
            avvikListe.add(Avvik(avvikTekst = feilmelding, avvikType = AvvikType.PERIODE_MANGLER_DATA))
        }
        return avvikListe
    }
}
