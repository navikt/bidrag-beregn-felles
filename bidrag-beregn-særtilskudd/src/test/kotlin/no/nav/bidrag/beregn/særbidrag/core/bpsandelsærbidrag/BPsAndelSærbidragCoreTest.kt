package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSaertilskuddResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Utgift
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.InntektPeriodeCore
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
internal class BPsAndelSærbidragCoreTest {

    private lateinit var bPsAndelSærbidragCore: BPsAndelSærbidragCore

    @Mock
    private lateinit var bPsAndelSærbidragPeriodeMock: BPsAndelSærbidragPeriode

    private lateinit var beregnBPsAndelSærtilskuddGrunnlagCore: BeregnBPsAndelSærtilskuddGrunnlagCore
    private lateinit var bPsAndelSaertilskuddPeriodeResultat: BeregnBPsAndelSaertilskuddResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        bPsAndelSærbidragCore = BPsAndelSærbidragCore(bPsAndelSærbidragPeriodeMock)
    }

    @Test
    @DisplayName("Skal beregne BPs andel av særtilskudd")
    fun skalBeregneBPsAndelSaertilskudd() {
        byggBPsAndelSaertilskuddPeriodeGrunnlagCore()
        byggBPsAndelSaertilskuddPeriodeResultat()

        `when`(bPsAndelSærbidragPeriodeMock.beregnPerioder(any())).thenReturn(bPsAndelSaertilskuddPeriodeResultat)

        val resultatCore = bPsAndelSærbidragCore.beregnBPsAndelSaertilskudd(beregnBPsAndelSærtilskuddGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(10)) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(20)) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].resultat.resultatAndelProsent).isEqualTo(BigDecimal.valueOf(30)) },
            { assertThat(resultatCore.sjablonListe[0].verdi).isEqualTo(BigDecimal.valueOf(1600)) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne BPs andel av Saertilskudd ved avvik")
    fun skalIkkeBeregneAndelVedAvvik() {
        byggBPsAndelSaertilskuddPeriodeGrunnlagCore()
        byggAvvik()

        `when`(bPsAndelSærbidragPeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = bPsAndelSærbidragCore.beregnBPsAndelSaertilskudd(beregnBPsAndelSærtilskuddGrunnlagCore)

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
            UtgiftPeriodeCore(
                referanse = TestUtil.UTGIFT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2020-08-01")),
                beløp = BigDecimal.valueOf(1000),
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

        beregnBPsAndelSærtilskuddGrunnlagCore = BeregnBPsAndelSærtilskuddGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            utgiftPeriodeListe = nettoSaertilskuddPeriodeListe,
            inntektBPPeriodeListe = inntektBPPeriodeListe,
            inntektBMPeriodeListe = inntektBMPeriodeListe,
            inntektBBPeriodeListe = inntektBBPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggBPsAndelSaertilskuddPeriodeResultat() {
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBPListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(111.0),
            ),
        )
        val inntektBMListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(222.0),
            ),
        )
        val inntektBBListe = listOf(
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektType = "LONN_SKE",
                inntektBelop = BigDecimal.valueOf(333.0),
            ),
        )

        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                resultat = ResultatBeregning(
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
                grunnlag = GrunnlagBeregning(
                    utgift = utgift,
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektSBListe = inntektBBListe,
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
                periode = Periode(datoFom = LocalDate.parse("2018-01-01"), datoTil = LocalDate.parse("2019-01-01")),
                resultat = ResultatBeregning(
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
                grunnlag = GrunnlagBeregning(
                    utgift = utgift,
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektSBListe = inntektBBListe,
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
                periode = Periode(datoFom = LocalDate.parse("2019-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                resultat = ResultatBeregning(
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
                grunnlag = GrunnlagBeregning(
                    utgift = utgift,
                    inntektBPListe = inntektBPListe,
                    inntektBMListe = inntektBMListe,
                    inntektSBListe = inntektBBListe,
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
