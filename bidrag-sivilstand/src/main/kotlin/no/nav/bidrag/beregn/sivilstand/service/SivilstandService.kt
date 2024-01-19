package no.nav.bidrag.beregn.sivilstand.service

import no.nav.bidrag.beregn.sivilstand.SivilstandBeregnet

private val logger = KotlinLogging.logger {}

internal class SivilstandService() {
    fun beregn(sivilstandGrunnlagDto: SivilstandGrunnlagDto): SivilstandBeregnet {


        return SivilstandBeregnet()
    }

}
