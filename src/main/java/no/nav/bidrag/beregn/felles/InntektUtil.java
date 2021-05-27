package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.enums.AvvikType;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.Rolle;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import no.nav.bidrag.beregn.felles.enums.SoknadType;
import no.nav.bidrag.beregn.felles.inntekt.InntektPeriodeGrunnlag;
import no.nav.bidrag.beregn.felles.inntekt.PeriodisertInntekt;
import no.nav.bidrag.beregn.felles.periode.Periodiserer;

public class InntektUtil {

  // Validerer inntekt
  public static List<Avvik> validerInntekter(List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe, SoknadType soknadType, Rolle rolle) {

    List<Avvik> avvikListe = new ArrayList<>();

    // Validerer søknadstype, rolle og fra- /til-dato for en inntektstype
    inntektPeriodeGrunnlagListe.forEach(inntektPeriodeGrunnlag -> {
      avvikListe.addAll(validerSoknadstypeOgRolle(inntektPeriodeGrunnlag.getType(), soknadType, rolle));
      avvikListe.addAll(validerPeriode(inntektPeriodeGrunnlag));
    });

    // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
    avvikListe.addAll(validerDatoFomPerInntektsgruppe(inntektPeriodeGrunnlagListe));

    return avvikListe;
  }

  // Justerer inntekt
  public static List<InntektPeriodeGrunnlag> justerInntekter(List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe) {

    return justerPerioder(inntektPeriodeGrunnlagListe);
  }


  // Validerer at søknadstype og rolle er gyldig for en inntektstype
  private static List<Avvik> validerSoknadstypeOgRolle(InntektType inntektType, SoknadType soknadType, Rolle rolle) {

    var soknadstypeOgRolleErGyldig = switch (soknadType) {
      case BIDRAG -> switch (rolle) {
        case BIDRAGSPLIKTIG -> inntektType.getBidrag() && inntektType.getBidragspliktig();
        case BIDRAGSMOTTAKER -> inntektType.getBidrag() && inntektType.getBidragsmottaker();
        case SOKNADSBARN -> inntektType.getBidrag() && inntektType.getSoknadsbarn();
      };
      case SAERTILSKUDD -> switch (rolle) {
        case BIDRAGSPLIKTIG -> inntektType.getSaertilskudd() && inntektType.getBidragspliktig();
        case BIDRAGSMOTTAKER -> inntektType.getSaertilskudd() && inntektType.getBidragsmottaker();
        case SOKNADSBARN -> inntektType.getSaertilskudd() && inntektType.getSoknadsbarn();
      };
      case FORSKUDD -> switch (rolle) {
        case BIDRAGSPLIKTIG -> inntektType.getForskudd() && inntektType.getBidragspliktig();
        case BIDRAGSMOTTAKER -> inntektType.getForskudd() && inntektType.getBidragsmottaker();
        case SOKNADSBARN -> inntektType.getForskudd() && inntektType.getSoknadsbarn();
      };
    };

    if (!soknadstypeOgRolleErGyldig) {
      return singletonList(new Avvik("inntektType " + inntektType + " er ugyldig for søknadstype " + soknadType + " og rolle " + rolle,
          AvvikType.UGYLDIG_INNTEKT_TYPE));
    } else {
      return emptyList();
    }
  }

  // Validerer at inntektstypen er gyldig innenfor den angitte tidsperioden
  private static List<Avvik> validerPeriode(InntektPeriodeGrunnlag inntektPeriodeGrunnlag) {

    var inntektDatoFom = inntektPeriodeGrunnlag.getPeriode().getDatoFom();
    LocalDate inntektDatoTil;
    var inntektType = inntektPeriodeGrunnlag.getType();

    // Åpen eller uendelig slutt-dato skal ikke ryke ut på dato-test (?). Setter datoTil lik siste dato i året til datoFom
    if (inntektPeriodeGrunnlag.getPeriode().getDatoTil() == null || inntektPeriodeGrunnlag.getPeriode().getDatoTil()
        .equals(LocalDate.MAX) ||
        inntektPeriodeGrunnlag.getPeriode().getDatoTil().equals(LocalDate.parse("9999-12-31"))) {
      inntektDatoTil = inntektDatoFom.withMonth(12).withDayOfMonth(31);
    } else {
      inntektDatoTil = inntektPeriodeGrunnlag.getPeriode().getDatoTil();
    }

    if ((inntektDatoFom.compareTo(inntektType.getGyldigFom()) < 0) || (inntektDatoTil.compareTo(inntektType.getGyldigTil()) > 0)) {
      return singletonList(new Avvik("inntektType " + inntektType + " er kun gyldig fom. " + inntektType.getGyldigFom().toString()
          + " tom. " + inntektType.getGyldigTil().toString(), AvvikType.UGYLDIG_INNTEKT_PERIODE));
    } else {
      return emptyList();
    }
  }

  // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
  private static List<Avvik> validerDatoFomPerInntektsgruppe(List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe) {

    var avvikListe = new ArrayList<Avvik>();
    Comparator<InntektPeriodeGrunnlag> kriterie = comparing(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getType().getGruppe());
    kriterie = kriterie.thenComparing(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().getDatoFom());

    var inntektGrunnlagListeSortert = inntektPeriodeGrunnlagListe.stream()
        .sorted(kriterie)
        .collect(toList());

    var inntektGrunnlagForrige = new InntektPeriodeGrunnlag("", new Periode(LocalDate.MIN, LocalDate.MAX),
        InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.ZERO, false, false);

    for (var inntektGrunnlag : inntektGrunnlagListeSortert) {

      var inntektGruppe = inntektGrunnlag.getType().getGruppe();
      var inntektGruppeForrige = inntektGrunnlagForrige.getType().getGruppe();
      var datoFom = inntektGrunnlag.getPeriode().getDatoFom();
      var datoFomForrige = inntektGrunnlagForrige.getPeriode().getDatoFom();

      if ((!inntektGruppe.isBlank()) && (inntektGruppe.equals(inntektGruppeForrige)) && (datoFom.equals(datoFomForrige))) {
        avvikListe.add(new Avvik(
            "inntektType " + inntektGrunnlag.getType() + " og inntektType " + inntektGrunnlagForrige.getType()
                + " tilhører samme inntektsgruppe og har samme datoFom (" + datoFom + ")", AvvikType.OVERLAPPENDE_INNTEKT));
      }
      inntektGrunnlagForrige = inntektGrunnlag;
    }

    return avvikListe;
  }

  // Justerer perioder for å unngå overlapp innefor samme inntektsgruppe.
  // Sorterer inntektGrunnlagListe på gruppe og datoFom.
  // datoTil (forrige forekomst) settes lik datoFom - 1 dag (denne forekomst) hvis de tilhører samme gruppe
  private static List<InntektPeriodeGrunnlag> justerPerioder(List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe) {

    Comparator<InntektPeriodeGrunnlag> kriterie = comparing(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getType().getGruppe());
    kriterie = kriterie.thenComparing(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().getDatoFom());

    var inntektGrunnlagListeSortert = inntektPeriodeGrunnlagListe.stream().sorted(kriterie).collect(toList());
    var inntektGrunnlagListeJustert = new ArrayList<InntektPeriodeGrunnlag>();

    InntektPeriodeGrunnlag inntektPeriodeGrunnlagForrige = null;
    var hoppOverInntekt = true;
    String inntektGruppe;
    String inntektGruppeForrige;
    LocalDate datoFom;
    LocalDate datoFomForrige;
    LocalDate nyDatoTilForrige;

    for (var inntektGrunnlag : inntektGrunnlagListeSortert) {
      if (hoppOverInntekt) {
        hoppOverInntekt = false;
        inntektPeriodeGrunnlagForrige = inntektGrunnlag;
        continue;
      }

      inntektGruppe = inntektGrunnlag.getType().getGruppe();
      inntektGruppeForrige = inntektPeriodeGrunnlagForrige.getType().getGruppe();
      datoFom = inntektGrunnlag.getPeriode().getDatoFom();
      datoFomForrige = inntektPeriodeGrunnlagForrige.getPeriode().getDatoFom();

      if ((!inntektGruppe.isBlank()) && (inntektGruppe.equals(inntektGruppeForrige)) && (datoFom.isAfter(datoFomForrige))) {
        nyDatoTilForrige = datoFom.minusDays(1);
        inntektGrunnlagListeJustert
            .add(new InntektPeriodeGrunnlag(inntektPeriodeGrunnlagForrige.getReferanse(), new Periode(datoFomForrige, nyDatoTilForrige),
                inntektPeriodeGrunnlagForrige.getType(), inntektPeriodeGrunnlagForrige.getBelop(), inntektPeriodeGrunnlagForrige.getDeltFordel(),
                inntektPeriodeGrunnlagForrige.getSkatteklasse2()));
      } else {
        inntektGrunnlagListeJustert.add(inntektPeriodeGrunnlagForrige);
      }
      inntektPeriodeGrunnlagForrige = inntektGrunnlag;
    }

    // Legg til siste forekomst (skal aldri justeres)
    inntektGrunnlagListeJustert.add(inntektPeriodeGrunnlagForrige);
    return inntektGrunnlagListeJustert;
  }


