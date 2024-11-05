package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.time.LocalDate
import java.util.Collections.emptyList

internal object BPsBeregnedeTotalbidragCoreMapper : CoreMapper() {
    fun mapBPsBeregnedeTotalbidragGrunnlagTilCore(beregnGrunnlag: BeregnGrunnlag, sjablonListe: SjablonListe): LøpendeBidragGrunnlagCore {
        // Hent løpende bidrag
        val grunnlag =
            try {
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<LøpendeBidragGrunnlag>(Grunnlagstype.LØPENDE_BIDRAG)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Ugyldig input ved beregning av sum løpende bidrag. Innhold i Grunnlagstype.LØPENDE_BIDRAG er ikke gyldig: " + e.message,
                )
            }.first()

        // Henter aktuelle sjabloner
        val sjablonPeriodeCoreListe =
            mapSjablonSamværsfradrag(
                beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
                beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
                sjablonSamværsfradragListe = sjablonListe.sjablonSamværsfradragResponse,
            )
        val sjablonPeriodeListe = mapSjablonPeriodeListe(sjablonPeriodeCoreListe)

        return LøpendeBidragGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            referanse = grunnlag.referanse,
            løpendeBidragCoreListe = grunnlag.innhold.løpendeBidragListe.map {
                LøpendeBidragCore(
                    saksnummer = it.saksnummer,
                    fødselsdatoBarn = finnFødselsdatoBarn(beregnGrunnlag.grunnlagListe, it.gjelderBarn),
                    personidentBarn = finnPersonidentBarn(beregnGrunnlag.grunnlagListe, it.gjelderBarn),
                    referanseBarn = it.gjelderBarn,
                    løpendeBeløp = it.løpendeBeløp,
                    valutakode = it.valutakode,
                    samværsklasse = it.samværsklasse,
                    beregnetBeløp = it.beregnetBeløp,
                    faktiskBeløp = it.faktiskBeløp,
                )
            },
            grunnlagsreferanseListe = emptyList(),
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun finnFødselsdatoBarn(beregnGrunnlag: List<GrunnlagDto>, referanse: String): LocalDate =
        finnPersonFraReferanse(beregnGrunnlag, referanse).fødselsdato

    private fun finnPersonidentBarn(beregnGrunnlag: List<GrunnlagDto>, referanse: String): Personident =
        finnPersonFraReferanse(beregnGrunnlag, referanse).ident!!

    private fun mapSjablonPeriodeListe(sjablonPeriodeListeCore: List<SjablonPeriodeCore>): List<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()
        sjablonPeriodeListeCore.forEach {
            val sjablonNøkkelListe = mutableListOf<SjablonNøkkel>()
            val sjablonInnholdListe = mutableListOf<SjablonInnhold>()
            it.nøkkelListe!!.forEach { nøkkel ->
                sjablonNøkkelListe.add(SjablonNøkkel(navn = nøkkel.navn, verdi = nøkkel.verdi))
            }
            it.innholdListe.forEach { innhold ->
                sjablonInnholdListe.add(SjablonInnhold(navn = innhold.navn, verdi = innhold.verdi, grunnlag = it.grunnlag))
            }
            sjablonPeriodeListe.add(
                SjablonPeriode(
                    sjablonPeriode = Periode(datoFom = it.periode.datoFom, datoTil = it.periode.datoTil),
                    sjablon = Sjablon(navn = it.navn, nøkkelListe = sjablonNøkkelListe, innholdListe = sjablonInnholdListe, grunnlag = it.grunnlag),
                    grunnlag = it.grunnlag,
                ),
            )
        }
        return sjablonPeriodeListe
    }
}
