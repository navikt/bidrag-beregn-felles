package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.PeriodeUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.Bidragsevneberegning;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SaerfradragPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SkatteklassePeriode;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.periode.Periodiserer;


public class BidragsevnePeriodeImpl implements BidragsevnePeriode {

  public BidragsevnePeriodeImpl(Bidragsevneberegning bidragsevneberegning) {
    this.bidragsevneberegning = bidragsevneberegning;
  }

  //  private Bidragsevneberegning bidragsevneberegning = Bidragsevneberegning.getInstance();
  private Bidragsevneberegning bidragsevneberegning;

  public BeregnBidragsevneResultat beregnPerioder(
      BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt) {

    var resultatPeriodeListe = new ArrayList<ResultatPeriode>();

    var justertSjablonPeriodeListe = beregnBidragsevneGrunnlagAlt.getSjablonPeriodeListe()
        .stream()
        .map(SjablonPeriode::new)
        .collect(toCollection(ArrayList::new));

    var justertInntektPeriodeListe = beregnBidragsevneGrunnlagAlt.getInntektPeriodeListe()
        .stream()
        .map(InntektPeriode::new)
        .collect(toCollection(ArrayList::new));

    var justertSkatteklassePeriodeListe = beregnBidragsevneGrunnlagAlt.getSkatteklassePeriodeListe()
        .stream()
        .map(SkatteklassePeriode::new)
        .collect(toCollection(ArrayList::new));

    var justertBostatusPeriodeListe = beregnBidragsevneGrunnlagAlt.getBostatusPeriodeListe()
        .stream()
        .map(BostatusPeriode::new)
        .collect(toCollection(ArrayList::new));

    var justertAntallBarnIEgetHusholdPeriodeListe = beregnBidragsevneGrunnlagAlt.getAntallBarnIEgetHusholdPeriodeListe()
        .stream()
        .map(AntallBarnIEgetHusholdPeriode::new)
        .collect(toCollection(ArrayList::new));

    var justertSaerfradragPeriodeListe = beregnBidragsevneGrunnlagAlt.getSaerfradragPeriodeListe()
        .stream()
        .map(SaerfradragPeriode::new)
        .collect(toCollection(ArrayList::new));

    // Bygger opp liste over perioder
    List<Periode> perioder = new Periodiserer()
        .addBruddpunkt(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra()) //For å sikre bruddpunkt på start-beregning-fra-dato
        .addBruddpunkter(justertSjablonPeriodeListe)
        .addBruddpunkter(justertInntektPeriodeListe)
        .addBruddpunkter(justertSkatteklassePeriodeListe)
        .addBruddpunkter(justertBostatusPeriodeListe)
        .addBruddpunkter(justertAntallBarnIEgetHusholdPeriodeListe)
        .addBruddpunkter(justertSaerfradragPeriodeListe)
        .addBruddpunkt(beregnBidragsevneGrunnlagAlt.getBeregnDatoTil()) //For å sikre bruddpunkt på start-beregning-til-dato
        .finnPerioder(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(), beregnBidragsevneGrunnlagAlt.getBeregnDatoTil());

    // Løper gjennom periodene og finner matchende verdi for hver kategori. Kaller beregningsmodulen for hver beregningsperiode

    // Hvis det ligger 2 perioder på slutten som i til-dato inneholder hhv. beregningsperiodens til-dato og null slås de sammen
    if (perioder.size() > 1) {
      if ((perioder.get(perioder.size() - 2).getDatoTil().equals(beregnBidragsevneGrunnlagAlt.getBeregnDatoTil())) &&
          (perioder.get(perioder.size() - 1).getDatoTil() == null)) {
        var nyPeriode = new Periode(perioder.get(perioder.size() - 2).getDatoFra(), null);
        perioder.remove(perioder.size() - 1);
        perioder.remove(perioder.size() - 1);
        perioder.add(nyPeriode);
      }
    }

    for (Periode beregningsperiode : perioder) {

      var inntektListe = justertInntektPeriodeListe.stream().filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode))
          .map(inntektPeriode -> new Inntekt(inntektPeriode.getInntektType(), inntektPeriode.getInntektBelop())).collect(toList());

      var skatteklasse = justertSkatteklassePeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(SkatteklassePeriode::getSkatteklasse).findFirst().orElse(null);

