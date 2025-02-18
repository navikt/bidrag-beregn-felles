package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.commons.web.mock.stubSjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetMedNullDesimaler
import no.nav.bidrag.domene.util.avrundetMedTiDesimaler
import no.nav.bidrag.domene.util.avrundetMedToDesimaler
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
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
internal class BeregnEndringSjekkGrensePeriodeApiTest: FellesApiTest() {
    private lateinit var filnavn: String
    private val forventetEndringsgrenseProsent = BigDecimal.valueOf(12)
    private lateinit var forventetBeregnetBidragBeløp: BigDecimal
    private var forventetLøpendeBidragBeløp: BigDecimal? = null
    private var forventetFaktiskEndringFaktor: BigDecimal? = null
    private var forventetEndringErOverGrense: Boolean = false
    private var forventetAntallSjablonSjablontall: Int = 1
    private var forventetAntallLøpendeBidrag: Int = 1

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 1 - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel01() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel1.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 2 - endring er under grense")
    fun testEndringSjekkGrensePeriode_Eksempel02() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel2.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(3800).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.05).avrundetMedTiDesimaler
        forventetEndringErOverGrense = false
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 3A - beløp i beløpshistorikk er null for periode")
    fun testEndringSjekkGrensePeriode_Eksempel03A() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel3A.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetFaktiskEndringFaktor = null
        forventetEndringErOverGrense = true
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 3B - beløpshistorikk mangler")
    fun testEndringSjekkGrensePeriode_Eksempel03B() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel3B.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetFaktiskEndringFaktor = null
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - flere perioder")
    fun testEndringSjekkGrensePeriode_EksempelFlerePerioder() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel_flere_perioder.json"
        utførBeregningerOgEvaluerResultatFlerePerioder()
    }

    private fun utførBeregningerOgEvaluerResultat() {
        val request = lesFilOgByggRequest(filnavn)
        val endringSjekkGrensePeriodeResultat = api.beregnEndringSjekkGrensePeriode(request)
        printJson(endringSjekkGrensePeriodeResultat)

        val alleReferanser = hentAlleReferanser(endringSjekkGrensePeriodeResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endringSjekkGrensePeriodeResultat)

        val endringSjekkGrensePeriodeResultatListe = endringSjekkGrensePeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val beregnetBidragBeløp = hentSluttberegning(endringSjekkGrensePeriodeResultat)[0].beregnetBeløp?.avrundetMedToDesimaler
        val sjablonSjablontallEndringsgrense = endringSjekkGrensePeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>(Grunnlagstype.SJABLON_SJABLONTALL)
            .map {
                SjablonSjablontallPeriode(
                    periode = it.innhold.periode,
                    sjablon = it.innhold.sjablon,
                    verdi = it.innhold.verdi,
                )
            }
            .first()
            .verdi

        val antallSluttberegning = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG }
            .size

        val antallLøpendeBidrag = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG }
            .size

        val antallSjablonSjablontall = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        assertAll(
            { assertThat(endringSjekkGrensePeriodeResultat).isNotNull },
            { assertThat(endringSjekkGrensePeriodeResultatListe).isNotNull },
            { assertThat(endringSjekkGrensePeriodeResultatListe).hasSize(1) },

            // Resultat
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), null)) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].faktiskEndringFaktor).isEqualTo(forventetFaktiskEndringFaktor) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isEqualTo(forventetEndringErOverGrense) },
            { assertThat(beregnetBidragBeløp).isEqualTo(forventetBeregnetBidragBeløp) },
            { assertThat(sjablonSjablontallEndringsgrense).isEqualTo(forventetEndringsgrenseProsent) },

            // Grunnlag
            { assertThat(antallSluttberegning).isEqualTo(1) },
            { assertThat(antallLøpendeBidrag).isEqualTo(forventetAntallLøpendeBidrag) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }

    private fun utførBeregningerOgEvaluerResultatFlerePerioder() {
        val request = lesFilOgByggRequest(filnavn)
        val endringSjekkGrensePeriodeResultat = api.beregnEndringSjekkGrensePeriode(request)
        printJson(endringSjekkGrensePeriodeResultat)

        val alleReferanser = hentAlleReferanser(endringSjekkGrensePeriodeResultat)
        val alleRefererteReferanser = hentAlleRefererteReferanser(endringSjekkGrensePeriodeResultat)

        val endringSjekkGrensePeriodeResultatListe = endringSjekkGrensePeriodeResultat
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningEndringSjekkGrensePeriode>(Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE)
            .map {
                DelberegningEndringSjekkGrensePeriode(
                    periode = it.innhold.periode,
                    faktiskEndringFaktor = it.innhold.faktiskEndringFaktor,
                    endringErOverGrense = it.innhold.endringErOverGrense,
                )
            }

        val antallSluttberegning = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG }
            .size

        val antallLøpendeBidrag = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG }
            .size

        val antallSjablonSjablontall = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
            .size

        assertAll(
            { assertThat(endringSjekkGrensePeriodeResultat).isNotNull },
            { assertThat(endringSjekkGrensePeriodeResultatListe).isNotNull },
            { assertThat(endringSjekkGrensePeriodeResultatListe).hasSize(5) },

            // Resultat
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-08"), YearMonth.parse("2024-09"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.125).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-09"), YearMonth.parse("2024-10"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.05).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isFalse },

            { assertThat(endringSjekkGrensePeriodeResultatListe[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-10"), YearMonth.parse("2024-11"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].faktiskEndringFaktor).isNull() },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-11"), YearMonth.parse("2024-12"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].faktiskEndringFaktor).isNull() },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-12"), null)) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.5).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].endringErOverGrense).isTrue },

            // Grunnlag
            { assertThat(antallSluttberegning).isEqualTo(3) },
            { assertThat(antallLøpendeBidrag).isEqualTo(1) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
