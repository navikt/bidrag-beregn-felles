package no.nav.bidrag.beregn.særtilskudd.core.bpsandelsærtilskudd

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
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.BPsAndelSaertilskuddCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.Inntekt
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.ResultatBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.dto.NettoSaertilskuddPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.bpsandelsaertilskudd.periode.BPsAndelSaertilskuddPeriode
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.InntektPeriodeCore
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
internal class BPsAndelSaertilskuddPeriodeCoreTest {

    private lateinit var bPsAndelSaertilskuddCore: BPsAndelSaertilskuddCore

    @Mock
    private lateinit var bPsAndelSaertilskuddPeriodeMock: BPsAndelSaertilskuddPeriode

    private lateinit var beregnBPsAndelSaertilskuddGrunnlagCore: BeregnBPsAndelSaertilskuddGrunnlagCore
    private lateinit var bPsAndelSaertilskuddPeriodeResultat: BeregnBPsAndelSaertilskuddResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        bPsAndelSaertilskuddCore = BPsAndelSaertilskuddCore(bPsAndelSaertilskuddPeriodeMock)
    }

    @Test
    @DisplayName("Skal beregne BPs andel av særtilskudd")
    fun skalBeregneBPsAndelSaertilskudd() {
        byggBPsAndelSaertilskuddPeriodeGrunnlagCore()
        byggBPsAndelSaertilskuddPeriodeResultat()

        `when`(bPsAndelSaertilskuddPeriodeMock.beregnPerioder(any())).thenReturn(bPsAndelSaertilskuddPeriodeResultat)

        val resultatCore = bPsAndelSaertilskuddCore.beregnBPsAndelSaertilskudd(beregnBPsAndelSaertilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].resultatBeregning.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(10)) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].resultatBeregning.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(20)) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].resultatBeregning.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(30)) },
            { assertThat(resultatCore.sjablonListe[0].verdi).isEqualTo(BigDecimal.valueOf(1600)) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne BPs andel av Saertilskudd ved avvik")
    fun skalIkkeBeregneAndelVedAvvik() {
        byggBPsAndelSaertilskuddPeriodeGrunnlagCore()
        byggAvvik()

        `when`(bPsAndelSaertilskuddPeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = bPsAndelSaertilskuddCore.beregnBPsAndelSaertilskudd(beregnBPsAndelSaertilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isNotEmpty() },
            { assertThat(resultatCore.avvikListe).hasSize(1) },
            { assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra") },
            { assertThat(resultatCore.avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString()) },
            { assertThat(resultatCore.resultatPeriodeListe).isEmpty() },
        )
    }

    private fun byggBPsAndelSaertilskuddPeriodeGrunnlagCore() {
        val nettoSaertilskuddPeriodeListe = listOf(
            NettoSaertilskuddPeriodeCore(
                referanse = TestUtil.NETTO_SÆRTILSKUDD_REFERANSE,
                periodeDatoFraTil = PeriodeCore(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
            ),
        )

        val inntektBPPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(111),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val inntektBMPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(222),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val inntektBBPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(333),
                grunnlagsreferanseListe = emptyList(),
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

        beregnBPsAndelSaertilskuddGrunnlagCore = BeregnBPsAndelSaertilskuddGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            nettoSaertilskuddPeriodeListe = nettoSaertilskuddPeriodeListe,
            inntektBPPeriodeListe = inntektBPPeriodeListe,
            inntektBMPeriodeListe = inntektBMPeriodeListe,
            inntektBBPeriodeListe = inntektBBPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggBPsAndelSaertilskuddPeriodeResultat() {
        val inntektBPListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(111.0),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBMListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(222.0),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )
        val inntektBBListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(333.0),
                deltFordel = false,
                skatteklasse2 = false,
            ),
        )

        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                resultatDatoFraTil = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                resultatBeregning = ResultatBeregning(
                    resultatAndelProsent = BigDecimal.valueOf(10),
                    resultatAndelBelop = BigDecimal.valueOf(1000),
                    barnetErSelvforsorget = false,
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                            verdi = BigDecimal.valueOf(1600),
                        ),
                    ),
                ),
                resultatGrunnlagBeregning = GrunnlagBeregning(
                    nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektBBListe = inntektBBListe,
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
                resultatBeregning = ResultatBeregning(
                    resultatAndelProsent = BigDecimal.valueOf(20),
                    resultatAndelBelop = BigDecimal.valueOf(1000),
                    barnetErSelvforsorget = false,
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                            verdi = BigDecimal.valueOf(1600),
                        ),
                    ),
                ),
                resultatGrunnlagBeregning = GrunnlagBeregning(
                    nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektBBListe = inntektBBListe,
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1640))),
                            ),
                        ),
                    ),
                ),
            ),
        )
        periodeResultatListe.add(
            ResultatPeriode(
                resultatDatoFraTil = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                resultatBeregning = ResultatBeregning(
                    resultatAndelProsent = BigDecimal.valueOf(30),
                    resultatAndelBelop = BigDecimal.valueOf(1000),
                    barnetErSelvforsorget = false,
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                            verdi = BigDecimal.valueOf(1600),
                        ),
                    ),
                ),
                resultatGrunnlagBeregning = GrunnlagBeregning(
                    nettoSaertilskuddBelop = BigDecimal.valueOf(1000),
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektBBListe = inntektBBListe,
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("9999-12-31")),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1680))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        bPsAndelSaertilskuddPeriodeResultat = BeregnBPsAndelSaertilskuddResultat(periodeResultatListe)
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
