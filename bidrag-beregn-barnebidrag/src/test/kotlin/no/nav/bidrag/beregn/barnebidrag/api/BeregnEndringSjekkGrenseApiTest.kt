package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
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
internal class BeregnEndringSjekkGrenseApiTest : FellesApiTest() {
    private lateinit var filnavn: String
    private var forventetEndringErOverGrense: Boolean = false

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Endring sjekk grense - eksempel 1 - minst en av periodene er over grense => endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel01() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrense/endring_sjekk_grense_eksempel1.json"
        forventetEndringErOverGrense = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense - eksempel 2 - alle periodene er under grense => endring er under grense")
    fun testEndringSjekkGrensePeriode_Eksempel02() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrense/endring_sjekk_grense_eksempel2.json"
        forventetEndringErOverGrense = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense - eksempel 3 - førstegangsfastsettelse og avslag i alle perioder => endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel03() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrense/endring_sjekk_grense_eksempel3.json"
        forventetEndringErOverGrense = true
        utførBeregningerOgEvaluerResultat()
    }

    private fun utførBeregningerOgEvaluerResultat() {
        val request = lesFilOgByggRequest(filnavn)
        val endringSjekkGrenseResultat = api.beregnEndringSjekkGrense(request)
        printJson(endringSjekkGrenseResultat)

        val alleReferanser = hentAlleReferanser(endringSjekkGrenseResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endringSjekkGrenseResultat)

        val endringSjekkGrenseResultatListe = endringSjekkGrenseResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrense>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE)
            .map {
                DelberegningEndringSjekkGrense(
                    periode = it.innhold.periode,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val antallEndringSjekkGrensePeriode = endringSjekkGrenseResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE }
            .size

        assertAll(
            { assertThat(endringSjekkGrenseResultat).isNotNull },
            { assertThat(endringSjekkGrenseResultatListe).isNotNull },
            { assertThat(endringSjekkGrenseResultatListe).hasSize(1) },

            // Resultat
            { assertThat(endringSjekkGrenseResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(endringSjekkGrenseResultatListe[0].endringErOverGrense).isEqualTo(forventetEndringErOverGrense) },

            // Grunnlag
            { assertThat(antallEndringSjekkGrensePeriode).isEqualTo(3) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
