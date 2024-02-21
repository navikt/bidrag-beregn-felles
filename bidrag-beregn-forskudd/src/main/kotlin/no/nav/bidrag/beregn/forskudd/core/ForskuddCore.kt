package no.nav.bidrag.beregn.forskudd.core

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.bo.BarnIHusstandenPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddGrunnlag
import no.nav.bidrag.beregn.forskudd.core.bo.BeregnForskuddResultat
import no.nav.bidrag.beregn.forskudd.core.bo.BostatusPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.InntektPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.ResultatPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.SivilstandPeriode
import no.nav.bidrag.beregn.forskudd.core.bo.Søknadsbarn
import no.nav.bidrag.beregn.forskudd.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnForskuddGrunnlagCore
import no.nav.bidrag.beregn.forskudd.core.dto.BeregnetForskuddResultatCore
import no.nav.bidrag.beregn.forskudd.core.dto.BostatusPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.forskudd.core.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SivilstandPeriodeCore
import no.nav.bidrag.beregn.forskudd.core.dto.SøknadsbarnCore
import no.nav.bidrag.beregn.forskudd.core.periode.ForskuddPeriode
import no.nav.bidrag.domene.enums.person.AldersgruppeForskudd
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import java.time.format.DateTimeFormatter

internal class ForskuddCore(private val forskuddPeriode: ForskuddPeriode = ForskuddPeriode()) {
    fun beregnForskudd(grunnlag: BeregnForskuddGrunnlagCore): BeregnetForskuddResultatCore {
        val beregnForskuddGrunnlag = mapTilBusinessObject(grunnlag)
        val avvikListe = forskuddPeriode.validerInput(beregnForskuddGrunnlag)
        val beregnForskuddResultat =
            if (avvikListe.isEmpty()) {
                forskuddPeriode.beregnPerioder(beregnForskuddGrunnlag)
            } else {
                BeregnForskuddResultat(emptyList())
            }
        return mapFraBusinessObject(avvikListe = avvikListe, resultat = beregnForskuddResultat)
    }

    private fun mapTilBusinessObject(grunnlag: BeregnForskuddGrunnlagCore) = BeregnForskuddGrunnlag(
        beregnDatoFra = grunnlag.beregnDatoFra,
        beregnDatoTil = grunnlag.beregnDatoTil,
        søknadsbarn = mapSøknadsbarn(grunnlag.søknadsbarn),
        bostatusPeriodeListe = mapBostatusPeriodeListe(grunnlag.bostatusPeriodeListe),
        inntektPeriodeListe = mapInntektPeriodeListe(grunnlag.inntektPeriodeListe),
        sivilstandPeriodeListe = mapSivilstandPeriodeListe(grunnlag.sivilstandPeriodeListe),
        barnIHusstandenPeriodeListe = mapBarnIHusstandenPeriodeListe(grunnlag.barnIHusstandenPeriodeListe),
        sjablonPeriodeListe = mapSjablonPeriodeListe(grunnlag.sjablonPeriodeListe),
    )

    private fun mapFraBusinessObject(avvikListe: List<Avvik>, resultat: BeregnForskuddResultat) = BeregnetForskuddResultatCore(
        beregnetForskuddPeriodeListe = mapResultatPeriode(resultat.beregnetForskuddPeriodeListe),
        sjablonListe = mapSjablonGrunnlagListe(resultat.beregnetForskuddPeriodeListe),
        avvikListe = mapAvvik(avvikListe),
    )

    private fun mapSøknadsbarn(søknadsbarnCore: SøknadsbarnCore) =
        Søknadsbarn(referanse = søknadsbarnCore.referanse, fødselsdato = søknadsbarnCore.fødselsdato)

    private fun mapBostatusPeriodeListe(bostatusPeriodeListeCore: List<BostatusPeriodeCore>): List<BostatusPeriode> {
        val bostatusPeriodeListe = mutableListOf<BostatusPeriode>()
        bostatusPeriodeListeCore.forEach {
            bostatusPeriodeListe.add(
                BostatusPeriode(
                    referanse = it.referanse,
                    bostatusPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    kode = Bostatuskode.valueOf(it.kode),
                ),
            )
        }
        return bostatusPeriodeListe.sortedBy { it.getPeriode().datoFom }
    }

    private fun mapInntektPeriodeListe(bidragMottakerInntektPeriodeListeCore: List<InntektPeriodeCore>): List<InntektPeriode> {
        val bidragMottakerInntektPeriodeListe = mutableListOf<InntektPeriode>()
        bidragMottakerInntektPeriodeListeCore.forEach {
            bidragMottakerInntektPeriodeListe.add(
                InntektPeriode(
                    referanse = it.referanse,
                    inntektPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    type = " ",
                    beløp = it.beløp,
                ),
            )
        }
        return bidragMottakerInntektPeriodeListe.sortedBy { it.getPeriode().datoFom }
    }

