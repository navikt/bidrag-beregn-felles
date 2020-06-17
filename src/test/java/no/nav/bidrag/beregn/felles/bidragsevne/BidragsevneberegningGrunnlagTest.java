package no.nav.bidrag.beregn.felles.bidragsevne;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.TestUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test hent av sjablonverdi")
class BidragsevneberegningGrunnlagTest {

  private List<Sjablon> sjablonListe = TestUtil.byggSjabloner();

  @Test
  void hentSjablon() {

    ArrayList<Inntekt> inntekter = new ArrayList<>();
    inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));

    var sjablonVerdi =
        SjablonUtil.hentSjablonverdi(sjablonListe, SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT);
    assertThat(sjablonVerdi).isEqualTo(22d);

    var sortertTrinnvisSkattesatsListe = SjablonUtil
        .hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS);

    assertThat(sortertTrinnvisSkattesatsListe.size()).isEqualTo(4);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getInntektGrense()).isEqualTo(174500d);
    assertThat(sortertTrinnvisSkattesatsListe.get(0).getSats()).isEqualTo(1.9d);

  }
}