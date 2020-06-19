package no.nav.bidrag.beregn.felles.bidragsevne;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Avvik;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagPeriodisert;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatBeregning;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BostatusPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.InntektPeriodeCore;
import no.nav.bidrag.beregn.felles.dto.PeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SaerfradragPeriodeCore;
import no.nav.bidrag.beregn.felles.dto.SjablonInnholdCore;
import no.nav.bidrag.beregn.felles.dto.SjablonPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SkatteklassePeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold;
import no.nav.bidrag.beregn.felles.enums.AvvikType;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;
import no.nav.bidrag.beregn.felles.enums.SjablonInnholdNavn;
import no.nav.bidrag.beregn.felles.enums.SjablonTallNavn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("BidragsevneCore (dto-test)")
public class BidragsevneCoreTest {
  private BidragsevneCore bidragsevneCore;

  private List<Sjablon> sjablonListe = new ArrayList<>();

  @Mock
  private BidragsevnePeriode bidragsevnePeriodeMock;

  private BeregnBidragsevneGrunnlagAltCore beregnBidragsevneGrunnlagCore;
  private BeregnBidragsevneResultat bidragsevnePeriodeResultat;
  private List<Avvik> avvikListe;

  @BeforeEach
  void initMocksAndService() {
    MockitoAnnotations.initMocks(this);
    bidragsevneCore = new BidragsevneCoreImpl(bidragsevnePeriodeMock);
  }

