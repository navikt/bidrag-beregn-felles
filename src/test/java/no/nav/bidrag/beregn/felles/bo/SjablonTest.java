package no.nav.bidrag.beregn.felles.bo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.TestUtil;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SjablonTest")
class SjablonTest {

  private final List<Sjablon> sjablonListe = TestUtil.byggSjabloner();
  private final List<SjablonNokkel> sjablonNokkelListe = new ArrayList<>();
  private Integer sjablonNokkelVerdiInteger;

  @Test
  @DisplayName("Test Barnetilsyn (N:1, eksakt match)")
  void testHentBarnetilsyn() {

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"));
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU"));

    var belopBarnetilsyn = SjablonUtil
        .hentSjablonverdi(sjablonListe, SjablonNavn.BARNETILSYN, sjablonNokkelListe, SjablonInnholdNavn.BARNETILSYN_BELOP);

    assertThat(belopBarnetilsyn).isEqualTo(BigDecimal.valueOf(258));
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

    assertThat(belopBoutgift).isEqualTo(BigDecimal.valueOf(5875));
    assertThat(belopUnderhold).isEqualTo(BigDecimal.valueOf(7557));
  }

  @Test
  @DisplayName("Test Forbruksutgifter (1:1, intervall)")
  void testHentForbruksutgifter() {

    sjablonNokkelVerdiInteger = 3;
    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(3661));

    sjablonNokkelVerdiInteger = 5;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(3661));

    sjablonNokkelVerdiInteger = 7;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(5113));

    sjablonNokkelVerdiInteger = 10;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(5113));

    sjablonNokkelVerdiInteger = 12;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6099));

    sjablonNokkelVerdiInteger = 99;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6985));
  }

  @Test
  @DisplayName("Test Maks Fradrag (1:1, intervall)")
  void testHentMaksFradrag() {

    sjablonNokkelVerdiInteger = 0;
    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33));

    sjablonNokkelVerdiInteger = 1;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33));

    sjablonNokkelVerdiInteger = 3;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(4583));

    sjablonNokkelVerdiInteger = 90;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(12083));

    sjablonNokkelVerdiInteger = 99;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(12083));
  }

  @Test
  @DisplayName("Test Maks Tilsyn (1:1, intervall)")
  void testHentMaksTilsyn() {

    sjablonNokkelVerdiInteger = 0;
    var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6214));

    sjablonNokkelVerdiInteger = 1;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6214));

    sjablonNokkelVerdiInteger = 2;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(8109));

    sjablonNokkelVerdiInteger = 90;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(9189));

    sjablonNokkelVerdiInteger = 99;
    belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger);
    assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(9189));
  }

  @Test
  @DisplayName("Test Samværsfradrag (N:N, eksakt match + intervall)")
  void testHentSamvaersfradrag() {

    sjablonNokkelListe.clear();
    sjablonNokkelListe.add(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"));

    sjablonNokkelVerdiInteger = 3;
    var antDagerTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_DAGER_TOM);
    var antNetterTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_NETTER_TOM);
    var belopFradrag = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.FRADRAG_BELOP);

    assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO);
    assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13));
    assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2082));

    sjablonNokkelVerdiInteger = 5;
    antDagerTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_DAGER_TOM);
    antNetterTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_NETTER_TOM);
    belopFradrag = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.FRADRAG_BELOP);

    assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO);
    assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13));
    assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2082));

    sjablonNokkelVerdiInteger = 12;
    antDagerTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_DAGER_TOM);
    antNetterTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_NETTER_TOM);
    belopFradrag = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.FRADRAG_BELOP);

    assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO);
    assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13));
    assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2914));

    sjablonNokkelVerdiInteger = 99;
    antDagerTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_DAGER_TOM);
    antNetterTom = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.ANTALL_NETTER_TOM);
    belopFradrag = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.SAMVAERSFRADRAG, sjablonNokkelListe, SjablonNokkelNavn.ALDER_TOM,
        sjablonNokkelVerdiInteger, SjablonInnholdNavn.FRADRAG_BELOP);

    assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO);
    assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13));
    assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(3196));
  }

  @Test
  @DisplayName("Test Sjablontall (1:1, eksakt match)")
  void testHentSjablontall() {

    var sjablonVerdi = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP);

    assertThat(sjablonVerdi).isEqualTo(BigDecimal.valueOf(2775));
  }

  @Test
  @DisplayName("Test Trinnvis Skattesats (0:N, hent alle)")
  void testHentTrinnvisSkattesats() {

    var sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS);

    assertThat(sortertTrinnvisSkattesatsListe.size()).isEqualTo(4);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getInntektGrense()).isEqualTo(BigDecimal.valueOf(174500));
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getSats()).isEqualTo(BigDecimal.valueOf(1.9));
  }
}