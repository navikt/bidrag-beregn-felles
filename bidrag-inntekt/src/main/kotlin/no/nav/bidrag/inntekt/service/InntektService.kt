package no.nav.bidrag.inntekt.service

import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.inntekt.util.InntektUtil.Companion.tilJson
import no.nav.bidrag.inntekt.util.VersionProvider.Companion.APP_VERSJON
import no.nav.bidrag.transport.behandling.inntekt.request.TransformerInntekterRequest
import no.nav.bidrag.transport.behandling.inntekt.response.InntektPost
import no.nav.bidrag.transport.behandling.inntekt.response.TransformerInntekterResponse
import java.math.BigDecimal
import java.time.YearMonth

class InntektService(
    private val ainntektService: AinntektService = AinntektService(),
    private val skattegrunnlagService: SkattegrunnlagService = SkattegrunnlagService(),
    private val kontantstøtteService: KontantstøtteService = KontantstøtteService(),
    private val utvidetBarnetrygdService: UtvidetBarnetrygdService = UtvidetBarnetrygdService(),
    private val småbarnstilleggService: SmåbarnstilleggService = SmåbarnstilleggService(),
    private val barnetilleggPensjonService: BarnetilleggPensjonService = BarnetilleggPensjonService(),
    private val ytelserService: YtelserService = YtelserService(),
    private val ytelserServiceOvergangsstønad: YtelserServiceOvergangsstønad = YtelserServiceOvergangsstønad(),
) {
    fun transformerInntekter(transformerInntekterRequest: TransformerInntekterRequest): TransformerInntekterResponse {
        val transformerInntekterResponse =
            TransformerInntekterResponse(
                versjon = APP_VERSJON,
                summertMånedsinntektListe =
                ainntektService.beregnMaanedsinntekt(
                    ainntektListeInn = transformerInntekterRequest.ainntektsposter,
                    ainntektHentetDato = transformerInntekterRequest.ainntektHentetDato,
                ),
                summertÅrsinntektListe = (
                    ainntektService.beregnAarsinntekt(
                        ainntektListeInn = transformerInntekterRequest.ainntektsposter,
                        ainntektHentetDato = transformerInntekterRequest.ainntektHentetDato,
                        vedtakstidspunktOpprinneligeVedtak = transformerInntekterRequest.vedtakstidspunktOpprinneligeVedtak,
                    ) +
                        skattegrunnlagService.beregnSkattegrunnlag(
                            skattegrunnlagListe = transformerInntekterRequest.skattegrunnlagsliste,
                            inntektsrapportering = Inntektsrapportering.LIGNINGSINNTEKT,
                        ) +
                        skattegrunnlagService.beregnSkattegrunnlag(
                            skattegrunnlagListe = transformerInntekterRequest.skattegrunnlagsliste,
                            inntektsrapportering = Inntektsrapportering.KAPITALINNTEKT,
                        ) +
                        kontantstøtteService.beregnKontantstøtte(
                            transformerInntekterRequest.kontantstøtteliste,
                        ) +
                        utvidetBarnetrygdService.beregnUtvidetBarnetrygd(
                            transformerInntekterRequest.utvidetBarnetrygdliste,
                        ) +
                        småbarnstilleggService.beregnSmåbarnstillegg(
                            transformerInntekterRequest.småbarnstilleggliste,
                        ) +
                        barnetilleggPensjonService.beregnBarnetilleggPensjon(
                            transformerInntekterRequest.barnetilleggsliste,
                        ) +
                        ytelserService.beregnYtelser(
                            ainntektListeInn = transformerInntekterRequest.ainntektsposter,
                            ainntektHentetDato = transformerInntekterRequest.ainntektHentetDato,
                        ) +
                        ytelserServiceOvergangsstønad.beregnYtelser(
                            ainntektListeInn = transformerInntekterRequest.ainntektsposter,
                            ainntektHentetDato = transformerInntekterRequest.ainntektHentetDato,
                        )
                    ),
            )

        secureLogger.debug { "TransformerInntekterRequestDto: ${tilJson(transformerInntekterRequest)}" }
        secureLogger.debug { "TransformerInntekterResponseDto: ${tilJson(transformerInntekterResponse)}" }

        return transformerInntekterResponse
    }
}

data class InntektSumPost(
    val sumInntekt: BigDecimal,
    val periodeFra: YearMonth,
    val periodeTil: YearMonth?,
    val inntektPostListe: MutableList<InntektPost>,
    val grunnlagreferanseListe: Set<String> = mutableSetOf(),
)

data class Periode(
    val periodeFra: YearMonth,
    val periodeTil: YearMonth?,
)
