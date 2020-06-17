package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;

//import com.google.common.base.Preconditions;

public class BidragsevneberegningImpl implements Bidragsevneberegning {

  private List<SjablonNokkel> sjablonNokkelListe = new ArrayList<>();

  @Override
  public ResultatBeregning beregn(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {

    //  Preconditions.checkNotNull(beregnBidragsevneGrunnlagPeriodisert, "Grunnlag kan ikke være null");

    System.out.println("Start beregning av bidragsevne");

    Double minstefradrag = beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert);

    // Legger sammen inntektene
    var inntekt = beregnBidragsevneGrunnlagPeriodisert.getInntektListe().stream()
        .map(Inntekt::getInntektBelop)
        .reduce(Double.valueOf(0), Double::sum);

    System.out.println("Samlede inntekter: " + inntekt);

    // finner personfradragklasse ut fra angitt skatteklasse
    Double personfradrag = 0.0;
    if (beregnBidragsevneGrunnlagPeriodisert.getSkatteklasse() == (1)) {
      System.out.println("Skatteklasse 1");
      personfradrag = SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
          SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP);
    } else {
      System.out.println("Skatteklasse 2");
      personfradrag = SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
          SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP);
    }

    System.out.println("Beregnet personfradrag: " + personfradrag);

    Double inntektMinusFradrag =
        inntekt - minstefradrag - personfradrag;

    // Trekker fra skatt
    Double forelopigBidragsevne = inntekt - (inntektMinusFradrag
        * SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
        SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT) / 100);

    System.out.println("Foreløpig evne etter fratrekk av ordinær skatt (totalt + månedlig beløp) : "
        + forelopigBidragsevne + " " + (Double.valueOf(Math.round(forelopigBidragsevne / 12))));

    // Trekker fra trygdeavgift
    System.out.println("Trygdeavgift: " + (Double.valueOf(Math.round(inntekt
        * (SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
        SjablonTallNavn.TRYGDEAVGIFT_PROSENT) / 100)))));

    forelopigBidragsevne =
        (forelopigBidragsevne - (inntekt * (
            SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
                SjablonTallNavn.TRYGDEAVGIFT_PROSENT) / 100)));
    System.out.println("Foreløpig evne etter fratrekk av trygdeavgift: " + forelopigBidragsevne);

    // Trekker fra trinnvis skatt
    forelopigBidragsevne -= beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert);
    System.out.println("Foreløpig evne etter fratrekk av trinnskatt: " + forelopigBidragsevne);


    // Trekker fra boutgifter og midler til eget underhold
    if (beregnBidragsevneGrunnlagPeriodisert.getBostatusKode().equals(BostatusKode.ALENE)) {
      sjablonNokkelListe.clear();
      sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN"));

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
              SjablonNavn.BIDRAGSEVNE,
              sjablonNokkelListe,
              SjablonInnholdNavn.BOUTGIFT_BELOP) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter bor alene: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
              SjablonNavn.BIDRAGSEVNE,
              sjablonNokkelListe,
              SjablonInnholdNavn.UNDERHOLD_BELOP) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold bor alene: "
              + forelopigBidragsevne);

    } else {
      sjablonNokkelListe.clear();
      sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS"));

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
              SjablonNavn.BIDRAGSEVNE,
              sjablonNokkelListe,
              SjablonInnholdNavn.BOUTGIFT_BELOP) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter gift/samboer: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
              SjablonNavn.BIDRAGSEVNE,
              sjablonNokkelListe,
              SjablonInnholdNavn.UNDERHOLD_BELOP) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold gift/samboer: "
              + forelopigBidragsevne);
    }

    // Trekker fra midler til underhold egne barn i egen husstand
    forelopigBidragsevne -= (SjablonUtil.hentSjablonverdi(
        beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
        SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP)
            * beregnBidragsevneGrunnlagPeriodisert.getAntallEgneBarnIHusstand()
            * 12);
    System.out.println("Foreløpig evne etter fratrekk av underhold for egne barn i egen husstand: "
        + forelopigBidragsevne);

    // Sjekker om og kalkulerer eventuell fordel særfradrag
    if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HELT)) {
      forelopigBidragsevne += SjablonUtil.hentSjablonverdi(
          beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
          SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP);

      System.out.println("Foreløpig evne etter tillegg for særfradrag: " + forelopigBidragsevne);
    } else {
      if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HALVT)) {
        forelopigBidragsevne += (
            SjablonUtil.hentSjablonverdi(
                beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
                SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP) / 2);
        System.out
            .println("Foreløpig evne etter tillegg for halvt særfradrag: " + forelopigBidragsevne);
      }
    }

    // Legger til fordel skatteklasse2
    if (beregnBidragsevneGrunnlagPeriodisert.getSkatteklasse() == (2)) {
      forelopigBidragsevne += SjablonUtil.hentSjablonverdi(
          beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
          SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP);
      System.out
          .println("Foreløpig evne etter tillegg for fordel skatteklasse2: " + forelopigBidragsevne);
    }

    // Finner månedlig beløp for bidragsevne
    Double maanedligBidragsevne = Double.valueOf(Math.round(forelopigBidragsevne / 12));
    System.out.println("Endelig beregnet bidragsevne: " + maanedligBidragsevne);

    if (maanedligBidragsevne.compareTo(0.0) < 0){
      System.out.println("Beregnet bidragsevne er mindre enn 0, settes til 0");
      maanedligBidragsevne = Double.valueOf(0.0);
      System.out.println("Korrigert bidragsevne: " + maanedligBidragsevne);
    }
    System.out.println("------------------------------------------------------");

    return new ResultatBeregning(maanedligBidragsevne);

  }

  @Override
  public Double beregnMinstefradrag(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {

    // Legger sammen inntektene
    var inntekt = beregnBidragsevneGrunnlagPeriodisert.getInntektListe().stream()
        .map(Inntekt::getInntektBelop)
        .reduce(Double.valueOf(0), Double::sum);

    Double minstefradrag = inntekt * (
        SjablonUtil.hentSjablonverdi(
            beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
            SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT) / 100);
    if (minstefradrag.compareTo(
        SjablonUtil.hentSjablonverdi(
            beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
            SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP)) > 0) {
      minstefradrag = SjablonUtil.hentSjablonverdi(
          beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
          SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP);
    }
    System.out.println("Beregnet minstefradrag: " + minstefradrag);
    return minstefradrag;
  }

  @Override
  public Double beregnSkattetrinnBelop(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {

    // Legger sammen inntektene
    var inntekt = beregnBidragsevneGrunnlagPeriodisert.getInntektListe().stream()
        .map(Inntekt::getInntektBelop)
        .reduce(Double.valueOf(0), Double::sum);

    var sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(
        beregnBidragsevneGrunnlagPeriodisert.getSjablonListe(),
        SjablonNavn.TRINNVIS_SKATTESATS);

    var samletSkattetrinnBelop = 0d;
    var indeks = 1;

    // Beregn skattetrinnbeløp
    while (indeks < sortertTrinnvisSkattesatsListe.size()) {
      if (inntekt > sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) {
        if (inntekt < sortertTrinnvisSkattesatsListe.get(indeks).getInntektGrense()) {
          samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
              (inntekt - sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) *
                  (sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
        } else {
          samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
              (sortertTrinnvisSkattesatsListe.get(indeks).getInntektGrense() -
                  sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) * (
                  sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
        }
      }
      indeks = indeks + 1;
    }

    if (inntekt > sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) {
      samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
          (inntekt - sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense())
              * (sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
    }


    System.out.println("Totalt skattetrinnbeløp: " + samletSkattetrinnBelop);

    return samletSkattetrinnBelop;
  }
}

