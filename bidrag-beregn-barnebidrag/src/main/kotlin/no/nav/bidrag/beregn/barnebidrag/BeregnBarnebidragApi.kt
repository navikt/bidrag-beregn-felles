package no.nav.bidrag.beregn.barnebidrag

import no.nav.bidrag.beregn.barnebidrag.bo.BeregnEndeligBidragServiceRespons
import no.nav.bidrag.beregn.barnebidrag.service.beregning.BeregnAldersjusteringService
import no.nav.bidrag.beregn.barnebidrag.service.beregning.BeregnBarnebidragService
import no.nav.bidrag.commons.service.sjablon.EnableSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * BeregnBarnebidragApi eksponerer api for å beregne barnebidrag.
 *
 * For å ta i bruk beregnings-apiet må følgende gjøres:
 *
 * Legg til Import-annotering i konfigurasjonen for å initalisere BeregnBarnebidragApi-bønnen
 * ```kotlin
 * @Import(BeregnBarnebidragApi::class)
 * ```
 *
 * Definer BIDRAG_SJABLON_URL miljøvariabler i naiskonfigurasjonen.
 * ```yaml
 *   BIDRAG_SJABLON_URL: https://bidrag-sjablon.<prod-fss|dev-fss>-pub.nais.io/bidrag-sjablon
 * ```
 *
 *  Åpne outbound traffik for `BIDRAG_SJABLON_URL` i naiskonfigurasjonen
 */
@EnableSjablonProvider
@Service
class BeregnBarnebidragApi {
    private val service = BeregnBarnebidragService()
    private val aldersjusteringService = BeregnAldersjusteringService()

    fun beregnMånedsbeløpFaktiskeUtgifter(faktiskUtgift: BigDecimal, kostpenger: BigDecimal): BigDecimal? =
        service.beregnMånedsbeløpFaktiskUtgift(faktiskUtgift, kostpenger)

    fun beregnMånedsbeløpTilleggsstønad(tilleggsstønad: BigDecimal): BigDecimal = service.beregnMånedsbeløpTilleggsstønad(tilleggsstønad)

    fun beregn(beregnGrunnlag: BeregnGrunnlag): BeregnetBarnebidragResultat = service.beregnBarnebidrag(beregnGrunnlag)
    fun opprettAvslag(beregnGrunnlag: BeregnGrunnlag): BeregnetBarnebidragResultat = service.opprettAvslagResultat(beregnGrunnlag)

    fun beregnBidragsevne(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnBidragsevne(beregnGrunnlag)

    fun beregnNettoTilsynsutgift(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnNettoTilsynsutgift(beregnGrunnlag)

    fun beregnUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag) = service.beregnUnderholdskostnad(beregnGrunnlag)

    fun beregnNettoTilsynsutgiftOgUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag) =
        service.beregnNettoTilsynsutgiftOgUnderholdskostnad(beregnGrunnlag)

    fun beregnBpAndelUnderholdskostnad(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnBpAndelUnderholdskostnad(beregnGrunnlag)

    fun beregnNettoBarnetillegg(beregnGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> =
        service.beregnNettoBarnetillegg(beregnGrunnlag, rolle)

    fun beregnSamværsfradrag(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnSamværsfradrag(beregnGrunnlag)

    fun beregnBarnetilleggSkattesats(beregnGrunnlag: BeregnGrunnlag, rolle: Grunnlagstype): List<GrunnlagDto> =
        service.beregnBarnetilleggSkattesats(beregnGrunnlag, rolle)

    fun beregnEndeligBidrag(beregnGrunnlag: BeregnGrunnlag): BeregnEndeligBidragServiceRespons = service.beregnEndeligBidrag(beregnGrunnlag)

    fun beregnEndringSjekkGrensePeriode(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnEndringSjekkGrensePeriode(beregnGrunnlag)

    fun beregnEndringSjekkGrense(beregnGrunnlag: BeregnGrunnlag): List<GrunnlagDto> = service.beregnEndringSjekkGrense(beregnGrunnlag)

    fun beregnAldersjustering(beregnGrunnlag: BeregnGrunnlagAldersjustering): BeregnetBarnebidragResultat =
        aldersjusteringService.beregnAldersjusteringBarnebidrag(beregnGrunnlag)
}
