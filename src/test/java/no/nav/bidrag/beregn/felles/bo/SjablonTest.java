package no.nav.bidrag.beregn.felles.bo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.TestUtil;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@DisplayName("SjablonTest")
class SjablonTest {

  private List<Sjablon> sjablonListe = TestUtil.byggSjabloner();
  private List<SjablonNokkel> sjablonNokkelListe = new ArrayList<>();
  private String sjablonNokkelVerdi;

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @DisplayName("Test Barnetilsyn (N:1, eksakt match)")
  void testHentBarnetilsyn() {

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"));
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU"));

    var belopBarnetilsyn = SjablonUtil
        .hentSjablonverdi(sjablonListe, SjablonNavn.BARNETILSYN, sjablonNokkelListe, SjablonInnholdNavn.BARNETILSYN_BELOP);

    assertThat(belopBarnetilsyn).isEqualTo(258d);
  }

  @Test
  @DisplayName("Test Bidragsevne (1:N, eksakt match)")
  void testHentBidragsevne() {

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS"));

    var belopBoutgift = SjablonUtil
        .hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNokkelListe, SjablonInnholdNavn.BOUTGIFT_BELOP);
    var belopUnderhold = SjablonUtil
        .hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNokkelListe, SjablonInnholdNavn.UNDERHOLD_BELOP);

    assertThat(belopBoutgift).isEqualTo(5875d);
    assertThat(belopUnderhold).isEqualTo(7557d);
  }

  @Test
  @DisplayName("Test Forbruksutgifter (1:1, intervall)")
  void testHentForbruksutgifter() {

    sjablonNokkelVerdi = "12";

    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(6099d);
  }

  @Test
  @DisplayName("Test Maks Fradrag (1:1, intervall)")
  void testHentMaksFradrag() {

    sjablonNokkelVerdi = "3";

    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(4583d);
  }

  @Test
  @DisplayName("Test Maks Tilsyn (1:1, intervall)")
  void testHentMaksTilsyn() {

    sjablonNokkelVerdi = "2";

    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdi);

    assertThat(belopForbrukTot).isEqualTo(8109d);
  }

  @Test
  @DisplayName("Test Samværsfradrag (N:N, eksakt match + intervall)")
  void testHentSamvaersfradrag() {

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"));
    sjablonNokkelVerdi = "12";

    var antDagerTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdi, SjablonInnholdNavn.ANTALL_DAGER_TOM);
    var antNetterTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdi, SjablonInnholdNavn.ANTALL_NETTER_TOM);
    var belopFradrag = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdi, SjablonInnholdNavn.FRADRAG_BELOP);

    assertThat(antDagerTom).isEqualTo(0d);
    assertThat(antNetterTom).isEqualTo(13d);
    assertThat(belopFradrag).isEqualTo(2914d);
  }

  @Test
  @DisplayName("Test Sjablontall (1:1, eksakt match)")
  void testHentSjablontall() {

    var sjablonVerdi = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP);

    assertThat(sjablonVerdi).isEqualTo(2775d);
  }

  @Test
  @DisplayName("Test Trinnvis Skattesats (0:N, hent alle)")
  void testHentTrinnvisSkattesats() {

    var sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS);

    assertThat(sortertTrinnvisSkattesatsListe.size()).isEqualTo(4);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getInntektGrense()).isEqualTo(174500d);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getSats()).isEqualTo(1.9d);
  }
}