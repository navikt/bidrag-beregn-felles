package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksFradragsbeløpPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksTilsynsbeløpPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoTilsynsutgiftService
import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.FaktiskUtgiftPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.TilleggsstønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Collections.emptyList

internal object NettoTilsynsutgiftMapper : CoreMapper() {
    fun mapNettoTilsynsutgiftPeriodeGrunnlag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): NettoTilsynsutgiftPeriodeGrunnlag {
        val referanseTilBM = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        val resultat = NettoTilsynsutgiftPeriodeGrunnlag(
            beregningsperiode = mottattGrunnlag.periode,
            faktiskUtgiftPeriodeCoreListe = mapFaktiskUtgift(mottattGrunnlag, referanseTilBM),
            tilleggsstønadPeriodeCoreListe = mapTilleggsstønad(mottattGrunnlag, referanseTilBM),
            sjablonSjablontallPeriodeGrunnlagListe = BeregnNettoTilsynsutgiftService.mapSjablonSjablontall(sjablonGrunnlag),
            sjablonMaksTilsynsbeløpPeriodeGrunnlagListe = mapSjablonMaksTilsynsbeløp(sjablonGrunnlag),
            sjablonMaksFradragsbeløpPeriodeGrunnlagListe = mapSjablonMaksFradrag(sjablonGrunnlag),

        )
        return resultat
    }

    private fun mapFaktiskUtgift(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse): List<FaktiskUtgiftPeriodeCore> {
        try {
            val faktiskTilsynsutgiftListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<FaktiskUtgiftPeriode>(Grunnlagstype.FAKTISK_UTGIFT_PERIODE)
                    .map {
                        FaktiskUtgiftPeriodeCore(
                            referanse = it.referanse,
                            periode = PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            gjelderBarn = it.innhold.gjelderBarn,
                            beregnetBeløp = beregnBeløpFaktiskUtgift(it.innhold.faktiskUtgiftBeløp, it.innhold.kostpengerBeløp),
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                grunnlagListe = faktiskTilsynsutgiftListe,
                søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                gjelderReferanse = referanseTilRolle,
                clazz = FaktiskUtgiftPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av netto tilsynsutgifter. Innhold i Grunnlagstype.FAKTISK_UTGIFT er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapTilleggsstønad(beregnGrunnlag: BeregnGrunnlag, referanseTilRolle: Grunnlagsreferanse): List<TilleggsstønadPeriodeCore> {
        try {
            val tilleggsstønadListe =
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<TilleggsstønadPeriode>(Grunnlagstype.TILLEGGSSTØNAD_PERIODE)
                    .map {
                        TilleggsstønadPeriodeCore(
                            referanse = it.referanse,
                            periode = PeriodeCore(
                                datoFom = it.innhold.periode.toDatoperiode().fom,
                                datoTil = it.innhold.periode.toDatoperiode().til,
                            ),
                            gjelderBarn = it.innhold.gjelderBarn,
                            beregnetBeløp = beregnBeløpTilleggsstønad(it.innhold.beløpDagsats),
                            grunnlagsreferanseListe = emptyList(),
                        )
                    }

            return akkumulerOgPeriodiser(
                grunnlagListe = tilleggsstønadListe,
                søknadsbarnreferanse = beregnGrunnlag.søknadsbarnReferanse,
                gjelderReferanse = referanseTilRolle,
                clazz = TilleggsstønadPeriodeCore::class.java,
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av netto tilsynsutgifter. Innhold i Grunnlagstype.TILLEGGSSTØNAD er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapSjablonMaksTilsynsbeløp(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonMaksTilsynsbeløpPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_MAKS_TILSYN }
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
                .filter { it.type == Grunnlagstype.SJABLON_MAKS_FRADRAG }
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

    fun beregnBeløpFaktiskUtgift(faktiskUtgiftBeløp: BigDecimal, kostpengerBeløp: BigDecimal?): BigDecimal =
        faktiskUtgiftBeløp.minus(kostpengerBeløp ?: BigDecimal.ZERO).multiply(BigDecimal.valueOf(11))
            .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP).coerceAtLeast(BigDecimal.ZERO)

    fun beregnBeløpTilleggsstønad(beløpDagsats: BigDecimal): BigDecimal =
        beløpDagsats.multiply(BigDecimal.valueOf(260)).divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP).coerceAtLeast(BigDecimal.ZERO)

    fun finnFødselsdatoBarn(beregnGrunnlag: List<GrunnlagDto>, referanse: String): LocalDate =
        finnPersonFraReferanse(beregnGrunnlag, referanse).fødselsdato
}
