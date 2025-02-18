package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndringSjekkGrensePeriodeBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BeregnetBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodeBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndringSjekkGrensePeriodePeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.LøpendeBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndringSjekkGrensePeriodeMapper.mapEndringSjekkGrensePeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndringSjekkGrensePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object BeregnEndringSjekkGrensePeriodeService : BeregnService() {

    fun delberegningEndringSjekkGrensePeriode(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.endringSjekkGrense }

        // Mapper ut grunnlag som skal brukes i beregningen
        val periodeGrunnlag = mapEndringSjekkGrensePeriodeGrunnlag(mottattGrunnlag, sjablonGrunnlag)

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
        val resultatGrunnlagListe = mapResultatGrunnlag(
            beregningResultatListe = beregningResultatListe,
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
        løpendeBidragBeregningGrunnlag = periodeGrunnlag.beløpshistorikkBidragPeriodeGrunnlag?.run {
            beløpshistorikkPeriode.beløpshistorikk.firstOrNull { it.periode.inneholder(bruddPeriode) }?.let {
                LøpendeBidragBeregningGrunnlag(referanse = referanse, beløp = it.beløp)
            }
        },
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

    private fun mapResultatGrunnlag(
        beregningResultatListe: List<EndringSjekkGrensePeriodePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            beregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = mottattGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Matcher sjablongrunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = sjablonGrunnlag,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        return resultatGrunnlagListe
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) = grunnlagListe
        .filter { grunnlagReferanseListe.contains(it.referanse) }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = it.type,
                innhold = it.innhold,
                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                gjelderReferanse = it.gjelderReferanse,
                gjelderBarnReferanse = it.gjelderBarnReferanse,
            )
        }

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
                        faktiskEndringFaktor = it.resultat.faktiskEndringFaktor,
                        endringErOverGrense = it.resultat.endringErOverGrense,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }
}
