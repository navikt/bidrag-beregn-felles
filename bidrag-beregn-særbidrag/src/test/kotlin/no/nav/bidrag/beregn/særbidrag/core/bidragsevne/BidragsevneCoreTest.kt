package no.nav.bidrag.beregn.særbidrag.core.bidragsevne

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.AntallBarnIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BeregnBidragsevneResultat
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.periode.BidragsevnePeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import org.assertj.core.api.Assertions.assertThat
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
internal class BidragsevneCoreTest {

    private lateinit var bidragsevneCore: BidragsevneCore

    @Mock
    private lateinit var bidragsevnePeriodeMock: BidragsevnePeriode

    private val beregnBidragsevneGrunnlagCore = byggBeregnBidragsevneGrunnlagCore()
    private val beregnBidragsevneResultat = byggBeregnBidragsevneResultat()
    private val avvikListe = byggAvvik()

    @BeforeEach
    fun initMocksAndService() {
        bidragsevneCore = BidragsevneCore(bidragsevnePeriodeMock)
    }

    @DisplayName("Beregning med ugyldig input gir avvik")
    @Test
    fun beregningMedUgyldigInputGirAvvik() {
        `when`(bidragsevnePeriodeMock.validerInput(any())).thenReturn(avvikListe)

        val resultatCore = bidragsevneCore.beregnBidragsevne(beregnBidragsevneGrunnlagCore)

        assertThat(resultatCore.avvikListe).isNotEmpty
        assertThat(resultatCore.avvikListe).hasSize(1)
        assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra")
        assertThat(resultatCore.resultatPeriodeListe).isEmpty()
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(bidragsevnePeriodeMock.validerInput(any())).thenReturn(emptyList())
        `when`(bidragsevnePeriodeMock.beregnPerioder(any())).thenReturn(beregnBidragsevneResultat)

        val resultatCore = bidragsevneCore.beregnBidragsevne(beregnBidragsevneGrunnlagCore)

        assertThat(resultatCore.avvikListe).isEmpty()
        assertThat(resultatCore.resultatPeriodeListe).isNotEmpty
        assertThat(resultatCore.resultatPeriodeListe).hasSize(1)
    }

    private fun byggBeregnBidragsevneGrunnlagCore(): BeregnBidragsevneGrunnlagCore {
        val inntektPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-01-01")),
                beløp = BigDecimal.valueOf(666000),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val barnIHusstandenPeriodeListe = listOf(
            BarnIHusstandenPeriodeCore(
                referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                antall = 1.0,
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val voksneIHusstandenPeriodeListe = listOf(
            VoksneIHusstandenPeriodeCore(
                referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                borMedAndre = false,
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val sjablonPeriodeListe = listOf(
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                nøkkelListe = emptyList(),
                innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
            ),
        )

        return BeregnBidragsevneGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2020-01-01"),
            beregnDatoTil = LocalDate.parse("2020-02-01"),
            inntektPeriodeListe = inntektPeriodeListe,
            barnIHusstandenPeriodeListe = barnIHusstandenPeriodeListe,
            voksneIHusstandenPeriodeListe = voksneIHusstandenPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggBeregnBidragsevneResultat(): BeregnBidragsevneResultat {
        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                resultat = ResultatBeregning(
                    beløp = BigDecimal.valueOf(666),
                    skatt = DelberegningBidragsevne.Skatt(
                        minstefradrag = BigDecimal.valueOf(80000),
                        skattAlminnelgInntekt = BigDecimal.valueOf(80000),
                        trinnskatt = BigDecimal.valueOf(20000),
                        trygdeavgift = BigDecimal.valueOf(30000),
                        sumSkatt = BigDecimal.valueOf(130000),
                    ),
                    underholdBarnEgenHusstand = BigDecimal.valueOf(10000),
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(
                                datoFom = LocalDate.parse("2020-01-01"),
                                datoTil = null,
                            ),
                            navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                            verdi = BigDecimal.valueOf(22),
                        ),
                    ),
                ),
                grunnlag = GrunnlagBeregning(
                    inntekt = Inntekt(referanse = TestUtil.INNTEKT_REFERANSE, inntektBeløp = BigDecimal.valueOf(666000)),
                    antallBarnIHusstand = AntallBarnIHusstand(referanse = TestUtil.BARN_I_HUSSTANDEN_REFERANSE, antallBarn = 1.0),
                    bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = TestUtil.VOKSNE_I_HUSSTANDEN_REFERANSE, borMedAndre = false),
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(
                                datoFom = LocalDate.parse("2020-01-01"),
                                datoTil = null,
                            ),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                                nøkkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(22))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        return BeregnBidragsevneResultat(periodeResultatListe)
    }

    private fun byggAvvik(): List<Avvik> = listOf(
        Avvik(
            avvikTekst = "beregnDatoTil må være etter beregnDatoFra",
            avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL,
        ),
    )

    companion object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
