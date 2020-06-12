package no.nav.bidrag.beregn.felles.bidragsevne;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.SjablonUtil;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnholdNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkelNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNy;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test hent av sjablonverdi")

class BidragsevneberegningGrunnlagTest {
    String sjablonnavn = "MinstefradragInntektBelop";

    private List<SjablonNy> sjablonListe = new ArrayList<>();

    @Test
    void hentSjablon() {

        byggSjabloner();

        ArrayList<Sjablon> sjabloner = new ArrayList<>();

        ArrayList<Inntekt> inntekter = new ArrayList<>();
        inntekter.add(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(1000000)));

        BeregnBidragsevneGrunnlagPeriodisert beregnBidragsevneGrunnlagPeriodisert
            = new BeregnBidragsevneGrunnlagPeriodisert(inntekter, 1, BostatusKode.ALENE, 1,
            SaerfradragKode.HELT, sjabloner, sjablonListe);

        var sjablonVerdi =
            SjablonUtil.hentSjablontall(sjablonListe, SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(),
            SjablonInnholdNavn.SJABLON_VERDI.getNavn());
        assertThat(sjablonVerdi).isEqualTo(22d);


        var sortertTrinnvisSkattesatsListe = SjablonUtil
            .hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS.getNavn());

        assertThat(sortertTrinnvisSkattesatsListe.size()).isEqualTo(4);
        assertThat(sortertTrinnvisSkattesatsListe.get(0).getInntektGrense()).isEqualTo(180800d);
        assertThat(sortertTrinnvisSkattesatsListe.get(0).getSats()).isEqualTo(1.9d);

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
    }
}