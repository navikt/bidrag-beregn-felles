package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
import no.nav.bidrag.beregn.barnebidrag.service.beregning.BeregnBarnebidragService
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnBarnebidragTestV2 : FellesTest() {
    private lateinit var filnavn: String

    @Mock
    private lateinit var api: BeregnBarnebidragService
//    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragService()
    }

    @Test
    @DisplayName("Barnebidrag - eksempel 1A - forholdsmessig fordeling 2 barn")
    fun testBarnebidrag_Eksempel01A() {
        filnavn = "src/test/resources/testfiler/barnebidrag/barnebidragV2_eksempel1A.json"
        utførBeregningerOgEvaluerResultatBarnebidrag()
    }

    private fun utførBeregningerOgEvaluerResultatBarnebidrag() {
        val request: List<BeregnGrunnlag> = lesFilOgByggRequestGenerisk(filnavn)

        val barnebidragResultat = api.beregnBarnebidragV2(
            beregningsperiode = ÅrMånedsperiode(YearMonth.parse("2020-08"), YearMonth.parse("2021-01")),
            grunnlagSøknadsbarnListe = request,
            grunnlagLøpendeBidragListe = emptyList(),
        )

        printJson(barnebidragResultat)

        barnebidragResultat.map { beregningResultat ->
            val alleReferanser = hentAlleReferanser(beregningResultat.beregnetBarnebidragResultat.grunnlagListe)
            val alleRefererteReferanser = hentAlleRefererteReferanser(
                resultatGrunnlagListe = beregningResultat.beregnetBarnebidragResultat.grunnlagListe,
                barnebidragResultat = beregningResultat.beregnetBarnebidragResultat,
            )

            // Fjerner referanser som er "frittstående" (refereres ikke av noe objekt)
            val alleReferanserFiltrert = alleReferanser
                .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_Person") }
                .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_person") }
                .filterNot { it.contains("delberegning_DELBEREGNING_ENDRING_SJEKK_GRENSE_PERSON") }

            // Fjerner referanser som ikke er med i inputen til beregning eller som refererer til annet søknadsbarn
            val alleRefererteReferanserFiltrert = alleRefererteReferanser
                .filterNot { it.contains("innhentet_husstandsmedlem") }
                .filterNot { it.contains("innhentet_andre_barn") }
                .filterNot { it.contains("DELBEREGNING_BIDRAG_TIL_FORDELING") && !it.contains(beregningResultat.søknadsbarnreferanse) }
                .filterNot { it.contains("DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL") && !it.contains(beregningResultat.søknadsbarnreferanse) }
                .filterNot { it.contains("DELBEREGNING_SAMVÆRSFRADRAG") && !it.contains(beregningResultat.søknadsbarnreferanse) }
                .filterNot { it.contains("DELBEREGNING_UNDERHOLDSKOSTNAD") && !it.contains(beregningResultat.søknadsbarnreferanse) }

            assertAll(
                { assertThat(alleReferanser).containsAll(alleRefererteReferanserFiltrert) },
                { assertThat(alleRefererteReferanser).containsAll(alleReferanserFiltrert) },
            )
        }
    }
}
