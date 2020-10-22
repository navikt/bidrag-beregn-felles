package no.nav.bidrag.beregn.felles.inntekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.beregn.felles.InntektUtil;
import no.nav.bidrag.beregn.felles.TestUtil;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.enums.AvvikType;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.Rolle;
import no.nav.bidrag.beregn.felles.enums.SoknadType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InntektValidatorTest")
public class InntektTest {

  private List<InntektGrunnlag> inntektGrunnlagListe;
  private List<Avvik> avvikListe;

  @Test
  @DisplayName("Søknadstype ikke gyldig for inntektstype")
  void testUgyldigSoknadstype() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_BEREGNET, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.SAERTILSKUDD, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_BEREGNET.toString() +
            " er ugyldig for søknadstype " + SoknadType.SAERTILSKUDD.toString() + " og rolle " + Rolle.BIDRAGSMOTTAKER.toString())
    );
  }

  @Test
  @DisplayName("Rolle ikke gyldig for inntektstype")
  void testUgyldigRolle() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_BEREGNET, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSPLIKTIG);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_BEREGNET.toString()
            + " er ugyldig for søknadstype " + SoknadType.BIDRAG.toString() + " og rolle " + Rolle.BIDRAGSPLIKTIG.toString())
    );
  }

  @Test
  @DisplayName("Fra-dato ikke gyldig for inntektstype")
  void testUgyldigFraDato() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.AINNTEKT_BEREGNET, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_BEREGNET.toString() +
            " er kun gyldig fom. " + InntektType.AINNTEKT_BEREGNET.getGyldigFom().toString() + " tom. " +
            InntektType.AINNTEKT_BEREGNET.getGyldigTom().toString())
    );
  }

  @Test
  @DisplayName("Til-dato ikke gyldig for inntektstype")
  void testUgyldigTilDato() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.BARNS_SYKDOM, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.BARNS_SYKDOM.toString() + " er kun gyldig fom. "
            + InntektType.BARNS_SYKDOM.getGyldigFom().toString() + " tom. " + InntektType.BARNS_SYKDOM.getGyldigTom().toString())
    );
  }

  @Test
  @DisplayName("Til-dato 9999-12-31 gyldig for inntektstype")
  void testGyldigTilDato99991231() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("9999-12-31")), InntektType.BARNS_SYKDOM, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Til-dato LocalDate.MAX gyldig for inntektstype")
  void testGyldigTilDatoMAX() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.MAX), InntektType.BARNS_SYKDOM, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Til-dato null gyldig for inntektstype")
  void testGyldigTilDatoNull() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektGrunnlag(new Periode(LocalDate.parse("2016-01-01"), null), InntektType.BARNS_SYKDOM, 200000d));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Flere inntekter innenfor samme gruppe med lik fra-dato")
  void testUgyldigSammeGruppeLikFraDato() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikFraDatoLikGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.OVERLAPPENDE_INNTEKT),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).contains("tilhører samme inntektsgruppe og har samme fraDato")
    );
  }

  @Test
  @DisplayName("Flere inntekter fra forskjellige grupper med lik fra-dato")
  void testGyldigUlikGruppeLikFraDato() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikFraDatoUlikGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Flere inntekter uten gruppe med lik fra-dato")
  void testGyldigUtenGruppeLikFraDato() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikFraDatoUtenGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Juster perioder for inntekter innefor samme gruppe som delvis overlapper")
  void testJusterDelvisOverlappSammeGruppe() {
    inntektGrunnlagListe = InntektUtil
        .justerInntekter(TestUtil.byggInntektGrunnlagListeDelvisOverlappSammeGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(inntektGrunnlagListe).isNotEmpty(),
        () -> assertThat(inntektGrunnlagListe.size()).isEqualTo(5),

        () -> assertThat(inntektGrunnlagListe.get(0).getInntektType()).isEqualTo(InntektType.INNTEKTSOPPL_ARBEIDSGIVER),
        () -> assertThat(inntektGrunnlagListe.get(0).getInntektBelop()).isEqualTo(200000d),
        () -> assertThat(inntektGrunnlagListe.get(0).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2018-01-01")),
        () -> assertThat(inntektGrunnlagListe.get(0).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2018-05-31")),

        () -> assertThat(inntektGrunnlagListe.get(1).getInntektType()).isEqualTo(InntektType.INNTEKTSOPPL_ARBEIDSGIVER),
        () -> assertThat(inntektGrunnlagListe.get(1).getInntektBelop()).isEqualTo(150000d),
        () -> assertThat(inntektGrunnlagListe.get(1).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2018-06-01")),
        () -> assertThat(inntektGrunnlagListe.get(1).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2018-12-31")),

        () -> assertThat(inntektGrunnlagListe.get(2).getInntektType()).isEqualTo(InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT),
        () -> assertThat(inntektGrunnlagListe.get(2).getInntektBelop()).isEqualTo(300000d),
        () -> assertThat(inntektGrunnlagListe.get(2).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektGrunnlagListe.get(2).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-12-31")),

        () -> assertThat(inntektGrunnlagListe.get(3).getInntektType()).isEqualTo(InntektType.ALOYSE),
        () -> assertThat(inntektGrunnlagListe.get(3).getInntektBelop()).isEqualTo(250000d),
        () -> assertThat(inntektGrunnlagListe.get(3).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2020-01-01")),
        () -> assertThat(inntektGrunnlagListe.get(3).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.MAX),

        () -> assertThat(inntektGrunnlagListe.get(4).getInntektType()).isEqualTo(InntektType.KAPITALINNTEKT_EGNE_OPPL),
        () -> assertThat(inntektGrunnlagListe.get(4).getInntektBelop()).isEqualTo(100000d),
        () -> assertThat(inntektGrunnlagListe.get(4).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektGrunnlagListe.get(4).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.MAX)
    );
  }
}
