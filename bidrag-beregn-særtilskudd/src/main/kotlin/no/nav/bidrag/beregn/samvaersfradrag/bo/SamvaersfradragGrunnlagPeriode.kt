package no.nav.bidrag.beregn.samvaersfradrag.bo

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.PeriodisertGrunnlag
import java.time.LocalDate

data class SamvaersfradragGrunnlagPeriode(
    val referanse: String,
    val samvaersfradragDatoFraTil: Periode,
    val barnPersonId: Int,
    val barnFodselsdato: LocalDate,
    val samvaersklasse: String,
) : PeriodisertGrunnlag {
    constructor(samvaersfradragGrunnlagPeriode: SamvaersfradragGrunnlagPeriode) :
        this(
            samvaersfradragGrunnlagPeriode.referanse,
            samvaersfradragGrunnlagPeriode.samvaersfradragDatoFraTil.justerDatoer(),
            samvaersfradragGrunnlagPeriode.barnPersonId,
            samvaersfradragGrunnlagPeriode.barnFodselsdato,
            samvaersfradragGrunnlagPeriode.samvaersklasse,
        )

    override fun getPeriode(): Periode {
        return samvaersfradragDatoFraTil
    }
}
