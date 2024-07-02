package no.nav.bidrag.beregn.særtilskudd.core.bidragsevne

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
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.AntallBarnIHusstand
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.Inntekt
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særtilskudd.core.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særtilskudd.core.felles.dto.VoksneIHusstandenPeriodeCore
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
internal class BidragsevnePeriodeCoreTest {

    private lateinit var bidragsevneCore: BidragsevneCore

    @Mock
    private lateinit var bidragsevnePeriodeMock: BidragsevnePeriode

    private lateinit var beregnBidragsevneGrunnlagCore: BeregnBidragsevneGrunnlagCore
    private lateinit var bidragsevnePeriodeResultat: BeregnBidragsevneResultat
    private lateinit var avvikListe: List<Avvik>

    @BeforeEach
    fun initMocksAndService() {
        bidragsevneCore = BidragsevneCore(bidragsevnePeriodeMock)
    }

    @Test
    @DisplayName("Skal beregne bidragsevne")
    fun skalBeregnebidragsevne() {
        byggBidragsevnePeriodeGrunnlagCore()
        byggBidragsevnePeriodeResultat()

        `when`(bidragsevnePeriodeMock.beregnPerioder(any())).thenReturn(bidragsevnePeriodeResultat)

        val resultatCore = bidragsevneCore.beregnBidragsevne(beregnBidragsevneGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).isNotEmpty() },
            { assertThat(resultatCore.resultatPeriodeListe).hasSize(3) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoFom).isEqualTo(LocalDate.parse("2017-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].periode.datoTil).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[0].resultat.belop).isEqualTo(BigDecimal.valueOf(666)) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe[0]).isEqualTo(TestUtil.BARN_I_HUSSTANDEN_REFERANSE) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe[1]).isEqualTo(TestUtil.BOSTATUS_REFERANSE) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe[2]).isEqualTo(TestUtil.INNTEKT_REFERANSE) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe[3]).isEqualTo(TestUtil.SÆRFRADRAG_REFERANSE) },
            { assertThat(resultatCore.resultatPeriodeListe[0].grunnlagsreferanseListe[4]).isEqualTo(TestUtil.SKATTEKLASSE_REFERANSE) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoFom).isEqualTo(LocalDate.parse("2018-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].periode.datoTil).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[1].resultat.belop).isEqualTo(BigDecimal.valueOf(667)) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoFom).isEqualTo(LocalDate.parse("2019-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].periode.datoTil).isEqualTo(LocalDate.parse("2020-01-01")) },
            { assertThat(resultatCore.resultatPeriodeListe[2].resultat.belop).isEqualTo(BigDecimal.valueOf(668)) },
        )
    }

    @Test
    @DisplayName("Skal ikke beregne bidragsevne ved avvik")
    fun skalIkkeBeregneBidragsevneVedAvvik() {
        byggBidragsevnePeriodeGrunnlagCore()
        byggAvvik()

        `when`(bidragsevnePeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = bidragsevneCore.beregnBidragsevne(beregnBidragsevneGrunnlagCore)

        assertAll(
            { assertThat(resultatCore).isNotNull() },
            { assertThat(resultatCore.avvikListe).isNotEmpty() },
            { assertThat(resultatCore.avvikListe).hasSize(1) },
            { assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra") },
            { assertThat(resultatCore.avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL.toString()) },
            { assertThat(resultatCore.resultatPeriodeListe).isEmpty() },
        )
    }

    private fun byggBidragsevnePeriodeGrunnlagCore() {
        val inntektPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                beløp = BigDecimal.valueOf(666000),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val barnIHusstandenPeriodeListe = listOf(
            BarnIHusstandenPeriodeCore(
                referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = null),
                antall = 1.0,
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val voksneIHusstandenPeriodeListe = listOf(
            VoksneIHusstandenPeriodeCore(
                referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                borMedAndre = false,
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val sjablonPeriodeListe = listOf(
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                nokkelListe = emptyList(),
                innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
            ),
        )

        beregnBidragsevneGrunnlagCore = BeregnBidragsevneGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2017-01-01"),
            beregnDatoTil = LocalDate.parse("2020-01-01"),
            inntektPeriodeListe = inntektPeriodeListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeListe,
            voksneIHusstandenPeriodeListe = voksneIHusstandenPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggBidragsevnePeriodeResultat() {
        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2017-01-01"), datoTil = LocalDate.parse("2018-01-01")),
                resultat = ResultatBeregning(
                    belop = BigDecimal.valueOf(666),
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                            verdi = BigDecimal.valueOf(22),
                        ),
                    ),
                ),
                grunnlag = GrunnlagBeregning(
                    inntektListe = listOf(
                        Inntekt(
                            referanse = TestUtil.INNTEKT_REFERANSE,
                            inntektType = "LONN_SKE",
                            inntektBelop = BigDecimal.valueOf(666000),
                        ),
                    ),
                    antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
                    bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
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
                    belop = BigDecimal.valueOf(667),
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                            verdi = BigDecimal.valueOf(22),
                        ),
                    ),
                ),
                grunnlag = GrunnlagBeregning(
                    inntektListe = listOf(
                        Inntekt(
                            referanse = TestUtil.INNTEKT_REFERANSE,
                            inntektType = "LONN_SKE",
                            inntektBelop = BigDecimal.valueOf(666000),
                        ),
                    ),
                    antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
                    bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
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
                    belop = BigDecimal.valueOf(668),
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                            verdi = BigDecimal.valueOf(22),
                        ),
                    ),
                ),
                grunnlag = GrunnlagBeregning(
                    inntektListe = listOf(Inntekt(TestUtil.INNTEKT_REFERANSE, "LONN_SKE", BigDecimal.valueOf(666000))),
                    antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
                    bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(
                                datoFom = LocalDate.parse("2017-01-01"),
                                datoTil = LocalDate.parse("9999-12-31"),
                            ),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                nokkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        bidragsevnePeriodeResultat = BeregnBidragsevneResultat(periodeResultatListe)
    }

    private fun byggAvvik() {
        avvikListe = listOf(
            Avvik(
                avvikTekst = "beregnDatoTil må være etter beregnDatoFra",
                avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL,
            ),
        )
    }

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