      var bostatusKode = justertBostatusPeriodeListe.stream().filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode))
          .map(BostatusPeriode::getBostatusKode).findFirst().orElse(null);

      var antallBarnIEgetHushold = justertAntallBarnIEgetHusholdPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(AntallBarnIEgetHusholdPeriode::getAntallBarn).findFirst().orElse(null);

      var saerfradrag = justertSaerfradragPeriodeListe.stream()
          .filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode)).map(SaerfradragPeriode::getSaerfradragKode).findFirst().orElse(null);

      var sjablonliste = justertSjablonPeriodeListe.stream().filter(i -> i.getDatoFraTil().overlapperMed(beregningsperiode))
          .map(sjablonPeriode -> new Sjablon(sjablonPeriode.getSjablon().getSjablonNavn(),
              sjablonPeriode.getSjablon().getSjablonNokkelListe(),
              sjablonPeriode.getSjablon().getSjablonInnholdListe())).collect(toList());

      System.out.println("Beregner bidragsevne for periode: " + beregningsperiode.getDatoFra() + " " + beregningsperiode.getDatoTil());

      // Kaller beregningsmodulen for hver beregningsperiode
      var beregnBidragsevneGrunnlagPeriodisert = new BeregnBidragsevneGrunnlagPeriodisert(inntektListe, skatteklasse, bostatusKode,
          antallBarnIEgetHushold,
          saerfradrag, sjablonliste);

      resultatPeriodeListe.add(new ResultatPeriode(beregningsperiode, bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert),
          beregnBidragsevneGrunnlagPeriodisert));
    }

    //Slår sammen perioder med samme resultat
    return new BeregnBidragsevneResultat(resultatPeriodeListe);

  }


  // Validerer at input-verdier til bidragsevneberegning er gyldige
  public List<Avvik> validerInput(BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt) {

    // Sjekk perioder for sjablonliste
    var sjablonPeriodeListe = new ArrayList<Periode>();
    for (SjablonPeriode sjablonPeriode : beregnBidragsevneGrunnlagAlt.getSjablonPeriodeListe()) {
      sjablonPeriodeListe.add(sjablonPeriode.getDatoFraTil());
    }
    var avvikListe = new ArrayList<>(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "sjablonPeriodeListe", sjablonPeriodeListe, false, false, false, false));

    // Sjekk perioder for inntekt
    var inntektPeriodeListe = new ArrayList<Periode>();
    for (InntektPeriode inntektPeriode : beregnBidragsevneGrunnlagAlt.getInntektPeriodeListe()) {
      inntektPeriodeListe.add(inntektPeriode.getDatoFraTil());
    }
    avvikListe.addAll(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "inntektPeriodeListe", inntektPeriodeListe, false, true, false, true));

    // Sjekk perioder for skatteklasse
    var skatteklassePeriodeListe = new ArrayList<Periode>();
    for (SkatteklassePeriode skatteklassePeriode : beregnBidragsevneGrunnlagAlt.getSkatteklassePeriodeListe()) {
      skatteklassePeriodeListe.add(skatteklassePeriode.getDatoFraTil());
    }
    avvikListe.addAll(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "skatteklassePeriodeListe", skatteklassePeriodeListe, true, true, true, true));

    // Sjekk perioder for bostatus
    var bostatusPeriodeListe = new ArrayList<Periode>();
    for (BostatusPeriode bostatusPeriode : beregnBidragsevneGrunnlagAlt.getBostatusPeriodeListe()) {
      bostatusPeriodeListe.add(bostatusPeriode.getDatoFraTil());
    }
    avvikListe.addAll(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "bostatusPeriodeListe", bostatusPeriodeListe, true, true, true, true));

    // Sjekk perioder for antall barn i eget hushold
    var antallBarnIEgetHusholdPeriodeListe = new ArrayList<Periode>();
    for (AntallBarnIEgetHusholdPeriode antallBarnIEgetHusholdPeriode : beregnBidragsevneGrunnlagAlt.getAntallBarnIEgetHusholdPeriodeListe()) {
      antallBarnIEgetHusholdPeriodeListe.add(antallBarnIEgetHusholdPeriode.getDatoFraTil());
    }
    avvikListe.addAll(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "antallBarnIEgetHusholdPeriodeListe", antallBarnIEgetHusholdPeriodeListe, false, false,
        false, true));

    // Sjekk perioder for særfradrag
    var saerfradragPeriodeListe = new ArrayList<Periode>();
    for (SaerfradragPeriode saerfradragPeriode : beregnBidragsevneGrunnlagAlt.getSaerfradragPeriodeListe()) {
      saerfradragPeriodeListe.add(saerfradragPeriode.getDatoFraTil());
    }
    avvikListe.addAll(PeriodeUtil.validerInputDatoer(beregnBidragsevneGrunnlagAlt.getBeregnDatoFra(),
        beregnBidragsevneGrunnlagAlt.getBeregnDatoTil(), "saerfradragPeriodeListe", saerfradragPeriodeListe, true, true, true, true));

    return avvikListe;
  }
}
