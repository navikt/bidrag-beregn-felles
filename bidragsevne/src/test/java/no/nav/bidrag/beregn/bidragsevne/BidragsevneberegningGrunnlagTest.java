package no.nav.bidrag.beregn.bidragsevne;

import java.time.LocalDate;
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.bidragsevne.bo.BostatusKode;
import no.nav.bidrag.beregn.bidragsevne.bo.SaerfradragKode;
import no.nav.bidrag.beregn.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.bidragsevne.periode.Periode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

@DisplayName("Test hent av sjablonverdi")

class BidragsevneberegningGrunnlagTest {
    String sjablonnavn = "MinstefradragBelop";

    @Test
    void hentSjablon() {


        ArrayList<SjablonPeriode> sjabloner = new ArrayList<>();
        //Sjablonverdier pr 2019-12-31
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "MinstefradragProsentInntekt", Double.valueOf(31), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "MinstefradragBelop", Double.valueOf(85500), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "personfradrag", Double.valueOf(56550), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "Skattesats", Double.valueOf(0.22), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "SatsTrygdeavgift", Double.valueOf(0.082), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "Skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
             "belop_Boutgift", Double.valueOf(9591), null));  //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "belopUnderholdEget", Double.valueOf(8925), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null)); //EN
        sjabloner.add(new SjablonPeriode(new Periode(LocalDate.parse("2017-06-06"), LocalDate.parse("2020-06-06")),
            "FordelSaerfradrag", Double.valueOf(12977), null)); //EN

        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
            = new BeregnBidragsevneGrunnlagPeriodisert(Double.valueOf(1000000), 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);

        Assertions
            .assertTrue(
                beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattesats").getSjablonVerdi1()
                    .equals(Double.valueOf(0.22)));
        Assertions
            .assertTrue(
                beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1").getSjablonVerdi1()
                    .equals(Double.valueOf(174500)));
        Assertions
            .assertTrue(
                beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1").getSjablonVerdi2()
                    .equals(Double.valueOf(0.019)));
        //System.out.println(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattesats").getVerdi1());



    }
}