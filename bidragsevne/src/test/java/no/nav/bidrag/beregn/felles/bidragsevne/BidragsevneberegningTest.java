package no.nav.bidrag.beregn.felles.bidragsevne;

import java.time.LocalDate;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BidragsevneBeregningTest")
class BidragsevneberegningTest {

  @Test
  void beregn() {

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert = new BeregnBidragsevneGrunnlagPeriodisert();
    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31

    sjabloner.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-01-01"), LocalDate.parse("9999-12-31")),
        "fordelSkatteklasse2", Double.valueOf(0), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "satsTrygdeavgift", Double.valueOf(0.082), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "minstefradragBelop", Double.valueOf(85050), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "minstefradragProsentsats", Double.valueOf(0.31), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "personfradragKlasse1", Double.valueOf(56550), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "personfradragKlasse2", Double.valueOf(56550), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "fordelSarfradrag", Double.valueOf(0), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattesats", Double.valueOf(0.22), null));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgiftEn", Double.valueOf(9591), null));  //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgetEn", Double.valueOf(8925), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgiftGs", Double.valueOf(5875), null));  //GS
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgetGs", Double.valueOf(7557), null)); //GS

    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "fordelSkatteklasse2", Double.valueOf(0), null));

    beregnBidragsevneGrunnlagPeriodisert.setSjablonPeriodeListe(sjabloner);
    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(1000000));
    beregnBidragsevneGrunnlagPeriodisert.setSkatteklasse(Integer.valueOf(1));
    beregnBidragsevneGrunnlagPeriodisert.setBorAlene(true);
    beregnBidragsevneGrunnlagPeriodisert.setAntallEgneBarnIHusstand(1);
    assertEquals(Double.valueOf(33050),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getEvne());

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(520000));
    beregnBidragsevneGrunnlagPeriodisert.setSkatteklasse(Integer.valueOf(1));
    beregnBidragsevneGrunnlagPeriodisert.setBorAlene(true);
    beregnBidragsevneGrunnlagPeriodisert.setAntallEgneBarnIHusstand(1);
    assertEquals(Double.valueOf(9767),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getEvne());

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(666000));
    beregnBidragsevneGrunnlagPeriodisert.setSkatteklasse(Integer.valueOf(1));
    beregnBidragsevneGrunnlagPeriodisert.setBorAlene(true);
    beregnBidragsevneGrunnlagPeriodisert.setAntallEgneBarnIHusstand(3);
    assertEquals(Double.valueOf(10410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getEvne());

  }

  @Test
  void beregnMinstefradrag() {

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert = new BeregnBidragsevneGrunnlagPeriodisert();

    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "minstefradragProsentsats", Double.valueOf(0.31), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "minstefradragBelop", Double.valueOf(85050), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "personfradrag", Double.valueOf(56550), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattesats", Double.valueOf(0.22), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "satsTrygdeavgift", Double.valueOf(0.082), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopBoutgift", Double.valueOf(9591), null));  //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEget", Double.valueOf(8925), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "fordelSarfradrag", Double.valueOf(0), null)); //EN
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "fordelSkatteklasse2", Double.valueOf(0), null)); //EN

    beregnBidragsevneGrunnlagPeriodisert.setSjablonPeriodeListe(sjabloner);
    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(200000));
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(62000)));

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(1000000));
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(85050)));

    //assertTrue(bidragsevneberegning.beregnMinstefradrag(bidragsevneberegningGrunnlag).compareTo(Double.valueOf(62000)) = 0);

  }

  @Test
  void beregnSkattetrinnBelop() {
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert = new BeregnBidragsevneGrunnlagPeriodisert();

    ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
    sjabloner.add(new SjablonPeriode((new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06"))),
        "skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(666000));

    beregnBidragsevneGrunnlagPeriodisert.setSjablonPeriodeListe(sjabloner);
    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();
    //System.out.println(bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert));

    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(1352+15618+6402+0)));

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(174600));

    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(2)));

    beregnBidragsevneGrunnlagPeriodisert.setInntektBelop(Double.valueOf(250000));

    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(1352+183)));
  }
}













