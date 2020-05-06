package no.nav.bidrag.beregn.felles.bidragsevne;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Sjablon;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test hent av sjablonverdi")

class BidragsevneberegningGrunnlagTest {
    String sjablonnavn = "MinstefradragBelop";

    @Test
    void hentSjablon() {

        ArrayList<Sjablon> sjabloner = new ArrayList<>();
        //Sjablonverdier pr 2019-12-31
        sjabloner.add(new Sjablon("MinstefradragProsentInntekt", Double.valueOf(31), null)); //EN
        sjabloner.add(new Sjablon("MinstefradragBelop", Double.valueOf(85500), null)); //EN
        sjabloner.add(new Sjablon("personfradrag", Double.valueOf(56550), null)); //EN
        sjabloner.add(new Sjablon("Skattesats", Double.valueOf(0.22), null)); //EN
        sjabloner.add(new Sjablon("SatsTrygdeavgift", Double.valueOf(0.082), null)); //EN
        sjabloner.add(new Sjablon("skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
        sjabloner.add(new Sjablon("skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
        sjabloner.add(new Sjablon("skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
        sjabloner.add(new Sjablon("Skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
        sjabloner.add(new Sjablon("belop_Boutgift", Double.valueOf(9591), null));  //EN
        sjabloner.add(new Sjablon("belopUnderholdEget", Double.valueOf(8925), null)); //EN
        sjabloner.add(new Sjablon("belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null)); //EN
        sjabloner.add(new Sjablon("FordelSaerfradrag", Double.valueOf(12977), null)); //EN

        ArrayList<Inntekt> inntekter = new ArrayList<>();
        inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));

        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
            = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattesats").getSjablonVerdi1(),
        Double.valueOf(0.22));

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1").getSjablonVerdi1(),
        Double.valueOf(174500));

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("skattetrinn1").getSjablonVerdi2(),
        Double.valueOf(0.019));

        //System.out.println(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattesats").getVerdi1());



    }
}