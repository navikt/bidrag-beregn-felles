package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.bo.SjablonSamværsfradragPeriodeGrunnlag
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsklassePeriode
import java.math.BigDecimal

data class SamværsfradragPeriodeGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val søknadsbarnPeriodeGrunnlag: SøknadsbarnPeriodeGrunnlag,
    val samværsklassePeriodeGrunnlagListe: List<SamværsklassePeriodeGrunnlag>,
    var sjablonSamværsfradragPeriodeGrunnlagListe: List<SjablonSamværsfradragPeriodeGrunnlag>,
)

data class SamværsklassePeriodeGrunnlag(val referanse: String, val samværsklassePeriode: SamværsklassePeriode)

data class SamværsfradragPeriodeResultat(val periode: ÅrMånedsperiode, val resultat: SamværsfradragBeregningResultat)

data class SamværsfradragBeregningGrunnlag(
    val søknadsbarn: SøknadsbarnBeregningGrunnlag,
    val samværsklasseBeregningGrunnlag: SamværsklasseBeregningGrunnlag,
    val sjablonSamværsfradragBeregningGrunnlagListe: List<SjablonSamværsfradragBeregningGrunnlag>,
)

data class SøknadsbarnBeregningGrunnlag(val referanse: String, val alder: Int)

data class SamværsklasseBeregningGrunnlag(val referanse: String, val samværsklasse: Samværsklasse)

data class SjablonSamværsfradragBeregningGrunnlag(
    val referanse: String,
    val samværsklasse: Samværsklasse,
    val alderTom: Int,
    val beløpFradrag: BigDecimal,
)

data class SamværsfradragBeregningResultat(val beløpFradrag: BigDecimal, val grunnlagsreferanseListe: List<String>)
