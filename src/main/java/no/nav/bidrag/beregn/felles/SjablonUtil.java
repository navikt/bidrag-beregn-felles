package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import no.nav.bidrag.beregn.felles.bo.SjablonInnholdNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkelNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNy;
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkelNy;
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkelSingelInnholdNy;
import no.nav.bidrag.beregn.felles.bo.TrinnvisSkattesatsNy;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;

public class SjablonUtil {

  // Henter verdier fra sjablon Barnetilsyn (N:1, eksakt match)
  public static double hentBarnetilsyn(List<SjablonNy> sjablonListe, String sjablonNavn, List<SjablonNokkelNy> sjablonNokkelListe,
      String sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNokkel(filtrerPaaSjablonNavn(sjablonListe, sjablonNavn), sjablonNokkelListe);
    var sjablonInnholdListe = hentSjablonInnholdListe(filtrertSjablonListe);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter verdier fra sjablon Bidragsevne (1:N, eksakt match)
  public static double hentBidragsevne(List<SjablonNy> sjablonListe, String sjablonNavn, List<SjablonNokkelNy> sjablonNokkelListe,
      String sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNokkel(filtrerPaaSjablonNavn(sjablonListe, sjablonNavn), sjablonNokkelListe);
    var sjablonInnholdListe = hentSjablonInnholdListe(filtrertSjablonListe);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter verdier fra sjablon Forbruksutgifter (1:1, intervall)
  public static double hentForbruksutgifter(List<SjablonNy> sjablonListe, String sjablonNavn, String sjablonNokkelVerdi) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn);
    var sortertSjablonSingelNokkelSingelInnholdListe = mapTilSingelListeNokkelInnhold(filtrertSjablonListe);
    return hentSjablonInnholdVerdiIntervall(sortertSjablonSingelNokkelSingelInnholdListe, sjablonNokkelVerdi);
  }

