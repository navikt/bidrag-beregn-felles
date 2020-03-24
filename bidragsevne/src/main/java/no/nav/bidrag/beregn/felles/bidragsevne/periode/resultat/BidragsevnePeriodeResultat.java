package no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat;

import java.util.List;

public class BidragsevnePeriodeResultat {
    private final List<PeriodeResultat> periodeResultatListe;

    public BidragsevnePeriodeResultat(
        List<PeriodeResultat> periodeResultatListe) {
        this.periodeResultatListe = periodeResultatListe;
    }

    public List<PeriodeResultat> getPeriodeResultatListe() {
        return periodeResultatListe;
    }
}
