package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.SamværsfradragBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklasseBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSamværsfradragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.SamværsfradragMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.SamværsfradragMapper.mapSamværsfradragGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.Period

internal object BeregnSamværsfradragService : BeregnService() {

    fun delberegningSamværsfradrag(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): List<GrunnlagDto> {
        val samværsfradragPeriodeGrunnlag = mapSamværsfradragGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        val bruddPeriodeListe = lagBruddPeriodeListeSamværsfradrag(samværsfradragPeriodeGrunnlag, mottattGrunnlag.periode)
        val samværsfradragBeregningResultatListe = mutableListOf<SamværsfradragPeriodeResultat>()
        bruddPeriodeListe.forEach { bruddPeriode ->
            val samværsfradragBeregningGrunnlag = lagSamværsfradragBeregningGrunnlag(samværsfradragPeriodeGrunnlag, bruddPeriode)
            samværsfradragBeregningResultatListe.add(
                SamværsfradragPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = SamværsfradragBeregning.beregn(samværsfradragBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapSamværsfradragResultatGrunnlag(
            samværsfradragBeregningResultatListe = samværsfradragBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning samværsfradrag
        resultatGrunnlagListe.addAll(
            mapDelberegningSamværsfradrag(
                samværsfradragPeriodeResultatListe = samværsfradragBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeSamværsfradrag(
        grunnlagListe: SamværsfradragPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.samværsklassePeriodeListe.asSequence().map { it.samværsklassePeriode.periode })
            .plus(grunnlagListe.sjablonSamværsfradragPeriodeListe.asSequence().map { it.sjablonSamværsfradragPeriode.periode })
            .plus(
                lagAlderBruddPerioder(
                    sjablonSamværsfradragPerioder = grunnlagListe.sjablonSamværsfradragPeriodeListe,
                    søknadsbarnPeriodeGrunnlag = grunnlagListe.søknadsbarnPeriodeGrunnlag,
                ).asSequence(),
            )

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager bruddperioder for alder basert på verdier i sjablon SAMVÆRSFRADRAG
    private fun lagAlderBruddPerioder(
        sjablonSamværsfradragPerioder: List<SjablonSamværsfradragPeriodeGrunnlag>,
        søknadsbarnPeriodeGrunnlag: SøknadsbarnPeriodeGrunnlag,
    ): List<ÅrMånedsperiode> = hentAlderTomListe(sjablonSamværsfradragPerioder)
        .map {
            val alderBruddDato = søknadsbarnPeriodeGrunnlag.fødselsdato.plusYears(it.toLong())
            ÅrMånedsperiode(alderBruddDato, alderBruddDato)
        }

    // Lager grunnlag for samværsfradragberegning som ligger innenfor bruddPeriode
    private fun lagSamværsfradragBeregningGrunnlag(
        samværsfradragPeriodeGrunnlag: SamværsfradragPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): SamværsfradragBeregningGrunnlag {
        // Lager liste over gyldige alderTom-verdier
        val alderTomListe = hentAlderTomListe(samværsfradragPeriodeGrunnlag.sjablonSamværsfradragPeriodeListe)

        // Finner barnets faktiske alder
        val faktiskAlder = Period.between(
            samværsfradragPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato,
            bruddPeriode.fom.atDay(1),
        ).years

        // Finner den nærmeste alderTom som er større enn eller lik faktisk alder (til bruk for å hente ut sjablonverdi)
        val alderTom = alderTomListe.firstOrNull { faktiskAlder <= it } ?: alderTomListe.last()

        return SamværsfradragBeregningGrunnlag(
            søknadsbarn = SøknadsbarnBeregningGrunnlag(
                referanse = samværsfradragPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                alder = alderTom,
            ),
            samværsklasseBeregningGrunnlag = samværsfradragPeriodeGrunnlag.samværsklassePeriodeListe
                .firstOrNull { it.samværsklassePeriode.periode.inneholder(bruddPeriode) }
                ?.let { SamværsklasseBeregningGrunnlag(referanse = it.referanse, samværsklasse = it.samværsklassePeriode.samværsklasse) }
                ?: throw IllegalArgumentException("Ingen samværsklasse funnet for periode $bruddPeriode"),
            sjablonSamværsfradragBeregningGrunnlagListe = samværsfradragPeriodeGrunnlag.sjablonSamværsfradragPeriodeListe
                .filter { it.sjablonSamværsfradragPeriode.periode.inneholder(bruddPeriode) }
                .map {
                    SjablonSamværsfradragBeregningGrunnlag(
                        referanse = it.referanse,
                        samværsklasse = Samværsklasse.fromBisysKode(it.sjablonSamværsfradragPeriode.samværsklasse)
                            ?: throw IllegalArgumentException("Ugyldig samværsklasse: ${it.sjablonSamværsfradragPeriode.samværsklasse}"),
                        alderTom = it.sjablonSamværsfradragPeriode.alderTom,
                        beløpFradrag = it.sjablonSamværsfradragPeriode.beløpFradrag,
                    )
                },
        )
    }

    // Lager liste over gyldige alderTom-verdier
    private fun hentAlderTomListe(sjablonSamværsfradragPerioder: List<SjablonSamværsfradragPeriodeGrunnlag>): List<Int> =
        sjablonSamværsfradragPerioder
            .map { it.sjablonSamværsfradragPeriode.alderTom }
            .distinct()
            .sorted()

    private fun mapSamværsfradragResultatGrunnlag(
        samværsfradragBeregningResultatListe: List<SamværsfradragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            samværsfradragBeregningResultatListe
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
            )
        }

    // Mapper ut DelberegningSamværsfradrag
    private fun mapDelberegningSamværsfradrag(
        samværsfradragPeriodeResultatListe: List<SamværsfradragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = samværsfradragPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
                    periode = it.periode,
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
                innhold = POJONode(
                    DelberegningSamværsfradrag(
                        periode = it.periode,
                        beløp = it.resultat.beløpFradrag,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderReferanse = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                ),
            )
        }
}
