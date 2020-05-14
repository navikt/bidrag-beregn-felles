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
    String sjablonnavn = "MinstefradragInntektBelop";

    @Test
    void hentSjablon() {

        ArrayList<Sjablon> sjabloner = new ArrayList<>();
        //Sjablonverdier pr 2019-12-31
        sjabloner.add(new Sjablon("MinstefradragInntektProsent", Double.valueOf(31), null));
        sjabloner.add(new Sjablon("MinstefradragInntektBelop", Double.valueOf(85500), null));
        sjabloner.add(new Sjablon("PersonfradragKlasse1Belop", Double.valueOf(56550), null));
        sjabloner.add(new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(56550), null));
        sjabloner.add(new Sjablon("SkattesatsAlminneligInntektProsent", Double.valueOf(0.22), null));
        sjabloner.add(new Sjablon("TrygdeavgiftProsent", Double.valueOf(0.082), null));
        sjabloner.add(new Sjablon("Skattetrinn1", Double.valueOf(174500), Double.valueOf((0.019))));
        sjabloner.add(new Sjablon("Skattetrinn2", Double.valueOf(245650), Double.valueOf((0.042))));
        sjabloner.add(new Sjablon("Skattetrinn3", Double.valueOf(617500), Double.valueOf((0.132))));
        sjabloner.add(new Sjablon("Skattetrinn4", Double.valueOf(964800), Double.valueOf((0.162))));
        sjabloner.add(new Sjablon("Belop_Boutgift", Double.valueOf(9591), null));  //EN
        sjabloner.add(new Sjablon("BelopUnderholdEget", Double.valueOf(8925), null)); //EN
        sjabloner.add(new Sjablon("UnderholdEgneBarnIHusstandBelop", Double.valueOf(3487), null)); //EN
        sjabloner.add(new Sjablon("FordelSaerfradragBelop", Double.valueOf(12977), null)); //EN

        ArrayList<Inntekt> inntekter = new ArrayList<>();
        inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));

        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
            = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1, SaerfradragKode.HELT, sjabloner);

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("SkattesatsAlminneligInntektProsent").getSjablonVerdi1(),
        Double.valueOf(0.22));

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn1").getSjablonVerdi1(),
        Double.valueOf(174500));

        assertEquals(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("Skattetrinn1").getSjablonVerdi2(),
        Double.valueOf(0.019));

        //System.out.println(beregnBidragsevneGrunnlagPeriodisert.hentSjablon("SkattesatsAlminneligInntektProsent").getVerdi1());



    }
}