package no.nav.bidrag.beregn.felles.bo;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNoekkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@DisplayName("SjablonTest")
class SjablonTest {

  private List<SjablonNy> sjablonListe = new ArrayList<>();
  private List<SjablonNokkelNy> sjablonNokkelListe = new ArrayList<>();
  private String sjablonNokkelVerdi;

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("Test Barnetilsyn (N:1, eksakt match)")
  void testHentBarnetilsyn() {

    byggSjabloner();

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNoekkelNavn.STOENAD_TYPE.getNavn(), "64"));
    sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNoekkelNavn.TILSYN_TYPE.getNavn(), "DU"));

    var belopBarnetilsyn = SjablonUtil
        .hentBarnetilsyn(sjablonListe, SjablonNavn.BARNETILSYN.getNavn(), sjablonNokkelListe, SjablonInnholdNavn.BARNETILSYN_BELOEP.getNavn());

    assertThat(belopBarnetilsyn).isEqualTo(258d);
  }

  @Test
  @DisplayName("Test Bidragsevne (1:N, eksakt match)")
  void testHentBidragsevne() {

    byggSjabloner();

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNoekkelNavn.BOSTATUS.getNavn(), "GS"));

    var belopBoutgift = SjablonUtil
        .hentBidragsevne(sjablonListe, SjablonNavn.BIDRAGSEVNE.getNavn(), sjablonNokkelListe, SjablonInnholdNavn.BOUTGIFT_BELOEP.getNavn());
    var belopUnderhold = SjablonUtil
        .hentBidragsevne(sjablonListe, SjablonNavn.BIDRAGSEVNE.getNavn(), sjablonNokkelListe, SjablonInnholdNavn.UNDERHOLD_BELOEP.getNavn());

    assertThat(belopBoutgift).isEqualTo(5875d);
    assertThat(belopUnderhold).isEqualTo(7557d);
  }

  @Test
  @DisplayName("Test Forbruksutgifter (1:1, intervall)")
  void testHentForbruksutgifter() {

    byggSjabloner();

    sjablonNokkelVerdi = "12";

    var belopForbrukTot = SjablonUtil.hentForbruksutgifter(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER.getNavn(), sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(6099d);
  }

  @Test
  @DisplayName("Test Maks Fradrag (1:1, intervall)")
  void testHentMaksFradrag() {

    byggSjabloner();

    sjablonNokkelVerdi = "3";

    var belopForbrukTot = SjablonUtil.hentMaksFradrag(sjablonListe, SjablonNavn.MAX_FRADRAG.getNavn(), sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(4583d);
  }

  @Test
  @DisplayName("Test Maks Tilsyn (1:1, intervall)")
  void testHentMaksTilsyn() {

    byggSjabloner();

    sjablonNokkelVerdi = "2";

    var belopForbrukTot = SjablonUtil.hentMaksTilsyn(sjablonListe, SjablonNavn.MAX_TILSYN.getNavn(), sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(8109d);
  }

  @Test
  @DisplayName("Test Samværsfradrag (N:N, eksakt match + intervall)")
  void testHentSamvaersfradrag() {

    byggSjabloner();

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"));
    sjablonNokkelVerdi = "12";

    var antDagerTom = SjablonUtil.hentSamvaersfradrag(sjablonListe, SjablonNavn.SAMVAERSFRADRAG.getNavn(), sjablonNokkelListe,
        SjablonNoekkelNavn.ALDER_TOM.getNavn(), sjablonNokkelVerdi, SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn());
    var antNetterTom = SjablonUtil.hentSamvaersfradrag(sjablonListe, SjablonNavn.SAMVAERSFRADRAG.getNavn(), sjablonNokkelListe,
        SjablonNoekkelNavn.ALDER_TOM.getNavn(), sjablonNokkelVerdi, SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn());
    var belopFradrag = SjablonUtil.hentSamvaersfradrag(sjablonListe, SjablonNavn.SAMVAERSFRADRAG.getNavn(), sjablonNokkelListe,
        SjablonNoekkelNavn.ALDER_TOM.getNavn(), sjablonNokkelVerdi, SjablonInnholdNavn.FRADRAG_BELOEP.getNavn());

    assertThat(antDagerTom).isEqualTo(0d);
    assertThat(antNetterTom).isEqualTo(13d);
    assertThat(belopFradrag).isEqualTo(2914d);
  }

  @Test
  @DisplayName("Test Sjablontall (1:1, eksakt match)")
  void testHentSjablontall() {

    byggSjabloner();

    var sjablonVerdi = SjablonUtil
        .hentSjablontall(sjablonListe, SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOEP.getNavn(), SjablonInnholdNavn.SJABLON_VERDI.getNavn());

    assertThat(sjablonVerdi).isEqualTo(2775d);
  }

  @Test
  @DisplayName("Test Trinnvis Skattesats (0:N, hent alle)")
  void testHentTrinnvisSkattesats() {

    byggSjabloner();

    var sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS.getNavn());

    assertThat(sortertTrinnvisSkattesatsListe.size()).isEqualTo(4);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getInntektGrense()).isEqualTo(180800d);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getSats()).isEqualTo(1.9d);
  }

  @Test
  @DisplayName("Test Trinnvis Skattesats kalkulasjon")
  void testHentTrinnvisSkattesatsKalkulasjon() {

    byggSjabloner();

    assertThat(trinnvisSkattesatsKalkulasjonNy(100000d)).isEqualTo(0d);
    assertThat(trinnvisSkattesatsKalkulasjonNy(200000d)).isEqualTo(365d);
    assertThat(trinnvisSkattesatsKalkulasjonNy(500000d)).isEqualTo(11711d);
    assertThat(trinnvisSkattesatsKalkulasjonNy(800000d)).isEqualTo(38734d);
    assertThat(trinnvisSkattesatsKalkulasjonNy(1000000d)).isEqualTo(65148d);

    assertThat(trinnvisSkattesatsKalkulasjonNy(100000d)).isEqualTo(trinnvisSkattesatsKalkulasjonGammel(100000d));
    assertThat(trinnvisSkattesatsKalkulasjonNy(200000d)).isEqualTo(trinnvisSkattesatsKalkulasjonGammel(200000d));
    assertThat(trinnvisSkattesatsKalkulasjonNy(500000d)).isEqualTo(trinnvisSkattesatsKalkulasjonGammel(500000d));
    assertThat(trinnvisSkattesatsKalkulasjonNy(800000d)).isEqualTo(trinnvisSkattesatsKalkulasjonGammel(800000d));
    assertThat(trinnvisSkattesatsKalkulasjonNy(1000000d)).isEqualTo(trinnvisSkattesatsKalkulasjonGammel(1000000d));
  }

  private double trinnvisSkattesatsKalkulasjonNy(double inntekt) {

    byggSjabloner();

    var sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS.getNavn());

    var samletSkattetrinnBelop = 0d;
    var indeks = 1;

    // Beregn skattetrinnbeløp
    while (indeks < sortertTrinnvisSkattesatsListe.size()) {
      if (inntekt > sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) {
        if (inntekt < sortertTrinnvisSkattesatsListe.get(indeks).getInntektGrense()) {
          samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
              (inntekt - sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) *
                  (sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
        } else {
          samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
              (sortertTrinnvisSkattesatsListe.get(indeks).getInntektGrense() -
                  sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) * (
                  sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
        }
      }
      indeks = indeks + 1;
    }

    if (inntekt > sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense()) {
      samletSkattetrinnBelop = Math.round(samletSkattetrinnBelop + (
          (inntekt - sortertTrinnvisSkattesatsListe.get(indeks - 1).getInntektGrense())
              * (sortertTrinnvisSkattesatsListe.get(indeks - 1).getSats() / 100)));
    }

    return samletSkattetrinnBelop;
  }

  private double trinnvisSkattesatsKalkulasjonGammel(double inntekt) {

    var belopSkattetrinn1 = 180800d;
    var belopSkattetrinn2 = 254500d;
    var belopSkattetrinn3 = 639750d;
    var belopSkattetrinn4 = 999550d;
    var satsSkattetrinn1 = 1.9d;
    var satsSkattetrinn2 = 4.2d;
    var satsSkattetrinn3 = 13.2d;
    var satsSkattetrinn4 = 16.2d;

    var samletSkattetrinnbelop = 0d;

    if (inntekt > belopSkattetrinn1) {
      if (inntekt < belopSkattetrinn2) {
        samletSkattetrinnbelop = Math.round(
            (inntekt - belopSkattetrinn1) * (satsSkattetrinn1 / 100));
      } else {
        samletSkattetrinnbelop = Math
            .round((belopSkattetrinn2 - belopSkattetrinn1) * (satsSkattetrinn1 / 100));
      }
    }

    if (inntekt > belopSkattetrinn2) {
      if (inntekt < belopSkattetrinn3) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (inntekt - belopSkattetrinn2) * (satsSkattetrinn2 / 100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn3 - belopSkattetrinn2) * (satsSkattetrinn2
                / 100)));
      }
    }

    if (inntekt > belopSkattetrinn3) {
      if (inntekt < belopSkattetrinn4) {
        samletSkattetrinnbelop = Math.round(samletSkattetrinnbelop + (
            (inntekt - belopSkattetrinn3) * (satsSkattetrinn3 / 100)));
      } else {
        samletSkattetrinnbelop = Math.round(
            samletSkattetrinnbelop + ((belopSkattetrinn4 - belopSkattetrinn3) * (satsSkattetrinn3
                / 100)));
      }
    }

    if (inntekt > belopSkattetrinn4) {
      samletSkattetrinnbelop = Math.round(
          samletSkattetrinnbelop + ((inntekt - belopSkattetrinn4)
              * (satsSkattetrinn4 / 100)));
    }

    return samletSkattetrinnbelop;
  }


  private void byggSjabloner() {

    // Barnetilsyn
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.STOENAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNoekkelNavn.TILSYN_TYPE.getNavn(), "DO")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOEP.getNavn(), 355d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.STOENAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNoekkelNavn.TILSYN_TYPE.getNavn(), "DU")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOEP.getNavn(), 258d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.STOENAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNoekkelNavn.TILSYN_TYPE.getNavn(), "HO")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOEP.getNavn(), 579d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.STOENAD_TYPE.getNavn(), "64"),
            new SjablonNokkelNy(SjablonNoekkelNavn.TILSYN_TYPE.getNavn(), "HU")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BARNETILSYN_BELOEP.getNavn(), 644d))));

    // Bidragsevne
    sjablonListe
        .add(new SjablonNy(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BOUTGIFT_BELOEP.getNavn(), 9591d),
                new SjablonInnholdNy(SjablonInnholdNavn.UNDERHOLD_BELOEP.getNavn(), 8925d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.BIDRAGSEVNE.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.BOUTGIFT_BELOEP.getNavn(), 5875d),
                new SjablonInnholdNy(SjablonInnholdNavn.UNDERHOLD_BELOEP.getNavn(), 7557d))));

    // Forbruksutgifter
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "18")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOEP.getNavn(), 6985d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOEP.getNavn(), 3661d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOEP.getNavn(), 6985d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "10")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOEP.getNavn(), 5113d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.FORBRUKSUTGIFTER.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "14")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.FORBRUK_TOTAL_BELOEP.getNavn(), 6099d))));

    // Maks fradrag
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 2083.33d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 3333d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "3")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 4583d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "4")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 5833d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "5")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 7083d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "6")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 8333d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "7")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 9583d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "8")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 10833d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_FRADRAG.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_FRADRAG_BELOEP.getNavn(), 12083d))));

    // Maks tilsyn
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_TILSYN_BELOEP.getNavn(), 6214d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_TILSYN_BELOEP.getNavn(), 8109d))));
    sjablonListe
        .add(new SjablonNy(SjablonNavn.MAX_TILSYN.getNavn(), Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.MAX_TILSYN_BELOEP.getNavn(), 9189d))));

    // Samvaersfradrag
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "00"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 1d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 1d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 0d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 219d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 318d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 400d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 460d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 3d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 460d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 727d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 1052d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(), Arrays
        .asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 1323d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 1525d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 8d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 1525d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 2082d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 2536d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 2914d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 3196d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 13d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 3196d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 2614d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 3184d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 3658d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 4012d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkelNy(SjablonNoekkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkelNy(SjablonNoekkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), 0d),
            new SjablonInnholdNy(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), 15d),
            new SjablonInnholdNy(SjablonInnholdNavn.FRADRAG_BELOEP.getNavn(), 4012d))));

    // Sjablontall
    sjablonListe.add(new SjablonNy(SjablonTallNavn.ORDINAER_BARNETRYGD_BELOEP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 1054d))));
    sjablonListe.add(new SjablonNy(SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));
    sjablonListe.add(new SjablonNy(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOEP.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 2775d))));

    // Trinnvis skattesats
    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOEP.getNavn(), 999550d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 16.2d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOEP.getNavn(), 254500d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 4.2d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOEP.getNavn(), 639750d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 13.2d))));
    sjablonListe.add(new SjablonNy(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnholdNy(SjablonInnholdNavn.INNTEKTSGRENSE_BELOEP.getNavn(), 180800d),
            new SjablonInnholdNy(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.9d))));
  }
}