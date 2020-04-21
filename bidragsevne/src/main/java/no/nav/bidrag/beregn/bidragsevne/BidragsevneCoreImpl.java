package no.nav.bidrag.beregn.bidragsevne;

import java.util.Collections;
import no.nav.bidrag.beregn.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore;
import no.nav.bidrag.beregn.bidragsevne.periode.BidragsevnePeriode;

public class BidragsevneCoreImpl implements BidragsevneCore {

  private BidragsevnePeriode bidragsevnePeriode = BidragsevnePeriode.getInstance();

  public BeregnBidragsevneResultatCore beregnBidragsevne(BeregnBidragsevneGrunnlagAltCore grunnlag) {
    var beregnBidragsevneGrunnlag = mapTilBusinessObject(grunnlag);
    var beregnBidragsevneResultat = new BeregnBidragsevneResultat(Collections.emptyList());
    var avvikListe = bidragsevnePeriode.validerInput(beregnBidragsevneGrunnlag);
    if (avvikListe.isEmpty()) {
      beregnBidragsevneResultat = bidragsevnePeriode.beregnPerioder(beregnBidragsevneGrunnlag);
    }
    return mapFraBusinessObject(avvikListe, beregnBidragsevneResultat);
  }

  private BeregnBidragsevneGrunnlag mapTilBusinessObject(BeregnBidragsevneGrunnlagAltCore grunnlag) {
    var beregnDatoFra = grunnlag.getBeregnDatoFra();
    var beregnDatoTil = grunnlag.getBeregnDatoTil();
    var soknadBarn = mapSoknadBarn(grunnlag.getSoknadBarn());
    var bMInntektPeriodeListe = mapBidragMottakerInntektPeriodeListe(grunnlag.getBidragMottakerInntektPeriodeListe());
    var bMSivilstandPeriodeListe = mapBidragMottakerSivilstandPeriodeListe(grunnlag.getBidragMottakerSivilstandPeriodeListe());
    var bMBarnPeriodeListe = mapBidragMottakerBarnPeriodeListe(grunnlag.getBidragMottakerBarnPeriodeListe());
    var sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.getSjablonPeriodeListe());
    return new BeregnBidragsevneGrunnlag(beregnDatoFra, beregnDatoTil, soknadBarn, bMInntektPeriodeListe, bMSivilstandPeriodeListe, bMBarnPeriodeListe,
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
