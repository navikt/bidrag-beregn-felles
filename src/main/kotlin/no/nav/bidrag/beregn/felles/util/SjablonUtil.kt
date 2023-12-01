package no.nav.bidrag.beregn.felles.util

import no.nav.bidrag.beregn.felles.bo.Sjablon
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkel
import no.nav.bidrag.beregn.felles.bo.SjablonSingelNokkelSingelInnhold
import no.nav.bidrag.beregn.felles.bo.TrinnvisSkattesats
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.util.Comparator.comparing
import java.util.stream.Stream

object SjablonUtil {
    // Henter verdier fra sjablonene Barnetilsyn (N:1, eksakt match) og Bidragsevne (1:N, eksakt match)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
        sjablonNokkelListe: List<SjablonNokkel>,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ): BigDecimal {
        val filtrertSjablonListe =
            filtrerSjablonNokkelListePaaSjablonNokkel(
                sjablonListe = filtrerPaaSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn),
                sjablonNokkelListe = sjablonNokkelListe,
            )
        val sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe)

        return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe = sjablonInnholdListe, sjablonInnholdNavn = sjablonInnholdNavn)
    }

    // Henter verdier fra sjablonene Forbruksutgifter, MaksFradrag og MaksTilsyn (1:1, intervall)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
        sjablonNokkelVerdi: Int,
    ): BigDecimal {
        val filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn)
        val sortertSjablonSingelNokkelSingelInnholdListe = mapTilSingelListeNokkelInnholdSortert(filtrertSjablonListe)

        return hentSjablonInnholdVerdiIntervall(
            sortertSjablonSingelNokkelSingelInnholdListe = sortertSjablonSingelNokkelSingelInnholdListe,
            sjablonNokkelVerdi = sjablonNokkelVerdi,
        )
    }

    // Henter verdier fra sjablon Samværsfradrag (N:N, eksakt match + intervall)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
        sjablonNokkelListe: List<SjablonNokkel>,
        sjablonNokkelNavn: SjablonNøkkelNavn,
        sjablonNokkelVerdi: Int,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ): BigDecimal {
        val filtrertSjablonListe =
            filtrerSjablonNokkelListePaaSjablonNokkel(
                sjablonListe = filtrerPaaSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn),
                sjablonNokkelListe = sjablonNokkelListe,
            )
        val sortertSjablonSingelNokkelListe =
            mapTilSingelListeNokkelSortert(
                filtrertSjablonListe = filtrertSjablonListe,
                sjablonNokkelNavn = sjablonNokkelNavn,
            )
        val sjablonInnholdListe =
            finnSjablonInnholdVerdiListeIntervall(
                sortertSjablonSingelNokkelListe = sortertSjablonSingelNokkelListe,
                sjablonNokkelVerdi = sjablonNokkelVerdi,
            )

        return hentSjablonInnholdVerdiEksakt(sjablonInnholdListe = sjablonInnholdListe, sjablonInnholdNavn = sjablonInnholdNavn)
    }

    // Henter verdier fra sjablon Sjablontall (1:1, eksakt match)
    @JvmStatic
    fun hentSjablonverdi(
        sjablonListe: List<Sjablon>,
        sjablonTallNavn: SjablonTallNavn,
    ): BigDecimal {
        val filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonTallNavn.navn)
        val sjablonInnholdListe = mapSjablonListeTilSjablonInnholdListe(filtrertSjablonListe)
        return hentSjablonInnholdVerdiEksakt(
            sjablonInnholdListe = sjablonInnholdListe,
            sjablonInnholdNavn = SjablonInnholdNavn.SJABLON_VERDI,
        )
    }

    // Henter liste med verdier fra sjablon TrinnvisSkattesats (0:N, hent alle)
    @JvmStatic
    fun hentTrinnvisSkattesats(
        sjablonListe: List<Sjablon>,
        sjablonNavn: SjablonNavn,
    ): List<TrinnvisSkattesats> {
        val filtrertSjablonListe = filtrerPaaSjablonNavn(sjablonListe = sjablonListe, sjablonNavn = sjablonNavn.navn)
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
    private fun filtrerPaaSjablonNavn(
        sjablonListe: List<Sjablon>,
        sjablonNavn: String,
    ) = sjablonListe.filter { it.navn == sjablonNavn }.toList()

    // Filtrerer sjablonListe på sjablonNokkelListe og returnerer en ny liste.
    // Brukes av sjabloner som har eksakt match på nøkkel (Barnetilsyn, Bidragsevne, Samværsfradrag).
    private fun filtrerSjablonNokkelListePaaSjablonNokkel(
        sjablonListe: List<Sjablon>,
        sjablonNokkelListe: List<SjablonNokkel>,
    ): List<Sjablon> {
        var sjablonStream = sjablonListe.stream()
        sjablonNokkelListe.forEach {
            sjablonStream = filtrerPaaSjablonNokkel(sjablonStream, it)
        }
        return sjablonStream.toList()
    }

    // Filtrerer sjablonStream på sjablonNokkelInput og returnerer en ny stream.
    // Intern bruk.
    private fun filtrerPaaSjablonNokkel(
        sjablonStream: Stream<Sjablon>,
        sjablonNokkelInput: SjablonNokkel,
    ) = sjablonStream.filter {
        it.nokkelListe!!
            .any { sjablonNokkel ->
                sjablonNokkel.navn == sjablonNokkelInput.navn &&
                    sjablonNokkel.verdi == sjablonNokkelInput.verdi
            }
    }

    // Tar inn en sjablonListe og returnerer en sjablonInnholdListe.
    // Brukes av Bidragsevne, Sjablontall, TrinnvisSkattesats.
    private fun mapSjablonListeTilSjablonInnholdListe(sjablonListe: List<Sjablon>) = sjablonListe.flatMap { it.innholdListe }

    // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og singel innholdverdi (1:1). Returnerer en ny liste sortert
    // på nøkkelverdi.
    // Brukes av sjabloner som har ett nøkkelobjekt og ett innholdobjekt (Forbruksutgifter, MaxFradrag, MaxTilsyn).
    private fun mapTilSingelListeNokkelInnholdSortert(filtrertSjablonListe: List<Sjablon>) =
        filtrertSjablonListe
            .map {
                SjablonSingelNokkelSingelInnhold(
                    navn = it.navn,
                    nokkelVerdi = it.nokkelListe!!.firstOrNull()?.verdi ?: " ",
                    innholdVerdi = it.innholdListe.firstOrNull()?.verdi ?: BigDecimal.ZERO,
                )
            }
            .sortedBy { Integer.valueOf(it.nokkelVerdi) }

    // Tar inn filtrertSjablonListe og mapper denne om til en liste med singel nøkkelverdi og liste med innholdverdier (1:N). Returnerer en ny liste
    // sortert på nøkkelverdi.
    // Brukes av sjabloner som har ett nøkkelobjekt med eksakt match og flere innholdobjekter (Samværsfradrag).
    private fun mapTilSingelListeNokkelSortert(
        filtrertSjablonListe: List<Sjablon>,
        sjablonNokkelNavn: SjablonNøkkelNavn,
    ): List<SjablonSingelNokkel> =
        filtrertSjablonListe
            .map {
                SjablonSingelNokkel(
                    navn = it.navn,
                    verdi =
                        it.nokkelListe!!
                            .filter { sjablonNokkel ->
                                sjablonNokkel.navn == sjablonNokkelNavn.navn
                            }
                            .map(SjablonNokkel::verdi)
                            .firstOrNull() ?: " ",
                    innholdListe = it.innholdListe,
                )
            }
            .sortedBy { Integer.valueOf(it.verdi) }

    // Filtrerer sjablonInnholdListe på sjablonInnholdNavn (eksakt match) og returnerer matchende verdi (0d hvis sjablonInnholdNavn mot formodning ikke
    // finnes).
    // Brukes av sjabloner som skal hente eksakt verdi (Barnetilsyn, Bidragsevne, Sjablontall, Samværsfradrag).
    private fun hentSjablonInnholdVerdiEksakt(
        sjablonInnholdListe: List<SjablonInnhold>,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ) = sjablonInnholdListe
        .filter { it.navn == sjablonInnholdNavn.navn }
        .map { it.verdi }
        .firstOrNull() ?: BigDecimal.ZERO

    // Filtrerer sortertSjablonSingelNokkelSingelInnholdListe på nøkkel-verdi >= sjablonNokkel og returnerer en singel verdi (0d hvis det mot formodning
    // ikke finnes noen verdi).
    // Brukes av 1:1 sjabloner som henter verdi basert på intervall (Forbruksutgifter, MaxFradrag, MaxTilsyn).

    private fun hentSjablonInnholdVerdiIntervall(
        sortertSjablonSingelNokkelSingelInnholdListe: List<SjablonSingelNokkelSingelInnhold>,
        sjablonNokkelVerdi: Int,
    ) = sortertSjablonSingelNokkelSingelInnholdListe
        .filter { it.nokkelVerdi.toInt() >= sjablonNokkelVerdi }
        .map { it.innholdVerdi }
        .firstOrNull() ?: BigDecimal.ZERO

    // Filtrerer sortertSjablonSingelNokkelListe på nøkkel-verdi >= sjablonNokkel og returnerer en liste av typen SjablonInnholdNy (tom liste hvis det
    // mot formodning ikke finnes noen forekomster).
    // Brukes av sjabloner som har flere innholdobjekter og som henter verdi(er) basert på intervall (Samværsfradrag).
    private fun finnSjablonInnholdVerdiListeIntervall(
        sortertSjablonSingelNokkelListe: List<SjablonSingelNokkel>,
        sjablonNokkelVerdi: Int,
    ) = sortertSjablonSingelNokkelListe
        .filter { it.verdi.toInt() >= sjablonNokkelVerdi }
        .map { it.innholdListe }
        .firstOrNull() ?: emptyList()

    // Filtrerer sjablonInnholdListe på sjablonInnholdNavn og returnerer en liste over alle matchende verdier.
    // Brukes av sjabloner som skal returnere en liste med innholdverdier (TrinnvisSkattesats).
    private fun finnSjablonInnholdVerdiListe(
        sjablonInnholdListe: List<SjablonInnhold>,
        sjablonInnholdNavn: SjablonInnholdNavn,
    ) = sjablonInnholdListe
        .filter { it.navn == sjablonInnholdNavn.navn }
        .map { it.verdi }
        .toList()
}
