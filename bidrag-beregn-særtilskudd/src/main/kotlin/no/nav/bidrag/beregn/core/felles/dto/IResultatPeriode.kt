package no.nav.bidrag.beregn.core.felles.dto

interface IResultatPeriode {
    val periode: PeriodeCore
    val grunnlagReferanseListe: List<String>
}
