package no.nav.bidrag.beregn.felles.bidragsevne;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.Periode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.BidragsevnePeriodeResultat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("BidragsevneperiodeTest")
class BidragsevnePeriodeTest {
  private BeregnBidragsevneGrunnlagAlt beregnBidragsevneGrunnlagAlt;
  private BidragsevnePeriode bidragsevnePeriode = BidragsevnePeriode.getInstance();

  @Test
  void lagGrunnlagTest() {
    System.out.println("Starter test");
    var beregnDatoFra = LocalDate.parse("2017-01-01");
    var beregnDatoTil = LocalDate.parse("2019-08-01");
//    lagInntektGrunnlag();
    lagBostatusGrunnlag();
    lagAntallBarnIEgetHusholdGrunnlag();
    lagSjablongGrunnlag();

    beregnBidragsevneGrunnlagAlt = new BeregnBidragsevneGrunnlagAlt(beregnDatoFra, beregnDatoTil,
        lagSjablongGrunnlag(), lagInntektGrunnlag(), lagBostatusGrunnlag(), lagAntallBarnIEgetHusholdGrunnlag());

    var resultat = bidragsevnePeriode.beregnPerioder(beregnBidragsevneGrunnlagAlt);
    assertThat(resultat).isNotNull();

    printGrunnlagResultat(resultat);


  }

  private List<InntektPeriode> lagInntektGrunnlag(){
    var inntektPeriode = new ArrayList<InntektPeriode>();

    inntektPeriode.add(new InntektPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        1, Double.valueOf(666000)));
    inntektPeriode.add(new InntektPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2015-12-31")),
        1, Double.valueOf(555000)));
    inntektPeriode.add(new InntektPeriode(
        new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2019-12-31")),
        1, Double.valueOf(444000)));

    return inntektPeriode;

//    beregnBidragsevneGrunnlag.setInntektPeriodeListe(inntektPeriode);

  }

  private List<BostatusPeriode> lagBostatusGrunnlag(){

    var bostatusPeriode = new ArrayList<BostatusPeriode>();

    bostatusPeriode.add(new BostatusPeriode(
        new Periode(LocalDate.parse("2001-01-01"), LocalDate.parse("2016-12-31")), Boolean.FALSE));

    bostatusPeriode.add(new BostatusPeriode(
        new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2019-12-31")), Boolean.TRUE));

    return bostatusPeriode;

//    beregnBidragsevneGrunnlag.setBostatusPeriodeListe(bostatusPeriode);

  }

  private List<AntallBarnIEgetHusholdPeriode> lagAntallBarnIEgetHusholdGrunnlag(){

    var antallBarnIEgetHusholdPeriode = new ArrayList<AntallBarnIEgetHusholdPeriode>();

    antallBarnIEgetHusholdPeriode.add(new AntallBarnIEgetHusholdPeriode(
        new Periode(LocalDate.parse("2001-01-01"), LocalDate.parse("2016-12-31")), 1));

    antallBarnIEgetHusholdPeriode.add(new AntallBarnIEgetHusholdPeriode(
        new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2019-12-31")), 2));

    return antallBarnIEgetHusholdPeriode;

//    beregnBidragsevneGrunnlag.setAntallBarnIEgetHusholdPeriodeListe(antallBarnIEgetHusholdPeriode);

  }


  private List<SjablonPeriode> lagSjablongGrunnlag() {

    var sjablonPeriode = new ArrayList<SjablonPeriode>();
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "fordelSkatteklasse2", Double.valueOf(8848), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-01-01"), LocalDate.parse("9999-12-31")),
        "fordelSkatteklasse2", Double.valueOf(0), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2013-12-31")),
        "satsTrygdeavgift", Double.valueOf(7.8), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("9999-12-31")),
        "satsTrygdeavgift", Double.valueOf(8.2), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3417), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-05-31")),
        "minstefradragBelop", Double.valueOf(57400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31")),
        "minstefradragBelop", Double.valueOf(75000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "minstefradragBelop", Double.valueOf(85050), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        "minstefradragProsentsats", Double.valueOf(31), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "personfradragKlasse1", Double.valueOf(54750), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "personfradragKlasse1", Double.valueOf(56550), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "personfradragKlasse2", Double.valueOf(54750), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "personfradragKlasse2", Double.valueOf(56550), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        "fordelSarfradrag", Double.valueOf(13132), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
        "fordelSarfradrag", Double.valueOf(12977), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattesats",
        Double.valueOf(23), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")), "skattesats",
        Double.valueOf(22), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattetrinn4",
        Double.valueOf(962050), Double.valueOf(15.4)));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn1",
        Double.valueOf(174500), Double.valueOf(1.9)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn2",
        Double.valueOf(245650), Double.valueOf(4.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn3",
        Double.valueOf(617500), Double.valueOf(13.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn4",
        Double.valueOf(964800), Double.valueOf(16.2)));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn1",
        Double.valueOf(180800), Double.valueOf(1.9)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn2",
        Double.valueOf(254500), Double.valueOf(4.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn3",
        Double.valueOf(639750), Double.valueOf(13.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn4",
        Double.valueOf(999550), Double.valueOf(16.2)));


    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopBoutgiftEn", Double.valueOf(9303), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(8657), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopBoutgiftGs", Double.valueOf(5698), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(7330), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopBoutgiftEn", Double.valueOf(9591), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgetEn", Double.valueOf(8925), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopBoutgiftGs", Double.valueOf(5875), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgetGs", Double.valueOf(7557), null));

    return sjablonPeriode;

//    beregnBidragsevneGrunnlag.setSjablonPeriodeListe(sjablonPeriode);


  }


  private void printGrunnlagResultat(BeregnBidragsevneResultat beregnBidragsevneResultat) {
    beregnBidragsevneResultat.getResultatPeriodeListe().stream().sorted(
        Comparator.comparing(pR -> pR.getResultatDatoFraTil().getDatoFra()))
        .forEach(sortedPR -> System.out
            .println("Dato fra: " + sortedPR.getResultatDatoFraTil().getDatoFra() + "; " + "Dato til: "
                + sortedPR.getResultatDatoFraTil().getDatoTil()
                + "; " + "Beløp: " + sortedPR.getResultatBeregning().getEvne()));
  }


