package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;

public class BidragsevneberegningImpl implements Bidragsevneberegning {

  @Override
  public ResultatBeregning beregn(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {

    Double minstefradrag = beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert);

    // finner personfradragklasse ut fra angitt skatteklasse
    Double personfradrag = 0.0;
    if (beregnBidragsevneGrunnlagPeriodisert.getSkatteklasse() == (1)) {
      personfradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("personfradragKlasse1")
          .getSjablonVerdi1();
    } else {
      personfradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("personfradragKlasse2")
          .getSjablonVerdi1();
    }

    Double inntektMinusFradrag =
        beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - minstefradrag - personfradrag;

    // Trekker fra skatt
    Double forelopigBidragsevne = beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - (inntektMinusFradrag
        * beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattesats").getSjablonVerdi1()/100);
    System.out.println("Foreløpig evne etter fratrekk av ordinær skatt: " + forelopigBidragsevne + " " + (Double.valueOf(Math.round(forelopigBidragsevne/12))));

    // Trekker fra trygdeavgift
    System.out.println("Trygdeavgift: " + (Double.valueOf(Math.round(beregnBidragsevneGrunnlagPeriodisert.getInntektBelop()
        * (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("satsTrygdeavgift").
        getSjablonVerdi1()/100)))));

    forelopigBidragsevne =
        (forelopigBidragsevne - (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop()
            * (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("satsTrygdeavgift").
            getSjablonVerdi1()/100)));
    System.out.println("Foreløpig evne etter fratrekk av trygdeavgift: " + forelopigBidragsevne);

    // Trekker fra trinnvis skatt
    forelopigBidragsevne -= beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert);
    System.out.println("Foreløpig evne etter fratrekk av trinnskatt: " + forelopigBidragsevne);

    // Trekker fra boutgifter og midler til eget underhold
    if(beregnBidragsevneGrunnlagPeriodisert.getBorAlene()) {
      forelopigBidragsevne -= (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("belopBoutgiftEn").
          getSjablonVerdi1() * 12);
      System.out.println("Foreløpig evne etter fratrekk av boutgifter bor alene: "
          + forelopigBidragsevne);

      forelopigBidragsevne -= (
          beregnBidragsevneGrunnlagPeriodisert.hentSjablon("belopUnderholdEgetEn").
              getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold bor alene: "
              + forelopigBidragsevne);

    }
    else {
      forelopigBidragsevne -= (beregnBidragsevneGrunnlagPeriodisert.hentSjablon("belopBoutgiftGs").
          getSjablonVerdi1() * 12);
      System.out.println("Foreløpig evne etter fratrekk av boutgifter gift/samboer: "
          + forelopigBidragsevne);

      forelopigBidragsevne -= (
          beregnBidragsevneGrunnlagPeriodisert.hentSjablon("belopUnderholdEgetGs").
              getSjablonVerdi1() * 12);
      System.out.println(
          "Foreløpig evne etter fratrekk av midler til eget underhold gift/samboer: "
              + forelopigBidragsevne);
    }

    // Trekker fra midler til underhold egne barn i egen husstand
    forelopigBidragsevne -= (
        beregnBidragsevneGrunnlagPeriodisert.hentSjablon("belopUnderholdEgneBarnIHusstand").
            getSjablonVerdi1() * beregnBidragsevneGrunnlagPeriodisert.getAntallEgneBarnIHusstand() * 12);
    System.out.println("Foreløpig evne etter fratrekk av underhold for egne barn i egen husstand: "
        + forelopigBidragsevne);

    // Legger til fordel særfradrag
    forelopigBidragsevne += beregnBidragsevneGrunnlagPeriodisert.hentSjablon("fordelSarfradrag").
        getSjablonVerdi1();
    System.out.println("Foreløpig evne etter tillegg for særfradrag: " + forelopigBidragsevne);

    // Legger til fordel skatteklasse2
    forelopigBidragsevne += beregnBidragsevneGrunnlagPeriodisert.hentSjablon("fordelSkatteklasse2").
        getSjablonVerdi1();
    System.out.println("Foreløpig evne etter tillegg for fordel skatteklasse2: " + forelopigBidragsevne);

    // Finner månedlig beløp for bidragsevne
    Double maanedligBidragsevne = Double.valueOf(Math.round(forelopigBidragsevne / 12));
    System.out.println("Endelig beregnet bidragsevne: " + maanedligBidragsevne);

    return new ResultatBeregning(maanedligBidragsevne);

  }

  @Override
  public Double beregnMinstefradrag(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {
    Double minstefradrag = beregnBidragsevneGrunnlagPeriodisert
        .getInntektBelop() * (beregnBidragsevneGrunnlagPeriodisert
        .hentSjablon("minstefradragProsentsats").getSjablonVerdi1()/100);
    if (minstefradrag.compareTo(
            beregnBidragsevneGrunnlagPeriodisert.hentSjablon("minstefradragBelop").getSjablonVerdi1())
        > 0) {
      minstefradrag = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("minstefradragBelop")
          .getSjablonVerdi1();
    }
    System.out.println("Beregnet minstefradrag: " + minstefradrag);
    return minstefradrag;
  }

  @Override
  public Double beregnSkattetrinnBelop(
      BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert) {
    long samletSkattetrinnbelop = 0;
    Double belopSkattetrinn1 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1")
        .getSjablonVerdi1();
    Double satsSkattetrinn1 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1")
        .getSjablonVerdi2();
    Double belopSkattetrinn2 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn2")
        .getSjablonVerdi1();
    Double satsSkattetrinn2 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn2")
        .getSjablonVerdi2();
    Double belopSkattetrinn3 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn3")
        .getSjablonVerdi1();
    Double satsSkattetrinn3 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn3")
        .getSjablonVerdi2();
    Double belopSkattetrinn4 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn4")
        .getSjablonVerdi1();
    Double satsSkattetrinn4 = beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn4")
        .getSjablonVerdi2();

    if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() > belopSkattetrinn1) {
      if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() < belopSkattetrinn2) {
        samletSkattetrinnbelop = Math.round(
            (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - belopSkattetrinn1) * (satsSkattetrinn1/100));
      } else {
        samletSkattetrinnbelop = Math
            .round((belopSkattetrinn2 - belopSkattetrinn1) * (satsSkattetrinn1/100));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp1: " + Math.round(samletSkattetrinnbelop));

    if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() > belopSkattetrinn2) {
      if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() < belopSkattetrinn3) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - belopSkattetrinn2) * (satsSkattetrinn2/100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn3 - belopSkattetrinn2) * (satsSkattetrinn2/100)));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp2: " + samletSkattetrinnbelop);

    if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() > belopSkattetrinn3) {
      if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() < belopSkattetrinn4) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - belopSkattetrinn3) * (satsSkattetrinn3/100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn4 - belopSkattetrinn3) * (satsSkattetrinn3/100)));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp3: " + samletSkattetrinnbelop);

    if (beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() > belopSkattetrinn4) {
      samletSkattetrinnbelop = Math.round(
          samletSkattetrinnbelop + ((beregnBidragsevneGrunnlagPeriodisert.getInntektBelop() - belopSkattetrinn4)
              * (satsSkattetrinn4/100)));
    }
    System.out.println("Totalt skattetrinnbeløp: " + samletSkattetrinnbelop);

    return Double.valueOf(samletSkattetrinnbelop);
  }
}

