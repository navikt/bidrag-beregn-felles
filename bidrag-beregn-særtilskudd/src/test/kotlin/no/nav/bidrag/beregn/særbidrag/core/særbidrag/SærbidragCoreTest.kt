package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNokkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.beregning.SærbidragBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
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
internal class SærbidragCoreTest {

    private lateinit var særbidragCoreWithMock: SærbidragCore
    private lateinit var særbidragCore: SærbidragCore

    @Mock
    private lateinit var særbidragPeriodeMock: SærbidragPeriode

    private lateinit var beregnSærbidragGrunnlagCore: BeregnSærbidragGrunnlagCore
    private lateinit var beregnSærbidragPeriodeResultat: BeregnSærbidragResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        særbidragCoreWithMock = SærbidragCore(særbidragPeriodeMock)
        val særbidragBeregning = SærbidragBeregning()
        val særbidragPeriode = SærbidragPeriode(særbidragBeregning)
        særbidragCore = SærbidragCore(særbidragPeriode)
    }

    @Test
    @DisplayName("Skal beregne særbidrag")
    fun skalBeregneSærbidrag() {
        byggSærbidragPeriodeGrunnlagCore()
        byggSærbidragPeriodeResultat()

        `when`(særbidragPeriodeMock.beregnPerioder(any())).thenReturn(beregnSærbidragPeriodeResultat)

        val resultatCore = særbidragCoreWithMock.beregnSærbidrag(beregnSærbidragGrunnlagCore)

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
    @DisplayName("Skal beregne særbidrag uten mocks")
    fun skalBeregneSærbidragUtenMocks() {
        byggSærbidragPeriodeGrunnlagCore()

        val resultatCore = særbidragCore.beregnSærbidrag(beregnSærbidragGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(1) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe).hasSize(2) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne særbidrag ved avvik")
    fun skalIkkeBeregneSærbidragVedAvvik() {
        byggSærbidragPeriodeGrunnlagCore()
        byggAvvik()

        `when`(særbidragPeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = særbidragCoreWithMock.beregnSærbidrag(beregnSærbidragGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isNotEmpty() },
            { assertThat(resultatCore.avvikListe).hasSize(1) },
            { assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra") },
            { assertThat(resultatCore.avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString()) },
            { assertThat(resultatCore.resultatPeriodeListe).isEmpty() },
        )
    }

    private fun byggSærbidragPeriodeGrunnlagCore() {
        val bidragsevnePeriodeListe = listOf(
            BidragsevnePeriodeCore(
                referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(100000),
            ),
        )

        val bPsAndelSærbidragPeriodeListe = listOf(
            BPsAndelSærbidragPeriodeCore(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                andelProsent = BigDecimal.valueOf(100000),
                andelBeløp = BigDecimal.valueOf(20000),
                barnetErSelvforsørget = false,
            ),
        )

        val sjablonPeriodeListe = mapSjablonSjablontall(TestUtil.byggSjablonPeriodeListe())

        beregnSærbidragGrunnlagCore = BeregnSærbidragGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            søknadsbarnPersonId = "1",
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            bPsAndelSærbidragPeriodeListe = bPsAndelSærbidragPeriodeListe,
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

    private fun byggSærbidragPeriodeResultat() {

        val periodeResultatListe = listOf(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                søknadsbarnPersonId = "1",
                resultat = ResultatBeregning(resultatBeløp = BigDecimal.valueOf(1000), resultatkode = Resultatkode.SÆRBIDRAG_INNVILGET),
                grunnlag = GrunnlagBeregning(
                    bidragsevne = Bidragsevne(referanse = TestUtil.BIDRAGSEVNE_REFERANSE, beløp = BigDecimal.valueOf(1000)),
                    bPsAndelSærbidrag = BPsAndelSærbidrag(
                        referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                        andelProsent = BigDecimal.valueOf(60),
                        andelBeløp = BigDecimal.valueOf(8000),
                        barnetErSelvforsørget = false,
                    ),
                ),
            ),
        )

        beregnSærbidragPeriodeResultat = BeregnSærbidragResultat(periodeResultatListe)
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