//  private void lagSjablongGrunnlagAlt() {
  public static List<SjablonPeriode> lagSjablongGrunnlagAlt() {

    var sjablonPeriode = new ArrayList<SjablonPeriode>();
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "fordelSkatteklasse2", Double.valueOf(8848), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-12-31")),
        "fordelSkatteklasse2", Double.valueOf(9212), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-12-31")),
        "fordelSkatteklasse2", Double.valueOf(9576), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")),
        "fordelSkatteklasse2", Double.valueOf(9912), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-01-01"), LocalDate.parse("2007-12-31")),
        "fordelSkatteklasse2", Double.valueOf(10360), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-01-01"), LocalDate.parse("2008-12-31")),
        "fordelSkatteklasse2", Double.valueOf(10878), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-01-01"), LocalDate.parse("2009-12-31")),
        "fordelSkatteklasse2", Double.valueOf(11424), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-01-01"), LocalDate.parse("2010-12-31")),
        "fordelSkatteklasse2", Double.valueOf(11818), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-01-01"), LocalDate.parse("2011-12-31")),
        "fordelSkatteklasse2", Double.valueOf(12208), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-01-01"), LocalDate.parse("2012-06-30")),
        "fordelSkatteklasse2", Double.valueOf(12712), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2012-12-31")),
        "fordelSkatteklasse2", Double.valueOf(12712), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-01-01"), LocalDate.parse("9999-12-31")),
        "fordelSkatteklasse2", Double.valueOf(0), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2013-12-31")),
        "satsTrygdeavgift", Double.valueOf(7.8), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("9999-12-31")),
        "satsTrygdeavgift", Double.valueOf(8.2), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(1560), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(1880), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-07-01"), LocalDate.parse("2005-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(1840), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-07-01"), LocalDate.parse("2006-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2030), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-07-01"), LocalDate.parse("2007-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(1960), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-07-01"), LocalDate.parse("2008-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2170), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-07-01"), LocalDate.parse("2008-12-31")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2380), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-01-01"), LocalDate.parse("2009-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2470), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2614), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2824), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2886), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2931), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(2943), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3091), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3150), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3294), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3365), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3417), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgneBarnIHusstand", Double.valueOf(3487), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "minstefradragBelop", Double.valueOf(45700), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-12-31")),
        "minstefradragBelop", Double.valueOf(47500), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-05-31")),
        "minstefradragBelop", Double.valueOf(57400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-06-01"), LocalDate.parse("2005-12-31")),
        "minstefradragBelop", Double.valueOf(49400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")),
        "minstefradragBelop", Double.valueOf(51100), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-01-01"), LocalDate.parse("2007-12-31")),
        "minstefradragBelop", Double.valueOf(53400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-01-01"), LocalDate.parse("2009-06-30")),
        "minstefradragBelop", Double.valueOf(56100), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "minstefradragBelop", Double.valueOf(58900), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "minstefradragBelop", Double.valueOf(60950), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "minstefradragBelop", Double.valueOf(62950), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "minstefradragBelop", Double.valueOf(65450), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "minstefradragBelop", Double.valueOf(68050), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "minstefradragBelop", Double.valueOf(70400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "minstefradragBelop", Double.valueOf(72200), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "minstefradragBelop", Double.valueOf(73600), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31")),
        "minstefradragBelop", Double.valueOf(75000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-06-30")),
        "minstefradragBelop", Double.valueOf(75000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "minstefradragBelop", Double.valueOf(83000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "minstefradragBelop", Double.valueOf(85050), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2004-12-31")),
        "minstefradragProsentsats", Double.valueOf(24), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-05-31")),
        "minstefradragProsentsats", Double.valueOf(31), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-06-01"), LocalDate.parse("2007-12-31")),
        "minstefradragProsentsats", Double.valueOf(24), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-01-01"), LocalDate.parse("2013-12-31")),
        "minstefradragProsentsats", Double.valueOf(26), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("2014-12-31")),
        "minstefradragProsentsats", Double.valueOf(27), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-01-01"), LocalDate.parse("2017-12-31")),
        "minstefradragProsentsats", Double.valueOf(29), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
        "minstefradragProsentsats", Double.valueOf(31), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "personfradragKlasse1", Double.valueOf(31600), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-12-31")),
        "personfradragKlasse1", Double.valueOf(32900), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-12-31")),
        "personfradragKlasse1", Double.valueOf(34200), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")),
        "personfradragKlasse1", Double.valueOf(35400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-01-01"), LocalDate.parse("2007-12-31")),
        "personfradragKlasse1", Double.valueOf(37000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-01-01"), LocalDate.parse("2009-06-30")),
        "personfradragKlasse1", Double.valueOf(38850), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "personfradragKlasse1", Double.valueOf(40800), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "personfradragKlasse1", Double.valueOf(42210), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "personfradragKlasse1", Double.valueOf(43600), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "personfradragKlasse1", Double.valueOf(45300), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2013-12-31")),
        "personfradragKlasse1", Double.valueOf(47150), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("2014-06-30")),
        "personfradragKlasse1", Double.valueOf(47150), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "personfradragKlasse1", Double.valueOf(48800), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "personfradragKlasse1", Double.valueOf(50400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "personfradragKlasse1", Double.valueOf(51750), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31")),
        "personfradragKlasse1", Double.valueOf(53150), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-06-30")),
        "personfradragKlasse1", Double.valueOf(53150), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "personfradragKlasse1", Double.valueOf(54750), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "personfradragKlasse1", Double.valueOf(56550), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "personfradragKlasse2", Double.valueOf(63200), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-12-31")),
        "personfradragKlasse2", Double.valueOf(65800), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-01-01"), LocalDate.parse("2005-12-31")),
        "personfradragKlasse2", Double.valueOf(68400), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")),
        "personfradragKlasse2", Double.valueOf(70800), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-01-01"), LocalDate.parse("2007-12-31")),
        "personfradragKlasse2", Double.valueOf(74000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-01-01"), LocalDate.parse("2009-06-30")),
        "personfradragKlasse2", Double.valueOf(77700), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "personfradragKlasse2", Double.valueOf(81600), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "personfradragKlasse2", Double.valueOf(84420), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "personfradragKlasse2", Double.valueOf(87200), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "personfradragKlasse2", Double.valueOf(90700), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2013-12-31")),
        "personfradragKlasse2", Double.valueOf(94300), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("2015-06-30")),
        "personfradragKlasse2", Double.valueOf(72000), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "personfradragKlasse2", Double.valueOf(74250), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "personfradragKlasse2", Double.valueOf(76250), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "personfradragKlasse2", Double.valueOf(78300), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "personfradragKlasse2", Double.valueOf(54750), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "personfradragKlasse2", Double.valueOf(56550), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "fordelSarfradrag", Double.valueOf(13205), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-01-01"), LocalDate.parse("2015-12-31")),
        "fordelSarfradrag", Double.valueOf(13177), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31")),
        "fordelSarfradrag", Double.valueOf(13505), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31")),
        "fordelSarfradrag", Double.valueOf(13298), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
        "fordelSarfradrag", Double.valueOf(13132), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")),
        "fordelSarfradrag", Double.valueOf(12977), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattesats",
        Double.valueOf(23), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31")), "skattesats",
        Double.valueOf(22), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattetrinn1",
        Double.valueOf(169000), Double.valueOf(1.4)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattetrinn2",
        Double.valueOf(237900), Double.valueOf(3.3)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattetrinn3",
        Double.valueOf(598050), Double.valueOf(12.4)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")), "skattetrinn4",
        Double.valueOf(962050), Double.valueOf(15.4)));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn1",
        Double.valueOf(174500), Double.valueOf(1.9)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn2",
        Double.valueOf(245650), Double.valueOf(4.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn3",
        Double.valueOf(617500), Double.valueOf(13.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31")), "skattetrinn4",
        Double.valueOf(964800), Double.valueOf(16.2)));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn1",
        Double.valueOf(180800), Double.valueOf(1.9)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn2",
        Double.valueOf(254500), Double.valueOf(4.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn3",
        Double.valueOf(639750), Double.valueOf(13.2)));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")), "skattetrinn4",
        Double.valueOf(999550), Double.valueOf(16.2)));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "belopBoutgiftEn", Double.valueOf(4313), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "belopUnderholdEgetEn", Double.valueOf(6245), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "belopBoutgiftGs", Double.valueOf(2631), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31")),
        "belopUnderholdEgetGs", Double.valueOf(5287), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-06-30")),
        "belopBoutgiftEn", Double.valueOf(4313), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(6565), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-06-30")),
        "belopBoutgiftGs", Double.valueOf(2631), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-01-01"), LocalDate.parse("2004-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(5558), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-07-01"), LocalDate.parse("2005-06-30")),
        "belopBoutgiftEn", Double.valueOf(4761), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-07-01"), LocalDate.parse("2005-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(6565), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-07-01"), LocalDate.parse("2005-06-30")),
        "belopBoutgiftGs", Double.valueOf(2826), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2004-07-01"), LocalDate.parse("2005-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(5558), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-07-01"), LocalDate.parse("2006-06-30")),
        "belopBoutgiftEn", Double.valueOf(4544), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-07-01"), LocalDate.parse("2006-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(6637), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-07-01"), LocalDate.parse("2006-06-30")),
        "belopBoutgiftGs", Double.valueOf(2806), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2005-07-01"), LocalDate.parse("2006-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(5619), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-07-01"), LocalDate.parse("2007-06-30")),
        "belopBoutgiftEn", Double.valueOf(4556), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-07-01"), LocalDate.parse("2007-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(6756), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-07-01"), LocalDate.parse("2007-06-30")),
        "belopBoutgiftGs", Double.valueOf(3040), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2006-07-01"), LocalDate.parse("2007-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(5720), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-07-01"), LocalDate.parse("2008-06-30")),
        "belopBoutgiftEn", Double.valueOf(4638), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-07-01"), LocalDate.parse("2008-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(6837), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-07-01"), LocalDate.parse("2008-06-30")),
        "belopBoutgiftGs", Double.valueOf(3364), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2007-07-01"), LocalDate.parse("2008-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(5789), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-07-01"), LocalDate.parse("2009-06-30")),
        "belopBoutgiftEn", Double.valueOf(5243), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-07-01"), LocalDate.parse("2009-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7090), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-07-01"), LocalDate.parse("2009-06-30")),
        "belopBoutgiftGs", Double.valueOf(3906), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2008-07-01"), LocalDate.parse("2009-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6003), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "belopBoutgiftEn", Double.valueOf(6113), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7246), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "belopBoutgiftGs", Double.valueOf(4323), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2009-07-01"), LocalDate.parse("2010-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6135), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "belopBoutgiftEn", Double.valueOf(6495), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7427), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "belopBoutgiftGs", Double.valueOf(4488), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2010-07-01"), LocalDate.parse("2011-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6288), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "belopBoutgiftEn", Double.valueOf(7259), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7576), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "belopBoutgiftGs", Double.valueOf(4776), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6414), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "belopBoutgiftEn", Double.valueOf(7295), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7614), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "belopBoutgiftGs", Double.valueOf(4800), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6446), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "belopBoutgiftEn", Double.valueOf(7390), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7713), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "belopBoutgiftGs", Double.valueOf(4862), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2013-07-01"), LocalDate.parse("2014-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6530), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "belopBoutgiftEn", Double.valueOf(7560), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(7890), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "belopBoutgiftGs", Double.valueOf(4974), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2014-07-01"), LocalDate.parse("2015-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6680), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "belopBoutgiftEn", Double.valueOf(7711), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(8048), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "belopBoutgiftGs", Double.valueOf(5073), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(6814), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "belopBoutgiftEn", Double.valueOf(8907), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(8289), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "belopBoutgiftGs", Double.valueOf(5456), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(7018), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "belopBoutgiftEn", Double.valueOf(9156), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(8521), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "belopBoutgiftGs", Double.valueOf(5609), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(7215), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopBoutgiftEn", Double.valueOf(9303), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgetEn", Double.valueOf(8657), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopBoutgiftGs", Double.valueOf(5698), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30")),
        "belopUnderholdEgetGs", Double.valueOf(7330), null));

    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopBoutgiftEn", Double.valueOf(9591), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgetEn", Double.valueOf(8925), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopBoutgiftGs", Double.valueOf(5875), null));
    sjablonPeriode.add(new SjablonPeriode(
        new Periode(LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31")),
        "belopUnderholdEgetGs", Double.valueOf(7557), null));

//    bidragsevnePeriodeGrunnlag.setSjablonPeriodeListe(sjablonPeriode);
    return sjablonPeriode;


  }


}