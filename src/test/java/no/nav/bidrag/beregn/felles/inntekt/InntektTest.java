package no.nav.bidrag.beregn.felles.inntekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
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

  private List<InntektPeriodeGrunnlag> inntektPeriodeGrunnlagListe;
  private List<Avvik> avvikListe;

  @Test
  @DisplayName("Søknadstype ikke gyldig for inntektstype")
  void testUgyldigSoknadstype() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.SAERTILSKUDD, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.toString() +
            " er ugyldig for søknadstype " + SoknadType.SAERTILSKUDD.toString() + " og rolle " + Rolle.BIDRAGSMOTTAKER.toString())
    );
  }

  @Test
  @DisplayName("Rolle ikke gyldig for inntektstype")
  void testUgyldigRolle() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSPLIKTIG);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.toString()
            + " er ugyldig for søknadstype " + SoknadType.BIDRAG.toString() + " og rolle " + Rolle.BIDRAGSPLIKTIG.toString())
    );
  }

  @Test
  @DisplayName("Fra-dato ikke gyldig for inntektstype")
  void testUgyldigFraDato() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.FORSKUDD, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.toString() +
            " er kun gyldig fom. " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.getGyldigFom().toString() + " tom. " +
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.getGyldigTil().toString())
    );
  }

  @Test
  @DisplayName("Til-dato ikke gyldig for inntektstype")
  void testUgyldigTilDato() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.BARNS_SYKDOM, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.BARNS_SYKDOM.toString() + " er kun gyldig fom. "
            + InntektType.BARNS_SYKDOM.getGyldigFom().toString() + " tom. " + InntektType.BARNS_SYKDOM.getGyldigTil().toString())
    );
  }

  @Test
  @DisplayName("Til-dato 9999-12-31 gyldig for inntektstype")
  void testGyldigTilDato99991231() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("9999-12-31")), InntektType.BARNS_SYKDOM,
            BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Til-dato LocalDate.MAX gyldig for inntektstype")
  void testGyldigTilDatoMAX() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2016-01-01"), LocalDate.MAX), InntektType.BARNS_SYKDOM, BigDecimal.valueOf(200000),
            false,
            false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Til-dato null gyldig for inntektstype")
  void testGyldigTilDatoNull() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag(new Periode(LocalDate.parse("2016-01-01"), null), InntektType.BARNS_SYKDOM, BigDecimal.valueOf(200000), false,
            false));
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
    inntektPeriodeGrunnlagListe = InntektUtil.justerInntekter(TestUtil.byggInntektGrunnlagListeDelvisOverlappSammeGruppe());

    assertAll(
        () -> assertThat(inntektPeriodeGrunnlagListe).isNotEmpty(),
        () -> assertThat(inntektPeriodeGrunnlagListe.size()).isEqualTo(5),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getInntektType()).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getInntektBelop()).isEqualTo(BigDecimal.valueOf(200000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2018-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2018-05-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getInntektType()).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getInntektBelop()).isEqualTo(BigDecimal.valueOf(150000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2018-06-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2018-12-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getInntektType()).isEqualTo(InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getInntektBelop()).isEqualTo(BigDecimal.valueOf(300000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-12-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getInntektType()).isEqualTo(InntektType.ALOYSE),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getInntektBelop()).isEqualTo(BigDecimal.valueOf(250000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2020-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.MAX),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getInntektType()).isEqualTo(InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getInntektBelop()).isEqualTo(BigDecimal.valueOf(100000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.MAX)
    );
  }

  @Test
  @DisplayName("Utvidet barnetrygd - full test av regelverk")
  void testUtvidetBarnetrygdFullTest() {

    var nyInntektGrunnlagListe = InntektUtil
        .behandlUtvidetBarnetrygd(TestUtil.byggInntektGrunnlagUtvidetBarnetrygdFull(), TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdFull());

    assertAll(
        () -> assertThat(nyInntektGrunnlagListe).isNotEmpty(),
        () -> assertThat(nyInntektGrunnlagListe.size()).isEqualTo(14),

        () -> assertThat(nyInntektGrunnlagListe.get(9).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-04-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getInntektBelop()).isEqualTo(BigDecimal.valueOf(13000)),

        () -> assertThat(nyInntektGrunnlagListe.get(10).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getInntektBelop()).isEqualTo(BigDecimal.valueOf(6500)),

        () -> assertThat(nyInntektGrunnlagListe.get(11).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2020-04-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2020-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getInntektBelop()).isEqualTo(BigDecimal.valueOf(13000)),

        () -> assertThat(nyInntektGrunnlagListe.get(12).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2020-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2020-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getInntektBelop()).isEqualTo(BigDecimal.valueOf(14000)),

        () -> assertThat(nyInntektGrunnlagListe.get(13).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2020-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2021-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getInntektBelop()).isEqualTo(BigDecimal.valueOf(7000))
    );
  }

  @Test
  @DisplayName("Utvidet barnetrygd - test av overgang mellom regelverk for skatteklasse 2 og fordel særfradrag")
  void testUtvidetBarnetrygdOvergangSkatteklasse2FordelSaerfradrag() {

    var nyInntektGrunnlagListe = InntektUtil.behandlUtvidetBarnetrygd(
        TestUtil.byggInntektGrunnlagUtvidetBarnetrygdOvergang(), TestUtil.byggSjablontallGrunnlagUtvidetBarnetrygdOvergang());

    assertAll(
        () -> assertThat(nyInntektGrunnlagListe).isNotEmpty(),
        () -> assertThat(nyInntektGrunnlagListe.size()).isEqualTo(5),

        () -> assertThat(nyInntektGrunnlagListe.get(2).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2012-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2012-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getInntektType()).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getInntektBelop()).isEqualTo(BigDecimal.valueOf(7500)),

        () -> assertThat(nyInntektGrunnlagListe.get(3).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2012-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2013-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getInntektType()).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getInntektBelop()).isEqualTo(BigDecimal.valueOf(8500)),

        () -> assertThat(nyInntektGrunnlagListe.get(4).getInntektDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2013-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getInntektDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2013-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getInntektType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getInntektBelop()).isEqualTo(BigDecimal.valueOf(12500))
    );
  }
}