  // Regelverk for utvidet barnetrygd: Sjekker om det skal legges til inntekt for fordel særfradrag enslig forsørger og skatteklasse 2
  public static List<InntektPeriodeGrunnlag> behandlUtvidetBarnetrygd(List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe,
      List<SjablonPeriode> sjablonPeriodeListe) {

    // Justerer datoer
    var justertInntektPeriodeGrunnlagListeAlleInntekter = inntektPeriodeGrunnlagListe
        .stream()
        .map(InntektPeriodeGrunnlag::new)
        .sorted(comparing(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().getDatoFom()))
        .collect(toCollection(ArrayList::new));

    // Danner liste over alle inntekter av type UTVIDET_BARNETRYGD
    var justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd = justertInntektPeriodeGrunnlagListeAlleInntekter
        .stream()
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getType().equals(InntektType.UTVIDET_BARNETRYGD))
        .collect(toList());

    // Hvis det ikke finnes inntekter av type UTVIDET_BARNETRYGD, returnerer den samme listen som ble sendt inn
    if (justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd.isEmpty()) {
      return inntektPeriodeGrunnlagListe;
    }

    // Finner laveste og høyeste dato i listen over inntekter av type UTVIDET_BARNETRYGD
    var minDato = justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd
        .stream()
        .map(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().getDatoFom())
        .min(LocalDate::compareTo)
        .orElse(LocalDate.parse("1900-01-01"));

    var maxDato = justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd
        .stream()
        .map(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().getDatoTil())
        .filter(Objects::nonNull)
        .max(LocalDate::compareTo)
        .orElse(LocalDate.parse("2100-01-01"));

    // Lager filter over de sjablonene som skal brukes (0004, 0030, 0031, 0039)
    var sjablonFilter = Arrays.asList(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP,
        SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP, SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP,
        SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP);

    // Filtrerer sjabloner og justerer datoer
    var justertSjablonListe = sjablonPeriodeListe
        .stream()
        .filter(sjablonPeriode -> sjablonFilter.stream()
            .anyMatch(sjablonTallNavn -> sjablonTallNavn.getNavn().equals(sjablonPeriode.getSjablon().getNavn())))
        .map(SjablonPeriode::new)
        .sorted(comparing(sjablonPeriode -> sjablonPeriode.getSjablonPeriode().getDatoFom()))
        .collect(toCollection(ArrayList::new));

    // Danner bruddperioder for inntekter og sjabloner. Legger til ekstra bruddperioder for gyldigheten til inntektstypene som skal beregnes
    var bruddPeriodeListe = new Periodiserer()
        .addBruddpunkter(justertInntektPeriodeGrunnlagListeAlleInntekter)
        .addBruddpunkter(justertSjablonListe)
        .addBruddpunkter(new Periode(InntektType.FORDEL_SKATTEKLASSE2.getGyldigFom(), InntektType.FORDEL_SKATTEKLASSE2.getGyldigTil()))
        .addBruddpunkter(new Periode(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.getGyldigFom(),
            InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.getGyldigTil()))
        .finnPerioder(minDato, maxDato);

    var periodisertInntektListe = new ArrayList<PeriodisertInntekt>();

    // Løper gjennom bruddperiodene og lager en liste over inntekter, sjablonverdier og andre parametre. Perioder uten inntektstype
    // UTVIDET_BARNETRYGD filtreres bort
    bruddPeriodeListe
        .forEach(periode -> {
          if (periodeHarUtvidetBarnetrygd(periode.getPeriode(), justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd)) {
            periodisertInntektListe.add(new PeriodisertInntekt(
                periode.getPeriode(), summerInntektPeriode(periode.getPeriode(), justertInntektPeriodeGrunnlagListeAlleInntekter),
                BigDecimal.ZERO,
                finnSjablonverdi(periode.getPeriode(), justertSjablonListe, SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP),
                finnSjablonverdi(periode.getPeriode(), justertSjablonListe, SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP),
                finnSjablonverdi(periode.getPeriode(), justertSjablonListe, SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP),
                finnSjablonverdi(periode.getPeriode(), justertSjablonListe, SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP),
                finnDeltFordel(periode.getPeriode(), justertInntektPeriodeGrunnlagListeAlleInntekter),
                finnSkatteklasse2(periode.getPeriode(), justertInntektPeriodeGrunnlagListeAlleInntekter)));
          }
        });

