package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnetilleggSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.YearMonth

@ExtendWith(MockitoExtension::class)
internal class BeregnBarnetilleggSkattesatsApiTest : FellesApiTest() {
    private lateinit var filnavn: String
    private lateinit var forventetFomPeriode: YearMonth
    private lateinit var forventetSkattFaktor: BigDecimal
    private var forventetAntallDelberegningSumInntektPeriode: Int = 1
    private var forventetAntallSjablonSjablontall: Int = 5
    private var forventetAntallSjablonTrinnvisSkattesats: Int = 1

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Barnetillegg skattesats - eksempel 1 - en periode - inntekt 200000")
    fun testBarnetilleggSkattesats_Eksempel01() {
        filnavn = "src/test/resources/testfiler/barnetilleggskattesats/barnetilleggskattesats_eksempel1.json"
        forventetFomPeriode = YearMonth.parse("2024-08")
        forventetSkattFaktor = BigDecimal.valueOf(0.112925).avrundetMedTiDesimaler
        utførBeregningerOgEvaluerResultatBarnetilleggSkattesats(Grunnlagstype.PERSON_BIDRAGSPLIKTIG)
    }

    @Test
    @DisplayName("Barnetillegg skattesats - eksempel 2 - flere perioder - inntekt siste periode 1000000")
    fun testBarnetilleggSkattesats_Eksempel02() {
        filnavn = "src/test/resources/testfiler/barnetilleggskattesats/barnetilleggskattesats_eksempel2.json"
        forventetFomPeriode = YearMonth.parse("2024-07")
        forventetSkattFaktor = BigDecimal.valueOf(0.3228806).avrundetMedTiDesimaler
        utførBeregningerOgEvaluerResultatBarnetilleggSkattesats(Grunnlagstype.PERSON_BIDRAGSPLIKTIG)
    }

    @Test
    @DisplayName("Barnetillegg skattesats - eksempel 3 - flere perioder - BM/BP - inntekt siste periode for BM 800000")
    fun testBarnetilleggSkattesats_Eksempel03() {
        filnavn = "src/test/resources/testfiler/barnetilleggskattesats/barnetilleggskattesats_eksempel3.json"
        forventetFomPeriode = YearMonth.parse("2024-07")
        forventetSkattFaktor = BigDecimal.valueOf(0.292772).avrundetMedTiDesimaler
        utførBeregningerOgEvaluerResultatBarnetilleggSkattesats(Grunnlagstype.PERSON_BIDRAGSMOTTAKER)
    }

    private fun utførBeregningerOgEvaluerResultatBarnetilleggSkattesats(rolle: Grunnlagstype) {
        val request = lesFilOgByggRequest(filnavn)
        val barnetilleggSkattesatsResultat = api.beregnBarnetilleggSkattesats(request, rolle)
        printJson(barnetilleggSkattesatsResultat)

        val alleReferanser = hentAlleReferanser(barnetilleggSkattesatsResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(barnetilleggSkattesatsResultat)

        val barnetilleggSkattesatsResultatListe = barnetilleggSkattesatsResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBarnetilleggSkattesats>(Grunnlagstype.DELBEREGNING_BARNETILLEGG_SKATTESATS)
            .map {
                DelberegningBarnetilleggSkattesats(
                    periode = it.innhold.periode,
                    skattFaktor = it.innhold.skattFaktor,
                    minstefradrag = it.innhold.minstefradrag,
                    skattAlminneligInntekt = it.innhold.skattAlminneligInntekt,
                    trygdeavgift = it.innhold.trygdeavgift,
                    trinnskatt = it.innhold.trinnskatt,
                    sumSkatt = it.innhold.sumSkatt,
                    sumInntekt = it.innhold.sumInntekt,
                )
            }

        val referanseRolle = request.grunnlagListe
            .filter { it.type == rolle }
            .map { it.referanse }
            .first()

        val antallDelberegningSumInntektPeriode = barnetilleggSkattesatsResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_SUM_INNTEKT }
            .filter { it.gjelderReferanse == referanseRolle }
            .size

        val antallSjablonSjablontall = barnetilleggSkattesatsResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        val antallSjablonTrinnvisSkattesats = barnetilleggSkattesatsResultat
            .filter { it.type == Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS }
            .size

        assertAll(
            { assertThat(barnetilleggSkattesatsResultat).isNotNull },
            { assertThat(barnetilleggSkattesatsResultatListe).isNotNull },
            { assertThat(barnetilleggSkattesatsResultatListe).hasSize(1) },

            // Resultat
            { assertThat(barnetilleggSkattesatsResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(forventetFomPeriode, null)) },
            { assertThat(barnetilleggSkattesatsResultatListe[0].skattFaktor).isEqualTo(forventetSkattFaktor) },

            // Grunnlag
            { assertThat(antallDelberegningSumInntektPeriode).isEqualTo(forventetAntallDelberegningSumInntektPeriode) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },
            { assertThat(antallSjablonTrinnvisSkattesats).isEqualTo(forventetAntallSjablonTrinnvisSkattesats) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
