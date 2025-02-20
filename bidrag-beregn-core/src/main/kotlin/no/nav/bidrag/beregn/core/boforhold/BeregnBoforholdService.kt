package no.nav.bidrag.beregn.core.boforhold

import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.BoforholdPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.mapping.tilGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.core.util.justerPeriodeTilOpphørsdato
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBoforhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.LocalDate
import java.util.Collections

data class BeregnBoforholdPeriodeCoreRespons(
    val boforholdPeriodeCoreListe: List<BoforholdPeriodeCore>,
    val barnIHusstandenPeriodeCoreListe: List<BarnIHusstandenPeriodeCore>,
    val voksneIHusstandenPeriodeCoreListe: List<VoksneIHusstandenPeriodeCore>,
    val gjelderReferanse: String,
)

internal class BeregnBoforholdService : CoreMapper() {
    fun beregnBoforholdPeriodeCore(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse? = null): BeregnBoforholdPeriodeCoreRespons {
        val gjelderReferanse = referanseTilRolle ?: finnReferanseTilRolle(
            grunnlagListe = beregnGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        val barnIHusstandenPeriodeCoreListe = mapBarnIHusstanden(beregnGrunnlag = beregnGrunnlag, gjelderReferanse = gjelderReferanse)
        val voksneIHusstandenPeriodeCoreListe = mapVoksneIHusstanden(beregnGrunnlag = beregnGrunnlag, gjelderReferanse = gjelderReferanse)
        val boforholdPeriodeGrunnlagListe = slåSammenBarnOgVoksneIHusstanden(
            barnIHusstandenPeriodeGrunnlagListe = barnIHusstandenPeriodeCoreListe,
            voksneIHusstandenPeriodeGrunnlagListe = voksneIHusstandenPeriodeCoreListe,
            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
            gjelderReferanse = gjelderReferanse,
            opphørsdato = if (beregnGrunnlag.opphørSistePeriode) beregnGrunnlag.periode.til?.plusMonths(1)?.atDay(1) else null,
        )

        return BeregnBoforholdPeriodeCoreRespons(
            boforholdPeriodeCoreListe = boforholdPeriodeGrunnlagListe,
            barnIHusstandenPeriodeCoreListe = barnIHusstandenPeriodeCoreListe,
            voksneIHusstandenPeriodeCoreListe = voksneIHusstandenPeriodeCoreListe,
            gjelderReferanse = gjelderReferanse,
        )
    }

    fun beregnDelberegningBoforholdListe(beregnGrunnlag: BeregnGrunnlag, gjelderReferanse: Grunnlagsreferanse? = null): List<DelberegningBoforhold> {
        val respons = beregnBoforholdPeriodeCore(beregnGrunnlag, gjelderReferanse)

        return respons.boforholdPeriodeCoreListe.map { it.tilGrunnlag() }
    }

    private fun mapBarnIHusstanden(beregnGrunnlag: BeregnGrunnlag, gjelderReferanse: Grunnlagsreferanse): List<BarnIHusstandenPeriodeCore> {
        try {
            val barnIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter {
                        it.innhold.bostatus == Bostatuskode.MED_FORELDER ||
                            it.innhold.bostatus == Bostatuskode.IKKE_MED_FORELDER ||
                            it.innhold.bostatus == Bostatuskode.DOKUMENTERT_SKOLEGANG ||
                            it.innhold.bostatus == Bostatuskode.DELT_BOSTED ||
                            it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN
                    }
                    .map {
                        BarnIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            antall =
                            when (it.innhold.bostatus) {
                                Bostatuskode.IKKE_MED_FORELDER -> 0.0
                                Bostatuskode.REGNES_IKKE_SOM_BARN -> 0.0
                                Bostatuskode.DELT_BOSTED -> 0.5
                                else -> 1.0
                            },
                            grunnlagsreferanseListe = Collections.emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                grunnlagListe = barnIHusstandenGrunnlagListe,
                søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                gjelderReferanse = gjelderReferanse,
                clazz = BarnIHusstandenPeriodeCore::class.java,
                beregningsperiode = beregnGrunnlag.periode,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapVoksneIHusstanden(beregnGrunnlag: BeregnGrunnlag, gjelderReferanse: Grunnlagsreferanse): List<VoksneIHusstandenPeriodeCore> {
        try {
            val voksneIHusstandenGrunnlagListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE)
                    .filter {
                        it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                            it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE ||
                            it.innhold.bostatus == Bostatuskode.BOR_IKKE_MED_ANDRE_VOKSNE
                    }
                    .map {
                        VoksneIHusstandenPeriodeCore(
                            referanse = it.referanse,
                            periode =
                            PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            borMedAndreVoksne = it.innhold.bostatus == Bostatuskode.REGNES_IKKE_SOM_BARN ||
                                it.innhold.bostatus == Bostatuskode.BOR_MED_ANDRE_VOKSNE,
                            grunnlagsreferanseListe = Collections.emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                voksneIHusstandenGrunnlagListe,
                beregnGrunnlag.søknadsbarnReferanse,
                gjelderReferanse,
                VoksneIHusstandenPeriodeCore::class.java,
                beregningsperiode = beregnGrunnlag.periode,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning. Innhold i Grunnlagstype.BOSTATUS_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }
    fun justerPerioderForOpphørsdato(periodeliste: List<PeriodeCore>, opphørsdato: LocalDate?): List<PeriodeCore> {
        if (opphørsdato == null) return periodeliste
        // Antar at opphørsdato er måneden perioden skal opphøre
        return periodeliste.filter {
            it.datoFom.isBefore(opphørsdato)
        }
            .map { grunnlag ->
                if (grunnlag.datoTil == null || grunnlag.datoTil.isAfter(opphørsdato)) {
                    grunnlag.copy(datoTil = justerPeriodeTilOpphørsdato(opphørsdato))
                } else {
                    grunnlag
                }
            }
    }

    // Slår sammen barn i husstanden og voksne i husstanden til et fellesobjekt
    private fun slåSammenBarnOgVoksneIHusstanden(
        barnIHusstandenPeriodeGrunnlagListe: List<BarnIHusstandenPeriodeCore>,
        voksneIHusstandenPeriodeGrunnlagListe: List<VoksneIHusstandenPeriodeCore>,
        søknadsbarnReferanse: String,
        gjelderReferanse: String,
        opphørsdato: LocalDate?,
    ): List<BoforholdPeriodeCore> {
        // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
        val bruddDatoListe = (
            barnIHusstandenPeriodeGrunnlagListe.flatMap { listOf(it.periode.datoFom, it.periode.datoTil) } +
                voksneIHusstandenPeriodeGrunnlagListe.flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
            )
            .distinct()
            .sortedBy { it }
            .sortedWith(compareBy { it == null })

        // Slå sammen brudddatoer til en liste med perioder (fom-/til-dato)
        val periodeListe = bruddDatoListe
            .zipWithNext()
            .map { PeriodeCore(it.first!!, it.second) }

        return justerPerioderForOpphørsdato(periodeListe, opphørsdato).map { bruddPeriode ->

            // Finner matchende barnIHusstanden for aktuell periode
            val barnIHusstanden = barnIHusstandenPeriodeGrunnlagListe
                .firstOrNull {
                    ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(
                        ÅrMånedsperiode(
                            bruddPeriode.datoFom,
                            bruddPeriode.datoTil,
                        ),
                    )
                }
            // Finner matchende voksneIHusstanden for aktuell periode
            val voksneIHusstanden = voksneIHusstandenPeriodeGrunnlagListe
                .firstOrNull {
                    ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(
                        ÅrMånedsperiode(
                            bruddPeriode.datoFom,
                            bruddPeriode.datoTil,
                        ),
                    )
                }

            // Oppretter BoforholdPeriodeCore
            BoforholdPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BOFORHOLD,
                    periode = ÅrMånedsperiode(fom = bruddPeriode.datoFom, til = null),
                    søknadsbarnReferanse = søknadsbarnReferanse,
                    gjelderReferanse = gjelderReferanse,
                ),
                periode = bruddPeriode,
                antallBarn = barnIHusstanden?.antall ?: 0.0,
                borMedAndreVoksne = voksneIHusstanden?.borMedAndreVoksne ?: false,
                grunnlagsreferanseListe = listOfNotNull(barnIHusstanden?.referanse, voksneIHusstanden?.referanse).distinct(),
            )
        }
    }
}