  @Test
  @DisplayName("Skal beregne bidragsevne")
  void skalBeregnebidragsevne() {
    byggBidragsevnePeriodeGrunnlagCore();
    byggBidragsevnePeriodeResultat();

    when(bidragsevnePeriodeMock.beregnPerioder(any())).thenReturn(bidragsevnePeriodeResultat);
    var beregnbidragsevneResultatCore = bidragsevneCore.beregnBidragsevne(
        beregnBidragsevneGrunnlagCore);

    assertAll(
        () -> assertThat(beregnbidragsevneResultatCore).isNotNull(),
        () -> assertThat(beregnbidragsevneResultatCore.getAvvikListe()).isEmpty(),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe()).isNotEmpty(),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().size()).isEqualTo(3),

        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatDatoFraTil().getPeriodeDatoFra())
            .isEqualTo(LocalDate.parse("2017-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatDatoFraTil().getPeriodeDatoTil())
            .isEqualTo(LocalDate.parse("2018-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatBeregning().getResultatEvne())
            .isEqualTo(Double.valueOf(666)),

        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatGrunnlag().getInntektListe().size())
            .isEqualTo(1),
        () -> assertThat(
            beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatGrunnlag().getInntektListe().get(0).getInntektType())
            .isEqualTo(InntektType.LØNNSINNTEKT.toString()),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatGrunnlag().getInntektListe().get(0)
            .getInntektBelop()).isEqualTo(Double.valueOf(666000)),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatGrunnlag().getBostatusKode()).isEqualTo("MED_ANDRE"),

        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(1).getResultatDatoFraTil().getPeriodeDatoFra())
            .isEqualTo(LocalDate.parse("2018-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(1).getResultatDatoFraTil().getPeriodeDatoTil())
            .isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(1).getResultatBeregning().getResultatEvne())
            .isEqualTo(Double.valueOf(667)),

        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(2).getResultatDatoFraTil().getPeriodeDatoFra())
            .isEqualTo(LocalDate.parse("2019-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(2).getResultatDatoFraTil().getPeriodeDatoTil())
            .isEqualTo(LocalDate.parse("2020-01-01")),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(2).getResultatBeregning().getResultatEvne())
            .isEqualTo(Double.valueOf(668)),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe().get(0).getResultatGrunnlag().getSjablonListe().get(0)
            .getSjablonInnholdListe().get(0).getSjablonInnholdVerdi()).isEqualTo(22)

    );
  }

  @Test
  @DisplayName("Skal ikke beregne bidragsevne ved avvik")
  void skalIkkeBeregneBidragsevneVedAvvik() {
    byggBidragsevnePeriodeGrunnlagCore();
    byggAvvik();

    when(bidragsevnePeriodeMock.validerInput(any())).thenReturn(avvikListe);
    var beregnbidragsevneResultatCore = bidragsevneCore.beregnBidragsevne(
        beregnBidragsevneGrunnlagCore);

    assertAll(
        () -> assertThat(beregnbidragsevneResultatCore).isNotNull(),
        () -> assertThat(beregnbidragsevneResultatCore.getAvvikListe()).isNotEmpty(),
        () -> assertThat(beregnbidragsevneResultatCore.getAvvikListe()).hasSize(1),
        () -> assertThat(beregnbidragsevneResultatCore.getAvvikListe().get(0).getAvvikTekst()).isEqualTo("beregnDatoTil må være etter beregnDatoFra"),
        () -> assertThat(beregnbidragsevneResultatCore.getAvvikListe().get(0).getAvvikType()).isEqualTo(
            AvvikType.DATO_FRA_ETTER_DATO_TIL.toString()),
        () -> assertThat(beregnbidragsevneResultatCore.getResultatPeriodeListe()).isEmpty()
    );
  }


  private void byggBidragsevnePeriodeGrunnlagCore() {

    var inntektPeriode = new InntektPeriodeCore(
        new PeriodeCore(LocalDate.parse("2017-01-01"), null), InntektType.LØNNSINNTEKT.toString(), Double.valueOf(666000));
    var inntektPeriodeListe = new ArrayList<InntektPeriodeCore>();
    inntektPeriodeListe.add(inntektPeriode);

    var skatteklassePeriode = new SkatteklassePeriodeCore(
        new PeriodeCore(LocalDate.parse("2017-01-01"), null), 1);
    var skatteklassePeriodeListe = new ArrayList<SkatteklassePeriodeCore>();
    skatteklassePeriodeListe.add(skatteklassePeriode);

    var bostatusPeriode = new BostatusPeriodeCore(new PeriodeCore(LocalDate.parse("2017-01-01"), LocalDate.parse("2020-01-01")),
        BostatusKode.MED_ANDRE.toString());
    var bostatusPeriodeListe = new ArrayList<BostatusPeriodeCore>();
    bostatusPeriodeListe.add(bostatusPeriode);

    var antallEgneBarnIHusstandPeriode = new AntallBarnIEgetHusholdPeriodeCore(
        new PeriodeCore(LocalDate.parse("2017-01-01"), null), 1);
    var antallEgneBarnIHusstandPeriodeListe = new ArrayList<AntallBarnIEgetHusholdPeriodeCore>();
    antallEgneBarnIHusstandPeriodeListe.add(antallEgneBarnIHusstandPeriode);

    var saerfradragPeriode = new SaerfradragPeriodeCore(
        new PeriodeCore(LocalDate.parse("2017-01-01"), null), SaerfradragKode.HELT.toString());
    var saerfradragPeriodeListe = new ArrayList<SaerfradragPeriodeCore>();
    saerfradragPeriodeListe.add(saerfradragPeriode);

    var sjablonPeriode = new SjablonPeriodeCore(new PeriodeCore(LocalDate.parse("2017-01-01"), LocalDate.parse("2020-01-01")),
        SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
            Arrays.asList(new SjablonInnholdCore(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d)));
    var sjablonPeriodeListe = new ArrayList<SjablonPeriodeCore>();
    sjablonPeriodeListe.add(sjablonPeriode);

    beregnBidragsevneGrunnlagCore = new BeregnBidragsevneGrunnlagAltCore(LocalDate.parse("2017-01-01"), LocalDate.parse("2020-01-01"),
        inntektPeriodeListe, skatteklassePeriodeListe, bostatusPeriodeListe, antallEgneBarnIHusstandPeriodeListe,
        saerfradragPeriodeListe, sjablonPeriodeListe);
  }

  private void byggBidragsevnePeriodeResultat() {
    List<ResultatPeriode> periodeResultatListe = new ArrayList<>();

    periodeResultatListe.add(new ResultatPeriode(
        new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2018-01-01")),
        new ResultatBeregning(Double.valueOf(666)),
        new BeregnBidragsevneGrunnlagPeriodisert(Arrays.asList(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(666000))), 1, BostatusKode.MED_ANDRE,
            1, SaerfradragKode.HELT,
            Arrays.asList(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
                Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d)))))));

    periodeResultatListe.add(new ResultatPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-01-01")),
        new ResultatBeregning(Double.valueOf(667)),
        new BeregnBidragsevneGrunnlagPeriodisert(Arrays.asList(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(500000))), 1, BostatusKode.MED_ANDRE,
            1, SaerfradragKode.HELT,
            Arrays.asList(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
                Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d)))))));

    periodeResultatListe.add(new ResultatPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2020-01-01")),
        new ResultatBeregning(Double.valueOf(668)),
        new BeregnBidragsevneGrunnlagPeriodisert(Arrays.asList(new Inntekt(InntektType.LØNNSINNTEKT, Double.valueOf(500000))), 1, BostatusKode.MED_ANDRE,
            1, SaerfradragKode.HELT,
            Arrays.asList(new Sjablon(SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.getNavn(), emptyList(),
                Arrays.asList(new SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.getNavn(), 22d)))))));

    bidragsevnePeriodeResultat = new BeregnBidragsevneResultat(periodeResultatListe);
  }

  private void byggAvvik() {
    avvikListe = new ArrayList<>();
    avvikListe.add(new Avvik("beregnDatoTil må være etter beregnDatoFra", AvvikType.DATO_FRA_ETTER_DATO_TIL));
  }
}
