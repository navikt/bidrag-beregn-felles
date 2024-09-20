package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.SumLøpendeBidragCoreCoreTest
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBp
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.SumLøpendeBidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BetaltAvBpPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.SumLøpendeBidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
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
internal class SærbidragCoreTest {

    private lateinit var særbidragCore: SærbidragCore

    @Mock
    private lateinit var særbidragPeriodeMock: SærbidragPeriode

    private val beregnSærbidragGrunnlagCore = byggBeregnSærbidragGrunnlagCore()
    private val beregnSærbidragPeriodeResultat = byggBeregnSærbidragResultat()
    private val avvikListe = byggAvvik()

    @BeforeEach
    fun initMocksAndService() {
        særbidragCore = SærbidragCore(særbidragPeriodeMock)
    }

    @DisplayName("Beregning med ugyldig input gir avvik")
    @Test
    fun beregningMedUgyldigInputGirAvvik() {
        `when`(særbidragPeriodeMock.validerInput(SumLøpendeBidragCoreCoreTest.any())).thenReturn(avvikListe)

        val resultatCore = særbidragCore.beregnSærbidrag(beregnSærbidragGrunnlagCore)

        assertThat(resultatCore.avvikListe).isNotEmpty
        assertThat(resultatCore.avvikListe).hasSize(1)
        assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra")
        assertThat(resultatCore.resultatPeriodeListe).isEmpty()
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(særbidragPeriodeMock.validerInput(SumLøpendeBidragCoreCoreTest.any())).thenReturn(emptyList())
        `when`(særbidragPeriodeMock.beregnPerioder(SumLøpendeBidragCoreCoreTest.any())).thenReturn(beregnSærbidragPeriodeResultat)

        val resultatCore = særbidragCore.beregnSærbidrag(beregnSærbidragGrunnlagCore)

        assertThat(resultatCore.avvikListe).isEmpty()
        assertThat(resultatCore.resultatPeriodeListe).isNotEmpty
        assertThat(resultatCore.resultatPeriodeListe).hasSize(1)
    }

    private fun byggBeregnSærbidragGrunnlagCore(): BeregnSærbidragGrunnlagCore {
        val betaltAvBpPeriodeListe = listOf(
            BetaltAvBpPeriodeCore(
                referanse = TestUtil.BETALT_AV_BP_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.ZERO,
            ),
        )

        val bidragsevnePeriodeListe = listOf(
            BidragsevnePeriodeCore(
                referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.valueOf(100000),
            ),
        )

        val sumLøpendeBidragPeriode = SumLøpendeBidragPeriodeCore(
            referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
            periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
            sum = BigDecimal.valueOf(10000),
        )

        val bPsAndelSærbidragPeriodeListe = listOf(
            BPsAndelSærbidragPeriodeCore(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                andelFaktor = BigDecimal.valueOf(1.5),
                andelBeløp = BigDecimal.valueOf(20000),
                barnetErSelvforsørget = false,
            ),
        )

        return BeregnSærbidragGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2020-01-01"),
            beregnDatoTil = LocalDate.parse("2020-02-01"),
            søknadsbarnPersonId = "1",
            betaltAvBpPeriodeListe = betaltAvBpPeriodeListe,
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            sumLøpendeBidrag = sumLøpendeBidragPeriode,
            bPsAndelSærbidragPeriodeListe = bPsAndelSærbidragPeriodeListe,
        )
    }

    private fun byggBeregnSærbidragResultat(): BeregnSærbidragResultat {
        val periodeResultatListe = listOf(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                søknadsbarnPersonId = "1",
                resultat = ResultatBeregning(
                    beregnetBeløp = BigDecimal.valueOf(1000),
                    resultatKode = Resultatkode.SÆRBIDRAG_INNVILGET,
                    resultatBeløp = BigDecimal.valueOf(1000),
                ),
                grunnlag = GrunnlagBeregning(
                    betaltAvBp = BetaltAvBp(referanse = TestUtil.BETALT_AV_BP_REFERANSE, beløp = BigDecimal.ZERO),
                    bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = BigDecimal.valueOf(1000)),
                    sumLøpendeBidrag = SumLøpendeBidragPeriode(
                        referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG,
                        periode = Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-01")),
                        sum = BigDecimal.valueOf(10000),
                    ),
                    bPsAndelSærbidrag = BPsAndelSærbidrag(
                        referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                        andelFaktor = BigDecimal.valueOf(0.60),
                        andelBeløp = BigDecimal.valueOf(8000),
                        barnetErSelvforsørget = false,
                    ),
                ),
            ),
        )

        return BeregnSærbidragResultat(periodeResultatListe)
    }

    private fun byggAvvik(): List<Avvik> = listOf(
        Avvik(avvikTekst = "beregnDatoTil må være etter beregnDatoFra", avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL),
    )
}
