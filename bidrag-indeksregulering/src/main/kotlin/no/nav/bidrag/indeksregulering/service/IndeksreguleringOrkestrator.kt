package no.nav.bidrag.indeksregulering.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.service.VedtakService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.beløp.Beløp
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.indeksregulering.BeregnIndeksreguleringApi
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.Year
import kotlin.Exception

private val log = KotlinLogging.logger {}

data class BeregnIndeksreguleringGrunnlag(
    val indeksreguleresForÅr: Year,
    val stønadsid: Stønadsid,
    val personobjektListe: List<GrunnlagDto> = emptyList(),
    val beløpshistorikkListe: List<GrunnlagDto> = emptyList(),
)

data class IndeksregulerPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val gjelderBarnReferanse: Grunnlagsreferanse,
    val beløp: Beløp,
    var sjablonIndeksreguleringFaktor: SjablonSjablontallBeregningGrunnlag,
    val referanseliste: List<String> = emptyList(),
)

fun BaseGrunnlag.tilDto() = GrunnlagDto(
    referanse,
    type,
    innhold,
    grunnlagsreferanseListe,
    gjelderReferanse,
    gjelderBarnReferanse,
)

data class Beløpshistorikk(val referanse: String, val perioder: List<BeløpshistorikkPeriode>)

@Service
@Import(BeregnIndeksreguleringApi::class, VedtakService::class)
class IndeksreguleringOrkestrator(private val vedtakService: VedtakService, private val beregnIndeksreguleringApi: BeregnIndeksreguleringApi) {
    fun utførIndeksreguleringBarnebidrag(
        indeksreguleresForÅr: Year = Year.now(),
        stønad: Stønadsid,
        grunnlagListe: List<GrunnlagDto> = emptyList(),
    ): List<GrunnlagDto> {
        try {
            log.info {
                "Indeksregulering barnebidrag gjøres for stønadstype ${stønad.type} og sak ${stønad.sak} for årstall $indeksreguleresForÅr"
            }
            secureLogger.info {
                "Indeksregulering barnebidrag gjøres for stønad $stønad og årstall $indeksreguleresForÅr"
            }

            val beregningInput = byggGrunnlagForBeregning(
                stønad,
                indeksreguleresForÅr,
                grunnlagListe,
            )

            val resultat =
                beregnIndeksreguleringApi.beregnIndeksreguleringBarnebidrag(beregningInput)

            secureLogger.info {
                "Resultat av beregning av indeksregulering for stønad $stønad og år $indeksreguleresForÅr: $resultat"
            }

            return resultat
        } catch (e: Exception) {
            log.error(e) { "Feil under indeksregulering for stønad $stønad og år $indeksreguleresForÅr" }
            throw e
        }
    }

    private fun byggGrunnlagForBeregning(
        stønad: Stønadsid,
        indeksregulerForÅr: Year,
        grunnlagListe: List<GrunnlagDto> = emptyList(),
    ): BeregnIndeksreguleringGrunnlag {
        val personobjektListe = grunnlagListe.hentAllePersoner().map { it.tilDto() }

        val beløpshistorikkGrunnlag = grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(
            grunnlagType = Grunnlagstype.BELØPSHISTORIKK_BIDRAG,
        ).firstOrNull()?.grunnlag?.tilDto() ?: run {
            vedtakService.hentBeløpshistorikkTilGrunnlag(
                stønadsid = stønad,
                personer = personobjektListe,
                tidspunkt = LocalDateTime.now().withYear(indeksregulerForÅr.value),
            )
        }

        return BeregnIndeksreguleringGrunnlag(
            indeksreguleresForÅr = indeksregulerForÅr,
            stønadsid = stønad,
            personobjektListe = personobjektListe,
            beløpshistorikkListe = listOf(beløpshistorikkGrunnlag),

        )
    }
}
