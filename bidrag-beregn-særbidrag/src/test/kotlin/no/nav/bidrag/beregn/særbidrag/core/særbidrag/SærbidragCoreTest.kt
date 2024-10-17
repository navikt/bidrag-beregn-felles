package no.nav.bidrag.beregn.særbidrag.core.særbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCoreTest
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsAndelSærbidrag
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BPsBeregnedeTotalbidragPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BeregnSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.BetaltAvBp
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.Bidragsevne
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsBeregnedeTotalbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BetaltAvBpPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.periode.SærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeregnetBidragPerBarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
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
import java.util.Collections.emptyList

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
        `when`(særbidragPeriodeMock.validerInput(BidragsevneCoreTest.any())).thenReturn(avvikListe)

        val resultatCore = særbidragCore.beregnSærbidrag(beregnSærbidragGrunnlagCore)

        assertThat(resultatCore.avvikListe).isNotEmpty
        assertThat(resultatCore.avvikListe).hasSize(1)
        assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra")
        assertThat(resultatCore.resultatPeriodeListe).isEmpty()
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(særbidragPeriodeMock.validerInput(BidragsevneCoreTest.any())).thenReturn(emptyList())
        `when`(særbidragPeriodeMock.beregnPerioder(BidragsevneCoreTest.any())).thenReturn(beregnSærbidragPeriodeResultat)

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
                skatt = DelberegningBidragsevne.Skatt(
                    minstefradrag = BigDecimal.valueOf(80000),
                    skattAlminneligInntekt = BigDecimal.valueOf(80000),
                    trinnskatt = BigDecimal.valueOf(20000),
                    trygdeavgift = BigDecimal.valueOf(30000),
                    sumSkatt = BigDecimal.valueOf(130000),
                ),
                underholdBarnEgenHusstand = BigDecimal.valueOf(10000),
                beløp = BigDecimal.valueOf(100000),
            ),
        )

        val bPsBeregnedeTotalbidrag = BPsBeregnedeTotalbidragPeriodeCore(
            referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG_REFERANSE,
            periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
            bPsBeregnedeTotalbidrag = BigDecimal.valueOf(1000),
            beregnetBidragPerBarnListe = listOf(
                BeregnetBidragPerBarn(
                    gjelderBarn = "referanse1",
                    saksnummer = Saksnummer("1"),
                    løpendeBeløp = BigDecimal.valueOf(800),
                    valutakode = "NOK",
                    samværsfradrag = BigDecimal.valueOf(100),
                    samværsklasse = Samværsklasse.SAMVÆRSKLASSE_1,
                    beregnetBeløp = BigDecimal.valueOf(700),
                    faktiskBeløp = BigDecimal.valueOf(600),
                    reduksjonUnderholdskostnad = BigDecimal.valueOf(100),
                    beregnetBidrag = BigDecimal.valueOf(1000),
                ),
            ),
        )

        val bPsAndelSærbidragPeriodeListe = listOf(
            BPsAndelSærbidragPeriodeCore(
                referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                endeligAndelFaktor = BigDecimal.valueOf(1.5),
                andelBeløp = BigDecimal.valueOf(20000),
                beregnetAndelFaktor = BigDecimal.valueOf(1.5),
                barnEndeligInntekt = BigDecimal.ZERO,
                barnetErSelvforsørget = false,
            ),
        )

        return BeregnSærbidragGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2020-01-01"),
            beregnDatoTil = LocalDate.parse("2020-02-01"),
            søknadsbarnPersonId = "1",
            betaltAvBpPeriodeListe = betaltAvBpPeriodeListe,
            bidragsevnePeriodeListe = bidragsevnePeriodeListe,
            bPsBeregnedeTotalbidragPeriodeCore = bPsBeregnedeTotalbidrag,
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
                    bidragsevne = Bidragsevne(
                        referanse = TestUtil.BIDRAGSEVNE_REFERANSE,
                        beløp = BigDecimal.valueOf(1000),
                        skatt = DelberegningBidragsevne.Skatt(
                            minstefradrag = BigDecimal.valueOf(80000),
                            skattAlminneligInntekt = BigDecimal.valueOf(90000),
                            trinnskatt = BigDecimal.valueOf(10000),
                            trygdeavgift = BigDecimal.valueOf(50000),
                            sumSkatt = BigDecimal.valueOf(150000),
                        ),
                        underholdBarnEgenHusstand = BigDecimal.valueOf(10000),
                    ),
                    bPsBeregnedeTotalbidrag = BPsBeregnedeTotalbidragPeriode(
                        referanse = TestUtil.LØPENDE_BIDRAG_GRUNNLAG_REFERANSE,
                        periode = Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-02-01")),
                        bPsBeregnedeTotalbidrag = BigDecimal.valueOf(10000),
                    ),
                    bPsAndelSærbidrag = BPsAndelSærbidrag(
                        referanse = TestUtil.BPS_ANDEL_SÆRBIDRAG_REFERANSE,
                        endeligAndelFaktor = BigDecimal.valueOf(0.60),
                        andelBeløp = BigDecimal.valueOf(8000),
                        beregnetAndelFaktor = BigDecimal.valueOf(0.60),
                        barnEndeligInntekt = BigDecimal.ZERO,
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
