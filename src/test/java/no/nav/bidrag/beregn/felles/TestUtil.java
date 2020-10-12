package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import no.nav.bidrag.beregn.felles.inntekt.InntektGrunnlag;

public class TestUtil {

  public static List<Sjablon> byggSjabloner() {

    var sjablonListe = new ArrayList<Sjablon>();

    // Barnetilsyn
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DO")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 355d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 258d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HO")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 579d))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HU")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), 644d))));

    // Bidragsevne
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 9591d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 8925d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 5875d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 7557d))));

    // Forbruksutgifter
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 3661d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6985d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 5113d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), 6099d))));

    // Maks fradrag
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 2083.33d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 3333d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "3")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 4583d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "4")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 5833d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "5")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 7083d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "6")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 8333d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "7")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 9583d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "8")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 10833d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), 12083d))));

    // Maks tilsyn
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 6214d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 8109d))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), 9189d))));

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
    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_BARNETRYGD_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 1054d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 2775d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 31d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 85050d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 56550d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 8.2d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 12977d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 0d))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 3487d))));

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

    return sjablonListe;
  }


  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoLikGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.INNTEKTSOPPL_ARBEIDSGIVER, 200000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, 150000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.ALOYSE, 250000d));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoUlikGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.INNTEKTSOPPL_ARBEIDSGIVER, 200000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, 150000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.KAPITALINNTEKT_SKE, 250000d));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoUtenGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.BARNETRYGD_MAN_VURDERING, 200000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, 150000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.OVERGANGSSTONAD, 250000d));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeDelvisOverlappSammeGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2020-01-01"), LocalDate.MAX),
        InntektType.ALOYSE, 250000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.INNTEKTSOPPL_ARBEIDSGIVER, 150000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
        InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT, 300000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
        InntektType.KAPITALINNTEKT_EGNE_OPPL, 100000d));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.MAX),
        InntektType.INNTEKTSOPPL_ARBEIDSGIVER, 200000d));

    return inntektGrunnlagListe;
  }
}
