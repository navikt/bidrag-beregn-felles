package no.nav.bidrag.beregn.særtilskudd.core.sluttberegning

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.SærtilskuddCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.beregning.SaertilskuddBeregning
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BPsAndelSaertilskudd
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.BeregnSaertilskuddResultat
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.Bidragsevne
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.LopendeBidrag
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.ResultatBeregning
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.bo.SamvaersfradragGrunnlag
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.BPsAndelSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.BeregnSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.LopendeBidragPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.dto.SamvaersfradragPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.særtilskudd.periode.SaertilskuddPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
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
internal class SluttberegningCoreTest {

    private lateinit var saertilskuddCoreWithMock: SærtilskuddCore
    private lateinit var saertilskuddCore: SærtilskuddCore

    @Mock
    private lateinit var saertilskuddPeriodeMock: SaertilskuddPeriode

    private lateinit var beregnSaertilskuddGrunnlagCore: BeregnSaertilskuddGrunnlagCore
    private lateinit var beregnSaertilskuddPeriodeResultat: BeregnSaertilskuddResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        saertilskuddCoreWithMock = SærtilskuddCore(saertilskuddPeriodeMock)
        val saertilskuddBeregning = SaertilskuddBeregning()
        val saertilskuddPeriode = SaertilskuddPeriode(saertilskuddBeregning)
        saertilskuddCore = SærtilskuddCore(saertilskuddPeriode)
    }

    @Test
    @DisplayName("Skal beregne særtilskudd")
    fun skalBeregneSaertilskudd() {
        byggSaertilskuddPeriodeGrunnlagCore()
        byggSaertilskuddPeriodeResultat()

        `when`(saertilskuddPeriodeMock.beregnPerioder(any())).thenReturn(beregnSaertilskuddPeriodeResultat)

        val resultatCore = saertilskuddCoreWithMock.beregnSaertilskudd(beregnSaertilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
        )
    }

    @Test
    @DisplayName("Skal beregne særtilskudd uten mocks")
    fun skalBeregneSaertilskuddUtenMocks() {
        byggSaertilskuddPeriodeGrunnlagCore()

        val resultatCore = saertilskuddCore.beregnSaertilskudd(beregnSaertilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagReferanseListe).hasSize(4) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne særtilskudd ved avvik")
    fun skalIkkeBeregneSaertilskuddVedAvvik() {
        byggSaertilskuddPeriodeGrunnlagCore()
        byggAvvik()

        `when`(saertilskuddPeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = saertilskuddCoreWithMock.beregnSaertilskudd(beregnSaertilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isNotEmpty() },
            { assertThat(resultatCore.avvikListe).hasSize(1) },
            { assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra") },
            { assertThat(resultatCore.avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString()) },
            { assertThat(resultatCore.resultatPeriodeListe).isEmpty() },
        )
    }

    private fun byggSaertilskuddPeriodeGrunnlagCore() {
        val bidragsevnePeriodeListe = listOf(
            BidragsevnePeriodeCore(
                referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                periodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                bidragsevneBelop = BigDecimal.valueOf(100000),
            ),
        )

        val bPsAndelSaertilskuddPeriodeListe = listOf(
            BPsAndelSaertilskuddPeriodeCore(
                referanse = TestUtil.BPS_ANDEL_SÆRTILSKUDD_REFERANSE,
                periodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                bPsAndelSaertilskuddProsent = BigDecimal.valueOf(100000),
                bPsAndelSaertilskuddBelop = BigDecimal.valueOf(20000),
                barnetErSelvforsorget = false,
            ),
        )

        val lopendeBidragPeriodeListe = listOf(
            LopendeBidragPeriodeCore(
                referanse = TestUtil.LØPENDE_BIDRAG_REFERANSE,
                periodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                barnPersonId = 1,
                lopendeBidragBelop = BigDecimal.valueOf(1000),
                opprinneligBPsAndelUnderholdskostnadBelop = BigDecimal.valueOf(1000),
                opprinneligBidragBelop = BigDecimal.valueOf(1000),
                opprinneligSamvaersfradragBelop = BigDecimal.valueOf(1000),
            ),
        )

        val samvaersfradragPeriodeListe = listOf(
            SamvaersfradragPeriodeCore(
                referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                periodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                barnPersonId = 1,
                samvaersfradragBelop = BigDecimal.valueOf(1000),
            ),
        )

        val sjablonPeriodeListe = mapSjablonSjablontall(TestUtil.byggSjablonPeriodeListe())

        beregnSaertilskuddGrunnlagCore = BeregnSaertilskuddGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            soknadsbarnPersonId = 1,
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            bPsAndelSaertilskuddPeriodeListe = bPsAndelSaertilskuddPeriodeListe,
            lopendeBidragPeriodeListe = lopendeBidragPeriodeListe,
            samvaersfradragPeriodeListe = samvaersfradragPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun mapSjablonSjablontall(sjablonPeriodeListe: List<SjablonPeriode>): MutableList<SjablonPeriodeCore> = sjablonPeriodeListe.stream()
        .map { sjablon: SjablonPeriode ->
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = sjablon.getPeriode().datoFom, datoTil = sjablon.getPeriode().datoTil),
                navn = sjablon.sjablon.navn,
                nokkelListe = sjablon.sjablon.nokkelListe!!.stream()
                    .map { (navn, verdi): SjablonNokkel ->
                        SjablonNokkelCore(
                            navn = navn,
                            verdi = verdi,
                        )
                    }
                    .toList(),
                innholdListe = sjablon.sjablon.innholdListe.stream()
                    .map { (navn, verdi): SjablonInnhold ->
                        SjablonInnholdCore(
                            navn = navn,
                            verdi = verdi,
                        )
                    }
                    .toList(),
            )
        }
        .toList()

    private fun byggSaertilskuddPeriodeResultat() {
        val lopendeBidragListe = listOf(
            LopendeBidrag(
                referanse = TestUtil.LØPENDE_BIDRAG_REFERANSE,
                barnPersonId = 1,
                lopendeBidragBelop = BigDecimal.valueOf(100),
                opprinneligBPsAndelUnderholdskostnadBelop = BigDecimal.valueOf(1000),
                opprinneligBidragBelop = BigDecimal.valueOf(1000),
                opprinneligSamvaersfradragBelop = BigDecimal.valueOf(1000),
            ),
        )

        val samvaersfradragListe = listOf(
            SamvaersfradragGrunnlag(referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE, barnPersonId = 1, samvaersfradragBelop = BigDecimal.valueOf(100)),
        )

        val periodeResultatListe = listOf(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                soknadsbarnPersonId = 1,
                resultat = ResultatBeregning(resultatBelop = BigDecimal.valueOf(1000), resultatkode = Resultatkode.SÆRTILSKUDD_INNVILGET),
                grunnlag = GrunnlagBeregning(
                    bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, bidragsevneBelop = BigDecimal.valueOf(1000)),
                    bPsAndelSaertilskudd = BPsAndelSaertilskudd(
                        referanse = TestUtil.BPS_ANDEL_SÆRTILSKUDD_REFERANSE,
                        bPsAndelSaertilskuddProsent = BigDecimal.valueOf(60),
                        bPsAndelSaertilskuddBelop = BigDecimal.valueOf(8000),
                        barnetErSelvforsorget = false,
                    ),
                    lopendeBidragListe = lopendeBidragListe,
                    samvaersfradragGrunnlagListe = samvaersfradragListe,
                ),
            ),
        )

        beregnSaertilskuddPeriodeResultat = BeregnSaertilskuddResultat(periodeResultatListe)
    }

    private fun byggAvvik() {
        avvikListe = listOf(
            Avvik(avvikTekst = "beregnDatoTil må være etter beregnDatoFra", avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL),
        )
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
