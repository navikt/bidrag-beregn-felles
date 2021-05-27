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
        .singletonList(new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.SAERTILSKUDD, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG +
            " er ugyldig for søknadstype " + SoknadType.SAERTILSKUDD + " og rolle " + Rolle.BIDRAGSMOTTAKER)
    );
  }

  @Test
  @DisplayName("Rolle ikke gyldig for inntektstype")
  void testUgyldigRolle() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSPLIKTIG);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_TYPE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG
            + " er ugyldig for søknadstype " + SoknadType.BIDRAG + " og rolle " + Rolle.BIDRAGSPLIKTIG)
    );
  }

  @Test
  @DisplayName("datoFom ikke gyldig for inntektstype")
  void testUgyldigDatoFom() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.FORSKUDD, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG +
            " er kun gyldig fom. " + InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.getGyldigFom().toString() + " tom. " +
            InntektType.AINNTEKT_KORRIGERT_BARNETILLEGG.getGyldigTil().toString())
    );
  }

  @Test
  @DisplayName("datoTil ikke gyldig for inntektstype")
  void testUgyldigDatoTil() {
    var inntektGrunnlagListe = Collections
        .singletonList(new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-12-31")),
            InntektType.BARNS_SYKDOM, BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.UGYLDIG_INNTEKT_PERIODE),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).isEqualTo("inntektType " + InntektType.BARNS_SYKDOM + " er kun gyldig fom. "
            + InntektType.BARNS_SYKDOM.getGyldigFom().toString() + " tom. " + InntektType.BARNS_SYKDOM.getGyldigTil().toString())
    );
  }

  @Test
  @DisplayName("datoTil 9999-12-31 gyldig for inntektstype")
  void testGyldigDatoTil99991231() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("9999-12-31")), InntektType.BARNS_SYKDOM,
            BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("datoTil LocalDate.MAX gyldig for inntektstype")
  void testGyldigDatoTilMAX() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2016-01-01"), LocalDate.MAX), InntektType.BARNS_SYKDOM,
            BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("datoTil null gyldig for inntektstype")
  void testGyldigDatoTilNull() {
    var inntektGrunnlagListe = Collections.singletonList(
        new InntektPeriodeGrunnlag("REF", new Periode(LocalDate.parse("2016-01-01"), null), InntektType.BARNS_SYKDOM,
            BigDecimal.valueOf(200000), false, false));
    avvikListe = InntektUtil.validerInntekter(inntektGrunnlagListe, SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Flere inntekter innenfor samme gruppe med lik datoFom")
  void testUgyldigSammeGruppeLikDatoFom() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikDatoFomLikGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe.size()).isEqualTo(1),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.OVERLAPPENDE_INNTEKT),
        () -> assertThat(avvikListe.get(0).getAvvikTekst()).contains("tilhører samme inntektsgruppe og har samme datoFom")
    );
  }

  @Test
  @DisplayName("Flere inntekter fra forskjellige grupper med lik datoFom")
  void testGyldigUlikGruppeLikDatoFom() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikDatoFomUlikGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

    assertAll(
        () -> assertThat(avvikListe).isEmpty()
    );
  }

  @Test
  @DisplayName("Flere inntekter uten gruppe med lik datoFom")
  void testGyldigUtenGruppeLikDatoFom() {
    avvikListe = InntektUtil.validerInntekter(TestUtil.byggInntektGrunnlagListeMedLikDatoFomUtenGruppe(), SoknadType.BIDRAG, Rolle.BIDRAGSMOTTAKER);

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

        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getType()).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getBelop()).isEqualTo(BigDecimal.valueOf(200000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2018-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(0).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2018-05-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getType()).isEqualTo(InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getBelop()).isEqualTo(BigDecimal.valueOf(150000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2018-06-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(1).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2018-12-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getType()).isEqualTo(InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getBelop()).isEqualTo(BigDecimal.valueOf(300000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(2).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2019-12-31")),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getType()).isEqualTo(InntektType.ALOYSE),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getBelop()).isEqualTo(BigDecimal.valueOf(250000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2020-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(3).getPeriode().getDatoTil()).isEqualTo(LocalDate.MAX),

        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getType()).isEqualTo(InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getBelop()).isEqualTo(BigDecimal.valueOf(100000)),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(inntektPeriodeGrunnlagListe.get(4).getPeriode().getDatoTil()).isEqualTo(LocalDate.MAX)
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

        () -> assertThat(nyInntektGrunnlagListe.get(9).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2019-04-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2019-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(9).getBelop()).isEqualTo(BigDecimal.valueOf(13000)),

        () -> assertThat(nyInntektGrunnlagListe.get(10).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2019-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2019-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(10).getBelop()).isEqualTo(BigDecimal.valueOf(6500)),

        () -> assertThat(nyInntektGrunnlagListe.get(11).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2020-04-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2020-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(11).getBelop()).isEqualTo(BigDecimal.valueOf(13000)),

        () -> assertThat(nyInntektGrunnlagListe.get(12).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2020-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2020-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(12).getBelop()).isEqualTo(BigDecimal.valueOf(14000)),

        () -> assertThat(nyInntektGrunnlagListe.get(13).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2020-08-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2021-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(13).getBelop()).isEqualTo(BigDecimal.valueOf(7000))
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

        () -> assertThat(nyInntektGrunnlagListe.get(2).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2012-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2012-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getType()).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2),
        () -> assertThat(nyInntektGrunnlagListe.get(2).getBelop()).isEqualTo(BigDecimal.valueOf(7500)),

        () -> assertThat(nyInntektGrunnlagListe.get(3).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2012-07-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2013-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getType()).isEqualTo(InntektType.FORDEL_SKATTEKLASSE2),
        () -> assertThat(nyInntektGrunnlagListe.get(3).getBelop()).isEqualTo(BigDecimal.valueOf(8500)),

        () -> assertThat(nyInntektGrunnlagListe.get(4).getPeriode().getDatoFom()).isEqualTo(LocalDate.parse("2013-01-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getPeriode().getDatoTil()).isEqualTo(LocalDate.parse("2013-06-01")),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getType()).isEqualTo(InntektType.FORDEL_SAERFRADRAG_ENSLIG_FORSORGER),
        () -> assertThat(nyInntektGrunnlagListe.get(4).getBelop()).isEqualTo(BigDecimal.valueOf(12500))
    );
  }
}
