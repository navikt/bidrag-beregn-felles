package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel;
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkel;
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkelSingelInnhold;
import no.nav.bidrag.beregn.felles.bo.TrinnvisSkattesats;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;

public class SjablonUtil {

  // Henter verdier fra sjablonene Barnetilsyn (N:1, eksakt match) og Bidragsevne (1:N, eksakt match)
  public static double hentSjablonverdi(List<Sjablon> sjablonListe, SjablonNavn sjablonNavn, List<SjablonNokkel> sjablonNokkelListe,
      SjablonInnholdNavn sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerSjablonNokkelListePaaSjablonNokkel(filtrerPaaSjablonNavn(sjablonListe, sjablonNavn.getNavn()), sjablonNokkelListe);
    var sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter verdier fra sjablonene Forbruksutgifter, MaksFradrag og MaksTilsyn (1:1, intervall)
  public static double hentSjablonverdi(List<Sjablon> sjablonListe, SjablonNavn sjablonNavn, String sjablonNokkelVerdi) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn.getNavn());
    var sortertSjablonSingelNokkelSingelInnholdListe = mapTilSingelListeNokkelInnholdSortert(filtrertSjablonListe);
    return hentSjablonInnholdVerdiIntervall(sortertSjablonSingelNokkelSingelInnholdListe, sjablonNokkelVerdi);
  }

  // Henter verdier fra sjablon Samværsfradrag (N:N, eksakt match + intervall)
  public static double hentSjablonverdi(List<Sjablon> sjablonListe, SjablonNavn sjablonNavn, List<SjablonNokkel> sjablonNokkelListe,
      SjablonNokkelNavn sjablonNokkelNavn, String sjablonNokkelVerdi, SjablonInnholdNavn sjablonInnholdNavn) {
    var filtrertSjablonListe = filtrerSjablonNokkelListePaaSjablonNokkel(filtrerPaaSjablonNavn(sjablonListe, sjablonNavn.getNavn()), sjablonNokkelListe);
    var sortertSjablonSingelNokkelListe = mapTilSingelListeNokkelSortert(filtrertSjablonListe, sjablonNokkelNavn);
    var sjablonInnholdListe = finnSjablonInnholdVerdiListeIntervall(sortertSjablonSingelNokkelListe, sjablonNokkelVerdi);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, sjablonInnholdNavn);
  }

  // Henter verdier fra sjablon Sjablontall (1:1, eksakt match)
  public static double hentSjablonverdi(List<Sjablon> sjablonListe, SjablonTallNavn sjablonTallNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonTallNavn.getNavn());
    var sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe);
    return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe, SjablonInnholdNavn.SJABLON_VERDI);
  }

  // Henter liste med verdier fra sjablon TrinnvisSkattesats (0:N, hent alle)
  public static List<TrinnvisSkattesats> hentTrinnvisSkattesats(List<Sjablon> sjablonListe, SjablonNavn sjablonNavn) {
    var filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe, sjablonNavn.getNavn());
    var sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe);
    var inntektGrenseListe = finnSjablonInnholdVerdiListe(sjablonInnholdListe, SjablonInnholdNavn.INNTEKTSGRENSE_BELOP);
    var satsListe = finnSjablonInnholdVerdiListe(sjablonInnholdListe, SjablonInnholdNavn.SKATTESATS_PROSENT);

    var indeks = 0;
    var trinnvisSkattesatsListe = new ArrayList<TrinnvisSkattesats>();
    while (indeks < inntektGrenseListe.size()) {
      trinnvisSkattesatsListe.add(new TrinnvisSkattesats(inntektGrenseListe.get(indeks), satsListe.get(indeks)));
      indeks = indeks + 1;
    }

    return trinnvisSkattesatsListe.stream().sorted(comparing(TrinnvisSkattesats::getInntektGrense)).collect(toList());
  }

  // Filtrerer sjablonListe på sjablonNavn og returnerer ny liste.
  // Brukes av alle typer sjabloner.
  private static List<Sjablon> filtrerPaaSjablonNavn(List<Sjablon> sjablonListe, String sjablonNavn) {
    return sjablonListe
        .stream()
        .filter(sjablon -> sjablon.getSjablonNavn().equals(sjablonNavn))
        .collect(toList());
  }

  // Filtrerer sjablonListe på sjablonNokkelListe og returnerer en ny liste.
  // Brukes av sjabloner som har eksakt match på nøkkel (Barnetilsyn, Bidragsevne, Samværsfradrag).
  private static List<Sjablon> filtrerSjablonNokkelListePaaSjablonNokkel(List<Sjablon> sjablonListe, List<SjablonNokkel> sjablonNokkelListe) {
    var sjablonStream = sjablonListe.stream();
    for (SjablonNokkel sjablonNokkel : sjablonNokkelListe) {
      sjablonStream = filtrerPaaSjablonNokkel(sjablonStream, sjablonNokkel);
    }
    return sjablonStream.collect(toList());
  }

  // Filtrerer sjablonStream på sjablonNokkelInput og returnerer en ny stream.
  // Intern bruk.
  private static Stream<Sjablon> filtrerPaaSjablonNokkel(Stream<Sjablon> sjablonStream, SjablonNokkel sjablonNokkelInput) {
    return sjablonStream
        .filter(sjablon -> sjablon.getSjablonNokkelListe()
            .stream()
            .anyMatch(sjablonNokkel -> (sjablonNokkel.getSjablonNokkelNavn().equals(sjablonNokkelInput.getSjablonNokkelNavn())) &&
                (sjablonNokkel.getSjablonNokkelVerdi().equals(sjablonNokkelInput.getSjablonNokkelVerdi()))));
  }

  // Tar inn en sjablonListe og returnerer en sjablonInnholdListe.
  // Brukes av Bidragsevne, Sjablontall, TrinnvisSkattesats.
  private static List<SjablonInnhold> mapSjablonListeTilSjablonInnholdListe(List<Sjablon> sjablonListe) {
    return sjablonListe.stream().map(Sjablon::getSjablonInnholdListe).flatMap(Collection::stream).collect(toList());
  }

  // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og singel innholdverdi (1:1). Returnerer en ny liste sortert
  // på nøkkelverdi.
  // Brukes av sjabloner som har ett nøkkelobjekt og ett innholdobjekt (Forbruksutgifter, MaxFradrag, MaxTilsyn).
  private static List<SjablonSingelNokkelSingelInnhold> mapTilSingelListeNokkelInnholdSortert(List<Sjablon> filtrertSjablonListe) {
    return filtrertSjablonListe
        .stream()
        .map(sjablon -> new SjablonSingelNokkelSingelInnhold(sjablon.getSjablonNavn(),
            sjablon.getSjablonNokkelListe().stream().map(sjablonNokkel -> sjablonNokkel.getSjablonNokkelVerdi()).findFirst().orElse(" "),
            sjablon.getSjablonInnholdListe().stream().map(sjablonInnhold -> sjablonInnhold.getSjablonInnholdVerdi()).findFirst().orElse(0d)))
        .sorted(comparing(SjablonSingelNokkelSingelInnhold::getSjablonNokkelVerdi))
        .collect(toList());
  }

  // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og liste med innholdverdier (1:N). Returnerer en ny liste
  // sortert på nøkkelverdi.
  // Brukes av sjabloner som har ett nøkkelobjekt med eksakt match og flere innholdobjekter (Samværsfradrag).
  private static List<SjablonSingelNokkel> mapTilSingelListeNokkelSortert(List<Sjablon> filtrertSjablonListe, SjablonNokkelNavn sjablonNokkelNavn) {
    return filtrertSjablonListe
        .stream()
        .map(sjablon -> new SjablonSingelNokkel(sjablon.getSjablonNavn(),
            sjablon.getSjablonNokkelListe()
                .stream()
                .filter(sjablonNokkel -> sjablonNokkel.getSjablonNokkelNavn().equals(sjablonNokkelNavn.getNavn()))
                .map(SjablonNokkel::getSjablonNokkelVerdi)
                .findFirst()
                .orElse(" "),
            sjablon.getSjablonInnholdListe()))
        .sorted(comparing(SjablonSingelNokkel::getSjablonNokkelVerdi))
        .collect(toList());
  }

  // Filtrerer sjablonInnholdListe på sjablonInnholdNavn (eksakt match) og returnerer matchende verdi (0d hvis sjablonInnholdNavn mot formodning ikke
  // finnes).
  // Brukes av sjabloner som skal hente eksakt verdi (Barnetilsyn, Bidragsevne, Sjablontall, Samværsfradrag).
  private static Double hentSjablonInnholdVerdiEksakt(List<SjablonInnhold> sjablonInnholdListe, SjablonInnholdNavn sjablonInnholdNavn) {
    return sjablonInnholdListe
        .stream()
        .filter(sjablonInnhold -> sjablonInnhold.getSjablonInnholdNavn().equals(sjablonInnholdNavn.getNavn()))
        .map(SjablonInnhold::getSjablonInnholdVerdi)
        .findFirst()
        .orElse(0d);
  }

  // Filtrerer sortertSjablonSingelNokkelSingelInnholdListe på nøkkel-verdi >= sjablonNokkel og returnerer en singel verdi (0d hvis det mot formodning
  // ikke finnes noen verdi).
  // Brukes av 1:1 sjabloner som henter verdi basert på intervall (Forbruksutgifter, MaxFradrag, MaxTilsyn).
  private static Double hentSjablonInnholdVerdiIntervall(List<SjablonSingelNokkelSingelInnhold> sortertSjablonSingelNokkelSingelInnholdListe,
      String sjablonNokkelVerdi) {
    return sortertSjablonSingelNokkelSingelInnholdListe
        .stream()
        .filter(sortertSjablonSingelNokkelSingelInnhold ->
            sortertSjablonSingelNokkelSingelInnhold.getSjablonNokkelVerdi().compareTo(sjablonNokkelVerdi) >= 0)
        .findFirst()
        .map(SjablonSingelNokkelSingelInnhold::getSjablonInnholdVerdi)
        .orElse(0d);
  }

  // Filtrerer sortertSjablonSingelNokkelListe på nøkkel-verdi >= sjablonNokkel og returnerer en liste av typen SjablonInnholdNy (tom liste hvis det
  // mot formodning ikke finnes noen forekomster).
  // Brukes av sjabloner som har flere innholdobjekter og som henter verdi(er) basert på intervall (Samværsfradrag).
  private static List<SjablonInnhold> finnSjablonInnholdVerdiListeIntervall(List<SjablonSingelNokkel> sortertSjablonSingelNokkelListe,
      String sjablonNokkelVerdi) {
    return sortertSjablonSingelNokkelListe
        .stream()
        .filter(sjablon -> sjablon.getSjablonNokkelVerdi().compareTo(sjablonNokkelVerdi) >= 0)
        .findFirst()
        .map(SjablonSingelNokkel::getSjablonInnholdListe)
        .orElse(emptyList());
  }


  // Filtrerer sjablonInnholdListe på sjablonInnholdNavn og returnerer en liste over alle matchende verdier.
  // Brukes av sjabloner som skal returnere en liste med innholdverdier (TrinnvisSkattesats).
  private static List<Double> finnSjablonInnholdVerdiListe(List<SjablonInnhold> sjablonInnholdListe, SjablonInnholdNavn sjablonInnholdNavn) {
    return sjablonInnholdListe
        .stream()
        .filter(sjablonInnhold -> sjablonInnhold.getSjablonInnholdNavn().equals(sjablonInnholdNavn.getNavn()))
        .map(SjablonInnhold::getSjablonInnholdVerdi)
        .collect(toList());
  }
}
