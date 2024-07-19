package no.nav.bidrag.beregn.core.sjablon

import no.nav.bidrag.beregn.core.TestUtil.byggSjabloner
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.util.SjablonUtil
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
@DisplayName("SjablonTest")
internal class SjablonTest {
    private val sjablonListe = byggSjabloner()
    private val sjablonNøkkelListe = mutableListOf<SjablonNøkkel>()
    private var sjablonNøkkelVerdiInteger = 0

    @Test
    @DisplayName("Test Barnetilsyn (N:1, eksakt match)")
    fun testHentBarnetilsyn() {
        sjablonNøkkelListe.clear()
        sjablonNøkkelListe.add(SjablonNøkkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"))
        sjablonNøkkelListe.add(SjablonNøkkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "DU"))
        val beløpBarnetilsyn =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BARNETILSYN, sjablonNøkkelListe, SjablonInnholdNavn.BARNETILSYN_BELØP)

        assertThat(beløpBarnetilsyn).isEqualTo(BigDecimal.valueOf(258))
    }

    @Test
    @DisplayName("Test Bidragsevne (1:N, eksakt match)")
    fun testHentBidragsevne() {
        sjablonNøkkelListe.clear()
        sjablonNøkkelListe.add(SjablonNøkkel(SjablonNøkkelNavn.BOSTATUS.navn, "GS"))
        val beløpBoutgift =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNøkkelListe, SjablonInnholdNavn.BOUTGIFT_BELØP)
        val beløpUnderhold =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNøkkelListe, SjablonInnholdNavn.UNDERHOLD_BELØP)

        assertThat(beløpBoutgift).isEqualTo(BigDecimal.valueOf(5875))
        assertThat(beløpUnderhold).isEqualTo(BigDecimal.valueOf(7557))
    }

    @Test
    @DisplayName("Test Forbruksutgifter (1:1, intervall)")
    fun testHentForbruksutgifter() {
        sjablonNøkkelVerdiInteger = 3
        var beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(3661))

        sjablonNøkkelVerdiInteger = 5
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(3661))

        sjablonNøkkelVerdiInteger = 7
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(5113))

        sjablonNøkkelVerdiInteger = 10
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(5113))

        sjablonNøkkelVerdiInteger = 12
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(6099))

        sjablonNøkkelVerdiInteger = 99
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(6985))
    }

    @Test
    @DisplayName("Test Maks Fradrag (1:1, intervall)")
    fun testHentMaksFradrag() {
        sjablonNøkkelVerdiInteger = 0
        var beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33))

        sjablonNøkkelVerdiInteger = 1
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33))

        sjablonNøkkelVerdiInteger = 3
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(4583))

        sjablonNøkkelVerdiInteger = 90
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(12083))

        sjablonNøkkelVerdiInteger = 99
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(12083))
    }

    @Test
    @DisplayName("Test Maks Tilsyn (1:1, intervall)")
    fun testHentMaksTilsyn() {
        sjablonNøkkelVerdiInteger = 0
        var beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(6214))

        sjablonNøkkelVerdiInteger = 1
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(6214))

        sjablonNøkkelVerdiInteger = 2
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(8109))

        sjablonNøkkelVerdiInteger = 90
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(9189))

        sjablonNøkkelVerdiInteger = 99
        beløpForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNøkkelVerdiInteger)
        assertThat(beløpForbrukTot).isEqualTo(BigDecimal.valueOf(9189))
    }

    @Test
    @DisplayName("Test Samværsfradrag (N:N, eksakt match + intervall)")
    fun testHentSamværsfradrag() {
        sjablonNøkkelListe.clear()
        sjablonNøkkelListe.add(SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"))

        sjablonNøkkelVerdiInteger = 3
        var antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        var antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        var beløpFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(beløpFradrag).isEqualTo(BigDecimal.valueOf(2082))

        sjablonNøkkelVerdiInteger = 5
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        beløpFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(beløpFradrag).isEqualTo(BigDecimal.valueOf(2082))

        sjablonNøkkelVerdiInteger = 12
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        beløpFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(beløpFradrag).isEqualTo(BigDecimal.valueOf(2914))

        sjablonNøkkelVerdiInteger = 99
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        beløpFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNøkkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNøkkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(beløpFradrag).isEqualTo(BigDecimal.valueOf(3196))
    }

    @Test
    @DisplayName("Test Sjablontall (1:1, eksakt match)")
    fun testHentSjablontall() {
        val sjablonVerdi = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELØP)
        assertThat(sjablonVerdi).isEqualTo(BigDecimal.valueOf(2775))
    }

    @Test
    @DisplayName("Test Trinnvis Skattesats (0:N, hent alle)")
    fun testHentTrinnvisSkattesats() {
        val sortertTrinnvisSkattesatsListe = SjablonUtil.hentTrinnvisSkattesats(sjablonListe, SjablonNavn.TRINNVIS_SKATTESATS)
        assertThat(sortertTrinnvisSkattesatsListe.size).isEqualTo(4)
        assertThat(sortertTrinnvisSkattesatsListe[0].inntektGrense).isEqualTo(BigDecimal.valueOf(174500))
        assertThat(sortertTrinnvisSkattesatsListe[0].sats).isEqualTo(BigDecimal.valueOf(1.9))
    }
}
