package no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag;

import java.time.LocalDate;
import java.util.List;

public class BidragsevnePeriodeGrunnlag {
    public BidragsevnePeriodeGrunnlag() {
    }
    private LocalDate beregnDatoFra;
    private LocalDate beregnDatoTil;
    private List<SjablonPeriode> sjablonPeriodeListe;
    private List<InntektPeriode> inntektPeriodeListe;
    private List<BostatusPeriode> bostatusPeriodeListe;
    private List<AntallBarnIEgetHusholdPeriode> antallBarnIEgetHusholdPeriodeListe;

    public LocalDate getBeregnDatoFra() {
        return beregnDatoFra;
    }

    public void setBeregnDatoFra(LocalDate beregnDatoFra) {
        this.beregnDatoFra = beregnDatoFra;
    }

    public LocalDate getBeregnDatoTil() {
        return beregnDatoTil;
    }

    public void setBeregnDatoTil(LocalDate beregnDatoTil) {
        this.beregnDatoTil = beregnDatoTil;
    }

    public List<SjablonPeriode> getSjablonPeriodeListe() {
        return sjablonPeriodeListe;
    }

    public void setSjablonPeriodeListe(
        List<SjablonPeriode> sjablonPeriodeListe) {
        this.sjablonPeriodeListe = sjablonPeriodeListe;
    }

    public List<InntektPeriode> getInntektPeriodeListe() {
        return inntektPeriodeListe;
    }

    public void setInntektPeriodeListe(
        List<InntektPeriode> inntektPeriodeListe) {
        this.inntektPeriodeListe = inntektPeriodeListe;
    }

    public List<BostatusPeriode> getBostatusPeriodeListe() {
        return bostatusPeriodeListe;
    }

    public void setBostatusPeriodeListe(
        List<BostatusPeriode> bostatusPeriodeListe) {
        this.bostatusPeriodeListe = bostatusPeriodeListe;
    }

    public List<AntallBarnIEgetHusholdPeriode> getAntallBarnIEgetHusholdPeriodeListe() {
        return antallBarnIEgetHusholdPeriodeListe;
    }

    public void setAntallBarnIEgetHusholdPeriodeListe(
        List<AntallBarnIEgetHusholdPeriode> antallBarnIEgetHusholdPeriodeListe) {
        this.antallBarnIEgetHusholdPeriodeListe = antallBarnIEgetHusholdPeriodeListe;
    }
}
