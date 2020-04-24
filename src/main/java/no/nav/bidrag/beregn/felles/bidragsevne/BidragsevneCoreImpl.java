package no.nav.bidrag.beregn.felles.bidragsevne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneResultatCore;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BostatusPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.InntektPeriodeCore;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;

public class BidragsevneCoreImpl implements BidragsevneCore {

//  private BidragsevnePeriode bidragsevnePeriode = BidragsevnePeriode.getInstance();

  public BidragsevneCoreImpl(BidragsevnePeriode bidragsevnePeriode) {
    this.bidragsevnePeriode = bidragsevnePeriode;
  }

  private BidragsevnePeriode bidragsevnePeriode;

  public BeregnBidragsevneResultatCore beregnBidragsevne(BeregnBidragsevneGrunnlagAltCore beregnBidragsevneGrunnlagAltCore) {
    var beregnBidragsevneGrunnlag = mapTilBusinessObject(beregnBidragsevneGrunnlagAltCore);
    var beregnBidragsevneResultat = new BeregnBidragsevneResultat(Collections.emptyList());
    var avvikListe = bidragsevnePeriode.validerInput(beregnBidragsevneGrunnlag);
    if (avvikListe.isEmpty()) {
      beregnBidragsevneResultat = bidragsevnePeriode.beregnPerioder(beregnBidragsevneGrunnlag);
    }
    return mapFraBusinessObject(avvikListe, beregnBidragsevneResultat);
  }

