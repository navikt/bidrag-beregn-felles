package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import java.math.BigDecimal;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatBeregning;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;

//import com.google.common.base.Preconditions;

public class BidragsevneberegningImpl implements Bidragsevneberegning {

  @Override
  public ResultatBeregning beregn(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {

    //  Preconditions.checkNotNull(beregnBidragsevneGrunnlagPeriodisert, "Grunnlag kan ikke være null");

    Double minstefradrag = beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert);

    // Legger sammen inntektene
    var inntekt = beregnBidragsevneGrunnlagPeriodisert.getInntektListe().stream()
        .map(Inntekt::getInntektBelop)
        .reduce(Double.valueOf(0), Double::sum);

    System.out.println("Start beregning av bidragsevne");

    // finner personfradragklasse ut fra angitt skatteklasse
    Double personfradrag = 0.0;
    if (beregnBidragsevneGrunnlagPeriodisert.getSkatteklasse() == (1)) {
      personfradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("PersonfradragKlasse1")
          .getSjablonVerdi1();
    } else {
      personfradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("PersonfradragKlasse2")
          .getSjablonVerdi1();
    }

    Double inntektMinusFradrag =
        inntekt - minstefradrag - personfradrag;

    // Trekker fra skatt
    Double forelopigBidragsevne = inntekt - (inntektMinusFradrag
        * beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattesats").getSjablonVerdi1() / 100);
    System.out.println("Foreløpig evne etter fratrekk av ordinær skatt (totalt + månedlig beløp) : "
        + forelopigBidragsevne + " " + (Double.valueOf(Math.round(forelopigBidragsevne / 12))));

    // Trekker fra trygdeavgift
    System.out.println("Trygdeavgift: " + (Double.valueOf(Math.round(inntekt
        * (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("SatsTrygdeavgift").
        getSjablonVerdi1() / 100)))));

    forelopigBidragsevne =
        (forelopigBidragsevne - (inntekt * (
            beregnBidragsevneGrunnlagPeriodisert.hentSjablon("SatsTrygdeavgift").
                getSjablonVerdi1() / 100)));
    System.out.println("Foreløpig evne etter fratrekk av trygdeavgift: " + forelopigBidragsevne);

    // Trekker fra trinnvis skatt
    forelopigBidragsevne -= beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert);
    System.out.println("Foreløpig evne etter fratrekk av trinnskatt: " + forelopigBidragsevne);

    // Trekker fra boutgifter og midler til eget underhold
    if (beregnBidragsevneGrunnlagPeriodisert.getBostatusKode().equals(BostatusKode.ALENE)) {
      forelopigBidragsevne -= (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("BelopBoutgiftEn").
          getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter bor alene: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          beregnBidragsevneGrunnlagPeriodisert.hentSjablon("BelopUnderholdEgetEn").
              getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold bor alene: "
              + forelopigBidragsevne);

    } else {
      forelopigBidragsevne -= (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("BelopBoutgiftGs").
          getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av boutgifter gift/samboer: " + forelopigBidragsevne);

      forelopigBidragsevne -= (
          beregnBidragsevneGrunnlagPeriodisert.hentSjablon("BelopUnderholdEgetGs").
              getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold gift/samboer: "
              + forelopigBidragsevne);
    }

    // Trekker fra midler til underhold egne barn i egen husstand
    forelopigBidragsevne -= (
        beregnBidragsevneGrunnlagPeriodisert.hentSjablon("BelopUnderholdEgneBarnIHusstand").
            getSjablonVerdi1() * beregnBidragsevneGrunnlagPeriodisert.getAntallEgneBarnIHusstand()
            * 12);
    System.out.println("Foreløpig evne etter fratrekk av underhold for egne barn i egen husstand: "
        + forelopigBidragsevne);

    // Sjekker om og kalkulerer eventuell fordel særfradrag
    if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HELT)) {
      forelopigBidragsevne += beregnBidragsevneGrunnlagPeriodisert.hentSjablon("FordelSaerfradrag").
          getSjablonVerdi1();
      System.out.println("Foreløpig evne etter tillegg for særfradrag: " + forelopigBidragsevne);
    } else {
      if (beregnBidragsevneGrunnlagPeriodisert.getSaerfradragkode().equals(SaerfradragKode.HALVT)) {
        forelopigBidragsevne += (
            beregnBidragsevneGrunnlagPeriodisert.hentSjablon("FordelSaerfradrag").
                getSjablonVerdi1() / 2);
        System.out
            .println("Foreløpig evne etter tillegg for halvt særfradrag: " + forelopigBidragsevne);
      }
    }

    // Legger til fordel skatteklasse2
    forelopigBidragsevne += beregnBidragsevneGrunnlagPeriodisert.hentSjablon("FordelSkatteklasse2").
        getSjablonVerdi1();
    System.out
        .println("Foreløpig evne etter tillegg for fordel skatteklasse2: " + forelopigBidragsevne);

    // Finner månedlig beløp for bidragsevne
    Double maanedligBidragsevne = Double.valueOf(Math.round(forelopigBidragsevne / 12));
    System.out.println("Endelig beregnet bidragsevne: " + maanedligBidragsevne);
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

    Double minstefradrag = inntekt * (beregnBidragsevneGrunnlagPeriodisert
        .hentSjablon("MinstefradragProsentInntekt").getSjablonVerdi1() / 100);
    if (minstefradrag.compareTo(
        beregnBidragsevneGrunnlagPeriodisert.hentSjablon("MinstefradragBelop").getSjablonVerdi1())
        > 0) {
      minstefradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("MinstefradragBelop")
          .getSjablonVerdi1();
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
    Double belopSkattetrinn1 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn1")
        .getSjablonVerdi1();
    Double satsSkattetrinn1 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn1")
        .getSjablonVerdi2();
    Double belopSkattetrinn2 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn2")
        .getSjablonVerdi1();
    Double satsSkattetrinn2 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn2")
        .getSjablonVerdi2();
    Double belopSkattetrinn3 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn3")
        .getSjablonVerdi1();
    Double satsSkattetrinn3 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn3")
        .getSjablonVerdi2();
    Double belopSkattetrinn4 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn4")
        .getSjablonVerdi1();
    Double satsSkattetrinn4 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn4")
        .getSjablonVerdi2();

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