    private fun mapSivilstandPeriodeListe(bidragMottakerSivilstandPeriodeListeCore: List<SivilstandPeriodeCore>): List<SivilstandPeriode> {
        val bidragMottakerSivilstandPeriodeListe = mutableListOf<SivilstandPeriode>()
        bidragMottakerSivilstandPeriodeListeCore.forEach {
            bidragMottakerSivilstandPeriodeListe.add(
                SivilstandPeriode(
                    referanse = it.referanse,
                    sivilstandPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    kode = Sivilstandskode.valueOf(it.kode),
                ),
            )
        }
        return bidragMottakerSivilstandPeriodeListe.sortedBy { it.getPeriode().datoFom }
    }

    private fun mapBarnIHusstandenPeriodeListe(barnIHusstandenPeriodeListeCore: List<BarnIHusstandenPeriodeCore>): List<BarnIHusstandenPeriode> {
        val barnIHusstandenPeriodeListe = mutableListOf<BarnIHusstandenPeriode>()
        barnIHusstandenPeriodeListeCore.forEach {
            barnIHusstandenPeriodeListe.add(
                BarnIHusstandenPeriode(
                    referanse = it.referanse,
                    barnIHusstandenPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    antall = it.antall,
                ),
            )
        }
        return barnIHusstandenPeriodeListe.sortedBy { it.getPeriode().datoFom }
    }

    private fun mapSjablonPeriodeListe(sjablonPeriodeListeCore: List<SjablonPeriodeCore>): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListeCore.forEach {
            val sjablonNøkkelListe = mutableListOf<SjablonNokkel>()
            val sjablonInnholdListe = mutableListOf<SjablonInnhold>()
            it.nokkelListe!!.forEach { nokkel ->
                sjablonNøkkelListe.add(SjablonNokkel(navn = nokkel.navn, verdi = nokkel.verdi))
            }
            it.innholdListe.forEach { innhold ->
                sjablonInnholdListe.add(SjablonInnhold(navn = innhold.navn, verdi = innhold.verdi))
            }
            sjablonPeriodeListe.add(
                SjablonPeriode(
                    sjablonPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    sjablon = Sjablon(navn = it.navn, nokkelListe = sjablonNøkkelListe, innholdListe = sjablonInnholdListe),
                ),
            )
        }
        return sjablonPeriodeListe
    }

    private fun mapResultatPeriode(periodeResultatListe: List<ResultatPeriode>): List<ResultatPeriodeCore> {
        val resultatPeriodeCoreListe = mutableListOf<ResultatPeriodeCore>()
        periodeResultatListe.forEach {
            resultatPeriodeCoreListe.add(
                ResultatPeriodeCore(
                    PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    ResultatBeregningCore(
                        beløp = it.resultat.beløp,
                        kode = it.resultat.kode,
                        regel = it.resultat.regel,
                        aldersgruppe = finnAldersgruppe(it.grunnlag.søknadsbarnAlder.alder),
                    ),
                    mapReferanseListe(it),
                ),
            )
        }
        return resultatPeriodeCoreListe
    }

    private fun finnAldersgruppe(alder: Int) = when (alder) {
        in (0..10) -> AldersgruppeForskudd.ALDER_0_10_ÅR
        in (11..17) -> AldersgruppeForskudd.ALDER_11_17_ÅR
        else -> AldersgruppeForskudd.ALDER_18_ÅR_OG_OVER
    }

    private fun mapReferanseListe(resultatPeriode: ResultatPeriode): List<String> {
        val (inntektListe, sivilstand, barnIHusstandenListe, soknadBarnAlder, soknadBarnBostatus) = resultatPeriode.grunnlag
        val sjablonListe = resultatPeriode.resultat.sjablonListe
        val referanseListe = mutableListOf<String>()
        inntektListe.forEach {
            referanseListe.add(it.referanse)
        }
        referanseListe.add(sivilstand.referanse)
        barnIHusstandenListe.forEach {
            referanseListe.add(it.referanse)
        }
        referanseListe.add(soknadBarnAlder.referanse)
        referanseListe.add(soknadBarnBostatus.referanse)
        referanseListe.addAll(sjablonListe.map { lagSjablonReferanse(it) }.distinct())
        return referanseListe.distinct().sorted()
    }

    private fun lagSjablonReferanse(sjablon: SjablonPeriodeNavnVerdi) =
        "Sjablon_${sjablon.navn}_${sjablon.periode.datoFom.format(DateTimeFormatter.ofPattern("yyyyMM"))}"

    private fun mapSjablonGrunnlagListe(periodeResultatListe: List<ResultatPeriode>) = periodeResultatListe.stream()
        .map { mapSjablonListe(it.resultat.sjablonListe) }
        .flatMap { it.stream() }
        .distinct()
        .toList()

    private fun mapSjablonListe(sjablonListe: List<SjablonPeriodeNavnVerdi>) = sjablonListe
        .map {
            SjablonResultatGrunnlagCore(
                referanse = lagSjablonReferanse(it),
                periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                navn = it.navn,
                verdi = it.verdi,
            )
        }

    private fun mapAvvik(avvikListe: List<Avvik>): List<AvvikCore> {
        val avvikCoreListe = mutableListOf<AvvikCore>()
        avvikListe.forEach {
            avvikCoreListe.add(AvvikCore(avvikTekst = it.avvikTekst, avvikType = it.avvikType.toString()))
        }
        return avvikCoreListe
    }
}
