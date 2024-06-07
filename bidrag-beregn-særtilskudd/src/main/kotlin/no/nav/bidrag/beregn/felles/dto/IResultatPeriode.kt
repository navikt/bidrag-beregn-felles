package no.nav.bidrag.beregn.felles.dto

interface IResultatPeriode {
    val periode: PeriodeCore
    val grunnlagReferanseListe: List<String>
}
