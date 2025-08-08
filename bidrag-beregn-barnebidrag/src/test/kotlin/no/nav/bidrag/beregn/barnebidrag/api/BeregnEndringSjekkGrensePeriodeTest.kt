package no.nav.bidrag.beregn.barnebidrag.api

import no.nav.bidrag.beregn.barnebidrag.BeregnBarnebidragApi
import no.nav.bidrag.beregn.barnebidrag.felles.FellesTest
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
internal class BeregnEndringSjekkGrensePeriodeTest : FellesTest() {
    private lateinit var filnavn: String
    private val forventetEndringsgrenseProsent = BigDecimal.valueOf(12)
    private var forventetBeregnetBidragBeløp: BigDecimal? = null
    private var forventetLøpendeBidragBeløp: BigDecimal? = null
    private var forventetLøpendeBidragFraPrivatAvtale: Boolean = false
    private var forventetFaktiskEndringFaktor: BigDecimal? = null
    private var forventetEndringErOverGrense: Boolean = false
    private var forventetAntallSjablonSjablontall: Int = 1
    private var forventetAntallLøpendeBidrag: Int = 1
    private var forventetAntallLøpendeBidrag18År: Int = 1
    private var forventetAntallPrivatAvtale: Int = 0

    @Mock
    private lateinit var api: BeregnBarnebidragApi

