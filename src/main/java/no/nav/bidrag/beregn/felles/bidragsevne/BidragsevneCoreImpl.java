package no.nav.bidrag.beregn.felles.bidragsevne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.AntallBarnIEgetHusholdPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Avvik;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneGrunnlagAlt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BeregnBidragsevneResultat;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.BostatusPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Inntekt;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.InntektPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.ResultatPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SaerfradragPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.Sjablon;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriode;
import no.nav.bidrag.beregn.felles.bidragsevne.bo.SjablonPeriodeNy;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.AntallBarnIEgetHusholdPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.AvvikCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneGrunnlagAltCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BeregnBidragsevneResultatCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.BostatusPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.InntektCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.InntektPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.PeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.ResultatBeregningCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.ResultatGrunnlagCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.ResultatPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SaerfradragPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SjablonCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SjablonInnholdCoreNy;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SjablonNokkelCoreNy;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SjablonPeriodeCore;
import no.nav.bidrag.beregn.felles.bidragsevne.dto.SjablonPeriodeCoreNy;
import no.nav.bidrag.beregn.felles.bidragsevne.periode.BidragsevnePeriode;
import no.nav.bidrag.beregn.felles.bo.Periode;
import no.nav.bidrag.beregn.felles.bo.SjablonInnholdNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNokkelNy;
import no.nav.bidrag.beregn.felles.bo.SjablonNy;
import no.nav.bidrag.beregn.felles.enums.BostatusKode;
import no.nav.bidrag.beregn.felles.enums.InntektType;
import no.nav.bidrag.beregn.felles.enums.SaerfradragKode;

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

  private BeregnBidragsevneGrunnlagAlt mapTilBusinessObject(BeregnBidragsevneGrunnlagAltCore beregnBidragsevneGrunnlagAltCore) {
    var beregnDatoFra = beregnBidragsevneGrunnlagAltCore.getBeregnDatoFra();
    var beregnDatoTil = beregnBidragsevneGrunnlagAltCore.getBeregnDatoTil();
    var inntektPeriodeListe = mapInntektPeriodeListe(beregnBidragsevneGrunnlagAltCore.getInntektPeriodeListe());
    var bostatusPeriodeListe = mapBostatusPeriodeListe(beregnBidragsevneGrunnlagAltCore.getBostatusPeriodeListe());
    var antallBarnIEgetHusholdPeriodeListe = mapAntallBarnIEgetHusholdPeriodeListe(
        beregnBidragsevneGrunnlagAltCore.getAntallBarnIEgetHusholdPeriodeListe());
    var saerfradragPeriodeListe = mapSaerfradragPeriodeListe(beregnBidragsevneGrunnlagAltCore.getSaerfradragPeriodeListe());
    var sjablonPeriodeListe = mapSjablonPeriodeListe(beregnBidragsevneGrunnlagAltCore.getSjablonPeriodeListe());
    var sjablonPeriodeListeNy = mapSjablonPeriodeListeNy(beregnBidragsevneGrunnlagAltCore.getSjablonPeriodeListeNy());
    return new BeregnBidragsevneGrunnlagAlt(beregnDatoFra, beregnDatoTil, inntektPeriodeListe, bostatusPeriodeListe,
        antallBarnIEgetHusholdPeriodeListe, saerfradragPeriodeListe, sjablonPeriodeListe, sjablonPeriodeListeNy);
  }

  private List<SjablonPeriode> mapSjablonPeriodeListe(List<SjablonPeriodeCore> sjablonPeriodeListeCore) {
    var sjablonPeriodeListe = new ArrayList<SjablonPeriode>();
    for (SjablonPeriodeCore sjablonPeriodeCore : sjablonPeriodeListeCore) {
      sjablonPeriodeListe.add(new SjablonPeriode(
          new Periode(sjablonPeriodeCore.getSjablonPeriodeDatoFraTil().getPeriodeDatoFra(),
              sjablonPeriodeCore.getSjablonPeriodeDatoFraTil().getPeriodeDatoTil()),
          sjablonPeriodeCore.getSjablonnavn(), sjablonPeriodeCore.getSjablonVerdi1(), sjablonPeriodeCore.getSjablonVerdi2()));
    }
    return sjablonPeriodeListe;
  }

  private List<SjablonPeriodeNy> mapSjablonPeriodeListeNy(List<SjablonPeriodeCoreNy> sjablonPeriodeListeCoreNy) {
    var sjablonPeriodeListeNy = new ArrayList<SjablonPeriodeNy>();
    for (SjablonPeriodeCoreNy sjablonPeriodeCoreNy : sjablonPeriodeListeCoreNy) {
      var sjablonNokkelListeNy = new ArrayList<SjablonNokkelNy>();
      var sjablonInnholdListeNy = new ArrayList<SjablonInnholdNy>();
      for (SjablonNokkelCoreNy sjablonNokkelCoreNy : sjablonPeriodeCoreNy.getSjablonNokkelListe()) {
        sjablonNokkelListeNy.add(new SjablonNokkelNy(sjablonNokkelCoreNy.getSjablonNokkelNavn(), sjablonNokkelCoreNy.getSjablonNokkelVerdi()));
      }
      for (SjablonInnholdCoreNy sjablonInnholdCoreNy : sjablonPeriodeCoreNy.getSjablonInnholdListe()) {
        sjablonInnholdListeNy.add(new SjablonInnholdNy(sjablonInnholdCoreNy.getSjablonInnholdNavn(), sjablonInnholdCoreNy.getSjablonInnholdVerdi()));
      }
      sjablonPeriodeListeNy.add(new SjablonPeriodeNy(
          new Periode(sjablonPeriodeCoreNy.getSjablonPeriodeDatoFraTil().getPeriodeDatoFra(),
              sjablonPeriodeCoreNy.getSjablonPeriodeDatoFraTil().getPeriodeDatoTil()),
          new SjablonNy(sjablonPeriodeCoreNy.getSjablonNavn(), sjablonNokkelListeNy, sjablonInnholdListeNy)));
    }
    return sjablonPeriodeListeNy;
  }

  private List<InntektPeriode> mapInntektPeriodeListe(List<InntektPeriodeCore> inntektPeriodeListeCore) {
    var inntektPeriodeListe = new ArrayList<InntektPeriode>();
    for (InntektPeriodeCore inntektPeriodeCore : inntektPeriodeListeCore) {
      inntektPeriodeListe.add(new InntektPeriode(
          new Periode(inntektPeriodeCore.getInntektPeriodeDatoFraTil().getPeriodeDatoFra(),
              inntektPeriodeCore.getInntektPeriodeDatoFraTil().getPeriodeDatoTil()),
          InntektType.valueOf(inntektPeriodeCore.getInntektType()),
          inntektPeriodeCore.getSkatteklasse(), inntektPeriodeCore.getInntektBelop()));
    }
    return inntektPeriodeListe;
  }

  private List<BostatusPeriode> mapBostatusPeriodeListe(List<BostatusPeriodeCore> bostatusPeriodeListeCore) {
    var bostatusPeriodeListe = new ArrayList<BostatusPeriode>();
    for (BostatusPeriodeCore bostatusPeriodeCore : bostatusPeriodeListeCore) {
      bostatusPeriodeListe.add(new BostatusPeriode(
          new Periode(bostatusPeriodeCore.getBostatusPeriodeDatoFraTil().getPeriodeDatoFra(),
              bostatusPeriodeCore.getBostatusPeriodeDatoFraTil().getPeriodeDatoTil()),
          BostatusKode.valueOf(bostatusPeriodeCore.getBostatusKode())));
    }
    return bostatusPeriodeListe;
  }

  private List<AntallBarnIEgetHusholdPeriode> mapAntallBarnIEgetHusholdPeriodeListe(
      List<AntallBarnIEgetHusholdPeriodeCore> antallBarnIEgetHusholdPeriodeListeCore) {
    var antallBarnIEgetHusholdPeriodeListe = new ArrayList<AntallBarnIEgetHusholdPeriode>();
    for (AntallBarnIEgetHusholdPeriodeCore antallBarnIEgetHusholdPeriodeCore : antallBarnIEgetHusholdPeriodeListeCore) {
      antallBarnIEgetHusholdPeriodeListe.add(new AntallBarnIEgetHusholdPeriode(
          new Periode(antallBarnIEgetHusholdPeriodeCore.getAntallBarnIEgetHusholdPeriodeDatoFraTil()
              .getPeriodeDatoFra(),
              antallBarnIEgetHusholdPeriodeCore.getAntallBarnIEgetHusholdPeriodeDatoFraTil()
                  .getPeriodeDatoTil()),
          antallBarnIEgetHusholdPeriodeCore.getAntallBarn()));
    }
    return antallBarnIEgetHusholdPeriodeListe;
  }

  private List<SaerfradragPeriode> mapSaerfradragPeriodeListe(List<SaerfradragPeriodeCore> saerfradragPeriodeListeCore) {
    var saerfradragPeriodeListe = new ArrayList<SaerfradragPeriode>();
    for (SaerfradragPeriodeCore saerfradragPeriodeCore : saerfradragPeriodeListeCore) {
      saerfradragPeriodeListe.add(new SaerfradragPeriode(
          new Periode(saerfradragPeriodeCore.getSaerfradragPeriodeDatoFraTil().getPeriodeDatoFra(),
              saerfradragPeriodeCore.getSaerfradragPeriodeDatoFraTil().getPeriodeDatoTil()),
          SaerfradragKode.valueOf(saerfradragPeriodeCore.getSaerfradragKode())));
    }
    return saerfradragPeriodeListe;
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
      var bidragsevneResultat = periodeResultat.getResultatBeregning();
      var bidragsevneResultatGrunnlag = periodeResultat.getResultatGrunnlag();
      resultatPeriodeCoreListe.add(new ResultatPeriodeCore(
          new PeriodeCore(periodeResultat.getResultatDatoFraTil().getDatoFra(), periodeResultat.getResultatDatoFraTil().getDatoTil()),
          new ResultatBeregningCore(bidragsevneResultat.getResultatBelopEvne()),
          new ResultatGrunnlagCore(mapResultatGrunnlagInntekt(bidragsevneResultatGrunnlag.getInntektListe()),
              bidragsevneResultatGrunnlag.getSkatteklasse(),
              bidragsevneResultatGrunnlag.getBostatusKode().toString(),
              bidragsevneResultatGrunnlag.getAntallEgneBarnIHusstand(),
              bidragsevneResultatGrunnlag.getSaerfradragkode().toString(),
              mapResultatGrunnlagSjabloner(bidragsevneResultatGrunnlag.getSjablonListe()))));
    }
    return resultatPeriodeCoreListe;
  }

  private List<InntektCore> mapResultatGrunnlagInntekt(List<Inntekt> resultatGrunnlagInntektListe) {
    var resultatGrunnlagInntektListeCore = new ArrayList<InntektCore>();
    for (Inntekt resultatGrunnlagInntekt : resultatGrunnlagInntektListe) {
      resultatGrunnlagInntektListeCore
          .add(new InntektCore(resultatGrunnlagInntekt.getInntektType().toString(), resultatGrunnlagInntekt.getInntektBelop()));
    }
    return resultatGrunnlagInntektListeCore;
  }

  private List<SjablonCore> mapResultatGrunnlagSjabloner(List<Sjablon> resultatGrunnlagSjablonListe) {
    var resultatGrunnlagSjablonListeCore = new ArrayList<SjablonCore>();
    for (Sjablon resultatGrunnlagSjablon : resultatGrunnlagSjablonListe) {
      resultatGrunnlagSjablonListeCore
          .add(new SjablonCore(resultatGrunnlagSjablon.getSjablonnavn(), resultatGrunnlagSjablon.getSjablonVerdi1(),
              resultatGrunnlagSjablon.getSjablonVerdi2()));
    }
    return resultatGrunnlagSjablonListeCore;
  }

}