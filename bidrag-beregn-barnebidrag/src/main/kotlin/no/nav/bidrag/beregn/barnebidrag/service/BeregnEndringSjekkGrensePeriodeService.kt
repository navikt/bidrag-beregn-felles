package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndringSjekkGrensePeriodeBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BeregnetBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.LøpendeBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.PrivatAvtaleBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndringSjekkGrensePeriodeMapper.mapEndringSjekkGrensePeriodeGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnEndringSjekkGrensePeriodeService : BeregnService() {

    fun delberegningEndringSjekkGrensePeriode(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.endringSjekkGrense }

        // Hvis det er 18-års-bidrag skal BELØPSHISTORIKK_BIDRAG_18_ÅR benyttes
        val grunnlagstype =
            if (mottattGrunnlag.stønadstype ==
                Stønadstype.BIDRAG18AAR
            ) {
                Grunnlagstype.BELØPSHISTORIKK_BIDRAG_18_ÅR
            } else {
                Grunnlagstype.BELØPSHISTORIKK_BIDRAG
            }

        // Mapper ut grunnlag som skal brukes i beregningen
        val periodeGrunnlag = mapEndringSjekkGrensePeriodeGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
            grunnlagstype = grunnlagstype,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeEndringSjekkGrensePeriode(
            grunnlagListe = periodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val beregningResultatListe = mutableListOf<EndringSjekkGrensePeriodePeriodeResultat>()

        // Løper gjennom hver bruddperiode og sjekker om endring er under/over grense
        bruddPeriodeListe.forEach { bruddPeriode ->
            val beregningGrunnlag = lagBeregningGrunnlag(
                periodeGrunnlag = periodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )
            beregningResultatListe.add(
                EndringSjekkGrensePeriodePeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndringSjekkGrensePeriodeBeregning.beregn(beregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (beregningResultatListe.isNotEmpty()) {
            val sisteElement = beregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                beregningResultatListe[beregningResultatListe.size - 1] = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = beregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning endring sjekk grense periode
        resultatGrunnlagListe.addAll(
            mapDelberegningEndringSjekkGrensePeriode(
                periodeResultatListe = beregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning)

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeEndringSjekkGrensePeriode(
        grunnlagListe: EndringSjekkGrensePeriodePeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.sluttberegningPeriodeGrunnlagListe.asSequence().map { it.sluttberegningPeriode.periode })
            .plus(
                grunnlagListe.beløpshistorikkBidragPeriodeGrunnlag?.beløpshistorikkPeriode?.beløpshistorikk
                    ?.asSequence()?.map { it.periode } ?: emptySequence(),
            )
            .plus(grunnlagListe.privatAvtaleIndeksregulertPeriodeGrunnlagListe.asSequence().map { it.privatAvtaleIndeksregulertPeriode.periode })
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for beregning som ligger innenfor bruddPeriode
    private fun lagBeregningGrunnlag(
        periodeGrunnlag: EndringSjekkGrensePeriodePeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): EndringSjekkGrensePeriodeBeregningGrunnlag = EndringSjekkGrensePeriodeBeregningGrunnlag(
        beregnetBidragBeregningGrunnlag = periodeGrunnlag.sluttberegningPeriodeGrunnlagListe
            .firstOrNull { it.sluttberegningPeriode.periode.inneholder(bruddPeriode) }
            ?.let { BeregnetBidragBeregningGrunnlag(referanse = it.referanse, beløp = it.sluttberegningPeriode.beregnetBeløp) }
            ?: throw IllegalArgumentException("Sluttberegning grunnlag mangler for periode $bruddPeriode"),
        // Hvis perioden mangler, bruk null i beløp, slik at beløpshistorikk-objektet blir referert av delberegningen
        løpendeBidragBeregningGrunnlag = periodeGrunnlag.beløpshistorikkBidragPeriodeGrunnlag?.run {
            val periode = beløpshistorikkPeriode.beløpshistorikk.firstOrNull { it.periode.inneholder(bruddPeriode) }
            LøpendeBidragBeregningGrunnlag(referanse = referanse, beløp = periode?.beløp)
        },
        privatAvtaleBeregningGrunnlag = periodeGrunnlag.privatAvtaleIndeksregulertPeriodeGrunnlagListe
            .firstOrNull { it.privatAvtaleIndeksregulertPeriode.periode.inneholder(bruddPeriode) }
            ?.let { PrivatAvtaleBeregningGrunnlag(referanse = it.referanse, beløp = it.privatAvtaleIndeksregulertPeriode.beløp) },
        sjablonSjablontallBeregningGrunnlagListe = periodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
            .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonSjablontallBeregningGrunnlag(
                    referanse = it.referanse,
                    type = it.sjablonSjablontallPeriode.sjablon.navn,
                    verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                )
            },
    )

    // Mapper ut DelberegningEndringSjekkGrensePeriode
    private fun mapDelberegningEndringSjekkGrensePeriode(
        periodeResultatListe: List<EndringSjekkGrensePeriodePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = periodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_ENDRING_SJEKK_GRENSE_PERIODE,
                innhold = POJONode(
                    DelberegningEndringSjekkGrensePeriode(
                        periode = it.periode,
                        løpendeBidragBeløp = it.resultat.løpendeBidragBeløp,
                        løpendeBidragFraPrivatAvtale = it.resultat.løpendeBidragFraPrivatAvtale,
                        beregnetBidragBeløp = it.resultat.beregnetBidragBeløp,
                        faktiskEndringFaktor = it.resultat.faktiskEndringFaktor,
                        endringErOverGrense = it.resultat.endringErOverGrense,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }
}