    // Løper gjennom periodisertInntektListe og beregner fordel særfradrag / fordel skatteklasse 2
    periodisertInntektListe
        .forEach(periodisertInntekt -> periodisertInntekt.setFordelSaerfradragBelop(beregnFordelSaerfradrag(periodisertInntekt)));

    // Slår sammen perioder med like beløp og rydder vekk perioder med 0 i beløp. Danner ny InntektPeriodeGrunnlag-liste
    var inntektPeriodeGrunnlagListeSaerfradragEnsligForsorger = dannInntektListeSaerfradragEnsligForsorger(periodisertInntektListe);

    // Returnerer en sammenslått liste med grunnlagsinntekter og beregnede inntekter
    return Stream.of(inntektPeriodeGrunnlagListe, inntektPeriodeGrunnlagListeSaerfradragEnsligForsorger)
        .flatMap(Collection::stream)
        .collect(toList());
  }

  // Sjekker om en gitt periode har utvidet barnetrygd
  private static boolean periodeHarUtvidetBarnetrygd(Periode periode,
      List<InntektPeriodeGrunnlag> justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd) {

    return justertInntektPeriodeGrunnlagListeUtvidetBarnetrygd
        .stream()
        .anyMatch(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().overlapperMed(periode));
  }

  // Summerer inntektene i en gitt periode (eksklusiv inntekttype utvidet barnetrygd)
  private static BigDecimal summerInntektPeriode(Periode periode, List<InntektPeriodeGrunnlag> justertInntektPeriodeGrunnlagListe) {

    return justertInntektPeriodeGrunnlagListe
        .stream()
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().overlapperMed(periode))
        .filter(inntektPeriodeGrunnlag -> !(inntektPeriodeGrunnlag.getType().equals(InntektType.UTVIDET_BARNETRYGD)))
        .map(InntektPeriodeGrunnlag::getBelop)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Finner verdien til en gitt sjablon i en gitt periode
  private static BigDecimal finnSjablonverdi(Periode periode, List<SjablonPeriode> justertsjablonListe, SjablonTallNavn sjablonTallNavn) {

    return justertsjablonListe
        .stream()
        .filter(sjablonPeriode -> sjablonPeriode.getPeriode().overlapperMed(periode))
        .filter(sjablonPeriode -> sjablonPeriode.getSjablon().getNavn().equals(sjablonTallNavn.getNavn()))
        .map(sjablonPeriode -> SjablonUtil.hentSjablonverdi(singletonList(sjablonPeriode.getSjablon()), sjablonTallNavn))
        .findFirst()
        .orElse(BigDecimal.ZERO);
  }

  // Finner verdien til flagget 'Delt fordel' i en gitt periode
  private static boolean finnDeltFordel(Periode periode, List<InntektPeriodeGrunnlag> justertInntektPeriodeGrunnlagListe) {

    return justertInntektPeriodeGrunnlagListe
        .stream()
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().overlapperMed(periode))
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getType().equals(InntektType.UTVIDET_BARNETRYGD))
        .map(InntektPeriodeGrunnlag::getDeltFordel)
        .findFirst()
        .orElse(false);
  }

  // Finner verdien til flagget 'Skatteklasse 2' i en gitt periode
  private static boolean finnSkatteklasse2(Periode periode, List<InntektPeriodeGrunnlag> justertInntektPeriodeGrunnlagListe) {

    return justertInntektPeriodeGrunnlagListe
        .stream()
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getPeriode().overlapperMed(periode))
        .filter(inntektPeriodeGrunnlag -> inntektPeriodeGrunnlag.getType().equals(InntektType.UTVIDET_BARNETRYGD))
        .map(InntektPeriodeGrunnlag::getSkatteklasse2)
        .findFirst()
        .orElse(false);
  }

  // Beregner fordel særfradrag
  private static BigDecimal beregnFordelSaerfradrag(PeriodisertInntekt periodisertInntekt) {

    if (periodisertInntekt.getSummertBelop().compareTo(periodisertInntekt.getSjablon0030OvreInntektsgrenseBelop()) < 0) {
      return BigDecimal.ZERO;
    }

    // Fordel skatteklasse 2 (før 2013-01-01)
    if (periodisertInntekt.getPeriode()
        .overlapperMed(new Periode(InntektType.FORDEL_SKATTEKLASSE2.getGyldigFom(), InntektType.FORDEL_SKATTEKLASSE2.getGyldigTil()))) {
      if (periodisertInntekt.getSkatteklasse2()) {
        if (periodisertInntekt.getSummertBelop().compareTo(periodisertInntekt.getSjablon0031NedreInntektsgrenseBelop()) < 0) {
          return periodisertInntekt.getSjablon0004FordelSkatteklasse2Belop().divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
        }
        if (periodisertInntekt.getDeltFordel()) {
          return periodisertInntekt.getSjablon0004FordelSkatteklasse2Belop().divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
        }
        return periodisertInntekt.getSjablon0004FordelSkatteklasse2Belop();
      }
    }

    // Fordel særfradrag (etter 2013-01-01)
    if (periodisertInntekt.getPeriode()
        .overlapperMed(new Periode(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.getGyldigFom(),
            InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER.getGyldigTil()))) {
      if (periodisertInntekt.getSummertBelop().compareTo(periodisertInntekt.getSjablon0031NedreInntektsgrenseBelop()) < 0) {
        return periodisertInntekt.getSjablon0039FordelSaerfradragBelop().divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
      }
      if (periodisertInntekt.getDeltFordel()) {
        return periodisertInntekt.getSjablon0039FordelSaerfradragBelop().divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
      }
      return periodisertInntekt.getSjablon0039FordelSaerfradragBelop();
    }

    return BigDecimal.ZERO;
  }

  // Slår sammen perioder med like beløp og rydder vekk perioder med 0 i beløp. Danner ny InntektPeriodeGrunnlag-liste
  private static List<InntektPeriodeGrunnlag> dannInntektListeSaerfradragEnsligForsorger(List<PeriodisertInntekt> periodisertInntektListe) {

    if (periodisertInntektListe.isEmpty()) {
      return emptyList();
    }

    var inntektListeSaerfradragEnsligForsorger = new ArrayList<InntektPeriodeGrunnlag>();

    var forrigeDatoFom = periodisertInntektListe.get(0).getPeriode().getDatoFom();
    var forrigeDatoTil = periodisertInntektListe.get(0).getPeriode().getDatoTil();
    var forrigeBelop = periodisertInntektListe.get(0).getFordelSaerfradragBelop();
    var inntektType = InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER;

    for (var periodisertInntekt : periodisertInntektListe) {
      if (forrigeBelop.compareTo(periodisertInntekt.getFordelSaerfradragBelop()) != 0) {
        if (forrigeBelop.compareTo(BigDecimal.ZERO) != 0) {
          inntektType = forrigeDatoFom.isBefore(InntektType.FORDEL_SKATTEKLASSE2.getGyldigTil()) ? InntektType.FORDEL_SKATTEKLASSE2
              : InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER;
          inntektListeSaerfradragEnsligForsorger.add(new InntektPeriodeGrunnlag(lagReferanse(inntektType, forrigeDatoFom),
              new Periode(forrigeDatoFom, forrigeDatoTil), inntektType, forrigeBelop,false, false));
        }
        forrigeDatoFom = periodisertInntekt.getPeriode().getDatoFom();
        forrigeBelop = periodisertInntekt.getFordelSaerfradragBelop();
      }
      forrigeDatoTil = periodisertInntekt.getPeriode().getDatoTil();
    }

    if (forrigeBelop.compareTo(BigDecimal.ZERO) != 0) {
      inntektType = forrigeDatoFom.isBefore(InntektType.FORDEL_SKATTEKLASSE2.getGyldigTil()) ? InntektType.FORDEL_SKATTEKLASSE2
          : InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER;
      inntektListeSaerfradragEnsligForsorger.add(new InntektPeriodeGrunnlag(lagReferanse(inntektType, forrigeDatoFom),
          new Periode(forrigeDatoFom, forrigeDatoTil), inntektType, forrigeBelop, false, false));
    }

    return inntektListeSaerfradragEnsligForsorger;
  }

  private static String lagReferanse(InntektType inntektType, LocalDate datoFom) {
    return "Beregnet_Inntekt_" + inntektType.getBelopstype() + "_" + datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }
}
