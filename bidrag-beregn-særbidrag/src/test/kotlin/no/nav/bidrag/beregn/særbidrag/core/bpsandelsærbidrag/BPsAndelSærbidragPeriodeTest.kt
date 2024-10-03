package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.beregning.BPsAndelSærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragGrunnlag
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.InntektPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.UtgiftPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
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
internal class BPsAndelSærbidragPeriodeTest {

    private lateinit var bPsAndelSærbidragPeriode: BPsAndelSærbidragPeriode

    @Mock
    private lateinit var bPsAndelSærbidragBeregningMock: BPsAndelSærbidragBeregning

    private val beregnBPsAndelSærbidragGrunnlag = byggBeregnBPsAndelSærbidragGrunnlag()
    private val beregnBPsAndelSærbidragResultat = byggResultatBeregning()

    @BeforeEach
    fun initMocksAndService() {
        bPsAndelSærbidragPeriode = BPsAndelSærbidragPeriode(bPsAndelSærbidragBeregningMock)
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(bPsAndelSærbidragBeregningMock.beregn(any())).thenReturn(beregnBPsAndelSærbidragResultat)

        val resultatPeriode = bPsAndelSærbidragPeriode.beregnPerioder(beregnBPsAndelSærbidragGrunnlag)

        assertThat(resultatPeriode.resultatPeriodeListe).isNotEmpty
        assertThat(resultatPeriode.resultatPeriodeListe).hasSize(1)
    }

    @DisplayName("Exception når Utgift mangler")
    @Test
    fun skalKasteExceptionNårUtgiftMangler() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            bPsAndelSærbidragPeriode.beregnPerioder(byggBeregnBPsAndelSærbidragGrunnlag(avvikUtgift = true))
        }.withMessageContaining("Grunnlagsobjekt DELBEREGNING_UTGIFT mangler data for periode")
    }

    private fun byggBeregnBPsAndelSærbidragGrunnlag(avvikUtgift: Boolean = false) = BeregnBPsAndelSærbidragGrunnlag(
        beregnDatoFra = LocalDate.parse("2020-01-01"),
        beregnDatoTil = LocalDate.parse("2020-02-01"),
        utgiftPeriodeListe = lagUtgiftGrunnlag(avvikUtgift),
        inntektBPPeriodeListe = lagInntektGrunnlag(),
        inntektBMPeriodeListe = lagInntektGrunnlag(),
        inntektSBPeriodeListe = lagInntektGrunnlag(),
        sjablonPeriodeListe = lagSjablonGrunnlag(),
    )

    private fun lagInntektGrunnlag() = listOf(
        InntektPeriode(
            referanse = TestUtil.INNTEKT_REFERANSE,
            periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
            beløp = BigDecimal.valueOf(666000),
        ),
    )

    private fun lagUtgiftGrunnlag(avvik: Boolean) = listOf(
        UtgiftPeriode(
            referanse = TestUtil.UTGIFT_REFERANSE,
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
            beløp = BigDecimal.valueOf(10000),
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
        endeligAndelFaktor = BigDecimal.valueOf(0.60),
        andelBeløp = BigDecimal.valueOf(6000),
        beregnetAndelFaktor = BigDecimal.valueOf(0.60),
        barnEndeligInntekt = BigDecimal.ZERO,
        barnetErSelvforsørget = false,
        sjablonListe = emptyList(),
    )

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
