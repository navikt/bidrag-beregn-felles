package no.nav.bidrag.beregn.felles.bidragsevne;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SaerfradragPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SkatteklassePeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;
import no.nav.bidrag.beregn.felles.bo.Avvik;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel;
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.enums.AvvikType;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonNokkelNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("BidragsevneperiodeTest")
class BidragsevnePeriodeTest {

  private BeregnBidragsevneGrunnlagAlt grunnlag;

  private BidragsevnePeriode bidragsevnePeriode = BidragsevnePeriode.getInstance();

  @Test
  @DisplayName("Test med OK grunnlag")
  void testGrunnlagOk() {

    lagGrunnlag();

    var resultat = bidragsevnePeriode.beregnPerioder(grunnlag);

    assertAll(
        () -> assertThat(resultat).isNotNull(),
        () -> assertThat(resultat.getResultatPeriodeListe()).isNotEmpty(),
        () -> assertThat(resultat.getResultatPeriodeListe()).hasSize(6),

        () -> assertThat(resultat.getResultatPeriodeListe().get(0).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2018-07-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(0).getResultatDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(0).getResultatBeregning().getResultatBelopEvne()).isEqualTo(3749),
        () -> assertThat(resultat.getResultatPeriodeListe().get(0).getResultatGrunnlag().getInntektListe().get(0).getInntektBelop())
            .isEqualTo(444000),

        () -> assertThat(resultat.getResultatPeriodeListe().get(1).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(1).getResultatDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-02-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(1).getResultatBeregning().getResultatBelopEvne()).isEqualTo(15604),
        () -> assertThat(resultat.getResultatPeriodeListe().get(1).getResultatGrunnlag().getBostatusKode()).isEqualTo(BostatusKode.ALENE),

        () -> assertThat(resultat.getResultatPeriodeListe().get(2).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-02-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(2).getResultatDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-04-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(2).getResultatBeregning().getResultatBelopEvne()).isEqualTo(20536),

        () -> assertThat(resultat.getResultatPeriodeListe().get(3).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-04-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(3).getResultatDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-05-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(3).getResultatBeregning().getResultatBelopEvne()).isEqualTo(20536),
        () -> assertThat(resultat.getResultatPeriodeListe().get(3).getResultatGrunnlag().getInntektListe().get(0).getInntektBelop())
            .isEqualTo(666001),

        () -> assertThat(resultat.getResultatPeriodeListe().get(4).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-05-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(4).getResultatDatoFraTil().getDatoTil()).isEqualTo(LocalDate.parse("2019-07-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(4).getResultatBeregning().getResultatBelopEvne()).isEqualTo(20536),
        () -> assertThat(resultat.getResultatPeriodeListe().get(4).getResultatGrunnlag().getInntektListe().get(0).getInntektBelop())
            .isEqualTo(666001),
        () -> assertThat(resultat.getResultatPeriodeListe().get(4).getResultatGrunnlag().getInntektListe().get(1).getInntektBelop()).isEqualTo(2),

        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatDatoFraTil().getDatoFra()).isEqualTo(LocalDate.parse("2019-07-01")),
        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatDatoFraTil().getDatoTil()).isNull(),
        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatBeregning().getResultatBelopEvne()).isEqualTo(20063),
        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatGrunnlag().getInntektListe().get(0).getInntektBelop())
            .isEqualTo(666001),
        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatGrunnlag().getInntektListe().get(1).getInntektBelop()).isEqualTo(2),
        () -> assertThat(resultat.getResultatPeriodeListe().get(5).getResultatGrunnlag().getInntektListe().get(2).getInntektBelop()).isEqualTo(3)
    );

    printGrunnlagResultat(resultat);

  }

  @Test
  @DisplayName("Test med feil i grunnlag som skal resultere i avvik")
  void testGrunnlagMedAvvik() {

    lagGrunnlagMedAvvik();
    var avvikListe = bidragsevnePeriode.validerInput(grunnlag);

    assertAll(
        () -> assertThat(avvikListe).isNotEmpty(),
        () -> assertThat(avvikListe).hasSize(6),

        () -> assertThat(avvikListe.get(0).getAvvikTekst())
            .isEqualTo("Første dato i inntektPeriodeListe (2003-01-01) er etter beregnDatoFra (2001-07-01)"),
        () -> assertThat(avvikListe.get(0).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA),

        () -> assertThat(avvikListe.get(1).getAvvikTekst())
            .isEqualTo("Siste dato i inntektPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)"),
        () -> assertThat(avvikListe.get(1).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA),

        () -> assertThat(avvikListe.get(2).getAvvikTekst())
            .isEqualTo("Første dato i skatteklassePeriodeListe (2003-01-01) er etter beregnDatoFra (2001-07-01)"),
        () -> assertThat(avvikListe.get(2).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA),

        () -> assertThat(avvikListe.get(3).getAvvikTekst())
            .isEqualTo("Siste dato i bostatusPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)"),
        () -> assertThat(avvikListe.get(3).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA),

        () -> assertThat(avvikListe.get(4).getAvvikTekst())
            .isEqualTo("Siste dato i antallBarnIEgetHusholdPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)"),
        () -> assertThat(avvikListe.get(4).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA),

        () -> assertThat(avvikListe.get(5).getAvvikTekst())
            .isEqualTo("Siste dato i saerfradragPeriodeListe (2020-01-01) er før beregnDatoTil (2021-01-01)"),
        () -> assertThat(avvikListe.get(5).getAvvikType()).isEqualTo(AvvikType.PERIODE_MANGLER_DATA)
    );

    printAvvikListe(avvikListe);
  }


  private void lagGrunnlag() {

    var beregnDatoFra = LocalDate.parse("2018-07-01");
    var beregnDatoTil = LocalDate.parse("2020-01-01");

    grunnlag = new BeregnBidragsevneGrunnlagAlt(beregnDatoFra, beregnDatoTil, lagInntektGrunnlag(), lagSkatteklasseGrunnlag(), lagBostatusGrunnlag(),
        lagAntallBarnIEgetHusholdGrunnlag(), lagSaerfradragGrunnlag(), lagSjablonGrunnlag());
  }

  private void lagGrunnlagMedAvvik() {

    var beregnDatoFra = LocalDate.parse("2001-07-01");
    var beregnDatoTil = LocalDate.parse("2021-01-01");

    grunnlag = new BeregnBidragsevneGrunnlagAlt(beregnDatoFra, beregnDatoTil, lagInntektGrunnlag(), lagSkatteklasseGrunnlag(), lagBostatusGrunnlag(),
        lagAntallBarnIEgetHusholdGrunnlag(), lagSaerfradragGrunnlag(), lagSjablonGrunnlag());
  }

  private List<InntektPeriode> lagInntektGrunnlag() {
    var inntektPeriodeListe = new ArrayList<InntektPeriode>();

    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2004-01-01")), InntektType.LØNNSINNTEKT, 666000d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2016-01-01")), InntektType.LØNNSINNTEKT, 555000d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-01-01")), InntektType.LØNNSINNTEKT, 444000d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-04-01")), InntektType.LØNNSINNTEKT, 666000d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2019-04-01"), LocalDate.parse("2020-01-01")), InntektType.LØNNSINNTEKT, 666001d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2019-05-01"), LocalDate.parse("2020-01-01")), InntektType.BARNETRYGD, 2d));
    inntektPeriodeListe
        .add(new InntektPeriode(new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")), InntektType.KONTANTSTØTTE, 3d));

    return inntektPeriodeListe;
  }

  private List<SkatteklassePeriode> lagSkatteklasseGrunnlag() {
    var skatteklassePeriodeListe = new ArrayList<SkatteklassePeriode>();

    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2004-01-01")), 2));
    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2016-01-01")), 2));
    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-01-01")), 1));
    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-04-01")), 1));
    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2019-04-01"), LocalDate.parse("2020-01-01")), 1));
    skatteklassePeriodeListe.add(new SkatteklassePeriode(new Periode(LocalDate.parse("2020-01-01"), null), 1));

    return skatteklassePeriodeListe;

  }


  private List<BostatusPeriode> lagBostatusGrunnlag() {

    var bostatusPeriodeListe = new ArrayList<BostatusPeriode>();

    bostatusPeriodeListe.add(new BostatusPeriode(new Periode(LocalDate.parse("2001-01-01"), LocalDate.parse("2017-01-01")), BostatusKode.MED_ANDRE));
    bostatusPeriodeListe.add(new BostatusPeriode(new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2019-02-01")), BostatusKode.ALENE));
    bostatusPeriodeListe.add(new BostatusPeriode(new Periode(LocalDate.parse("2019-02-01"), LocalDate.parse("2020-01-01")), BostatusKode.MED_ANDRE));

    return bostatusPeriodeListe;
  }

  private List<AntallBarnIEgetHusholdPeriode> lagAntallBarnIEgetHusholdGrunnlag() {

    var antallBarnIEgetHusholdPeriodeListe = new ArrayList<AntallBarnIEgetHusholdPeriode>();

    antallBarnIEgetHusholdPeriodeListe
        .add(new AntallBarnIEgetHusholdPeriode(new Periode(LocalDate.parse("2001-01-01"), LocalDate.parse("2017-01-01")), 1));
    antallBarnIEgetHusholdPeriodeListe
        .add(new AntallBarnIEgetHusholdPeriode(new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2020-01-01")), 2));

    return antallBarnIEgetHusholdPeriodeListe;
  }

  private List<SaerfradragPeriode> lagSaerfradragGrunnlag() {

    var saerfradragPeriodeListe = new ArrayList<SaerfradragPeriode>();

    saerfradragPeriodeListe
        .add(new SaerfradragPeriode(new Periode(LocalDate.parse("2001-01-01"), LocalDate.parse("2017-01-01")), SaerfradragKode.HELT));
    saerfradragPeriodeListe
        .add(new SaerfradragPeriode(new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2020-01-01")), SaerfradragKode.HELT));

    return saerfradragPeriodeListe;

  }

  private List<SjablonPeriode> lagSjablonGrunnlag() {

    var sjablonPeriodeListe = new ArrayList<SjablonPeriode>();
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                8848d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-01-01"), null),
        new Sjablon(SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                0d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2013-12-31")),
        new Sjablon(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                7.8d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), null),
        new Sjablon(SjablonTallNavn.TRYGDEAVGIFT_PROSENT.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                8.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                3417d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                3487d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-05-31")),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                57400d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31")),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                75000d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-06-30")),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                75000d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                83000d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                85050d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        new Sjablon(SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                31d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                54750d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                56550d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                54750d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                56550d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                13132d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), null),
        new Sjablon(SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                12977d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                23d)))));
    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), null),
        new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
            Collections.singletonList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(),
                22d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 169000d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.4d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 237900d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 3.3d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 598050d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 12.4d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 962050d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 15.4d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 174500d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.9d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 245650d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 4.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 617500d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 13.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 964800d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 16.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), null),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 180800d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 1.9d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), null),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 254500d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 4.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), null),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 639750d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 13.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), null),
        new Sjablon(SjablonNavn.TRINNVIS_SKATTESATS.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.getNavn(), 999550d),
                new SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.getNavn(), 16.2d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 9303d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 8657d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 5698d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 7330d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "EN")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 9591d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 8925d)))));

    sjablonPeriodeListe.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), null),
        new Sjablon(SjablonNavn.BIDRAGSEVNE.getNavn(), Collections.singletonList(new SjablonNokkel(SjablonNokkelNavn.BOSTATUS.getNavn(), "GS")),
            Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.getNavn(), 5875d),
                new SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.getNavn(), 7557d)))));

    return sjablonPeriodeListe;
  }


  private void printGrunnlagResultat(BeregnBidragsevneResultat beregnBidragsevneResultat) {
    beregnBidragsevneResultat.getResultatPeriodeListe().stream().sorted(
        Comparator.comparing(pR -> pR.getResultatDatoFraTil().getDatoFra()))
        .forEach(sortedPR -> System.out
            .println("Dato fra: " + sortedPR.getResultatDatoFraTil().getDatoFra() + "; " + "Dato til: "
                + sortedPR.getResultatDatoFraTil().getDatoTil()
                + "; " + "Beløp: " + sortedPR.getResultatBeregning().getResultatBelopEvne()));
  }

  private void printAvvikListe(List<Avvik> avvikListe) {
    avvikListe.forEach(avvik -> System.out.println("Avvik tekst: " + avvik.getAvvikTekst() + "; " + "Avvik type: " + avvik.getAvvikType()));
  }
}
