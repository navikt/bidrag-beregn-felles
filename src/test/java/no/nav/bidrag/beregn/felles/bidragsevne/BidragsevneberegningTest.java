package no.nav.bidrag.beregn.felles.bidragsevne;

import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnholdNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkelNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNy;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BidragsevneBeregningTest")
class BidragsevneberegningTest {
  private List<SjablonNy> sjablonListe = new ArrayList<>();

  @Test
  void beregn() {

    byggSjabloner();

    ArrayList<Sjablon> sjabloner = new ArrayList<>();
    //Sjablonverdier pr 2019-12-31

    sjabloner.add(new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));
    sjabloner.add(new Sjablon("TrygdeavgiftProsent", Double.valueOf(8.2), null));
    sjabloner.add(new Sjablon("UnderholdEgneBarnIHusstandBelop", Double.valueOf(3487), null));
    sjabloner.add(new Sjablon("MinstefradragInntektBelop", Double.valueOf(85050), null));
    sjabloner.add(new Sjablon("MinstefradragInntektProsent", Double.valueOf(31), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse1Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(56550), null));
    sjabloner.add(new Sjablon("FordelSaerfradragBelop", Double.valueOf(12977), null));
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

    BidragsevneberegningImpl bidragsevneberegning = new BidragsevneberegningImpl();

    // Tester beregning med ulike inntekter
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.INGEN, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(33050),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(520000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.INGEN, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(9767),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert2).getResultatBelopEvne());

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(10410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert3).getResultatBelopEvne());

    // Test på at beregnet bidragsevne blir satt til 0 når evne er negativ
    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(100000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert4
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.MED_ANDRE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(0),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert4).getResultatBelopEvne());

    // Test at fordel skatteklasse 2 legges til på beregnet evne
    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000)));
    sjabloner.set(0, new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(12000), null));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert5
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(11410),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert5).getResultatBelopEvne());

    // Test at personfradrag skatteklasse 2 brukes hvis skatteklasse 2 er angitt
    sjabloner.set(0, new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));
    sjabloner.set(6, new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(24000), null));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert6
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 2, BostatusKode.ALENE, 3,
        SaerfradragKode.INGEN, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(9814),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert6).getResultatBelopEvne());

    // Test av halvt særfradrag
    sjabloner.set(0, new Sjablon("FordelSkatteklasse2Belop", Double.valueOf(0), null));
    sjabloner.set(6, new Sjablon("PersonfradragKlasse2Belop", Double.valueOf(56550), null));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert7
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 3,
        SaerfradragKode.HALVT, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(10951),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert7).getResultatBelopEvne());

    // Test av bostatus MED_FLERE
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert8
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.MED_ANDRE, 3,
        SaerfradragKode.HALVT, sjabloner, sjablonListe);
    assertEquals(Double.valueOf(16035),
        bidragsevneberegning.beregn(beregnBidragsevneGrunnlagPeriodisert8).getResultatBelopEvne());

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
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
    System.out.println(bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnMinstefradrag(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(62000)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
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
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
    //System.out.println(bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert));
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert))
        .equals(Double.valueOf(1352+15618+6402+0)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(174600)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert2
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert2))
        .equals(Double.valueOf(2)));

    inntekter.set(0, new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(250000)));
    BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert3
        = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
        SaerfradragKode.HELT, sjabloner, sjablonListe);
    assertTrue((bidragsevneberegning.beregnSkattetrinnBelop(beregnBidragsevneGrunnlagPeriodisert3))
        .equals(Double.valueOf(1352+183)));
  }

  private void byggSjabloner() {

    // Barnetilsyn
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DO")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 355d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 258d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HO")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 579d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HU")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 644d))));

    // Bidragsevne
    sjablonListe
        .add(new SjablonNy(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 9591d),
                new SjablonInnholdNy(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 8925d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 5875d),
                new SjablonInnholdNy(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 7557d))));

    // Forbruksutgifter
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 3661d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 5113d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6099d))));

    // Maks fradrag
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 2083.33d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 3333d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "3")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 4583d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "4")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 5833d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 7083d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "6")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 8333d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "7")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 9583d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "8")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 10833d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 12083d))));

    // Maks tilsyn
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 6214d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 8109d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 9189d))));

    // Samvaersfradrag
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "00"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 1d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 1d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 0d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 219d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 318d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 400d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 460d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 460d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 727d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1052d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(), Arrays
        .asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1323d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1525d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1525d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2082d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2536d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2914d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3196d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3196d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2614d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3184d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3658d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 4012d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 4012d))));

    // Sjablontall
    sjablonListe.add(new SjablonNy(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 31d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 85500d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 8.2d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 12977d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.ORDINAER_BARNETRYGD_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 1054d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));

    sjablonListe.add(new SjablonNy(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 2775d))));

    // Trinnvis skattesats
    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 999550d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 16.2d))));

    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 254500d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 4.2d))));

    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 639750d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 13.2d))));

    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 180800d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.9d))));
  }}













