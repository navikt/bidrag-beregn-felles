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
    private List<BostatusPeriode> bostatusPeriode;
    private List<AntallBarnIEgetHusholdPeriode> antallBarnIEgetHusholdPeriode;

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

    public List<BostatusPeriode> getBostatusPeriode() {
        return bostatusPeriode;
    }

    public void setBostatusPeriode(
        List<BostatusPeriode> bostatusPeriode) {
        this.bostatusPeriode = bostatusPeriode;
    }

    public List<AntallBarnIEgetHusholdPeriode> getAntallBarnIEgetHusholdPeriode() {
        return antallBarnIEgetHusholdPeriode;
    }

    public void setAntallBarnIEgetHusholdPeriode(
        List<AntallBarnIEgetHusholdPeriode> antallBarnIEgetHusholdPeriode) {
        this.antallBarnIEgetHusholdPeriode = antallBarnIEgetHusholdPeriode;
    }
}
