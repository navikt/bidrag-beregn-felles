package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBpPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BidragsevnePeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.SumLøpendeBidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class SærbidragPeriodeTest {

    private lateinit var særbidragPeriode: SærbidragPeriode

    @Mock
    private lateinit var særbidragBeregningMock: SærbidragBeregning

    private val beregnSærbidragGrunnlag = byggBeregnSærbidragGrunnlag()
    private val beregnSærbidragResultat = byggResultatBeregning()

    @BeforeEach
    fun initMocksAndService() {
        særbidragPeriode = SærbidragPeriode(særbidragBeregningMock)
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(særbidragBeregningMock.beregn(any())).thenReturn(beregnSærbidragResultat)

        val resultatPeriode = særbidragPeriode.beregnPerioder(beregnSærbidragGrunnlag)

        assertThat(resultatPeriode.resultatPeriodeListe).isNotEmpty
        assertThat(resultatPeriode.resultatPeriodeListe).hasSize(1)
    }

    @DisplayName("Exception når BetaltAvBP mangler")
    @Test
    fun skalKasteExceptionNårBetaltAvBPMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            særbidragPeriode.beregnPerioder(byggBeregnSærbidragGrunnlag(avvikBetaltAvBP = true))
        }.withMessageContaining("Grunnlagsobjekt BETALT_AV_BP mangler data for periode")
    }

    @DisplayName("Exception når Bidragsevne mangler")
    @Test
    fun skalKasteExceptionNårBidragsevneMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            særbidragPeriode.beregnPerioder(byggBeregnSærbidragGrunnlag(avvikBidragsevne = true))
        }.withMessageContaining("Grunnlagsobjekt BIDRAGSEVNE mangler data for periode")
    }

    @DisplayName("Exception når BPs andel særbidrag mangler")
    @Test
    fun skalKasteExceptionNårBPsAndelSærbidragMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            særbidragPeriode.beregnPerioder(byggBeregnSærbidragGrunnlag(avvikBPsAndelSærbidrag = true))
        }.withMessageContaining("Grunnlagsobjekt BP_ANDEL_SÆRBIDRAG mangler data for periode")
    }

    private fun byggBeregnSærbidragGrunnlag(
        avvikBetaltAvBP: Boolean = false,
        avvikBidragsevne: Boolean = false,
        avvikBPsAndelSærbidrag: Boolean = false,
    ) = BeregnSærbidragGrunnlag(
        beregnDatoFra = LocalDate.parse("2020-01-01"),
        beregnDatoTil = LocalDate.parse("2020-02-01"),
        søknadsbarnPersonId = "11111111110",
        betaltAvBpPeriodeListe = lagBetaltAvBPGrunnlag(avvikBetaltAvBP),
        bidragsevnePeriodeListe = lagBidragsevneGrunnlag(avvikBidragsevne),
        sumLøpendeBidrag = lagSumLøpendeBidragGrunnlag(),
        bPsAndelSærbidragPeriodeListe = lagBPsAndelSærbidragGrunnlag(avvikBPsAndelSærbidrag),
    )

    private fun lagBetaltAvBPGrunnlag(avvik: Boolean) = listOf(
        BetaltAvBpPeriode(
            referanse = TestUtil.BETALT_AV_BP_REFERANSE,
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
            beløp = BigDecimal.valueOf(2000),
        ),
    )

    private fun lagBidragsevneGrunnlag(avvik: Boolean) = listOf(
        BidragsevnePeriode(
            referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
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
            beløp = BigDecimal.valueOf(5000),
        ),
    )

    private fun lagSumLøpendeBidragGrunnlag() = SumLøpendeBidragPeriode(
        referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
        Periode(
            datoFom = LocalDate.parse("2020-01-01"),
            datoTil = LocalDate.parse("2020-02-01"),
        ),
        sumLøpendeBidrag = BigDecimal.valueOf(5000),

    )

    private fun lagBPsAndelSærbidragGrunnlag(avvik: Boolean) = listOf(
        BPsAndelSærbidragPeriode(
            referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
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
            andelFaktor = BigDecimal.valueOf(0.60),
            andelBeløp = BigDecimal.valueOf(6000),
            barnetErSelvforsørget = false,
        ),
    )

    private fun byggResultatBeregning(): ResultatBeregning = ResultatBeregning(
        beregnetBeløp = BigDecimal.valueOf(6000),
        resultatKode = Resultatkode.SÆRBIDRAG_INNVILGET,
        resultatBeløp = BigDecimal.valueOf(4000),
    )

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
