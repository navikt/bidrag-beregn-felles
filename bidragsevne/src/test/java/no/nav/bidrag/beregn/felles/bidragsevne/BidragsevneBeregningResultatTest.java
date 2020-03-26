package no.nav.bidrag.beregn.felles.bidragsevne;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.resultat.BidragsevneBeregningResultat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("beregning.BidragsevneresultatTest")
class BidragsevneBeregningResultatTest {

    @Test
    void getEvne() {
    }

    @Test
    void testResultatKanMerges() {
        Boolean resultatKanMerges = new BidragsevneBeregningResultat(Double.valueOf(100))
                .kanMergesMed(new BidragsevneBeregningResultat(Double.valueOf(100)));
        assertThat(resultatKanMerges).isTrue();
    }
    @Test
    void testResultatKanIkkeMerges() {
        Boolean resultatKanMerges = new BidragsevneBeregningResultat(Double.valueOf(100))
                .kanMergesMed(new BidragsevneBeregningResultat(Double.valueOf(99)));
        assertThat(resultatKanMerges).isFalse();
    }
}