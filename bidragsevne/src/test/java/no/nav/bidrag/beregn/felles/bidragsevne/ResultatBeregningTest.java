package no.nav.bidrag.beregn.felles.bidragsevne;

import no.nav.bidrag.beregn.felles.bidragsevne.beregning.ResultatBeregning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("beregning.BidragsevneresultatTest")
class ResultatBeregningTest {

    @Test
    void getEvne() {
    }

    @Test
    void testResultatKanMerges() {
        Boolean resultatKanMerges = new ResultatBeregning(Double.valueOf(100))
                .kanMergesMed(new ResultatBeregning(Double.valueOf(100)));
        assertThat(resultatKanMerges).isTrue();
    }
    @Test
    void testResultatKanIkkeMerges() {
        Boolean resultatKanMerges = new ResultatBeregning(Double.valueOf(100))
                .kanMergesMed(new ResultatBeregning(Double.valueOf(99)));
        assertThat(resultatKanMerges).isFalse();
    }
}