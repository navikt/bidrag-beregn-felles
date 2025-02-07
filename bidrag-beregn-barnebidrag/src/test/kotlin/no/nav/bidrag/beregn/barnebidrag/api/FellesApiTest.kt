package no.nav.bidrag.beregn.barnebidrag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.junit.jupiter.api.Assertions.fail
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

internal open class FellesApiTest {

    fun hentSluttberegning(resultatGrunnlagListe: List<GrunnlagDto>) =
        resultatGrunnlagListe
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
                    ingenEndringUnderGrense = it.innhold.ingenEndringUnderGrense,
                    barnetErSelvforsørget = it.innhold.barnetErSelvforsørget,
                    bidragJustertForDeltBosted = it.innhold.bidragJustertForDeltBosted,
                    bidragJustertForNettoBarnetilleggBP = it.innhold.bidragJustertForNettoBarnetilleggBP,
                    bidragJustertForNettoBarnetilleggBM = it.innhold.bidragJustertForNettoBarnetilleggBM,
                    bidragJustertNedTilEvne = it.innhold.bidragJustertNedTilEvne,
                    bidragJustertNedTil25ProsentAvInntekt = it.innhold.bidragJustertNedTil25ProsentAvInntekt,
                    bidragJustertTilForskuddssats = it.innhold.bidragJustertTilForskuddssats,
                    begrensetRevurderingUtført = it.innhold.begrensetRevurderingUtført,
                )
            }

    fun hentAlleReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .map { it.referanse }
        .distinct()

    fun hentAlleRefererteReferanser(resultatGrunnlagListe: List<GrunnlagDto>) = resultatGrunnlagListe
        .flatMap { it.grunnlagsreferanseListe }
        .distinct()

    fun lesFilOgByggRequest(filnavn: String): BeregnGrunnlag {
        var json = ""

        // Les inn fil med request-data (json)
        try {
            json = Files.readString(Paths.get(filnavn))
        } catch (e: Exception) {
            fail("Klarte ikke å lese fil: $filnavn")
        }

        // Lag request
        return ObjectMapper().findAndRegisterModules().readValue(json, BeregnGrunnlag::class.java)
    }

    fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
