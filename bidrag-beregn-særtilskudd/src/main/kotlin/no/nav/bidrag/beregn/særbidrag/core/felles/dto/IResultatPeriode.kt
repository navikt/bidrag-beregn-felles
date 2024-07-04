package no.nav.bidrag.beregn.særbidrag.core.felles.dto

import no.nav.bidrag.beregn.core.dto.PeriodeCore

interface IResultatPeriode {
    val periode: PeriodeCore
    val grunnlagsreferanseListe: List<String>
}
