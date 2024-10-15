package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoTilsynsutgiftBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklasseBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.mapNettoTilsynsutgiftGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.Period

internal object BeregnNettoTilsynsutgiftService : BeregnService() {

    fun delberegningNettoTilsynsutgift(grunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>): List<GrunnlagDto> {
        val nettoTilsynsutgiftPeriodeGrunnlag = mapNettoTilsynsutgiftGrunnlag(grunnlag, sjablonGrunnlag)

        val bruddPeriodeListe = lagBruddPeriodeListeNettoTilsynsutgift(nettoTilsynsutgiftPeriodeGrunnlag, grunnlag.periode)
        val nettoTilsynsutgiftBeregningResultatListe = mutableListOf<NettoTilsynsutgiftPeriodeResultat>()
        bruddPeriodeListe.forEach { bruddPeriode ->
            val nettoTilsynsutgiftBeregningGrunnlag = lagNettoTilsynsutgiftBeregningGrunnlag(nettoTilsynsutgiftPeriodeGrunnlag, bruddPeriode)
            nettoTilsynsutgiftBeregningResultatListe.add(
                NettoTilsynsutgiftPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = NettoTilsynsutgiftBeregning.beregn(nettoTilsynsutgiftBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapNettoTilsynsutgiftResultatGrunnlag(
            nettoTilsynsutgiftBeregningResultatListe = nettoTilsynsutgiftBeregningResultatListe,
            mottattGrunnlag = grunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning nettoTilsynsutgift
        resultatGrunnlagListe.addAll(
            mapDelberegningNettoTilsynsutgift(
                nettoTilsynsutgiftPeriodeResultatListe = nettoTilsynsutgiftBeregningResultatListe,
                mottattGrunnlag = grunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeNettoTilsynsutgift(
        grunnlagListe: NettoTilsynsutgiftPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.samværsklassePeriodeListe.asSequence().map { it.samværsklassePeriode.periode })
            .plus(grunnlagListe.sjablonNettoTilsynsutgiftPeriodeListe.asSequence().map { it.sjablonNettoTilsynsutgiftPeriode.periode })
            .plus(
                lagAlderBruddPerioder(
                    sjablonNettoTilsynsutgiftPerioder = grunnlagListe.sjablonNettoTilsynsutgiftPeriodeListe,
                    søknadsbarnPeriodeGrunnlag = grunnlagListe.søknadsbarnPeriodeGrunnlag,
                ).asSequence(),
            )

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager bruddperioder for alder basert på verdier i sjablon SAMVÆRSFRADRAG
    private fun lagAlderBruddPerioder(
        sjablonNettoTilsynsutgiftPerioder: List<SjablonNettoTilsynsutgiftPeriodeGrunnlag>,
        søknadsbarnPeriodeGrunnlag: SøknadsbarnPeriodeGrunnlag,
    ): List<ÅrMånedsperiode> = hentAlderTomListe(sjablonNettoTilsynsutgiftPerioder)
        .map {
            val alderBruddDato = søknadsbarnPeriodeGrunnlag.fødselsdato.plusYears(it.toLong())
            ÅrMånedsperiode(alderBruddDato, alderBruddDato)
        }

    // Lager grunnlag for nettoTilsynsutgiftberegning som ligger innenfor bruddPeriode
    private fun lagNettoTilsynsutgiftBeregningGrunnlag(
        nettoTilsynsutgiftPeriodeGrunnlag: NettoTilsynsutgiftPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): NettoTilsynsutgiftBeregningGrunnlag {
        // Lager liste over gyldige alderTom-verdier
        val alderTomListe = hentAlderTomListe(nettoTilsynsutgiftPeriodeGrunnlag.sjablonNettoTilsynsutgiftPeriodeListe)

        // Finner barnets faktiske alder
        val faktiskAlder = Period.between(
            nettoTilsynsutgiftPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato,
            bruddPeriode.fom.atDay(1),
        ).years

        // Finner den nærmeste alderTom som er større enn eller lik faktisk alder (til bruk for å hente ut sjablonverdi)
        val alderTom = alderTomListe.firstOrNull { faktiskAlder <= it } ?: alderTomListe.last()

        return NettoTilsynsutgiftBeregningGrunnlag(
            søknadsbarn = SøknadsbarnBeregningGrunnlag(
                referanse = nettoTilsynsutgiftPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                alder = alderTom,
            ),
            samværsklasseBeregningGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag.samværsklassePeriodeListe
                .firstOrNull { it.samværsklassePeriode.periode.inneholder(bruddPeriode) }
                ?.let { SamværsklasseBeregningGrunnlag(referanse = it.referanse, samværsklasse = it.samværsklassePeriode.samværsklasse) }
                ?: throw IllegalArgumentException("Ingen samværsklasse funnet for periode $bruddPeriode"),
            sjablonNettoTilsynsutgiftBeregningGrunnlagListe = nettoTilsynsutgiftPeriodeGrunnlag.sjablonNettoTilsynsutgiftPeriodeListe
                .filter { it.sjablonNettoTilsynsutgiftPeriode.periode.inneholder(bruddPeriode) }
                .map {
                    SjablonNettoTilsynsutgiftBeregningGrunnlag(
                        referanse = it.referanse,
                        samværsklasse = Samværsklasse.fromBisysKode(it.sjablonNettoTilsynsutgiftPeriode.samværsklasse)
                            ?: throw IllegalArgumentException("Ugyldig samværsklasse: ${it.sjablonNettoTilsynsutgiftPeriode.samværsklasse}"),
                        alderTom = it.sjablonNettoTilsynsutgiftPeriode.alderTom,
                        beløpFradrag = it.sjablonNettoTilsynsutgiftPeriode.beløpFradrag,
                    )
                },
        )
    }

    // Lager liste over gyldige alderTom-verdier
    private fun hentAlderTomListe(sjablonNettoTilsynsutgiftPerioder: List<SjablonNettoTilsynsutgiftPeriodeGrunnlag>): List<Int> =
        sjablonNettoTilsynsutgiftPerioder
            .map { it.sjablonNettoTilsynsutgiftPeriode.alderTom }
            .distinct()
            .sorted()

    private fun mapNettoTilsynsutgiftResultatGrunnlag(
        nettoTilsynsutgiftBeregningResultatListe: List<NettoTilsynsutgiftPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            nettoTilsynsutgiftBeregningResultatListe
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

    // Mapper ut DelberegningNettoTilsynsutgift
    private fun mapDelberegningNettoTilsynsutgift(
        nettoTilsynsutgiftPeriodeResultatListe: List<NettoTilsynsutgiftPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = nettoTilsynsutgiftPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
                    periode = it.periode,
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
                innhold = POJONode(
                    DelberegningNettoTilsynsutgift(
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
