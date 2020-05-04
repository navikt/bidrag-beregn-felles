package no.nav.bidrag.beregn.felles.bidragsevne;

import java.time.LocalDate;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bo.Periode;
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

    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31

    sjabloner.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-01-01"), LocalDate.parse("9999-12-31")),
        "FordelSkatteklasse2", Double.valueOf(0), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "SatsTrygdeavgift", Double.valueOf(8.2), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "MinstefradragBelop", Double.valueOf(85050), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "MinstefradragProsentInntekt", Double.valueOf(31), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "PersonfradragKlasse1", Double.valueOf(56550), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "PersonfradragKlasse2", Double.valueOf(56550), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "FordelSaerfradrag", Double.valueOf(0), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "Skattesats", Double.valueOf(22), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgiftEn", Double.valueOf(9591), null));  //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgetEn", Double.valueOf(8925), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgiftGs", Double.valueOf(5875), null));  //GS
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgetGs", Double.valueOf(7557), null)); //GS
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "FordelSkatteklasse2", Double.valueOf(0), null));

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

    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "MinstefradragProsentInntekt", Double.valueOf(31), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "MinstefradragBelop", Double.valueOf(85050), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "personfradrag", Double.valueOf(56550), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "Skattesats", Double.valueOf(22), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "SatsTrygdeavgift", Double.valueOf(8.2), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgift", Double.valueOf(9591), null));  //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEget", Double.valueOf(8925), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "FordelSaerfradrag", Double.valueOf(0), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "FordelSkatteklasse2", Double.valueOf(0), null)); //EN

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
    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((1.9))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((4.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((13.2))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((16.2))));

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













