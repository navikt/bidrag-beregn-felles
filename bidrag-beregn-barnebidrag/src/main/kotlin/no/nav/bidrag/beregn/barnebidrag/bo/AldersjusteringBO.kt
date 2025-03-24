package no.nav.bidrag.beregn.barnebidrag.bo

import no.nav.bidrag.beregn.core.bo.SjablonBarnetilsynPeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonForbruksutgifterPeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSamværsfradragPeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.domene.enums.barnetilsyn.Skolealder
import no.nav.bidrag.domene.enums.barnetilsyn.Tilsynstype
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import java.math.BigDecimal

data class AldersjusteringBeregningGrunnlag(
    val beregningsperiode: ÅrMånedsperiode,
    val søknadsbarnReferanse: String,
    val bidragsmottakerReferanse: String,
    val bidragspliktigReferanse: String,
    val søknadsbarnPeriodeGrunnlag: SøknadsbarnPeriodeGrunnlag,
    val vedtakId: Long,
    val nettoTilsynsutgift: BigDecimal?,
    val tilsynstype: Tilsynstype?,
    val skolealder: Skolealder?,
    val bpAndelFaktor: BigDecimal,
    val samværsklasse: Samværsklasse,
    val søknadsbarnAlder: Int,
    val beløpshistorikk: BeløpshistorikkPeriodeGrunnlag?,
    var sjablonSjablontallPeriodeGrunnlagListe: List<SjablonSjablontallPeriodeGrunnlag>,
    var sjablonBarnetilsynPeriodeGrunnlagListe: List<SjablonBarnetilsynPeriodeGrunnlag>,
    var sjablonForbruksutgifterPeriodeGrunnlagListe: List<SjablonForbruksutgifterPeriodeGrunnlag>,
    var sjablonSamværsfradragPeriodeGrunnlagListe: List<SjablonSamværsfradragPeriodeGrunnlag>,
)
