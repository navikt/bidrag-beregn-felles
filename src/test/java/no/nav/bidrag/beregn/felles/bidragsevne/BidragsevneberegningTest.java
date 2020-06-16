package no.nav.bidrag.beregn.felles.bidragsevne;

import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.BidragsevneberegningImpl;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
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
  private List<Sjablon> sjablonListe = new ArrayList<>();

  @Test
  void beregn() {

    byggSjabloner();

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

    byggSjabloner();

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

    byggSjabloner();

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

  private void byggSjabloner() {

    // Barnetilsyn
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DO")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 355d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 258d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HO")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 579d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HU")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 644d))));

    // Bidragsevne
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 9591d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 8925d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 5875d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 7557d))));

    // Forbruksutgifter
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 3661d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 5113d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6099d))));

    // Maks fradrag
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 2083.33d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 3333d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "3")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 4583d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "4")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 5833d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 7083d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "6")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 8333d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "7")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 9583d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "8")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 10833d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 12083d))));

    // Maks tilsyn
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 6214d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 8109d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(), Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 9189d))));

    // Samvaersfradrag
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "00"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 1d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 1d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 0d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 219d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 318d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 400d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 460d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 460d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 727d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1052d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(), Arrays
        .asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1323d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1525d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 1525d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2082d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2536d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2914d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3196d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3196d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 2614d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3184d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 3658d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 4012d))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), 4012d))));

    // Sjablontall

    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 8.2d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 3487d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 85050d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 31d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 12977d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_BARNETRYGD_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 1054d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));

    sjablonListe.add(new Sjablon(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 2775d))));


    // Trinnvis skattesats
    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 964800d),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 16.2d))));

    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 245650d),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 4.2d))));

    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 617500d),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 13.2d))));

    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 174500d),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.9d))));
  }}













