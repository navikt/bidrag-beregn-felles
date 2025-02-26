package no.nav.bidrag.indeksregulering.dto

import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Delberegning
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagInnhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagPeriodeInnhold
import java.math.BigDecimal
import java.time.YearMonth


data class IndeksreguleringGrunnlag(
    val indeksregulerFra: YearMonth,
    val indeksregulerTil: YearMonth?,
    val sistePeriodeGrunnlag: SistePeriodeGrunnlag
) :
    GrunnlagInnhold

data class SistePeriodeGrunnlag(
    override val periode: ÅrMånedsperiode,
    val beløp: BigDecimal,
    val nesteIndeksreguleringsår: Int,
    val valutakode: String,
    override val manueltRegistrert: Boolean = false,
) : GrunnlagPeriodeInnhold

data class DelberegningIndeksreguleringPeriode(
    override val periode: ÅrMånedsperiode,
    val indeksreguleringFaktor: BigDecimal? = null,
    val beløp: BigDecimal,
    val valutakode: String,
) : Delberegning

