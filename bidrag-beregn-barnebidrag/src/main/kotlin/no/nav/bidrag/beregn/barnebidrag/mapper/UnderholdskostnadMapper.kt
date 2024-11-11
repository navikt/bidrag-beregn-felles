package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilsynMedStønadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlagDto
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBarnetilsynPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilsynMedStønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBarnetilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonForbruksutgifterPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.util.*

internal object UnderholdskostnadMapper : CoreMapper() {
    fun mapUnderholdskostnadGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): UnderholdskostnadPeriodeGrunnlag {
        val barnetilsynMedStønadPeriodeGrunnlagListe = mapBarnetilsynMedStønad(mottattGrunnlag)
        val nettoTilsynsutgiftPeriodeGrunnlagListe = mapNettoTilsynsutgift(mottattGrunnlag, mottattGrunnlag.søknadsbarnReferanse)

        return UnderholdskostnadPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            søknadsbarnPeriodeGrunnlag = mapSøknadsbarn(mottattGrunnlag),
            barnetilsynMedStønadPeriodeGrunnlagListe = barnetilsynMedStønadPeriodeGrunnlagListe,
            nettoTilsynsutgiftPeriodeGrunnlagListe = nettoTilsynsutgiftPeriodeGrunnlagListe,
            sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlag),
            sjablonBarnetilsynPeriodeGrunnlagListe = mapSjablonBarnetilsyn(sjablonGrunnlag),
            sjablonForbruksutgifterPeriodeGrunnlagListe = mapSjablonForbruksutgifter(sjablonGrunnlag),
        )
    }

    private fun mapSøknadsbarn(beregnGrunnlag: BeregnGrunnlag): SøknadsbarnPeriodeGrunnlag {
        try {
            val søknadsbarnGrunnlag =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<Person>(referanse = beregnGrunnlag.søknadsbarnReferanse)

            return SøknadsbarnPeriodeGrunnlag(referanse = søknadsbarnGrunnlag[0].referanse, fødselsdato = søknadsbarnGrunnlag[0].innhold.fødselsdato)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av barnebidrag. Feil i grunnlag som inneholder søknadsbarn: " + e.message,
            )
        }
    }

    private fun mapBarnetilsynMedStønad(beregnGrunnlag: BeregnGrunnlag): List<BarnetilsynMedStønadPeriodeGrunnlag> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<BarnetilsynMedStønadPeriode>(Grunnlagstype.BARNETILSYN_MED_STØNAD_PERIODE)
                .map {
                    BarnetilsynMedStønadPeriodeGrunnlag(
                        referanse = it.referanse,
                        barnetilsynMedStønadPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved mapping av barnetilsyn med stønad. Innhold i Grunnlagstype.BARNETILSYN_MED_STØNAD er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapNettoTilsynsutgift(beregnGrunnlag: BeregnGrunnlag, gjelderBarn: Grunnlagsreferanse): List<NettoTilsynsutgiftPeriodeGrunnlagDto> {
        try {
            return beregnGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningNettoTilsynsutgift>(Grunnlagstype.NETTO_TILSYNSUTGIFT)
                .filter { it.innhold.tilsynsutgiftBarnListe.any { tilsynsutgiftBarn -> tilsynsutgiftBarn.gjelderBarn == gjelderBarn } }
                .map {
                    NettoTilsynsutgiftPeriodeGrunnlagDto(
                        referanse = it.referanse,
                        nettoTilsynsutgiftPeriodeGrunnlag = NettoTilsynsutgiftPeriode(
                            referanse = it.referanse,
                            periode =
                            ÅrMånedsperiode(
                                it.innhold.periode.fom,
                                it.innhold.periode.til,
                            ),
                            nettoTilsynsutgift = it.innhold.tilsynsutgiftBarnListe.asSequence()
                                .filter { tilsynsutgiftBarn -> tilsynsutgiftBarn.gjelderBarn == gjelderBarn }
                                .map { tilsynsutgiftBarn -> tilsynsutgiftBarn.nettoTilsynsutgift }
                                .first(),
                        ),
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av underholdskostnad. Innhold i Grunnlagstype.NETTO_TILSYNSUTGIFT_PERIODE er ikke gyldig: " + e.message,
            )
        }
    }

    // TODO Flytte til CoreMapper
    private fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.uppercase().contains("SJABLONTALL") }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
                .map {
                    SjablonSjablontallPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonSjablontallPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for sjablontall: " + e.message,
            )
        }
    }

    private fun mapSjablonBarnetilsyn(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonBarnetilsynPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.contains(SjablonNavn.BARNETILSYN.navn) }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonBarnetilsynPeriode>()
                .map {
                    SjablonBarnetilsynPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonBarnetilsynPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon Barnetilsyn: " + e.message,
            )
        }
    }

    private fun mapSjablonForbruksutgifter(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonForbruksutgifterPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.contains(SjablonNavn.FORBRUKSUTGIFTER.navn) }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonForbruksutgifterPeriode>()
                .map {
                    SjablonForbruksutgifterPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonForbruksutgifterPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon Forbruktsutgifter: " + e.message,
            )
        }
    }
}
