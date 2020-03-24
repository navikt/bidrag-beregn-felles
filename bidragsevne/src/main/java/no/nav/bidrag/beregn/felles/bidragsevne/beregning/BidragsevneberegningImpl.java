package no.nav.bidrag.beregn.felles.bidragsevne.beregning;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.grunnlag.BidragsevneberegningGrunnlag;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.resultat.BidragsevneBeregningResultat;

public class BidragsevneberegningImpl implements Bidragsevneberegning {

  @Override
  public BidragsevneBeregningResultat beregn(
      BidragsevneberegningGrunnlag bidragsevneberegningGrunnlag) {

    Double minstefradrag = beregnMinstefradrag(bidragsevneberegningGrunnlag);
    Double personfradrag = bidragsevneberegningGrunnlag.hentSjablon("personfradrag").getSjablonVerdi1();
    Double inntektMinusFradrag =
        bidragsevneberegningGrunnlag.getInntekt() - minstefradrag - personfradrag;

    // Trekker fra skatt
    Double forelopigBidragsevne = bidragsevneberegningGrunnlag.getInntekt() - (inntektMinusFradrag
        * bidragsevneberegningGrunnlag.hentSjablon("skattesats").getSjablonVerdi1());
    System.out.println("Foreløpig evne etter fratrekk av ordinær skatt: " + forelopigBidragsevne);

    // Trekker fra trygdeavgift
    forelopigBidragsevne =
        (forelopigBidragsevne - (bidragsevneberegningGrunnlag.getInntekt()
            * bidragsevneberegningGrunnlag.hentSjablon("satsTrygdeavgift").getSjablonVerdi1()));
    System.out.println("Foreløpig evne etter fratrekk av trygdeavgift: " + forelopigBidragsevne);

    // Trekker fra trinnvis skatt
    forelopigBidragsevne -= beregnSkattetrinnBelop(bidragsevneberegningGrunnlag);
    System.out.println("Foreløpig evne etter fratrekk av trinnskatt: " + forelopigBidragsevne);

    // Trekker fra boutgifter
    forelopigBidragsevne -= (bidragsevneberegningGrunnlag.hentSjablon("belopBoutgift").getSjablonVerdi1()
        * 12);
    System.out.println("Foreløpig evne etter fratrekk av boutgifter: " + forelopigBidragsevne);

    // Trekker fra midler til eget underhold
    forelopigBidragsevne -= (
        bidragsevneberegningGrunnlag.hentSjablon("belopUnderholdEget").getSjablonVerdi1() * 12);
    System.out.println(
        "Foreløpig evne etter fratrekk av midler til eget underhold: " + forelopigBidragsevne);

    // Trekker fra midler til underhold egne barn i egen husstand
    forelopigBidragsevne -= (
        bidragsevneberegningGrunnlag.hentSjablon("belopUnderholdEgneBarnIHusstand").getSjablonVerdi1() *
            bidragsevneberegningGrunnlag.getAntallEgneBarnIHusstand()
            * 12);
    System.out.println("Foreløpig evne etter fratrekk av underhold for egne barn i egen husstand: "
        + forelopigBidragsevne);

    // Legger til fordel særfradrag
    forelopigBidragsevne += bidragsevneberegningGrunnlag.hentSjablon("fordelSarfradrag")
        .getSjablonVerdi1();
    System.out.println("Foreløpig evne etter tillegg for særfradrag: " + forelopigBidragsevne);

    // Legger til fordel skatteklasse2
    forelopigBidragsevne += bidragsevneberegningGrunnlag.hentSjablon("fordelSkatteklasse2")
        .getSjablonVerdi1();
    System.out
        .println("Foreløpig evne etter tillegg for fordel skatteklasse2: " + forelopigBidragsevne);

    // Finner månedlig beløp for bidragsevne
    Double maanedligBidragsevne = Double.valueOf(Math.round(forelopigBidragsevne / 12));
    System.out.println("Endelig beregnet bidragsevne: " + maanedligBidragsevne);

    return new BidragsevneBeregningResultat(maanedligBidragsevne);
  }

