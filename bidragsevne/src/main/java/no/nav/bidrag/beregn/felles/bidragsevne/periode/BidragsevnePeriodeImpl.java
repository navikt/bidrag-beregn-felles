package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import static java.util.stream.Collectors.toCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.Bidragsevneberegning;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.BidragsevnePeriodeGrunnlag;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.BidragsevnePeriodeResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.PeriodeResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;


public class BidragsevnePeriodeImpl implements BidragsevnePeriode {

  private Bidragsevneberegning bidragsevneberegning = Bidragsevneberegning.getInstance();

  public BidragsevnePeriodeResultat beregnPerioder(BidragsevnePeriodeGrunnlag bidragsevnePeriodeGrunnlag) {

    var periodeResultatListe = new ArrayList<PeriodeResultat>();

    var justertSjablonPeriodeListe = bidragsevnePeriodeGrunnlag.getSjablonPeriodeListe().stream()
        .map(sP -> new SjablonPeriode(PeriodeUtil.justerPeriode(sP.getDatoFraTil()), sP.getSjablonnavn(),
            sP.getSjablonVerdi1(), sP.getSjablonVerdi2())).collect(toCollection(ArrayList::new));

    var justertInntektPeriodeListe = bidragsevnePeriodeGrunnlag.getInntektPeriodeListe().stream()
        .map(iP -> new InntektPeriode(PeriodeUtil.justerPeriode(iP.getDatoFraTil()), iP.getSkatteklasse(),
            iP.getInntektBelop())).collect(toCollection(ArrayList::new));

    var justertBostatusPeriodeListe = bidragsevnePeriodeGrunnlag.getBostatusPeriodeListe().stream()
        .map(bP -> new BostatusPeriode(PeriodeUtil.justerPeriode(bP.getDatoFraTil()), bP.getBorAlene()))
            .collect(toCollection(ArrayList::new));

    var justertAntallBarnIEgetHusholdPeriodeListe = bidragsevnePeriodeGrunnlag.getAntallBarnIEgetHusholdPeriodeListe().stream()
        .map(aP -> new AntallBarnIEgetHusholdPeriode(PeriodeUtil.justerPeriode(aP.getDatoFraTil()), aP.getAntallBarn()))
        .collect(toCollection(ArrayList::new));


    // Bygger opp liste over perioder
    List<Periode> perioder = new Periodiserer()
        .addBruddpunkt(bidragsevnePeriodeGrunnlag.getBeregnDatoFra()) //For å sikre bruddpunkt på start-beregning-fra-dato
        .addBruddpunkter(justertSjablonPeriodeListe)
        .addBruddpunkter(justertInntektPeriodeListe)
        .addBruddpunkter(justertBostatusPeriodeListe)
        .addBruddpunkter(justertAntallBarnIEgetHusholdPeriodeListe)
        .finnPerioder(bidragsevnePeriodeGrunnlag.getBeregnDatoFra(), bidragsevnePeriodeGrunnlag.getBeregnDatoTil());

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode

    for (Periode beregningsperiode : perioder) {
      var sjabloner = justertSjablonPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).findAny().orElse(null);

      var inntektBelop = justertInntektPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(InntektPeriode::getInntektBelop).findFirst().orElse(null);

      var skatteklasse = justertInntektPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(InntektPeriode::getSkatteklasse).findFirst().orElse(null);

      var borAlene = justertBostatusPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(BostatusPeriode::getBorAlene).findFirst().orElse(null);

      var antallBarnIEgetHushold = justertAntallBarnIEgetHusholdPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(AntallBarnIEgetHusholdPeriode::getAntallBarn).findFirst().orElse(null);



//      periodeResultatListe.add(new PeriodeResultat(beregningsperiode, bidragsevneberegning
//          .beregn(new BidragsevneberegningGrunnlag(inntektBelop, skatteklasse, borAlene, antallBarnIEgetHushold, sjabloner))));
    }

    //Slår sammen perioder med samme resultat
    return mergePerioder(periodeResultatListe);

  }


  // Slår sammen perioder hvis Beløp, ResultatKode og ResultatBeskrivelse er like i tilgrensende perioder
  private BidragsevnePeriodeResultat mergePerioder(ArrayList<PeriodeResultat> periodeResultatListe) {
    var filtrertPeriodeResultatListe = new ArrayList<PeriodeResultat>();
    var periodeResultatForrige = new PeriodeResultat(new Periode(LocalDate.MIN, LocalDate.MAX),
        new ResultatBeregning(Double.valueOf(0.0)));
    var datoFra = periodeResultatListe.get(0).getDatoFraTil().getDatoFra();
    var mergePerioder = false;
    int count = 0;

    for (PeriodeResultat periodeResultat : periodeResultatListe) {
      count++;

      if (periodeResultat.getResultatBeregning().kanMergesMed(periodeResultatForrige.getResultatBeregning())) {
        mergePerioder = true;
      } else {
        if (mergePerioder) {
          periodeResultatForrige.getDatoFraTil().setDatoFra(datoFra);
          mergePerioder = false;
        }
        if (count > 1) {
          filtrertPeriodeResultatListe.add(periodeResultatForrige);
        }
        datoFra = periodeResultat.getDatoFraTil().getDatoFra();
      }

      periodeResultatForrige = periodeResultat;
    }

    if (count > 0) {
      if (mergePerioder) {
        periodeResultatForrige.getDatoFraTil().setDatoFra(datoFra);
      }
      filtrertPeriodeResultatListe.add(periodeResultatForrige);
    }

    return new BidragsevnePeriodeResultat(filtrertPeriodeResultatListe);
  }
}
