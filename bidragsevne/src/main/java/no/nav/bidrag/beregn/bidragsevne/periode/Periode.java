package no.nav.bidrag.beregn.bidragsevne.periode;

import java.time.LocalDate;

public class Periode implements PeriodisertGrunnlag {

    private LocalDate datoFra;
    private LocalDate datoTil;

    public Periode(LocalDate datoFra, LocalDate datoTil) {
        this.datoFra = datoFra;
        this.datoTil = datoTil;
    }

    public Periode getDatoFraTil() {
        return this;
    }

    public LocalDate getDatoFra() {
        return datoFra;
    }

    public LocalDate getDatoTil() {
        return datoTil;
    }

    public void setDatoFra(LocalDate datoFra) {
        this.datoFra = datoFra;
    }

    // Sjekker at en denne perioden overlapper med annenPeriode (intersect)
    public boolean overlapperMed(Periode annenPeriode) {
        return (annenPeriode.datoTil == null || this.datoFra.isBefore(annenPeriode.datoTil))
                && (this.datoTil == null || this.datoTil.isAfter(annenPeriode.datoFra));
    }
}
