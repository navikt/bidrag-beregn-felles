package no.nav.bidrag.beregn.barnebidrag.utils

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.AldersjusteringDetaljerGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.time.LocalDate
import java.time.YearMonth

val aldersjusteringAldersgrupper = listOf(6, 11, 15)

object AldersjusteringUtils {

    fun finnBarnAlder(fødselsdato: LocalDate, aldersjusteresForÅr: Int): Int = aldersjusteresForÅr - fødselsdato.year
    fun skalAldersjusteres(fødselsdato: LocalDate, aldersjusteresForÅr: Int = YearMonth.now().year): Boolean {
        val alder = finnBarnAlder(fødselsdato, aldersjusteresForÅr)
        return aldersjusteringAldersgrupper.contains(alder)
    }
    fun opprettAldersjusteringDetaljerGrunnlag(
        søknadsbarnReferanse: String,
        aldersjusteresForÅr: Int,
        aldersjusteresManuelt: Boolean = false,
        aldersjustert: Boolean = true,
        stønad: Stønadsid,
        begrunnelser: List<String>? = null,
        vedtaksidBeregning: Int? = null,
    ) = GrunnlagDto(
        referanse = "${no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype.ALDERSJUSTERING_DETALJER}_${stønad.toReferanse()}",
        type = no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype.ALDERSJUSTERING_DETALJER,
        gjelderBarnReferanse = søknadsbarnReferanse,
        gjelderReferanse = søknadsbarnReferanse,
        innhold =
        POJONode(
            AldersjusteringDetaljerGrunnlag(
                periode = ÅrMånedsperiode(YearMonth.of(aldersjusteresForÅr, 7), null),
                aldersjusteresManuelt = aldersjusteresManuelt,
                aldersjustert = aldersjustert,
                begrunnelser = begrunnelser,
                følgerAutomatiskVedtak = null,
                aldersjustertManuelt = false,
                grunnlagFraVedtak = vedtaksidBeregning,
            ),
        ),
    )
}
