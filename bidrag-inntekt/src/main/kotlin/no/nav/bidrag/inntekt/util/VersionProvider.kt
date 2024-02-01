package no.nav.bidrag.inntekt.util

class VersionProvider {
    companion object {
        val APP_VERSJON get() = hentFil("/versjon.txt").readText().trim()
    }
}
fun hentFil(filsti: String) = VersionProvider::class.java.getResource(
    filsti,
) ?: throw RuntimeException("Fant ingen fil på sti $filsti")
