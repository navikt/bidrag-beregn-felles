package no.nav.bidrag.beregn.felles.bidragsevne.periode;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import no.nav.bidrag.beregn.felles.bidragsevne.beregning.Bidragsevneberegning;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.AntallBarnIEgetHusholdPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.BidragsevnePeriodeGrunnlag;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.grunnlag.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.resultat.BidragsevnePeriodeResultat;

public class BidragsevnePeriodeImpl implements BidragsevnePeriode {

  private Bidragsevneberegning bidragsevneberegning = Bidragsevneberegning.getInstance();

  public BidragsevnePeriodeResultat beregnPerioder(BidragsevnePeriodeGrunnlag bidragsevnePeriodeGrunnlag) {

    var justertSjablonPeriodeListe = bidragsevnePeriodeGrunnlag.getSjablonPeriodeListe().stream()
        .map(sP -> new SjablonPeriode(PeriodeUtil.justerPeriode(sP.getDatoFraTil()), sP.getSjablonnavn(),
            sP.getSjablonVerdi1(), sP.getSjablonVerdi2())).collect(toCollection(ArrayList::new));

    var justertInntektPeriodeListe = bidragsevnePeriodeGrunnlag.getInntektPeriodeListe().stream()
        .map(iP -> new InntektPeriode(PeriodeUtil.justerPeriode(iP.getDatoFraTil()), iP.getSkatteklasse(),
            iP.getBelop())).collect(toCollection(ArrayList::new));

    var justertBostatusPeriodeListe = bidragsevnePeriodeGrunnlag.getBostatusPeriodeListe().stream()
        .map(bP -> new BostatusPeriode(PeriodeUtil.justerPeriode(bP.getDatoFraTil()), bP.getBorAlene()))
            .collect(toCollection(ArrayList::new));

    var justertAntallBarnIEgetHusholdPeriodeListe = bidragsevnePeriodeGrunnlag.getAntallBarnIEgetHusholdPeriodeListe().stream()
        .map(aP -> new AntallBarnIEgetHusholdPeriode(PeriodeUtil.justerPeriode(aP.getDatoFraTil()), aP.getAntallBarn()))
        .collect(toCollection(ArrayList::new));



    return null;

  }
}
