package no.nav.bidrag.beregn.særbidrag.core.felles

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import java.time.format.DateTimeFormatter

open class FellesCore {
    protected fun lagSjablonReferanse(sjablon: SjablonPeriodeNavnVerdi): String =
        "Sjablon_${sjablon.navn}_${sjablon.periode.datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"

    protected fun mapSjablonListe(sjablonListe: List<SjablonPeriodeNavnVerdi>): List<SjablonResultatGrunnlagCore> = sjablonListe
        .map {
            SjablonResultatGrunnlagCore(
                referanse = lagSjablonReferanse(it),
                periode = PeriodeCore(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                navn = it.navn,
                verdi = it.verdi,
                grunnlag = it.grunnlag,
            )
        }

    protected fun mapSjablonPeriodeListe(sjablonPeriodeListeCore: List<SjablonPeriodeCore>): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListeCore.forEach {
            val sjablonNøkkelListe = mutableListOf<SjablonNøkkel>()
            val sjablonInnholdListe = mutableListOf<SjablonInnhold>()
            it.nøkkelListe!!.forEach { nøkkel ->
                sjablonNøkkelListe.add(SjablonNøkkel(navn = nøkkel.navn, verdi = nøkkel.verdi))
            }
            it.innholdListe.forEach { innhold ->
                sjablonInnholdListe.add(SjablonInnhold(navn = innhold.navn, verdi = innhold.verdi, grunnlag = innhold.grunnlag))
            }
            sjablonPeriodeListe.add(
                SjablonPeriode(
                    sjablonPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    sjablon = Sjablon(navn = it.navn, nøkkelListe = sjablonNøkkelListe, innholdListe = sjablonInnholdListe),
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
