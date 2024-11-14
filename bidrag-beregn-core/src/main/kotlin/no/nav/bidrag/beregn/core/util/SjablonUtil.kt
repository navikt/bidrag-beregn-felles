package no.nav.bidrag.beregn.core.util

import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonSingelNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonSingelNøkkelSingelInnhold
import no.nav.bidrag.beregn.core.bo.TrinnvisSkattesats
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Comparator.comparing
import java.util.stream.Stream

object SjablonUtil {
    // Henter verdier fra sjablonene Barnetilsyn (N:1, eksakt match) og Bidragsevne (1:N, eksakt match)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
        sjablonNøkkelListe: List<SjablonNøkkel>,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ): BigDecimal {
        val filtrertSjablonListe =
            filtrerSjablonNøkkelListePåSjablonNøkkel(
                sjablonListe = filtrerPåSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn),
                sjablonNøkkelListe = sjablonNøkkelListe,
            )
        val sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe)

        return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe = sjablonInnholdListe, sjablonInnholdNavn = sjablonInnholdNavn)
    }

    // Henter verdier fra sjablonene Forbruksutgifter, MaksFradrag og MaksTilsyn (1:1, intervall)
    @JvmStatic
    fun hentSjablonverdi(sjablonListe: List<Sjablon>, sjablonNavn: SjablonNavn, sjablonNøkkelVerdi: Int): BigDecimal {
        val filtrertSjablonListe = filtrerPåSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn)
        val sortertSjablonSingelNøkkelSingelInnholdListe = mapTilSingelListeNøkkelInnholdSortert(filtrertSjablonListe)

        return hentSjablonInnholdVerdiIntervall(
            sortertSjablonSingelNøkkelSingelInnholdListe = sortertSjablonSingelNøkkelSingelInnholdListe,
            sjablonNøkkelVerdi = sjablonNøkkelVerdi,
        )
    }

    // Henter verdier fra sjablon Samværsfradrag (N:N, eksakt match + intervall)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
        sjablonNøkkelListe: List<SjablonNøkkel>,
        sjablonNøkkelNavn: SjablonNøkkelNavn,
        sjablonNøkkelVerdi: Int,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ): BigDecimal {
        val filtrertSjablonListe =
            filtrerSjablonNøkkelListePåSjablonNøkkel(
                sjablonListe = filtrerPåSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn),
                sjablonNøkkelListe = sjablonNøkkelListe,
            )
        val sortertSjablonSingelNøkkelListe =
            mapTilSingelListeNøkkelSortert(
                filtrertSjablonListe = filtrertSjablonListe,
                sjablonNøkkelNavn = sjablonNøkkelNavn,
            )
        val sjablonInnholdListe =
            finnSjablonInnholdVerdiListeIntervall(
                sortertSjablonSingelNøkkelListe = sortertSjablonSingelNøkkelListe,
                sjablonNøkkelVerdi = sjablonNøkkelVerdi,
            )

        return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe = sjablonInnholdListe, sjablonInnholdNavn = sjablonInnholdNavn)
    }

    // Henter verdier fra sjablon Sjablontall (1:1, eksakt match)
    @JvmStatic
    fun hentSjablonverdi(sjablonListe: List<Sjablon>, sjablonTallNavn: SjablonTallNavn): BigDecimal {
        val filtrertSjablonListe = filtrerPåSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonTallNavn.navn)
        val sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe)
        return hentSjablonInnholdVerdiEksakt(
            sjablonInnholdListe = sjablonInnholdListe,
            sjablonInnholdNavn = SjablonInnholdNavn.SJABLON_VERDI,
        )
    }

    // Henter liste med verdier fra sjablon TrinnvisSkattesats (0:N, hent alle)
    @JvmStatic
    fun hentTrinnvisSkattesats(sjablonListe: List<Sjablon>, sjablonNavn: SjablonNavn): List<TrinnvisSkattesats> {
        val filtrertSjablonListe = filtrerPåSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn)
        val sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe)
        val inntektGrenseListe =
            finnSjablonInnholdVerdiListe(
                sjablonInnholdListe = sjablonInnholdListe,
                sjablonInnholdNavn = SjablonInnholdNavn.INNTEKTSGRENSE_BELØP,
            )
        val satsListe =
            finnSjablonInnholdVerdiListe(
                sjablonInnholdListe = sjablonInnholdListe,
                sjablonInnholdNavn = SjablonInnholdNavn.SKATTESATS_PROSENT,
            )
        var indeks = 0
        val trinnvisSkattesatsListe = ArrayList<TrinnvisSkattesats>()
        while (indeks < inntektGrenseListe.size) {
            trinnvisSkattesatsListe.add(TrinnvisSkattesats(inntektGrense = inntektGrenseListe[indeks], sats = satsListe[indeks]))
            indeks += 1
        }

        return trinnvisSkattesatsListe.sortedWith(comparing(TrinnvisSkattesats::inntektGrense)).toList()
    }

    // Filtrerer sjablonListe på sjablonNavn og returnerer ny liste.
    // Brukes av alle typer sjabloner.
    private fun filtrerPåSjablonNavn(sjablonListe: List<Sjablon>, sjablonNavn: String) = sjablonListe.filter { it.navn == sjablonNavn }.toList()

    // Filtrerer sjablonListe på sjablonNøkkelListe og returnerer en ny liste.
    // Brukes av sjabloner som har eksakt match på nøkkel (Barnetilsyn, Bidragsevne, Samværsfradrag).
    private fun filtrerSjablonNøkkelListePåSjablonNøkkel(sjablonListe: List<Sjablon>, sjablonNøkkelListe: List<SjablonNøkkel>): List<Sjablon> {
        var sjablonStream = sjablonListe.stream()
        sjablonNøkkelListe.forEach {
            sjablonStream = filtrerPåSjablonNøkkel(sjablonStream, it)
        }
        return sjablonStream.toList()
    }

    // Filtrerer sjablonStream på sjablonNøkkelInput og returnerer en ny stream.
    // Intern bruk.
    private fun filtrerPåSjablonNøkkel(sjablonStream: Stream<Sjablon>, sjablonNøkkelInput: SjablonNøkkel) = sjablonStream.filter {
        it.nøkkelListe!!
            .any { sjablonNøkkel ->
                sjablonNøkkel.navn == sjablonNøkkelInput.navn &&
                    sjablonNøkkel.verdi == sjablonNøkkelInput.verdi
            }
    }

    // Tar inn en sjablonListe og returnerer en sjablonInnholdListe.
    // Brukes av Bidragsevne, Sjablontall, TrinnvisSkattesats.
    private fun mapSjablonListeTilSjablonInnholdListe(sjablonListe: List<Sjablon>) = sjablonListe.flatMap { it.innholdListe }

    // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og singel innholdverdi (1:1). Returnerer en ny liste sortert
    // på nøkkelverdi.
    // Brukes av sjabloner som har ett nøkkelobjekt og ett innholdobjekt (Forbruksutgifter, MaxFradrag, MaxTilsyn).
    private fun mapTilSingelListeNøkkelInnholdSortert(filtrertSjablonListe: List<Sjablon>) = filtrertSjablonListe
        .map {
            SjablonSingelNøkkelSingelInnhold(
                navn = it.navn,
                nøkkelVerdi = it.nøkkelListe!!.firstOrNull()?.verdi ?: " ",
                innholdVerdi = it.innholdListe.firstOrNull()?.verdi ?: BigDecimal.ZERO,
            )
        }
        .sortedBy { Integer.valueOf(it.nøkkelVerdi) }

    // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og liste med innholdverdier (1:N). Returnerer en ny liste
    // sortert på nøkkelverdi.
    // Brukes av sjabloner som har ett nøkkelobjekt med eksakt match og flere innholdobjekter (Samværsfradrag).
    private fun mapTilSingelListeNøkkelSortert(
        filtrertSjablonListe: List<Sjablon>,
        sjablonNøkkelNavn: SjablonNøkkelNavn,
    ): List<SjablonSingelNøkkel> = filtrertSjablonListe
        .map {
            SjablonSingelNøkkel(
                navn = it.navn,
                verdi =
                it.nøkkelListe!!
                    .filter { sjablonNøkkel ->
                        sjablonNøkkel.navn == sjablonNøkkelNavn.navn
                    }
                    .map(SjablonNøkkel::verdi)
                    .firstOrNull() ?: " ",
                innholdListe = it.innholdListe,
            )
        }
        .sortedBy { Integer.valueOf(it.verdi) }

    // Filtrerer sjablonInnholdListe på sjablonInnholdNavn (eksakt match) og returnerer matchende verdi (0d hvis sjablonInnholdNavn mot formodning ikke
    // finnes).
    // Brukes av sjabloner som skal hente eksakt verdi (Barnetilsyn, Bidragsevne, Sjablontall, Samværsfradrag).
    private fun hentSjablonInnholdVerdiEksakt(sjablonInnholdListe: List<SjablonInnhold>, sjablonInnholdNavn: SjablonInnholdNavn) = sjablonInnholdListe
        .filter { it.navn == sjablonInnholdNavn.navn }
        .map { it.verdi }
        .firstOrNull() ?: BigDecimal.ZERO

    // Filtrerer sortertSjablonSingelNøkkelSingelInnholdListe på nøkkel-verdi >= sjablonNøkkel og returnerer en singel verdi (0d hvis det mot formodning
    // ikke finnes noen verdi).
    // Brukes av 1:1 sjabloner som henter verdi basert på intervall (Forbruksutgifter, MaxFradrag, MaxTilsyn).

    private fun hentSjablonInnholdVerdiIntervall(
        sortertSjablonSingelNøkkelSingelInnholdListe: List<SjablonSingelNøkkelSingelInnhold>,
        sjablonNøkkelVerdi: Int,
    ) = sortertSjablonSingelNøkkelSingelInnholdListe
        .filter { it.nøkkelVerdi.toInt() >= sjablonNøkkelVerdi }
        .map { it.innholdVerdi }
        .firstOrNull() ?: BigDecimal.ZERO

    // Filtrerer sortertSjablonSingelNøkkelListe på nøkkel-verdi >= sjablonNøkkel og returnerer en liste av typen SjablonInnholdNy (tom liste hvis det
    // mot formodning ikke finnes noen forekomster).
    // Brukes av sjabloner som har flere innholdobjekter og som henter verdi(er) basert på intervall (Samværsfradrag).
    private fun finnSjablonInnholdVerdiListeIntervall(sortertSjablonSingelNøkkelListe: List<SjablonSingelNøkkel>, sjablonNøkkelVerdi: Int) =
        sortertSjablonSingelNøkkelListe
            .filter { it.verdi.toInt() >= sjablonNøkkelVerdi }
            .map { it.innholdListe }
            .firstOrNull() ?: emptyList()

    // Filtrerer sjablonInnholdListe på sjablonInnholdNavn og returnerer en liste over alle matchende verdier.
    // Brukes av sjabloner som skal returnere en liste med innholdverdier (TrinnvisSkattesats).
    private fun finnSjablonInnholdVerdiListe(sjablonInnholdListe: List<SjablonInnhold>, sjablonInnholdNavn: SjablonInnholdNavn) = sjablonInnholdListe
        .filter { it.navn == sjablonInnholdNavn.navn }
        .map { it.verdi }
        .toList()

    fun justerSjablonTomDato(datoTom: LocalDate?): LocalDate? =
        if (datoTom == LocalDate.parse("9999-12-31") || datoTom == null) null else datoTom.plusMonths(1)

    fun lagSjablonReferanse(sjablonNavn: String, fomDato: LocalDate, postfix: String = ""): String =
        "sjablon_${sjablonNavn}_${fomDato.format(DateTimeFormatter.ofPattern("yyyyMM"))}$postfix"
}
