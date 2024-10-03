package no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag

import no.nav.bidrag.beregn.core.bo.Avvik
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.bo.SjablonPeriodeNavnVerdi
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.core.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.særbidrag.TestUtil
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCoreTest
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.BeregnBPsAndelSærbidragResultat
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.GrunnlagBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Inntekt
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatBeregning
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.ResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.bo.Utgift
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.UtgiftPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.periode.BPsAndelSærbidragPeriode
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
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
internal class BPsAndelSærbidragCoreTest {

    private lateinit var bPsAndelSærbidragCore: BPsAndelSærbidragCore

    @Mock
    private lateinit var bPsAndelSærbidragPeriodeMock: BPsAndelSærbidragPeriode

    private val beregnBPsAndelSærbidragGrunnlagCore = byggBeregnBPsAndelSærbidragGrunnlagCore()
    private val beregnBPsAndelSærbidragResultat = byggBeregnBPsAndelSærbidragResultat()
    private val avvikListe = byggAvvik()

    @BeforeEach
    fun initMocksAndService() {
        bPsAndelSærbidragCore = BPsAndelSærbidragCore(bPsAndelSærbidragPeriodeMock)
    }

    @DisplayName("Beregning med ugyldig input gir avvik")
    @Test
    fun beregningMedUgyldigInputGirAvvik() {
        `when`(bPsAndelSærbidragPeriodeMock.validerInput(BidragsevneCoreTest.any())).thenReturn(avvikListe)

        val resultatCore = bPsAndelSærbidragCore.beregnBPsAndelSærbidrag(beregnBPsAndelSærbidragGrunnlagCore)

        assertThat(resultatCore.avvikListe).isNotEmpty
        assertThat(resultatCore.avvikListe).hasSize(1)
        assertThat(resultatCore.avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFra")
        assertThat(resultatCore.resultatPeriodeListe).isEmpty()
    }

    @DisplayName("Beregning med gyldig input gir korrekt resultat")
    @Test
    fun beregningMedGyldigInputGirKorrektResultat() {
        `when`(bPsAndelSærbidragPeriodeMock.validerInput(BidragsevneCoreTest.any())).thenReturn(emptyList())
        `when`(bPsAndelSærbidragPeriodeMock.beregnPerioder(BidragsevneCoreTest.any())).thenReturn(beregnBPsAndelSærbidragResultat)

        val resultatCore = bPsAndelSærbidragCore.beregnBPsAndelSærbidrag(beregnBPsAndelSærbidragGrunnlagCore)

        assertThat(resultatCore.avvikListe).isEmpty()
        assertThat(resultatCore.resultatPeriodeListe).isNotEmpty
        assertThat(resultatCore.resultatPeriodeListe).hasSize(1)
    }

    private fun byggBeregnBPsAndelSærbidragGrunnlagCore(): BeregnBPsAndelSærbidragGrunnlagCore {
        val utgiftPeriodeListe = listOf(
            UtgiftPeriodeCore(
                referanse = TestUtil.UTGIFT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.valueOf(1000),
            ),
        )

        val inntektBPPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.valueOf(111),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val inntektBMPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.valueOf(222),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val inntektSBPeriodeListe = listOf(
            InntektPeriodeCore(
                referanse = TestUtil.INNTEKT_REFERANSE,
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                beløp = BigDecimal.valueOf(333),
                grunnlagsreferanseListe = emptyList(),
            ),
        )

        val sjablonPeriodeListe = listOf(
            SjablonPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                nøkkelListe = emptyList(),
                innholdListe = listOf(SjablonInnholdCore(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
            ),
        )

        return BeregnBPsAndelSærbidragGrunnlagCore(
            beregnDatoFra = LocalDate.parse("2020-01-01"),
            beregnDatoTil = LocalDate.parse("2020-02-01"),
            utgiftPeriodeListe = utgiftPeriodeListe,
            inntektBPPeriodeListe = inntektBPPeriodeListe,
            inntektBMPeriodeListe = inntektBMPeriodeListe,
            inntektSBPeriodeListe = inntektSBPeriodeListe,
            sjablonPeriodeListe = sjablonPeriodeListe,
        )
    }

    private fun byggBeregnBPsAndelSærbidragResultat(): BeregnBPsAndelSærbidragResultat {
        val utgift =
            Utgift(
                referanse = TestUtil.UTGIFT_REFERANSE,
                beløp = BigDecimal.valueOf(1000),
            )
        val inntektBP =
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektBeløp = BigDecimal.valueOf(111.0),
            )
        val inntektBM =
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektBeløp = BigDecimal.valueOf(222.0),
            )
        val inntektSB =
            Inntekt(
                referanse = TestUtil.INNTEKT_REFERANSE,
                inntektBeløp = BigDecimal.valueOf(333.0),
            )

        val periodeResultatListe = mutableListOf<ResultatPeriode>()
        periodeResultatListe.add(
            ResultatPeriode(
                periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = LocalDate.parse("2020-02-01")),
                resultat = ResultatBeregning(
                    endeligAndelFaktor = BigDecimal.valueOf(0.10),
                    andelBeløp = BigDecimal.valueOf(1000),
                    beregnetAndelFaktor = BigDecimal.valueOf(0.10),
                    barnEndeligInntekt = BigDecimal.ZERO,
                    barnetErSelvforsørget = false,
                    sjablonListe = listOf(
                        SjablonPeriodeNavnVerdi(
                            periode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                            navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                            verdi = BigDecimal.valueOf(1600),
                        ),
                    ),
                ),
                grunnlag = GrunnlagBeregning(
                    utgift = utgift,
                    inntektBP = inntektBP,
                    inntektBM = inntektBM,
                    inntektSB = inntektSB,
                    sjablonListe = listOf(
                        SjablonPeriode(
                            sjablonPeriode = Periode(datoFom = LocalDate.parse("2020-01-01"), datoTil = null),
                            sjablon = Sjablon(
                                navn = SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                                nøkkelListe = emptyList(),
                                innholdListe = listOf(SjablonInnhold(navn = SjablonInnholdNavn.SJABLON_VERDI.navn, verdi = BigDecimal.valueOf(1600))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        return BeregnBPsAndelSærbidragResultat(periodeResultatListe)
    }

    private fun byggAvvik(): List<Avvik> = listOf(
        Avvik(avvikTekst = "beregnDatoTil må være etter beregnDatoFra", avvikType = Avvikstype.DATO_FOM_ETTER_DATO_TIL),
    )
}
