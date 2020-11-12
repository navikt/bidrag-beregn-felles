package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;

import java.math.BigDecimal;
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
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), BigDecimal.valueOf(355)))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "DU")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), BigDecimal.valueOf(258)))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HO")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), BigDecimal.valueOf(579)))));
    sjablonListe.add(new Sjablon(SjablonNavn.BARNETILSYN.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.getNavn(), "64"),
            new SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.getNavn(), "HU")),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.getNavn(), BigDecimal.valueOf(644)))));

    // Bidragsevne
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), BigDecimal.valueOf(9591)),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), BigDecimal.valueOf(8925)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), BigDecimal.valueOf(5875)),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), BigDecimal.valueOf(7557)))));

    // Forbruksutgifter
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), BigDecimal.valueOf(6985)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), BigDecimal.valueOf(3661)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), BigDecimal.valueOf(6985)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), BigDecimal.valueOf(5113)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.FORBRUKSUTGIFTER.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.getNavn(), BigDecimal.valueOf(6099)))));

    // Maks fradrag
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(2083.33)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(3333)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "3")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(4583)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "4")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(5833)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "5")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(7083)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "6")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(8333)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "7")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(9583)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "8")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(10833)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_FRADRAG.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(12083)))));

    // Maks tilsyn
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "1")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), BigDecimal.valueOf(6214)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "2")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), BigDecimal.valueOf(8109)))));
    sjablonListe
        .add(new Sjablon(SjablonNavn.MAKS_TILSYN.getNavn(),
            Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.getNavn(), "99")),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.getNavn(), BigDecimal.valueOf(9189)))));

    // Samvaersfradrag
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "00"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(1)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(1)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.ZERO))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(219)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(318)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(400)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(460)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "01"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(3)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(460)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(8)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(727)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(8)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(1052)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(), Arrays
        .asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(8)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(1323)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(8)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(1525)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "02"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(8)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(1525)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(13)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(2082)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(13)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(2536)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(13)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(2914)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(13)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(3196)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "03"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(13)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(3196)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "5")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(15)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(2614)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "10")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(15)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(3184)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "14")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(15)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(3658)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "18")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(15)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(4012)))));
    sjablonListe.add(new Sjablon(SjablonNavn.SAMVAERSFRADRAG.getNavn(),
        Arrays.asList(new SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.getNavn(), "04"),
            new SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.getNavn(), "99")),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.getNavn(), BigDecimal.ZERO),
            new SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.getNavn(), BigDecimal.valueOf(15)),
            new SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.getNavn(), BigDecimal.valueOf(4012)))));

    // Sjablontall
    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_BARNETRYGD_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(1054)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.ZERO))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(2775)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(31)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(85050)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(56550)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(56550)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(22)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(8.2)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(12977)))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.ZERO))));
    sjablonListe.add(new Sjablon(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(), emptyList(),
        Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), BigDecimal.valueOf(3487)))));

    // Trinnvis skattesats
    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), BigDecimal.valueOf(964800)),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), BigDecimal.valueOf(16.2)))));
    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), BigDecimal.valueOf(245650)),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), BigDecimal.valueOf(4.2)))));
    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), BigDecimal.valueOf(617500)),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), BigDecimal.valueOf(13.2)))));
    sjablonListe.add(new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
        Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), BigDecimal.valueOf(174500)),
            new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), BigDecimal.valueOf(1.9)))));

    return sjablonListe;
  }


  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoLikGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER, BigDecimal.valueOf(200000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, BigDecimal.valueOf(150000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.ALOYSE, BigDecimal.valueOf(250000)));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoUlikGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER, BigDecimal.valueOf(200000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, BigDecimal.valueOf(150000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.KAPITALINNTEKT_SKE, BigDecimal.valueOf(250000)));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeMedLikFraDatoUtenGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        InntektType.BARNETRYGD_MANUELL_VURDERING, BigDecimal.valueOf(200000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.ATTFORING_AAP, BigDecimal.valueOf(150000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        InntektType.OVERGANGSSTONAD, BigDecimal.valueOf(250000)));

    return inntektGrunnlagListe;
  }

  public static List<InntektGrunnlag> byggInntektGrunnlagListeDelvisOverlappSammeGruppe() {
    var inntektGrunnlagListe = new ArrayList<InntektGrunnlag>();

    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2020-01-01"), LocalDate.MAX),
        InntektType.ALOYSE, BigDecimal.valueOf(250000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
        InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER, BigDecimal.valueOf(150000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
        InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT, BigDecimal.valueOf(300000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
        InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER, BigDecimal.valueOf(100000)));
    inntektGrunnlagListe.add(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.MAX),
        InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER, BigDecimal.valueOf(200000)));

    return inntektGrunnlagListe;
  }
}
