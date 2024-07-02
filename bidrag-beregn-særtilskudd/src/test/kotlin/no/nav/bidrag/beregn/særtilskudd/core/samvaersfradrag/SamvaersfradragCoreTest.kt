package no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.TestUtil
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.BeregnSamvaersfradragResultat
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.GrunnlagBeregningPeriodisert
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.bo.SamvaersfradragGrunnlagPerBarn
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.dto.BeregnSamvaersfradragGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.dto.SamvaersklassePeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.samvaersfradrag.periode.SamvaersfradragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
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
internal class SamvaersfradragCoreTest {

    private lateinit var samvaersfradragCore: SamvaersfradragCore

    @Mock
    private lateinit var samvaersfradragPeriodeMock: SamvaersfradragPeriode

    private lateinit var beregnSamvaersfradragGrunnlagCore: BeregnSamvaersfradragGrunnlagCore
    private lateinit var samvaersfradragPeriodeResultat: BeregnSamvaersfradragResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        samvaersfradragCore = SamvaersfradragCore(samvaersfradragPeriodeMock)
    }

    @Test
    @DisplayName("Skal beregne samværsfradrag")
    fun skalBeregneSamvaersfradrag() {
        byggSamvaersfradragPeriodeGrunnlagCore()
        byggSamvaersfradragPeriodeResultat()

        `when`(samvaersfradragPeriodeMock.beregnPerioder(any())).thenReturn(samvaersfradragPeriodeResultat)

        val resultatCore = samvaersfradragCore.beregnSamvaersfradrag(beregnSamvaersfradragGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].resultatBeregningListe[0].belop).isEqualTo(BigDecimal.valueOf(666)) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].resultatBeregningListe[0].belop).isEqualTo(BigDecimal.valueOf(667)) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].resultatBeregningListe[0].belop).isEqualTo(BigDecimal.valueOf(668)) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne samværsfradrag ved avvik")
    fun skalIkkeBeregneAndelVedAvvik() {
        byggSamvaersfradragPeriodeGrunnlagCore()
        byggAvvik()

        `when`(samvaersfradragPeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = samvaersfradragCore.beregnSamvaersfradrag(beregnSamvaersfradragGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isNotEmpty() },
            { assertThat(resultatCore.avvikListe).hasSize(1) },
            { assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra") },
            { assertThat(resultatCore.avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString()) },
            { assertThat(resultatCore.resultatPeriodeListe).isEmpty() },
        )
    }

    private fun byggSamvaersfradragPeriodeGrunnlagCore() {
        val samvaersklassePeriodeListe = listOf(
            SamvaersklassePeriodeCore(
                referanse = TestUtil.SAMVÆRSKLASSE_REFERANSE,
                samvaersklassePeriodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                barnPersonId = 1,
                barnFodselsdato = LocalDate.parse("2017-01-01"),
                samvaersklasse = "03",
            ),
        )

        val sjablonPeriodeListe = listOf(
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
            ),
        )

        beregnSamvaersfradragGrunnlagCore = BeregnSamvaersfradragGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            samvaersklassePeriodeListe = samvaersklassePeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggSamvaersfradragPeriodeResultat() {
        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                resultatDatoFraTil = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                resultatBeregningListe = listOf(
                    ResultatBeregning(
                        barnPersonId = 1,
                        belop = BigDecimal.valueOf(666),
                        sjablonListe = listOf(
                            SjablonPeriodeNavnVerdi(
                                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                verdi = BigDecimal.valueOf(22),
                            ),
                        ),
                    ),
                ),
                resultatGrunnlag = GrunnlagBeregningPeriodisert(
                    samvaersfradragGrunnlagPerBarnListe = listOf(
                        SamvaersfradragGrunnlagPerBarn(
                            referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                            barnPersonId = 1,
                            barnAlder = 4,
                            samvaersklasse = "02",
                        ),
                    ),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                            ),
                        ),
                    ),
                ),
            ),
        )
        periodeResultatListe.add(
            ResultatPeriode(
                resultatDatoFraTil = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                resultatBeregningListe = listOf(
                    ResultatBeregning(
                        barnPersonId = 1,
                        belop = BigDecimal.valueOf(667),
                        sjablonListe = listOf(
                            SjablonPeriodeNavnVerdi(
                                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                verdi = BigDecimal.valueOf(22),
                            ),
                        ),
                    ),
                ),
                resultatGrunnlag = GrunnlagBeregningPeriodisert(
                    samvaersfradragGrunnlagPerBarnListe = listOf(
                        SamvaersfradragGrunnlagPerBarn(
                            referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                            barnPersonId = 1,
                            barnAlder = 4,
                            samvaersklasse = "02",
                        ),
                    ),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                            ),
                        ),
                    ),
                ),
            ),
        )
        periodeResultatListe.add(
            ResultatPeriode(
                resultatDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                resultatBeregningListe = listOf(
                    ResultatBeregning(
                        barnPersonId = 1,
                        belop = BigDecimal.valueOf(668),
                        sjablonListe = listOf(
                            SjablonPeriodeNavnVerdi(
                                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                verdi = BigDecimal.valueOf(22),
                            ),
                        ),
                    ),
                ),
                resultatGrunnlag = GrunnlagBeregningPeriodisert(
                    samvaersfradragGrunnlagPerBarnListe = listOf(
                        SamvaersfradragGrunnlagPerBarn(
                            referanse = TestUtil.SAMVÆRSFRADRAG_REFERANSE,
                            barnPersonId = 1,
                            barnAlder = 4,
                            samvaersklasse = "02",
                        ),
                    ),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        samvaersfradragPeriodeResultat = BeregnSamvaersfradragResultat(periodeResultatListe)
    }

    private fun byggAvvik() {
        avvikListe = listOf(
            Avvik("beregnDatoTil må være etter beregnDatoFra", Avvikstype.DATO_FOM_ETTER_DATO_TIL),
        )
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