  private BeregnBidragsevneGrunnlagAlt mapTilBusinessObject(BeregnBidragsevneGrunnlagAltCore grunnlag) {
    var beregnDatoFra = grunnlag.getBeregnDatoFra();
    var beregnDatoTil = grunnlag.getBeregnDatoTil();
    var soknadBarn = mapSoknadBarn(grunnlag.getSoknadBarn());
    var bMInntektPeriodeListe = mapBidragMottakerInntektPeriodeListe(grunnlag.getBidragMottakerInntektPeriodeListe());
    var bMSivilstandPeriodeListe = mapBidragMottakerSivilstandPeriodeListe(grunnlag.getBidragMottakerSivilstandPeriodeListe());
    var bMBarnPeriodeListe = mapBidragMottakerBarnPeriodeListe(grunnlag.getBidragMottakerBarnPeriodeListe());
    var sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.getSjablonPeriodeListe());
    return new BeregnBidragsevneGrunnlagAlt(beregnDatoFra, beregnDatoTil, soknadBarn, bMInntektPeriodeListe, bMSivilstandPeriodeListe, bMBarnPeriodeListe,
        sjablonPeriodeListe);
  }

  private SoknadBarn mapSoknadBarn(SoknadBarnCore soknadBarnCore) {
    var sBFodselsdato = soknadBarnCore.getSoknadBarnFodselsdato();
    var sBBostatusPeriodeListe = mapSoknadBarnBostatusPeriodeListe(soknadBarnCore.getSoknadBarnBostatusPeriodeListe());
    return new SoknadBarn(sBFodselsdato, sBBostatusPeriodeListe);
  }

  private List<BostatusPeriode> mapSoknadBarnBostatusPeriodeListe(List<BostatusPeriodeCore> bidragMottakerBostatusPeriodeListeCore) {
    var bidragMottakerBostatusPeriodeListe = new ArrayList<BostatusPeriode>();
    for (BostatusPeriodeCore bidragMottakerBostatusPeriodeCore : bidragMottakerBostatusPeriodeListeCore) {
      bidragMottakerBostatusPeriodeListe.add(new BostatusPeriode(
          new Periode(bidragMottakerBostatusPeriodeCore.getBostatusDatoFraTil().getPeriodeDatoFra(),
              bidragMottakerBostatusPeriodeCore.getBostatusDatoFraTil().getPeriodeDatoTil()),
          BostatusKode.valueOf(bidragMottakerBostatusPeriodeCore.getBostatusKode())));
    }
    return bidragMottakerBostatusPeriodeListe;
  }

  private List<InntektPeriode> mapBidragMottakerInntektPeriodeListe(List<InntektPeriodeCore> bidragMottakerInntektPeriodeListeCore) {
    var bidragMottakerInntektPeriodeListe = new ArrayList<InntektPeriode>();
    for (InntektPeriodeCore bidragMottakerInntektPeriodeCore : bidragMottakerInntektPeriodeListeCore) {
      bidragMottakerInntektPeriodeListe.add(new InntektPeriode(
          new Periode(bidragMottakerInntektPeriodeCore.getInntektDatoFraTil().getPeriodeDatoFra(),
              bidragMottakerInntektPeriodeCore.getInntektDatoFraTil().getPeriodeDatoTil()),
          bidragMottakerInntektPeriodeCore.getInntektBelop()));
    }
    return bidragMottakerInntektPeriodeListe;
  }

  private List<SivilstandPeriode> mapBidragMottakerSivilstandPeriodeListe(List<SivilstandPeriodeCore> bidragMottakerSivilstandPeriodeListeCore) {
    var bidragMottakerSivilstandPeriodeListe = new ArrayList<SivilstandPeriode>();
    for (SivilstandPeriodeCore bidragMottakerSivilstandPeriodeCore : bidragMottakerSivilstandPeriodeListeCore) {
      bidragMottakerSivilstandPeriodeListe.add(new SivilstandPeriode(
          new Periode(bidragMottakerSivilstandPeriodeCore.getSivilstandDatoFraTil().getPeriodeDatoFra(),
              bidragMottakerSivilstandPeriodeCore.getSivilstandDatoFraTil().getPeriodeDatoTil()),
          SivilstandKode.valueOf(bidragMottakerSivilstandPeriodeCore.getSivilstandKode())));
    }
    return bidragMottakerSivilstandPeriodeListe;
  }

  private List<Periode> mapBidragMottakerBarnPeriodeListe(List<PeriodeCore> bidragMottakerBarnPeriodeListeCore) {
    var bidragMottakerBarnPeriodeListe = new ArrayList<Periode>();
    for (PeriodeCore bidragMottakerBarnPeriodeCore : bidragMottakerBarnPeriodeListeCore) {
      bidragMottakerBarnPeriodeListe
          .add(new Periode(bidragMottakerBarnPeriodeCore.getPeriodeDatoFra(), bidragMottakerBarnPeriodeCore.getPeriodeDatoTil()));
    }
    return bidragMottakerBarnPeriodeListe;
  }

  private List<SjablonPeriode> mapSjablonPeriodeListe(List<SjablonPeriodeCore> sjablonPeriodeListeCore) {
    var sjablonPeriodeListe = new ArrayList<SjablonPeriode>();
    for (SjablonPeriodeCore sjablonPeriodeCore : sjablonPeriodeListeCore) {
      sjablonPeriodeListe.add(new SjablonPeriode(
          new Periode(sjablonPeriodeCore.getSjablonDatoFraTil().getPeriodeDatoFra(), sjablonPeriodeCore.getSjablonDatoFraTil().getPeriodeDatoTil()),
          sjablonPeriodeCore.getSjablonType(), sjablonPeriodeCore.getSjablonVerdi().intValue()));
    }
    return sjablonPeriodeListe;
  }

  private BeregnBidragsevneResultatCore mapFraBusinessObject(List<Avvik> avvikListe, BeregnBidragsevneResultat resultat) {
    return new BeregnBidragsevneResultatCore(mapResultatPeriode(resultat.getResultatPeriodeListe()), mapAvvik(avvikListe));
  }

  private List<AvvikCore> mapAvvik(List<Avvik> avvikListe) {
    var avvikCoreListe = new ArrayList<AvvikCore>();
    for (Avvik avvik : avvikListe) {
      avvikCoreListe.add(new AvvikCore(avvik.getAvvikTekst(), avvik.getAvvikType().toString()));
    }
    return avvikCoreListe;
  }

  private List<ResultatPeriodeCore> mapResultatPeriode(List<ResultatPeriode> periodeResultatListe) {
    var resultatPeriodeCoreListe = new ArrayList<ResultatPeriodeCore>();
    for (ResultatPeriode periodeResultat : periodeResultatListe) {
      var BidragsevneBeregningResultat = periodeResultat.getResultatBeregning();
      resultatPeriodeCoreListe.add(new ResultatPeriodeCore(
          new PeriodeCore(periodeResultat.getResultatDatoFraTil().getDatoFra(), periodeResultat.getResultatDatoFraTil().getDatoTil()),
          new ResultatBeregningCore(BidragsevneBeregningResultat.getResultatBelop(), BidragsevneBeregningResultat.getResultatKode().toString(),
              BidragsevneBeregningResultat.getResultatBeskrivelse())));
    }
    return resultatPeriodeCoreListe;
  }
}

}
