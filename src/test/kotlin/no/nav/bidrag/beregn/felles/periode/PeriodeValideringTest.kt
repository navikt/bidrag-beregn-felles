package no.nav.bidrag.beregn.felles.periode

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.util.PeriodeUtil
import no.nav.bidrag.domene.enums.beregning.Avvikstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.time.LocalDate

internal class PeriodeValideringTest {
    private val dataElement = "bidragMottakerInntektPeriodeListe"

    @Test
    @DisplayName("Test av overlappende perioder")
    fun testOverlappendePerioder() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-01-01"), datoTil = LocalDate.parse("2023-03-01")),
                        Periode(datoFom = LocalDate.parse("2023-02-01"), datoTil = LocalDate.parse("2023-04-01")),
                    ),
                sjekkOverlappendePerioder = true,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = false,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODER_OVERLAPPER) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("Overlappende perioder i $dataElement: datoTil=2023-03-01, datoFom=2023-02-01")
            },
        )
    }

    @Test
    @DisplayName("Test av opphold mellom perioder")
    fun testOppholdMellomPerioder() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-01-01"), datoTil = LocalDate.parse("2023-02-01")),
                        Periode(datoFom = LocalDate.parse("2023-03-01"), datoTil = LocalDate.parse("2023-04-01")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = true,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = false,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODER_HAR_OPPHOLD) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("Opphold mellom perioder i $dataElement: datoTil=2023-02-01, datoFom=2023-03-01")
            },
        )
    }

    @Test
    @DisplayName("Test av at datoTil er null")
    fun testDatoTilErNull() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-01-01"), datoTil = null),
                        Periode(datoFom = LocalDate.parse("2023-03-01"), datoTil = LocalDate.parse("2023-04-01")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = true,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = false,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.NULL_VERDI_I_DATO) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("datoTil kan ikke være null i $dataElement: datoFom=2023-01-01, datoTil=null")
            },
        )
    }

    @Test
    @DisplayName("Test av at datoer ikke er første dag i måneden")
    fun testDatoerErIkkeFørsteDagIMåneden() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-01-04"), datoTil = LocalDate.parse("2023-02-01")),
                        Periode(datoFom = LocalDate.parse("2023-03-01"), datoTil = LocalDate.parse("2023-04-16")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = true,
                sjekkBeregnPeriode = false,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(2) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.DAG_ER_IKKE_FØRSTE_DAG_I_MND) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("datoFom i $dataElement må være den første dagen i måneden: datoFom=2023-01-04")
            },
            Executable { assertThat(avvikListe[1].avvikType).isEqualTo(Avvikstype.DAG_ER_IKKE_FØRSTE_DAG_I_MND) },
            Executable {
                assertThat(
                    avvikListe[1].avvikTekst,
                ).isEqualTo("datoTil i $dataElement må være den første dagen i måneden: datoTil=2023-04-16")
            },
        )
    }

    @Test
    @DisplayName("Test av at datoFom er etter datoTil")
    fun testDatoFomErEtterDatoTil() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-02-01"), datoTil = LocalDate.parse("2023-01-01")),
                        Periode(datoFom = LocalDate.parse("2023-02-01"), datoTil = LocalDate.parse("2023-04-01")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = false,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("datoTil må være etter datoFom i $dataElement: datoFom=2023-02-01, datoTil=2023-01-01")
            },
        )
    }

    @Test
    @DisplayName("Test av at første dato i periodelisten er etter beregnDatoFom")
    fun testFørsteDatoPeriodelisteErEtterBeregnDatoFom() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-02-01"), datoTil = LocalDate.parse("2023-03-01")),
                        Periode(datoFom = LocalDate.parse("2023-03-01"), datoTil = LocalDate.parse("2023-04-01")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = true,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("Første dato i $dataElement (2023-02-01) er etter beregnDatoFom (2023-01-01)")
            },
        )
    }

    @Test
    @DisplayName("Test av at siste dato i periodelisten er før beregnDatoTil")
    fun testSisteDatoPeriodelisteErFørBeregnDatoTil() {
        val avvikListe =
            PeriodeUtil.validerInputDatoer(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
                dataElement = dataElement,
                periodeListe =
                    listOf(
                        Periode(datoFom = LocalDate.parse("2023-01-01"), datoTil = LocalDate.parse("2023-02-01")),
                        Periode(datoFom = LocalDate.parse("2023-02-01"), datoTil = LocalDate.parse("2023-03-01")),
                    ),
                sjekkOverlappendePerioder = false,
                sjekkOppholdMellomPerioder = false,
                sjekkDatoTilNull = false,
                sjekkDatoStartSluttAvPerioden = false,
                sjekkBeregnPeriode = true,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.PERIODE_MANGLER_DATA) },
            Executable {
                assertThat(
                    avvikListe[0].avvikTekst,
                ).isEqualTo("Siste dato i $dataElement (2023-03-01) er før beregnDatoTil (2023-04-01)")
            },
        )
    }

    @Test
    @DisplayName("Test av at beregnDatoFom er null")
    fun testBeregnDatoFomErNull() {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(
                beregnDatoFom = null,
                beregnDatoTil = LocalDate.parse("2023-04-01"),
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.NULL_VERDI_I_DATO) },
            Executable { assertThat(avvikListe[0].avvikTekst).isEqualTo("beregnDatoFom kan ikke være null") },
        )
    }

    @Test
    @DisplayName("Test av at beregnDatoTil er null")
    fun testBeregnDatoTilErNull() {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(
                beregnDatoFom = LocalDate.parse("2023-01-01"),
                beregnDatoTil = null,
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.NULL_VERDI_I_DATO) },
            Executable { assertThat(avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil kan ikke være null") },
        )
    }

    @Test
    @DisplayName("Test av at beregnDatoTil er før beregnDatoFom")
    fun testBeregnDatoTilErFørBeregnDatoFom() {
        val avvikListe =
            PeriodeUtil.validerBeregnPeriodeInput(
                beregnDatoFom = LocalDate.parse("2023-05-01"),
                beregnDatoTil = LocalDate.parse("2023-04-01"),
            )

        assertAll(
            Executable { assertThat(avvikListe).isNotEmpty() },
            Executable { assertThat(avvikListe.size).isEqualTo(1) },
            Executable { assertThat(avvikListe[0].avvikType).isEqualTo(Avvikstype.DATO_FOM_ETTER_DATO_TIL) },
            Executable { assertThat(avvikListe[0].avvikTekst).isEqualTo("beregnDatoTil må være etter beregnDatoFom") },
        )
    }
}
