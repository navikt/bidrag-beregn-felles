package no.nav.bidrag.beregn.felles.bidragsevne;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Sjablon;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BidragsevneBeregningTest")
class BidragsevneberegningTest {

  @Test
  void beregn() {

    ArrayList<Sjablon> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31

    sjabloner.add(new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));
    sjabloner.add(new Sjablon("TrygdeavgiftProsent", Double.valueOf(8.2), null));
    sjabloner.add(new Sjablon("UnderholdEgneBarnIHusstandBelop", Double.valueOf(3487), null));
    sjabloner.add(new Sjablon("MinstefradragInntektBelop", Double.valueOf(85050), null));
    sjabloner.add(new Sjablon("MinstefradragInntektProsent", Double.valueOf(31), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse1Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("FordelSaerfradragBelop", Double.valueOf(0), null));
    sjabloner.add(new Sjablon("SkattesatsAlminneligInntektProsent", Double.valueOf(22), null));
    sjabloner.add(new Sjablon("Skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new Sjablon("Skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new Sjablon("Skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new Sjablon("Skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));
    sjabloner.add(new Sjablon("BoutgiftEnBelop", Double.valueOf(9591), null));  //EN
    sjabloner.add(new Sjablon("UnderholdEgetEnBelop", Double.valueOf(8925), null)); //EN
    sjabloner.add(new Sjablon("BoutgiftGsBelop", Double.valueOf(5875), null));  //GS
    sjabloner.add(new Sjablon("UnderholdEgetGsBelop", Double.valueOf(7557), null)); //GS
    sjabloner.add(new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));

    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    assertEquals(Double.valueOf(33050),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(520000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    assertEquals(Double.valueOf(9767),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert2).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3, SaerfradragKode.HELT, sjabloner);
    assertEquals(Double.valueOf(10410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert3).getResultatBelopEvne());

  }

  @Test
  void beregnMinstefradrag() {

    ArrayList<Sjablon> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));
    sjabloner.add(new Sjablon("TrygdeavgiftProsent", Double.valueOf(8.2), null));
    sjabloner.add(new Sjablon("UnderholdEgneBarnIHusstandBelop", Double.valueOf(3487), null));
    sjabloner.add(new Sjablon("MinstefradragInntektBelop", Double.valueOf(85050), null));
    sjabloner.add(new Sjablon("MinstefradragInntektProsent", Double.valueOf(31), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse1Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("FordelSaerfradragBelop", Double.valueOf(0), null));
    sjabloner.add(new Sjablon("SkattesatsAlminneligInntektProsent", Double.valueOf(22), null));
    sjabloner.add(new Sjablon("Skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new Sjablon("Skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new Sjablon("Skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new Sjablon("Skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));
    sjabloner.add(new Sjablon("BoutgiftEnBelop", Double.valueOf(9591), null));  //EN
    sjabloner.add(new Sjablon("UnderholdEgetEnBelop", Double.valueOf(8925), null)); //EN
    sjabloner.add(new Sjablon("BoutgiftGsBelop", Double.valueOf(5875), null));  //GS
    sjabloner.add(new Sjablon("UnderholdEgetGsBelop", Double.valueOf(7557), null)); //GS
    sjabloner.add(new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(200000)));

//    beregnBidragsevneGrunnlagPeriodisert.setSjablonPeriodeListe(sjabloner);
    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(62000)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert2));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert2))
        .equals(Double.valueOf(85050)));

    //assertTrue(bidragsevneberegning.beregnMinstefradrag(bidragsevneberegningGrunnlag).compareTo(Double.valueOf(62000)) = 0);

  }

  @Test
  void beregnSkattetrinnBelop() {
    ArrayList<Sjablon> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new Sjablon("Skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new Sjablon("Skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new Sjablon("Skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new Sjablon("Skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));

    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    //System.out.println(bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(1352+15618+6402+0)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(174600)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert2))
        .equals(Double.valueOf(2)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(250000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert3))
        .equals(Double.valueOf(1352+183)));
  }
}