  // Henter verdier fra sjablon MaksFradrag 1:1, intervall)
  public static double hentMaksFradrag(List<SjablonNy> sjablonListe, String sjablonNavn, String sjablonNokkelVerdi) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn);
    var sortertSjablonSingelNokkelSingelInnholdListe = mapTilSingelListeNokkelInnhold(filtrertSjablonListe);
    return hentSjablonInnholdVerdiIntervall(sortertSjablonSingelNokkelSingelInnholdListe, sjablonNokkelVerdi);
  }

  // Henter verdier fra sjablon MaksTilsyn (1:1, intervall)
  public static double hentMaksTilsyn(List<SjablonNy> sjablonListe, String sjablonNavn, String sjablonNokkelVerdi) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn);
    var sortertSjablonSingelNokkelSingelInnholdListe = mapTilSingelListeNokkelInnhold(filtrertSjablonListe);
    return hentSjablonInnholdVerdiIntervall(sortertSjablonSingelNokkelSingelInnholdListe, sjablonNokkelVerdi);
  }

  // Henter verdier fra sjablon Samværsfradrag (N:N, eksakt match + intervall)
  public static double hentSamvaersfradrag(List<SjablonNy> sjablonListe, String sjablonNavn, List<SjablonNokkelNy> sjablonNokkelListe,
      String sjablonNokkelNavn, String sjablonNokkelVerdi, String sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNokkel(filtrerPaaSjablonNavn(sjablonListe, sjablonNavn), sjablonNokkelListe);
    var sortertSjablonSingelNokkelListe = mapTilSingelListeNokkel(filtrertSjablonListe, sjablonNokkelNavn);
    var sjablonInnholdListe = SjablonUtil.hentSjablonInnholdVerdiListeIntervall(sortertSjablonSingelNokkelListe, sjablonNokkelVerdi);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter verdier fra sjablon Sjablontall (1:1, eksakt match)
  public static double hentSjablontall(List<SjablonNy> sjablonListe, String sjablonNavn, String sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn);
    var sjablonInnholdListe = hentSjablonInnholdListe(filtrertSjablonListe);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter liste med verdier fra sjablon TrinnvisSkattesats (0:N, hent alle)
  public static List<TrinnvisSkattesatsNy> hentTrinnvisSkattesats(List<SjablonNy> sjablonListe, String sjablonNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn);
    var sjablonInnholdListe = hentSjablonInnholdListe(filtrertSjablonListe);
    var inntektGrenseListe = hentSjablonInnholdVerdiListe(sjablonInnholdListe, SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn());
    var satsListe = hentSjablonInnholdVerdiListe(sjablonInnholdListe, SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn());

    var indeks = 0;
    var trinnvisSkattesatsListe = new ArrayList<TrinnvisSkattesatsNy>();
    while (indeks < inntektGrenseListe.size()) {
      trinnvisSkattesatsListe.add(new TrinnvisSkattesatsNy(inntektGrenseListe.get(indeks), satsListe.get(indeks)));
      indeks = indeks + 1;
    }

    return trinnvisSkattesatsListe.stream().sorted(comparing(TrinnvisSkattesatsNy::getInntektGrense)).collect(toList());
  }

  // Filtrerer sjablonListe på sjablonNavn og returnerer ny liste.
  // Brukes av alle typer sjabloner.
  private static List<SjablonNy> filtrerPaaSjablonNavn(List<SjablonNy> sjablonListe, String sjablonNavn) {
    return sjablonListe
        .stream()
        .filter(sjablon -> sjablon.getSjablonNavn().equals(sjablonNavn))
        .collect(toList());
  }

  // Filtrerer sjablonListe på sjablonNokkelListe og returnerer en ny liste.
  // Brukes av sjabloner som har eksakt match på nøkkel (Barnetilsyn, Bidragsevne, Samværsfradrag).
  private static List<SjablonNy> filtrerPaaSjablonNokkel(List<SjablonNy> sjablonListe, List<SjablonNokkelNy> sjablonNokkelListe) {
    var sjablonStream = sjablonListe.stream();
    for (SjablonNokkelNy sjablonNokkel : sjablonNokkelListe) {
      sjablonStream = filtrerPaaSjablonNokkel(sjablonStream, sjablonNokkel);
    }
    return sjablonStream.collect(toList());
  }

  // Tar inn en sjablonListe og returnerer en sjablonInnholdListe.
  // Brukes av Bidragsevne, Sjablontall, TrinnvisSkattesats.
  public static List<SjablonInnholdNy> hentSjablonInnholdListe(List<SjablonNy> sjablonListe) {
    return sjablonListe.stream().map(SjablonNy::getSjablonInnholdListe).flatMap(Collection::stream).collect(toList());
  }

  // Filtrerer sjablonStream på sjablonNokkelInput og returnerer en ny stream.
  // Intern bruk.
  private static Stream<SjablonNy> filtrerPaaSjablonNokkel(Stream<SjablonNy> sjablonStream, SjablonNokkelNy sjablonNokkelInput) {
    return sjablonStream
        .filter(sjablon -> sjablon.getSjablonNokkelListe()
            .stream()
            .anyMatch(sjablonNokkel -> (sjablonNokkel.getSjablonNokkelNavn().equals(sjablonNokkelInput.getSjablonNokkelNavn())) &&
                (sjablonNokkel.getSjablonNokkelVerdi().equals(sjablonNokkelInput.getSjablonNokkelVerdi()))));
  }

  // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og singel innholdverdi (1:1). Returnerer en ny liste sortert
  // på nøkkelverdi.
  // Brukes av sjabloner som har ett nøkkelobjekt og ett innholdobjekt (Forbruksutgifter, MaxFradrag, MaxTilsyn).
  public static List<SjablonSingelNokkelSingelInnholdNy> mapTilSingelListeNokkelInnhold(List<SjablonNy> filtrertSjablonListe) {
    return filtrertSjablonListe
        .stream()
        .map(sjablon -> new SjablonSingelNokkelSingelInnholdNy(sjablon.getSjablonNavn(),
            sjablon.getSjablonNokkelListe().stream().map(sjablonNokkel -> sjablonNokkel.getSjablonNokkelVerdi()).findFirst().orElse(" "),
            sjablon.getSjablonInnholdListe().stream().map(sjablonInnhold -> sjablonInnhold.getSjablonInnholdVerdi()).findFirst().orElse(0d)))
        .sorted(comparing(SjablonSingelNokkelSingelInnholdNy::getSjablonNokkelVerdi))
        .collect(toList());
  }

  // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og liste med innholdverdier (1:N). Returnerer en ny liste
  // sortert på nøkkelverdi.
  // Brukes av sjabloner som har ett nøkkelobjekt med eksakt match og flere innholdobjekter (Samværsfradrag).
  private static List<SjablonSingelNokkelNy> mapTilSingelListeNokkel(List<SjablonNy> filtrertSjablonListe, String nokkelNavn) {
    return filtrertSjablonListe
        .stream()
        .map(sjablon -> new SjablonSingelNokkelNy(sjablon.getSjablonNavn(),
            sjablon.getSjablonNokkelListe()
                .stream()
                .filter(sjablonNokkel -> sjablonNokkel.getSjablonNokkelNavn().equals(nokkelNavn))
                .map(SjablonNokkelNy::getSjablonNokkelVerdi)
                .findFirst()
                .orElse(" "),
            sjablon.getSjablonInnholdListe()))
        .sorted(comparing(SjablonSingelNokkelNy::getSjablonNokkelVerdi))
        .collect(toList());
  }

  // Filtrerer sjablonInnholdListe på sjablonInnholdNavn (eksakt match) og returnerer matchende verdi (0d hvis sjablonInnholdNavn mot formodning ikke
  // finnes).
  // Brukes av sjabloner som skal hente eksakt verdi (Barnetilsyn, Bidragsevne, Sjablontall, Samværsfradrag).
  private static Double hentSjablonInnholdVerdiEksakt(List<SjablonInnholdNy> sjablonInnholdListe, String sjablonInnholdNavn) {
    return sjablonInnholdListe
        .stream()
        .filter(sjablonInnhold -> sjablonInnhold.getSjablonInnholdNavn().equals(sjablonInnholdNavn))
        .map(SjablonInnholdNy::getSjablonInnholdVerdi)
        .findFirst()
        .orElse(0d);
  }

  // Filtrerer sortertSjablonSingelNokkelSingelInnholdListe på nøkkel-verdi > sjablonNokkel og returnerer en singel verdi (0d hvis det mot formodning
  // ikke finnes noen verdi).
  // Brukes av 1:1 sjabloner som henter verdi basert på intervall (Forbruksutgifter, MaxFradrag, MaxTilsyn).
  private static Double hentSjablonInnholdVerdiIntervall(List<SjablonSingelNokkelSingelInnholdNy> sortertSjablonSingelNokkelSingelInnholdListe,
      String sjablonNokkel) {
    return sortertSjablonSingelNokkelSingelInnholdListe
        .stream()
        .filter(
            sortertSjablonSingelNokkelSingelInnhold -> sortertSjablonSingelNokkelSingelInnhold.getSjablonNokkelVerdi().compareTo(sjablonNokkel) >= 0)
        .findFirst()
        .map(SjablonSingelNokkelSingelInnholdNy::getSjablonInnholdVerdi)
        .orElse(0d);
  }

  // Filtrerer sortertSjablonSingelNokkelListe på nøkkel-verdi > sjablonNokkel og returnerer en liste av typen SjablonInnholdNy (tom liste hvis det
  // mot formodning ikke finnes noen forekomster).
  // Brukes av sjabloner som har flere innholdobjekter og som henter verdi(er) basert på intervall (Samværsfradrag).
  private static List<SjablonInnholdNy> hentSjablonInnholdVerdiListeIntervall(List<SjablonSingelNokkelNy> sortertSjablonSingelNokkelListe,
      String sjablonNokkel) {
    return sortertSjablonSingelNokkelListe
        .stream()
        .filter(sjablon -> sjablon.getSjablonNokkelVerdi().compareTo(sjablonNokkel) >= 0)
        .findFirst()
        .map(SjablonSingelNokkelNy::getSjablonInnholdListe)
        .orElse(emptyList());
  }


  // Filtrerer sjablonInnholdListe på sjablonInnholdNavn og returnerer en liste over alle matchende verdier.
  // Brukes av sjabloner som skal returnere en liste med innholdverdier (TrinnvisSkattesats).
  private static List<Double> hentSjablonInnholdVerdiListe(List<SjablonInnholdNy> sjablonInnholdListe, String sjablonInnholdNavn) {
    return sjablonInnholdListe
        .stream()
        .filter(sjablonInnhold -> sjablonInnhold.getSjablonInnholdNavn().equals(sjablonInnholdNavn))
        .map(SjablonInnholdNy::getSjablonInnholdVerdi)
        .collect(toList());
  }
}
