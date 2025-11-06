package no.nav.bidrag.beregn.barnebidrag.felles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagBeregningPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InnholdMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.junit.jupiter.api.Assertions.fail
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.YearMonth

internal open class FellesTest {

    fun hentSluttberegning(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG)
        .map {
            SluttberegningBarnebidrag(
                periode = it.innhold.periode,
                beregnetBeløp = it.innhold.beregnetBeløp,
                resultatBeløp = it.innhold.resultatBeløp,
                uMinusNettoBarnetilleggBM = it.innhold.uMinusNettoBarnetilleggBM,
                bruttoBidragEtterBarnetilleggBM = it.innhold.bruttoBidragEtterBarnetilleggBM,
                nettoBidragEtterBarnetilleggBM = it.innhold.nettoBidragEtterBarnetilleggBM,
                bruttoBidragJustertForEvneOg25Prosent = it.innhold.bruttoBidragJustertForEvneOg25Prosent,
                bruttoBidragEtterBegrensetRevurdering = it.innhold.bruttoBidragEtterBegrensetRevurdering,
                bruttoBidragEtterBarnetilleggBP = it.innhold.bruttoBidragEtterBarnetilleggBP,
                nettoBidragEtterSamværsfradrag = it.innhold.nettoBidragEtterSamværsfradrag,
                bpAndelAvUVedDeltBostedFaktor = it.innhold.bpAndelAvUVedDeltBostedFaktor,
                bpAndelAvUVedDeltBostedBeløp = it.innhold.bpAndelAvUVedDeltBostedBeløp,
                løpendeForskudd = it.innhold.løpendeForskudd,
                løpendeBidrag = it.innhold.løpendeBidrag,
                barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                bidragJustertForDeltBosted = it.innhold.bidragJustertForDeltBosted,
                bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                bidragJustertForNettoBarnetilleggBM = it.innhold.bidragJustertForNettoBarnetilleggBM,
                bidragJustertNedTilEvne = it.innhold.bidragJustertNedTilEvne,
                bidragJustertNedTil25ProsentAvInntekt = it.innhold.bidragJustertNedTil25ProsentAvInntekt,
                bidragJustertTilForskuddssats = it.innhold.bidragJustertTilForskuddssats,
                begrensetRevurderingUtført = it.innhold.begrensetRevurderingUtført,
                ikkeOmsorgForBarnet = it.innhold.ikkeOmsorgForBarnet,
            )
        }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe + it.gjelderBarnReferanse + it.gjelderReferanse }
        .filterNotNull()
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>, barnebidragResultat: BeregnetBarnebidragResultat) = (
        resultatGrunnlagListe.flatMap { it.grunnlagsreferanseListe + it.gjelderBarnReferanse + it.gjelderReferanse } +
            barnebidragResultat.beregnetBarnebidragPeriodeListe.flatMap { it.grunnlagsreferanseListe }
        )
        .filterNotNull()
        .distinct()

    fun sjekkReferanser(resultatVedtakListe: List<ResultatVedtak>) {
        val alleReferanser = hentAlleReferanser(resultatVedtakListe.last().resultat.grunnlagListe)
        val alleRefererteReferanser = hentAlleRefererteReferanser(
            resultatGrunnlagListe = resultatVedtakListe.last().resultat.grunnlagListe,
            barnebidragResultat = resultatVedtakListe.last().resultat,
        )

        assertSoftly {
            alleReferanser.containsAll(alleRefererteReferanser)
            alleRefererteReferanser.containsAll(alleReferanser)
        }
    }

    inline fun <reified T> lesFilOgByggRequestGenerisk(filnavn: String): T {
        val json = try {
            Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn", e)
        }
        val mapper = jacksonObjectMapper().findAndRegisterModules()
        return mapper.readValue(json, object : com.fasterxml.jackson.core.type.TypeReference<T>() {})
    }

    // Overload for BeregnGrunnlag
    fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag = lesFilOgByggRequestGenerisk<BeregnGrunnlag>(filnavn)

    fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}

inline fun <reified T : GrunnlagBeregningPeriode> List<InnholdMedReferanse<T>>.validerSistePeriodeErLikDato(opphørsdato: YearMonth?) {
    maxBy { it.innhold.periode.fom }.innhold.periode.til shouldBe opphørsdato
}
