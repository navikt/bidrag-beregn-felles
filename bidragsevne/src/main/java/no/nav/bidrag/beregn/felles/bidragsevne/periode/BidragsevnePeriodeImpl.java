package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import static java.util.stream.Collectors.toCollection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.Bidragsevneberegning;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.BidragsevnePeriodeResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.PeriodeResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;


public class BidragsevnePeriodeImpl implements BidragsevnePeriode {

  private Bidragsevneberegning bidragsevneberegning = Bidragsevneberegning.getInstance();

  public BeregnBidragsevneResultat beregnPerioder(
      BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt) {

    var resultatPeriodeListe = new ArrayList<ResultatPeriode>();

    var justertSjablonPeriodeListe = beregnBidragsevneGrunnlagAlt.getSjablonPeriodeListe().stream()
        .map(sP -> new SjablonPeriode(PeriodeUtil.justerPeriode(sP.getDatoFraTil()), sP.getSjablonnavn(),
            sP.getSjablonVerdi1(), sP.getSjablonVerdi2())).collect(toCollection(ArrayList::new));

    var justertInntektPeriodeListe = beregnBidragsevneGrunnlagAlt.getInntektPeriodeListe().stream()
        .map(iP -> new InntektPeriode(PeriodeUtil.justerPeriode(iP.getDatoFraTil()), iP.getSkatteklasse(),
            iP.getInntektBelop())).collect(toCollection(ArrayList::new));

    var justertBostatusPeriodeListe = beregnBidragsevneGrunnlagAlt.getBostatusPeriodeListe().stream()
        .map(bP -> new BostatusPeriode(PeriodeUtil.justerPeriode(bP.getDatoFraTil()), bP.getBorAlene()))
            .collect(toCollection(ArrayList::new));

    var justertAntallBarnIEgetHusholdPeriodeListe = beregnBidragsevneGrunnlagAlt.getAntallBarnIEgetHusholdPeriodeListe().stream()
        .map(aP -> new AntallBarnIEgetHusholdPeriode(PeriodeUtil.justerPeriode(aP.getDatoFraTil()), aP.getAntallBarn()))
        .collect(toCollection(ArrayList::new));


    // Bygger opp liste over perioder
    List<Periode> perioder = new Periodiserer()
        .addBruddpunkt(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra()) //For å sikre bruddpunkt på start-beregning-fra-dato
        .addBruddpunkter(justertSjablonPeriodeListe)
        .addBruddpunkter(justertInntektPeriodeListe)
        .addBruddpunkter(justertBostatusPeriodeListe)
        .addBruddpunkter(justertAntallBarnIEgetHusholdPeriodeListe)
        .finnPerioder(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(), beregnBidragsevneGrunnlagAlt.getBeregnDatoTil());

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
    return mergePerioder(resultatPeriodeListe);

  }


  // Slår sammen perioder hvis Beløp, ResultatKode og ResultatBeskrivelse er like i tilgrensende perioder
  private BeregnBidragsevneResultat mergePerioder(ArrayList<ResultatPeriode> resultatPeriodeListe) {
    var filtrertPeriodeResultatListe = new ArrayList<ResultatPeriode>();
    var resultatPeriodeForrige = new ResultatPeriode(new Periode(LocalDate.MIN, LocalDate.MAX),
        new ResultatBeregning(Double.valueOf(0.0)));
    var datoFra = resultatPeriodeListe.get(0).getResultatDatoFraTil().getDatoFra();
    var mergePerioder = false;
    int count = 0;

    for (ResultatPeriode resultatPeriode : resultatPeriodeListe) {
      count++;

      if (resultatPeriode.getResultatBeregning().kanMergesMed(resultatPeriodeForrige.getResultatBeregning())) {
        mergePerioder = true;
      } else {
        if (mergePerioder) {
          resultatPeriodeForrige.getResultatDatoFraTil().setDatoFra(datoFra);
          mergePerioder = false;
        }
        if (count > 1) {
          filtrertPeriodeResultatListe.add(resultatPeriodeForrige);
        }
        datoFra = resultatPeriode.getResultatDatoFraTil().getDatoFra();
      }

      resultatPeriodeForrige = resultatPeriode;
    }

    if (count > 0) {
      if (mergePerioder) {
        resultatPeriodeForrige.getResultatDatoFraTil().setDatoFra(datoFra);
      }
      filtrertPeriodeResultatListe.add(resultatPeriodeForrige);
    }

    return new BeregnBidragsevneResultat(filtrertPeriodeResultatListe);
  }
}
