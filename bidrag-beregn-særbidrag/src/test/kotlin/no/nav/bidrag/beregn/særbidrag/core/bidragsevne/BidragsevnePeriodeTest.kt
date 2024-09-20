package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.SumLøpendeBidragCoreCoreTest.MockitoHelper.any
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BarnIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.VoksneIHusstandPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class BidragsevnePeriodeTest {

    private lateinit var bidragsevnePeriode: BidragsevnePeriode

    @Mock
    private lateinit var bidragsevneBeregningMock: BidragsevneBeregning

    private val beregnBidragsevneGrunnlag = byggBeregnBidragsevneGrunnlag()
    private val beregnBidragsevneResultat = byggResultatBeregning()

    @BeforeEach
    fun initMocksAndService() {
        bidragsevnePeriode = BidragsevnePeriode(bidragsevneBeregningMock)
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(bidragsevneBeregningMock.beregn(any())).thenReturn(beregnBidragsevneResultat)

        val resultatPeriode = bidragsevnePeriode.beregnPerioder(beregnBidragsevneGrunnlag)

        assertThat(resultatPeriode.resultatPeriodeListe).isNotEmpty
        assertThat(resultatPeriode.resultatPeriodeListe).hasSize(1)
    }

    @DisplayName("Exception når AntallBarnIHusstand mangler")
    @Test
    fun skalKasteExceptionNårAntallBarnIHusstandMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            bidragsevnePeriode.beregnPerioder(byggBeregnBidragsevneGrunnlag(avvikBarnIHusstand = true))
        }.withMessageContaining("Antall barn i husstand mangler data for periode")
    }

    @DisplayName("Exception når BostatusVoksneIHusstand mangler")
    @Test
    fun skalKasteExceptionNårBostatusVoksneIHusstandMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            bidragsevnePeriode.beregnPerioder(byggBeregnBidragsevneGrunnlag(avvikVoksneIHusstand = true))
        }.withMessageContaining("Bostatus voksne i husstand mangler data for periode")
    }

    private fun byggBeregnBidragsevneGrunnlag(avvikBarnIHusstand: Boolean = false, avvikVoksneIHusstand: Boolean = false) = BeregnBidragsevneGrunnlag(
        beregnDatoFra = LocalDate.parse("2020-01-01"),
        beregnDatoTil = LocalDate.parse("2020-02-01"),
        inntektPeriodeListe = lagInntektGrunnlag(),
        barnIHusstandPeriodeListe = lagBarnIHusstandGrunnlag(avvikBarnIHusstand),
        voksneIHusstandPeriodeListe = lagVoksneIHusstandGrunnlag(avvikVoksneIHusstand),
        sjablonPeriodeListe = lagSjablonGrunnlag(),
    )

    private fun lagInntektGrunnlag() = listOf(
        InntektPeriode(
            referanse = TestUtil.INNTEKT_REFERANSE,
            periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
            beløp = BigDecimal.valueOf(666000),
        ),
    )

    private fun lagBarnIHusstandGrunnlag(avvik: Boolean) = listOf(
        BarnIHusstandPeriode(
            referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE,
            periode =
            if (!avvik) {
                Periode(
                    datoFom = LocalDate.parse("2020-01-01"),
                    datoTil = LocalDate.parse("2020-02-01"),
                )
            } else {
                Periode(
                    datoFom = LocalDate.parse("2021-01-01"),
                    datoTil = LocalDate.parse("2021-02-01"),
                )
            },
            antall = 1.0,
        ),
    )

    private fun lagVoksneIHusstandGrunnlag(avvik: Boolean) = listOf(
        VoksneIHusstandPeriode(
            referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE,
            if (!avvik) {
                Periode(
                    datoFom = LocalDate.parse("2020-01-01"),
                    datoTil = LocalDate.parse("2020-02-01"),
                )
            } else {
                Periode(
                    datoFom = LocalDate.parse("2021-01-01"),
                    datoTil = LocalDate.parse("2021-02-01"),
                )
            },
            borMedAndre = true,
        ),
    )

    private fun lagSjablonGrunnlag() = listOf(
        SjablonPeriode(
            sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
            sjablon = Sjablon(
                navn = SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn,
                nøkkelListe = emptyList(),
                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(7.8))),
            ),
        ),
    )

    private fun byggResultatBeregning(): ResultatBeregning = ResultatBeregning(
        beløp = BigDecimal.valueOf(666),
        sjablonListe = emptyList(),
    )
}
