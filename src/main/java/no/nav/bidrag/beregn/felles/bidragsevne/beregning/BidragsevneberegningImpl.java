package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkelNy;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;

//import com.google.common.base.Preconditions;

public class BidragsevneberegningImpl implements Bidragsevneberegning {

  private List<SjablonNokkelNy> sjablonNokkelListe = new ArrayList<>();

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
      personfradrag = SjablonUtil.hentSjablontall(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
          SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), SjablonInnholdNavn.SJABLON_VERDI.getNavn());
    } else {
      personfradrag = SjablonUtil.hentSjablontall(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
          SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), SjablonInnholdNavn.SJABLON_VERDI.getNavn());
    }

    System.out.println("Beregnet personfradrag: " + personfradrag);

    Double inntektMinusFradrag =
        inntekt - minstefradrag - personfradrag;

    // Trekker fra skatt
    Double forelopigBidragsevne = inntekt - (inntektMinusFradrag
        * SjablonUtil.hentSjablontall(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
        SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(),
        SjablonInnholdNavn.SJABLON_VERDI.getNavn()) / 100);

    System.out.println("Foreløpig evne etter fratrekk av ordinær skatt (totalt + månedlig beløp) : "
        + forelopigBidragsevne + " " + (Double.valueOf(Math.round(forelopigBidragsevne / 12))));

    // Trekker fra trygdeavgift
    System.out.println("Trygdeavgift: " + (Double.valueOf(Math.round(inntekt
        * (SjablonUtil.hentSjablontall(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
        SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), SjablonInnholdNavn.SJABLON_VERDI.getNavn()) / 100)))));

    forelopigBidragsevne =
        (forelopigBidragsevne - (inntekt * (
            SjablonUtil.hentSjablontall(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
                SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), SjablonInnholdNavn.SJABLON_VERDI.getNavn()) / 100)));
    System.out.println("Foreløpig evne etter fratrekk av trygdeavgift: " + forelopigBidragsevne);

    // Trekker fra trinnvis skatt
    forelopigBidragsevne -= beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert);
    System.out.println("Foreløpig evne etter fratrekk av trinnskatt: " + forelopigBidragsevne);


    // Trekker fra boutgifter og midler til eget underhold
    if (beregnBidragsevneGrunnlagPeriodisert.getBostatusKode().equals(BostatusKode.ALENE)) {
      sjablonNokkelListe.clear();
      sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN"));

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
              SjablonNavn.BIDRAGSEVNE.getNavn(),
              sjablonNokkelListe,
              SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn()) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter bor alene: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
              SjablonNavn.BIDRAGSEVNE.getNavn(),
              sjablonNokkelListe,
              SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn()) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold bor alene: "
              + forelopigBidragsevne);

    } else {
      sjablonNokkelListe.clear();
      sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS"));

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
              SjablonNavn.BIDRAGSEVNE.getNavn(),
              sjablonNokkelListe,
              SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn()) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter gift/samboer: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          SjablonUtil.hentSjablonverdi(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
              SjablonNavn.BIDRAGSEVNE.getNavn(),
              sjablonNokkelListe,
              SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn()) * 12);

      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold gift/samboer: "
              + forelopigBidragsevne);
    }

    // Trekker fra midler til underhold egne barn i egen husstand
    forelopigBidragsevne -= (SjablonUtil.hentSjablontall(
        beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
        SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(),
        SjablonInnholdNavn.SJABLON_VERDI.getNavn())
            * beregnBidragsevneGrunnlagPeriodisert.getAntallEgneBarnIHusstand()
            * 12);
    System.out.println("Foreløpig evne etter fratrekk av underhold for egne barn i egen husstand: "
        + forelopigBidragsevne);

    // Sjekker om og kalkulerer eventuell fordel særfradrag
    if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HELT)) {
      forelopigBidragsevne += SjablonUtil.hentSjablontall(
          beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
          SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(),
          SjablonInnholdNavn.SJABLON_VERDI.getNavn());

      System.out.println("Foreløpig evne etter tillegg for særfradrag: " + forelopigBidragsevne);
    } else {
      if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HALVT)) {
        forelopigBidragsevne += (
            SjablonUtil.hentSjablontall(
                beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
                SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(),
                SjablonInnholdNavn.SJABLON_VERDI.getNavn()) / 2);
        System.out
            .println("Foreløpig evne etter tillegg for halvt særfradrag: " + forelopigBidragsevne);
      }
    }

    // Legger til fordel skatteklasse2
    forelopigBidragsevne += SjablonUtil.hentSjablontall(
        beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
        SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(),
        SjablonInnholdNavn.SJABLON_VERDI.getNavn());
    System.out
        .println("Foreløpig evne etter tillegg for fordel skatteklasse2: " + forelopigBidragsevne);

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
        SjablonUtil.hentSjablontall(
            beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(),
            SjablonInnholdNavn.SJABLON_VERDI.getNavn()) / 100);
    if (minstefradrag.compareTo(
        SjablonUtil.hentSjablontall(
            beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(),
            SjablonInnholdNavn.SJABLON_VERDI.getNavn())) > 0) {
      minstefradrag = SjablonUtil.hentSjablontall(
          beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
          SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(),
          SjablonInnholdNavn.SJABLON_VERDI.getNavn());
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

    long samletSkattetrinnbelop = 0;
    Double belopSkattetrinn1 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(0).getInntektGrense();

    Double satsSkattetrinn1 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(0).getSats();

    Double belopSkattetrinn2 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(1).getInntektGrense();

    Double satsSkattetrinn2 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(1).getSats();

    Double belopSkattetrinn3 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(2).getInntektGrense();

    Double satsSkattetrinn3 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(2).getSats();

    Double belopSkattetrinn4 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(3).getInntektGrense();

    Double satsSkattetrinn4 =
        SjablonUtil.hentTrinnvisSkattesats(beregnBidragsevneGrunnlagPeriodisert.getSjablonListeNy(),
            SjablonNavn.TRINNVIS_SKATTESATS.getNavn()).get(3).getSats();

    if (inntekt > belopSkattetrinn1) {
      if (inntekt < belopSkattetrinn2) {
        samletSkattetrinnbelop = Math.round(
            (inntekt - belopSkattetrinn1) * (satsSkattetrinn1 / 100));
      } else {
        samletSkattetrinnbelop = Math
            .round((belopSkattetrinn2 - belopSkattetrinn1) * (satsSkattetrinn1 / 100));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp1: " + Math.round(samletSkattetrinnbelop));

    if (inntekt > belopSkattetrinn2) {
      if (inntekt < belopSkattetrinn3) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (inntekt - belopSkattetrinn2) * (satsSkattetrinn2 / 100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn3 - belopSkattetrinn2) * (satsSkattetrinn2
                / 100)));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp2: " + samletSkattetrinnbelop);

    if (inntekt > belopSkattetrinn3) {
      if (inntekt < belopSkattetrinn4) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (inntekt - belopSkattetrinn3) * (satsSkattetrinn3 / 100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn4 - belopSkattetrinn3) * (satsSkattetrinn3
                / 100)));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp3: " + samletSkattetrinnbelop);

    if (inntekt > belopSkattetrinn4) {
      samletSkattetrinnbelop = Math.round(
          samletSkattetrinnbelop + ((inntekt - belopSkattetrinn4)
              * (satsSkattetrinn4 / 100)));
    }
    System.out.println("Totalt skattetrinnbeløp: " + samletSkattetrinnbelop);

    return Double.valueOf(samletSkattetrinnbelop);
  }
}

