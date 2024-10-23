package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.FaktiskUtgiftPeriode
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksFradragsbeløpPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksTilsynsbeløpPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.TilleggsstønadPeriode
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.time.LocalDate

internal object NettoTilsynsutgiftMapper : CoreMapper() {
    fun mapNettoTilsynsutgiftGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): NettoTilsynsutgiftPeriodeGrunnlag {
        val resultat = NettoTilsynsutgiftPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            faktiskUtgiftPeriodeCoreListe = mapFaktiskUtgift(mottattGrunnlag),
            tilleggsstønadPeriodeCoreListe = mapTilleggsstønad(mottattGrunnlag),
            sjablonMaksTilsynsbeløpPeriodeGrunnlagListe = mapSjablonMaksTilsynsbeløp(sjablonGrunnlag),
            sjablonMaksFradragsbeløpPeriodeGrunnlagListe = mapSjablonMaksFradrag(sjablonGrunnlag),

        )
        return resultat
    }

    private fun mapFaktiskUtgift(beregnGrunnlag: BeregnGrunnlag): List<FaktiskUtgiftPeriode> {
        try {
            val faktiskTilsynsutgiftListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<FaktiskUtgiftPeriode>(Grunnlagstype.FAKTISK_UTGIFT)
                    .map {
                        FaktiskUtgiftPeriode(
                            referanse = it.referanse,
                            periode = it.innhold.periode,
                            referanseBarn = it.innhold.referanseBarn,
                            fødselsdatoBarn = finnFødselsdatoBarn(beregnGrunnlag.grunnlagListe, it.innhold.referanseBarn),
                            faktiskUtgiftBeløp = it.innhold.faktiskUtgiftBeløp,
                            kostpengerBeløp = it.innhold.kostpengerBeløp,
                            manueltRegistrert = it.innhold.manueltRegistrert,
                        )
                    }

            return faktiskTilsynsutgiftListe
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av netto tilsynsutgifter. Innhold i Grunnlagstype.FAKTISK_UTGIFT er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapTilleggsstønad(beregnGrunnlag: BeregnGrunnlag): List<TilleggsstønadPeriode> {
        try {
            val tilleggsstønadListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<TilleggsstønadPeriode>(Grunnlagstype.TILLEGGSSTØNAD)
                    .map {
                        TilleggsstønadPeriode(
                            referanse = it.referanse,
                            periode = it.innhold.periode,
                            beløpDagsats = it.innhold.beløpDagsats,
                            referanseBarn = it.innhold.referanseBarn,
                            manueltRegistrert = it.innhold.manueltRegistrert,
                        )
                    }

            return tilleggsstønadListe
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av netto tilsynsutgifter. Innhold i Grunnlagstype.TILLEGGSSTØNAD er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSjablonMaksTilsynsbeløp(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonMaksTilsynsbeløpPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.contains(SjablonNavn.MAKS_TILSYN.navn) }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonMaksTilsynPeriode>()
                .map {
                    SjablonMaksTilsynsbeløpPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonMaksTilsynsbeløpPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for maks tilsyn: " + e.message,
            )
        }
    }

    private fun mapSjablonMaksFradrag(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonMaksFradragsbeløpPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.contains(SjablonNavn.MAKS_TILSYN.navn) }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonMaksFradragPeriode>()
                .map {
                    SjablonMaksFradragsbeløpPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonMaksFradragsbeløpPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for maks fradrag: " + e.message,
            )
        }
    }

    fun finnFødselsdatoBarn(beregnGrunnlag: List<GrunnlagDto>, referanse: String): LocalDate =
        finnPersonFraReferanse(beregnGrunnlag, referanse).fødselsdato
}