    @BeforeEach
    fun initMock() {
        stubSjablonProvider()
        api = BeregnBarnebidragApi()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 1 - ordinært bidrag - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel01() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel1.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 2 - ordinært bidrag - endring er under grense")
    fun testEndringSjekkGrensePeriode_Eksempel02() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel2.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(3800).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.05).avrundetMedTiDesimaler
        forventetEndringErOverGrense = false
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 3A - ordinært bidrag - beløp i beløpshistorikk er null for periode")
    fun testEndringSjekkGrensePeriode_Eksempel03A() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel3A.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 3B - ordinært bidrag - beløpshistorikk mangler")
    fun testEndringSjekkGrensePeriode_Eksempel03B() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel3B.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 4 - 18-års-bidrag - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel04() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel4.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 5 - 18-års-bidrag - endring er under grense")
    fun testEndringSjekkGrensePeriode_Eksempel05() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel5.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(3800).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.05).avrundetMedTiDesimaler
        forventetEndringErOverGrense = false
        forventetAntallLøpendeBidrag = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 6A - 18-års-bidrag - beløp i beløpshistorikk er null for periode")
    fun testEndringSjekkGrensePeriode_Eksempel06A() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel6A.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 6B - 18-års-bidrag - beløpshistorikk mangler")
    fun testEndringSjekkGrensePeriode_Eksempel06B() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel6B.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 7A - ordinært bidrag - løpende bidrag er null og beregnet bidrag er null")
    fun testEndringSjekkGrensePeriode_Eksempel07A() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel7A.json"
        forventetBeregnetBidragBeløp = null
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = null
        forventetEndringErOverGrense = false
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 7B - ordinært bidrag - løpende bidrag er null og beregnet bidrag er 0")
    fun testEndringSjekkGrensePeriode_Eksempel07B() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel7B.json"
        forventetBeregnetBidragBeløp = BigDecimal.ZERO.avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = null
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 7C - ordinært bidrag - løpende bidrag er 0 og beregnet bidrag er null")
    fun testEndringSjekkGrensePeriode_Eksempel07C() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel7C.json"
        forventetBeregnetBidragBeløp = null
        forventetLøpendeBidragBeløp = BigDecimal.ZERO.avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ONE.avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 7D - ordinært bidrag - løpende bidrag er 0 og beregnet bidrag er 0")
    fun testEndringSjekkGrensePeriode_Eksempel07D() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel7D.json"
        forventetBeregnetBidragBeløp = BigDecimal.ZERO.avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.ZERO.avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.ZERO.avrundetMedTiDesimaler
        forventetEndringErOverGrense = false
        forventetAntallLøpendeBidrag18År = 0
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 8A - ordinært bidrag fra privat avtale - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel08A() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel8A.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = true
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        forventetAntallPrivatAvtale = 1
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 8B - ordinært bidrag fra privat avtale og beløpshistorikk annen periode - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel08B() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel8B.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = true
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 0
        forventetAntallLøpendeBidrag18År = 0
        forventetAntallPrivatAvtale = 1
        utførBeregningerOgEvaluerResultat()
    }

    @Test
    @DisplayName("Endring sjekk grense periode - eksempel 8C - ordinært bidrag fra beløpshistorikk og privat avtale i samme periode - endring er over grense")
    fun testEndringSjekkGrensePeriode_Eksempel08C() {
        filnavn = "src/test/resources/testfiler/endringsjekkgrenseperiode/endring_sjekk_grense_periode_eksempel8C.json"
        forventetBeregnetBidragBeløp = BigDecimal.valueOf(4500).avrundetMedToDesimaler
        forventetLøpendeBidragBeløp = BigDecimal.valueOf(4000).avrundetMedNullDesimaler
        forventetLøpendeBidragFraPrivatAvtale = false
        forventetFaktiskEndringFaktor = BigDecimal.valueOf(0.125).avrundetMedTiDesimaler
        forventetEndringErOverGrense = true
        forventetAntallLøpendeBidrag = 1
        forventetAntallLøpendeBidrag18År = 0
        forventetAntallPrivatAvtale = 0
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
                    løpendeBidragBeløp = it.innhold.løpendeBidragBeløp,
                    løpendeBidragFraPrivatAvtale = it.innhold.løpendeBidragFraPrivatAvtale,
                    beregnetBidragBeløp = it.innhold.beregnetBidragBeløp,
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

        val antallLøpendeBidrag18År = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR }
            .size

        val antallPrivatAvtale = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE }
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
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].løpendeBidragBeløp).isEqualTo(forventetLøpendeBidragBeløp) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].løpendeBidragFraPrivatAvtale).isEqualTo(forventetLøpendeBidragFraPrivatAvtale) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].beregnetBidragBeløp).isEqualTo(forventetBeregnetBidragBeløp) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].faktiskEndringFaktor).isEqualTo(forventetFaktiskEndringFaktor) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isEqualTo(forventetEndringErOverGrense) },
            { assertThat(beregnetBidragBeløp).isEqualTo(forventetBeregnetBidragBeløp) },
            { assertThat(sjablonSjablontallEndringsgrense).isEqualTo(forventetEndringsgrenseProsent) },

            // Grunnlag
            { assertThat(antallSluttberegning).isEqualTo(1) },
            { assertThat(antallLøpendeBidrag).isEqualTo(forventetAntallLøpendeBidrag) },
            { assertThat(antallLøpendeBidrag18År).isEqualTo(forventetAntallLøpendeBidrag18År) },
            { assertThat(antallPrivatAvtale).isEqualTo(forventetAntallPrivatAvtale) },
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
                    løpendeBidragBeløp = it.innhold.løpendeBidragBeløp,
                    løpendeBidragFraPrivatAvtale = it.innhold.løpendeBidragFraPrivatAvtale,
                    beregnetBidragBeløp = it.innhold.beregnetBidragBeløp,
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

        val antallPrivatAvtale = endringSjekkGrensePeriodeResultat
            .filter { it.type == Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE }
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
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].løpendeBidragBeløp).isEqualTo(BigDecimal.valueOf(4000).avrundetMedNullDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].løpendeBidragFraPrivatAvtale).isFalse },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].beregnetBidragBeløp).isEqualTo(BigDecimal.valueOf(4500).avrundetMedToDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.125).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[0].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[1].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-09"), YearMonth.parse("2024-10"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].løpendeBidragBeløp).isEqualTo(BigDecimal.valueOf(4000).avrundetMedNullDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].løpendeBidragFraPrivatAvtale).isFalse },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].beregnetBidragBeløp).isEqualTo(BigDecimal.valueOf(3800).avrundetMedToDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.05).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[1].endringErOverGrense).isFalse },

            { assertThat(endringSjekkGrensePeriodeResultatListe[2].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-10"), YearMonth.parse("2024-11"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].løpendeBidragBeløp).isNull() },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].løpendeBidragFraPrivatAvtale).isFalse },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].beregnetBidragBeløp).isEqualTo(BigDecimal.valueOf(3800).avrundetMedToDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].faktiskEndringFaktor).isEqualTo(BigDecimal.ONE.avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[2].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[3].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-11"), YearMonth.parse("2024-12"))) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].løpendeBidragBeløp).isNull() },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].løpendeBidragFraPrivatAvtale).isFalse },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].beregnetBidragBeløp).isEqualTo(BigDecimal.valueOf(3800).avrundetMedToDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].faktiskEndringFaktor).isEqualTo(BigDecimal.ONE.avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[3].endringErOverGrense).isTrue },

            { assertThat(endringSjekkGrensePeriodeResultatListe[4].periode).isEqualTo(ÅrMånedsperiode(YearMonth.parse("2024-12"), null)) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].løpendeBidragBeløp).isEqualTo(BigDecimal.valueOf(4000).avrundetMedNullDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].løpendeBidragFraPrivatAvtale).isFalse },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].beregnetBidragBeløp).isEqualTo(BigDecimal.valueOf(6000).avrundetMedToDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].faktiskEndringFaktor).isEqualTo(BigDecimal.valueOf(0.5).avrundetMedTiDesimaler) },
            { assertThat(endringSjekkGrensePeriodeResultatListe[4].endringErOverGrense).isTrue },

            // Grunnlag
            { assertThat(antallSluttberegning).isEqualTo(3) },
            { assertThat(antallLøpendeBidrag).isEqualTo(1) },
            { assertThat(antallPrivatAvtale).isEqualTo(0) },
            { assertThat(antallSjablonSjablontall).isEqualTo(forventetAntallSjablonSjablontall) },

            // Referanser
            { assertThat(alleReferanser).containsAll(alleRefererteReferanser) },
        )
    }
}
