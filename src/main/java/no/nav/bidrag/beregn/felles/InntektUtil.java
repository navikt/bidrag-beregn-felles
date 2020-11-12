package no.nav.bidrag.beregn.felles;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.enums.AvvikType;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.Rolle;
import no.nav.bidrag.beregn.felles.enums.SoknadType;
import no.nav.bidrag.beregn.felles.inntekt.InntektGrunnlag;

public class InntektUtil {

  // Validerer inntekt
  public static List<Avvik> validerInntekter(List<InntektGrunnlag> inntektGrunnlagListe, SoknadType soknadType, Rolle rolle) {

    List<Avvik> avvikListe = new ArrayList<>();

    // Validerer søknadstype, rolle og fra- /til-dato for en inntektstype
    inntektGrunnlagListe.forEach(inntektGrunnlag -> {
      avvikListe.addAll(validerSoknadstypeOgRolle(inntektGrunnlag.getInntektType(), soknadType, rolle));
      avvikListe.addAll(validerPeriode(inntektGrunnlag));
    });

    // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
    avvikListe.addAll(validerFraDatoPerInntektsgruppe(inntektGrunnlagListe));

    return avvikListe;
  }

  // Justerer inntekt
  public static List<InntektGrunnlag> justerInntekter(List<InntektGrunnlag> inntektGrunnlagListe) {

    return justerPerioder(inntektGrunnlagListe);
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
      return singletonList(new Avvik("inntektType " + inntektType.toString() + " er ugyldig for søknadstype " + soknadType.toString() + " og rolle "
          + rolle.toString(), AvvikType.UGYLDIG_INNTEKT_TYPE));
    } else {
      return emptyList();
    }
  }

  // Validerer at inntektstypen er gyldig innenfor den angitte tidsperioden
  private static List<Avvik> validerPeriode(InntektGrunnlag inntektGrunnlag) {

    var inntektDatoFra = inntektGrunnlag.getInntektDatoFraTil().getDatoFra();
    LocalDate inntektDatoTil;
    var inntektType = inntektGrunnlag.getInntektType();

    // Åpen eller uendelig slutt-dato skal ikke ryke ut på dato-test (?). Setter datoTil lik siste dato i året til datoFra
    if (inntektGrunnlag.getInntektDatoFraTil().getDatoTil() == null || inntektGrunnlag.getInntektDatoFraTil().getDatoTil().equals(LocalDate.MAX) ||
        inntektGrunnlag.getInntektDatoFraTil().getDatoTil().equals(LocalDate.parse("9999-12-31"))) {
      inntektDatoTil = inntektDatoFra.withMonth(12).withDayOfMonth(31);
    } else {
      inntektDatoTil = inntektGrunnlag.getInntektDatoFraTil().getDatoTil();
    }

    if ((inntektDatoFra.compareTo(inntektType.getGyldigFom()) < 0) || (inntektDatoTil.compareTo(inntektType.getGyldigTom()) > 0)) {
      return singletonList(new Avvik("inntektType " + inntektType.toString() + " er kun gyldig fom. " + inntektType.getGyldigFom().toString()
          + " tom. " + inntektType.getGyldigTom().toString(), AvvikType.UGYLDIG_INNTEKT_PERIODE));
    } else {
      return emptyList();
    }
  }

  // Validerer at flere inntekter innenfor samme inntektsgruppe ikke starter på samme dato
  private static List<Avvik> validerFraDatoPerInntektsgruppe(List<InntektGrunnlag> inntektGrunnlagListe) {

    var avvikListe = new ArrayList<Avvik>();
    Comparator<InntektGrunnlag> kriterie = comparing(inntektGrunnlag -> inntektGrunnlag.getInntektType().getGruppe());
    kriterie = kriterie.thenComparing(inntektGrunnlag -> inntektGrunnlag.getInntektDatoFraTil().getDatoFra());

    var inntektGrunnlagListeSortert = inntektGrunnlagListe.stream()
        .sorted(kriterie)
        .collect(toList());

    var inntektGrunnlagForrige = new InntektGrunnlag(new Periode(LocalDate.MIN, LocalDate.MAX), InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG,
        BigDecimal.ZERO);

    for (var inntektGrunnlag : inntektGrunnlagListeSortert) {

      var inntektGruppe = inntektGrunnlag.getInntektType().getGruppe();
      var inntektGruppeForrige = inntektGrunnlagForrige.getInntektType().getGruppe();
      var datoFra = inntektGrunnlag.getInntektDatoFraTil().getDatoFra();
      var datoFraForrige = inntektGrunnlagForrige.getInntektDatoFraTil().getDatoFra();

      if ((!inntektGruppe.isBlank()) && (inntektGruppe.equals(inntektGruppeForrige)) && (datoFra.equals(datoFraForrige))) {
        avvikListe.add(new Avvik(
            "inntektType " + inntektGrunnlag.getInntektType().toString() + " og inntektType " + inntektGrunnlagForrige.getInntektType().toString()
                + " tilhører samme inntektsgruppe og har samme fraDato (" + datoFra.toString() + ")", AvvikType.OVERLAPPENDE_INNTEKT));
      }
      inntektGrunnlagForrige = inntektGrunnlag;
    }

    return avvikListe;
  }

  // Justerer perioder for å unngå overlapp innefor samme inntektsgruppe.
  // Sorterer inntektGrunnlagListe på gruppe og datoFra.
  // datoTil (forrige forekomst) settes lik datoFra - 1 dag (denne forekomst) hvis de tilhører samme gruppe
  private static List<InntektGrunnlag> justerPerioder(List<InntektGrunnlag> inntektGrunnlagListe) {

    Comparator<InntektGrunnlag> kriterie = comparing(inntektGrunnlag -> inntektGrunnlag.getInntektType().getGruppe());
    kriterie = kriterie.thenComparing(inntektGrunnlag -> inntektGrunnlag.getInntektDatoFraTil().getDatoFra());

    var inntektGrunnlagListeSortert = inntektGrunnlagListe.stream().sorted(kriterie).collect(toList());
    var inntektGrunnlagListeJustert = new ArrayList<InntektGrunnlag>();

    InntektGrunnlag inntektGrunnlagForrige = null;
    var hoppOverInntekt = true;
    String inntektGruppe;
    String inntektGruppeForrige;
    LocalDate datoFra;
    LocalDate datoFraForrige;
    LocalDate nyDatoTilForrige;

    for (var inntektGrunnlag : inntektGrunnlagListeSortert) {
      if (hoppOverInntekt) {
        hoppOverInntekt = false;
        inntektGrunnlagForrige = inntektGrunnlag;
        continue;
      }

      inntektGruppe = inntektGrunnlag.getInntektType().getGruppe();
      inntektGruppeForrige = inntektGrunnlagForrige.getInntektType().getGruppe();
      datoFra = inntektGrunnlag.getInntektDatoFraTil().getDatoFra();
      datoFraForrige = inntektGrunnlagForrige.getInntektDatoFraTil().getDatoFra();

      if ((!inntektGruppe.isBlank()) && (inntektGruppe.equals(inntektGruppeForrige)) && (datoFra.isAfter(datoFraForrige))) {
        nyDatoTilForrige = datoFra.minusDays(1);
        inntektGrunnlagListeJustert.add(new InntektGrunnlag(new Periode(datoFraForrige, nyDatoTilForrige), inntektGrunnlagForrige.getInntektType(),
            inntektGrunnlagForrige.getInntektBelop()));
      } else {
        inntektGrunnlagListeJustert.add(inntektGrunnlagForrige);
      }
      inntektGrunnlagForrige = inntektGrunnlag;
    }

    // Legg til siste forekomst (skal aldri justeres)
    inntektGrunnlagListeJustert.add(inntektGrunnlagForrige);
    return inntektGrunnlagListeJustert;
  }
}
