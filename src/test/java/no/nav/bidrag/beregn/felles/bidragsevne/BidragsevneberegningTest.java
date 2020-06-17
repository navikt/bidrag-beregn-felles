package no.nav.bidrag.beregn.felles.bidragsevne;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.TestUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BidragsevneBeregningTest")
class BidragsevneberegningTest {

  private List<Sjablon> sjablonListe = TestUtil.byggSjabloner();

  @Test
  void beregn() {

    ArrayList<Inntekt> inntekter = new ArrayList<>();

    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    // Tester beregning med ulike inntekter
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(33050),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(520000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(9767),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert2).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(10410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert3).getResultatBelopEvne());

    // Test på at beregnet bidragsevne blir satt til 0 når evne er negativ
    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(100000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert4
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.MED_ANDRE, 1,
        SaerfradragKode.HELT, sjablonListe);
    assertEquals(Double.valueOf(0),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert4).getResultatBelopEvne());

    // Test at fordel skatteklasse 2 ikke legges til beregnet evne når skatteklasse = 1
    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    sjablonListe.set(0, new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 12000d))));

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert5
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(10410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert5).getResultatBelopEvne());

    // Test at fordel skatteklasse 2 legges til beregnet evne når skatteklasse = 2
    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    sjablonListe.set(0, new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 12000d))));

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert6
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 2, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(11410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert6).getResultatBelopEvne());

    // Test at personfradrag skatteklasse 2 brukes hvis skatteklasse 2 er angitt
    sjablonListe.set(0, new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));

    sjablonListe.set(1, new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(),emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), Double.valueOf(24000d)))));

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert7
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 2, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjablonListe);
    assertEquals(Double.valueOf(9814),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert7).getResultatBelopEvne());


    // Test av halvt særfradrag
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert8
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.HALVT, sjablonListe);
    assertEquals(Double.valueOf(10951),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert8).getResultatBelopEvne());

    // Test av bostatus MED_FLERE
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert9
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.MED_ANDRE, 3,
        SaerfradragKode.HALVT, sjablonListe);
    assertEquals(Double.valueOf(16035),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert9).getResultatBelopEvne());

  }

  @Test
  void beregnMinstefradrag() {

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(200000)));

//    beregnBidragsevneGrunnlagPeriodisert.setSjablonPeriodeListe(sjabloner);
    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjablonListe);
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(62000)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjablonListe);
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert2));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert2))
        .equals(Double.valueOf(85050)));

  }

  @Test
  void beregnSkattetrinnBelop() {

    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));

    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjablonListe);
    //System.out.println(bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(1352+15618+6402+0)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(174600)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjablonListe);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert2))
        .equals(Double.valueOf(2)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(250000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjablonListe);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert3))
        .equals(Double.valueOf(1352+183)));
  }
}
