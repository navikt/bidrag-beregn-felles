package no.nav.bidrag.beregn.felles

import no.nav.bidrag.beregn.felles.bo.Avvik
import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.Sjablon
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.felles.dto.AvvikCore
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonResultatGrunnlagCore
import java.time.format.DateTimeFormatter

open class FellesCore {
    protected fun lagSjablonReferanse(sjablon: SjablonPeriodeNavnVerdi): String {
        return "Sjablon_${sjablon.navn}_${sjablon.periode.datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
    }

    protected fun mapSjablonListe(sjablonListe: List<SjablonPeriodeNavnVerdi>): List<SjablonResultatGrunnlagCore> {
        return sjablonListe
            .map {
                SjablonResultatGrunnlagCore(
                    referanse = lagSjablonReferanse(it),
                    periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    navn = it.navn,
                    verdi = it.verdi,
                )
            }
    }

    protected fun mapSjablonPeriodeListe(sjablonPeriodeListeCore: List<SjablonPeriodeCore>): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListeCore.forEach {
            val sjablonNokkelListe = mutableListOf<SjablonNokkel>()
            val sjablonInnholdListe = mutableListOf<SjablonInnhold>()
            it.nokkelListe!!.forEach { nokkel ->
                sjablonNokkelListe.add(SjablonNokkel(navn = nokkel.navn, verdi = nokkel.verdi))
            }
            it.innholdListe.forEach { innhold ->
                sjablonInnholdListe.add(SjablonInnhold(navn = innhold.navn, verdi = innhold.verdi))
            }
            sjablonPeriodeListe.add(
                SjablonPeriode(
                    sjablonPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    sjablon = Sjablon(navn = it.navn, nokkelListe = sjablonNokkelListe, innholdListe = sjablonInnholdListe),
                ),
            )
        }
        return sjablonPeriodeListe
    }

    protected fun mapAvvik(avvikListe: List<Avvik>): List<AvvikCore> {
        val avvikCoreListe = mutableListOf<AvvikCore>()
        avvikListe.forEach {
            avvikCoreListe.add(AvvikCore(avvikTekst = it.avvikTekst, avvikType = it.avvikType.toString()))
        }
        return avvikCoreListe
    }
}
