package no.nav.bidrag.beregn.core.felles.dto

import no.nav.bidrag.beregn.core.dto.PeriodeCore

interface IResultatPeriode {
    val periode: PeriodeCore
    val grunnlagReferanseListe: List<String>
}
