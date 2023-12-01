package no.nav.bidrag.beregn.felles.sjablon

import no.nav.bidrag.beregn.felles.TestUtil.byggSjabloner
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel
import no.nav.bidrag.beregn.felles.util.SjablonUtil
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
    private val sjablonNokkelListe = mutableListOf<SjablonNokkel>()
    private var sjablonNokkelVerdiInteger = 0

    @Test
    @DisplayName("Test Barnetilsyn (N:1, eksakt match)")
    fun testHentBarnetilsyn() {
        sjablonNokkelListe.clear()
        sjablonNokkelListe.add(SjablonNokkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"))
        sjablonNokkelListe.add(SjablonNokkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "DU"))
        val belopBarnetilsyn =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BARNETILSYN, sjablonNokkelListe, SjablonInnholdNavn.BARNETILSYN_BELØP)

        assertThat(belopBarnetilsyn).isEqualTo(BigDecimal.valueOf(258))
    }

    @Test
    @DisplayName("Test Bidragsevne (1:N, eksakt match)")
    fun testHentBidragsevne() {
        sjablonNokkelListe.clear()
        sjablonNokkelListe.add(SjablonNokkel(SjablonNøkkelNavn.BOSTATUS.navn, "GS"))
        val belopBoutgift =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNokkelListe, SjablonInnholdNavn.BOUTGIFT_BELØP)
        val belopUnderhold =
            SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.BIDRAGSEVNE, sjablonNokkelListe, SjablonInnholdNavn.UNDERHOLD_BELØP)

        assertThat(belopBoutgift).isEqualTo(BigDecimal.valueOf(5875))
        assertThat(belopUnderhold).isEqualTo(BigDecimal.valueOf(7557))
    }

    @Test
    @DisplayName("Test Forbruksutgifter (1:1, intervall)")
    fun testHentForbruksutgifter() {
        sjablonNokkelVerdiInteger = 3
        var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(3661))

        sjablonNokkelVerdiInteger = 5
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(3661))

        sjablonNokkelVerdiInteger = 7
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(5113))

        sjablonNokkelVerdiInteger = 10
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(5113))

        sjablonNokkelVerdiInteger = 12
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6099))

        sjablonNokkelVerdiInteger = 99
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.FORBRUKSUTGIFTER, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6985))
    }

    @Test
    @DisplayName("Test Maks Fradrag (1:1, intervall)")
    fun testHentMaksFradrag() {
        sjablonNokkelVerdiInteger = 0
        var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33))

        sjablonNokkelVerdiInteger = 1
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(2083.33))

        sjablonNokkelVerdiInteger = 3
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(4583))

        sjablonNokkelVerdiInteger = 90
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(12083))

        sjablonNokkelVerdiInteger = 99
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_FRADRAG, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(12083))
    }

    @Test
    @DisplayName("Test Maks Tilsyn (1:1, intervall)")
    fun testHentMaksTilsyn() {
        sjablonNokkelVerdiInteger = 0
        var belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6214))

        sjablonNokkelVerdiInteger = 1
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(6214))

        sjablonNokkelVerdiInteger = 2
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(8109))

        sjablonNokkelVerdiInteger = 90
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(9189))

        sjablonNokkelVerdiInteger = 99
        belopForbrukTot = SjablonUtil.hentSjablonverdi(sjablonListe, SjablonNavn.MAKS_TILSYN, sjablonNokkelVerdiInteger)
        assertThat(belopForbrukTot).isEqualTo(BigDecimal.valueOf(9189))
    }

    @Test
    @DisplayName("Test Samværsfradrag (N:N, eksakt match + intervall)")
    fun testHentSamvaersfradrag() {
        sjablonNokkelListe.clear()
        sjablonNokkelListe.add(SjablonNokkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"))

        sjablonNokkelVerdiInteger = 3
        var antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        var antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        var belopFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2082))

        sjablonNokkelVerdiInteger = 5
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        belopFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2082))

        sjablonNokkelVerdiInteger = 12
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        belopFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(2914))

        sjablonNokkelVerdiInteger = 99
        antDagerTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_DAGER_TOM,
            )
        antNetterTom =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.ANTALL_NETTER_TOM,
            )
        belopFradrag =
            SjablonUtil.hentSjablonverdi(
                sjablonListe,
                SjablonNavn.SAMVÆRSFRADRAG,
                sjablonNokkelListe,
                SjablonNøkkelNavn.ALDER_TOM,
                sjablonNokkelVerdiInteger,
                SjablonInnholdNavn.FRADRAG_BELØP,
            )

        assertThat(antDagerTom).isEqualTo(BigDecimal.ZERO)
        assertThat(antNetterTom).isEqualTo(BigDecimal.valueOf(13))
        assertThat(belopFradrag).isEqualTo(BigDecimal.valueOf(3196))
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