  @Override
  public Double beregnMinstefradrag(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag) {
    Double minstefradrag = bidragsevneBeregningGrunnlag.getInntekt() * (bidragsevneBeregningGrunnlag
        .hentSjablon("minstefradragProsentsats").getSjablonVerdi1());
    if (minstefradrag
        .compareTo(bidragsevneBeregningGrunnlag.hentSjablon("minstefradragBelop").getSjablonVerdi1())
        > 0) {
      minstefradrag = bidragsevneBeregningGrunnlag.hentSjablon("minstefradragBelop").getSjablonVerdi1();
    }
    System.out.println("Beregnet minstefradrag: " + minstefradrag);
    return minstefradrag;
  }

  @Override
  public Double beregnSkattetrinnBelop(BidragsevneberegningGrunnlag bidragsevneBeregningGrunnlag) {
    long samletSkattetrinnbelop = 0;
    Double belopSkattetrinn1 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn1").getSjablonVerdi1();
    Double satsSkattetrinn1 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn1").getSjablonVerdi2();

    Double belopSkattetrinn2 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn2").getSjablonVerdi1();
    Double satsSkattetrinn2 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn2").getSjablonVerdi2();

    Double belopSkattetrinn3 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn3").getSjablonVerdi1();
    Double satsSkattetrinn3 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn3").getSjablonVerdi2();

    Double belopSkattetrinn4 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn4").getSjablonVerdi1();
    Double satsSkattetrinn4 = bidragsevneBeregningGrunnlag.hentSjablon("skattetrinn4").getSjablonVerdi2();

    if (bidragsevneBeregningGrunnlag.getInntekt() > belopSkattetrinn1) {
      if (bidragsevneBeregningGrunnlag.getInntekt() < belopSkattetrinn2) {
        samletSkattetrinnbelop = Math.round(
            (bidragsevneBeregningGrunnlag.getInntekt() - belopSkattetrinn1) * satsSkattetrinn1);
      } else {
        samletSkattetrinnbelop = Math
            .round((belopSkattetrinn2 - belopSkattetrinn1) * satsSkattetrinn1);
      }
    }
//        System.out.println("Samlet skattetrinnbeløp1: " + Math.round(samletSkattetrinnbelop));

    if (bidragsevneBeregningGrunnlag.getInntekt() > belopSkattetrinn2) {
      if (bidragsevneBeregningGrunnlag.getInntekt() < belopSkattetrinn3) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (bidragsevneBeregningGrunnlag.getInntekt() - belopSkattetrinn2) * satsSkattetrinn2));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn3 - belopSkattetrinn2) * satsSkattetrinn2));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp2: " + samletSkattetrinnbelop);

    if (bidragsevneBeregningGrunnlag.getInntekt() > belopSkattetrinn3) {
      if (bidragsevneBeregningGrunnlag.getInntekt() < belopSkattetrinn4) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (bidragsevneBeregningGrunnlag.getInntekt() - belopSkattetrinn3) * satsSkattetrinn3));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn4 - belopSkattetrinn3) * satsSkattetrinn3));
      }
    }
//        System.out.println("Samlet skattetrinnbeløp3: " + samletSkattetrinnbelop);

    if (bidragsevneBeregningGrunnlag.getInntekt() > belopSkattetrinn4) {
      samletSkattetrinnbelop = Math.round(
          samletSkattetrinnbelop + ((bidragsevneBeregningGrunnlag.getInntekt() - belopSkattetrinn4)
              * satsSkattetrinn4));
    }
    System.out.println("Totalt skattetrinnbeløp: " + samletSkattetrinnbelop);

    return Double.valueOf(samletSkattetrinnbelop);
  }
}

