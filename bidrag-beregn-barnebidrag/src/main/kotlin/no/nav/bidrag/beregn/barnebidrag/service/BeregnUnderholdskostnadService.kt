package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.UnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilsynMedStønad
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBarnetilsynBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper.mapUnderholdskostnadGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.Period

internal object BeregnUnderholdskostnadService : BeregnService() {

    fun delberegningUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        val referanseTilSøknadsbarn = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN,
        )

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.underholdskostnad }

        // Mapper ut grunnlag som skal brukes for å beregne underholdskostnad
        val underholdskostnadPeriodeGrunnlag = mapUnderholdskostnadGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeUnderholdskostnad(underholdskostnadPeriodeGrunnlag, mottattGrunnlag.periode)

        val underholdskostnadBeregningResultatListe = mutableListOf<UnderholdskostnadPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner underholdskostnad
        bruddPeriodeListe.forEach { bruddPeriode ->
            val underholdskostnadBeregningGrunnlag = lagUnderholdskostnadBeregningGrunnlag(underholdskostnadPeriodeGrunnlag, bruddPeriode)
            underholdskostnadBeregningResultatListe.add(
                UnderholdskostnadPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = UnderholdskostnadBeregning.beregn(underholdskostnadBeregningGrunnlag),
                ),
            )
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapUnderholdskostnadResultatGrunnlag(
            underholdskostnadBeregningResultatListe = underholdskostnadBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut "sub"-delberegninger
        resultatGrunnlagListe.addAll(
            mapDelberegninger(
                mottattGrunnlag = mottattGrunnlag,
                underholdskostnadPeriodeGrunnlag = underholdskostnadPeriodeGrunnlag,
                underholdskostnadBeregningResultatListe = underholdskostnadBeregningResultatListe,
                referanseTilSøknadsbarn = referanseTilSøknadsbarn,
            ),
        )

        // Mapper ut grunnlag for delberegning underholdskostnad
        resultatGrunnlagListe.addAll(
            mapDelberegningUnderholdskostnad(
                underholdskostnadPeriodeResultatListe = underholdskostnadBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning) +
            mapSjablonBarnetilsynGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonBarnetilsyn()) +
            mapSjablonForbruksutgifterGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonForbruksutgifter())

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeUnderholdskostnad(
        grunnlagListe: UnderholdskostnadPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.barnetilsynMedStønadPeriodeGrunnlagListe.asSequence().map { it.barnetilsynMedStønadPeriode.periode })
            .plus(grunnlagListe.nettoTilsynsutgiftPeriodeGrunnlagListe.asSequence().map { it.nettoTilsynsutgiftPeriodeGrunnlag.periode })
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })
            .plus(grunnlagListe.sjablonBarnetilsynPeriodeGrunnlagListe.asSequence().map { it.sjablonBarnetilsynPeriode.periode })
            .plus(grunnlagListe.sjablonForbruksutgifterPeriodeGrunnlagListe.asSequence().map { it.sjablonForbruksutgifterPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for underholdskostnadberegning som ligger innenfor bruddPeriode
    private fun lagUnderholdskostnadBeregningGrunnlag(
        underholdskostnadPeriodeGrunnlag: UnderholdskostnadPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): UnderholdskostnadBeregningGrunnlag {
        val barnetilsynMedStønad = underholdskostnadPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
            .firstOrNull {
                ÅrMånedsperiode(it.barnetilsynMedStønadPeriode.periode.fom, it.barnetilsynMedStønadPeriode.periode.til).inneholder(
                    bruddPeriode,
                )
            }
            ?.let {
                BarnetilsynMedStønad(
                    referanse = it.referanse,
                    tilsynstype = it.barnetilsynMedStønadPeriode.tilsynstype!!,
                    skolealder = it.barnetilsynMedStønadPeriode.skolealder!!,
                )
            }

        val typeTilsyn = when (barnetilsynMedStønad!!.tilsynstype.toString() + barnetilsynMedStønad.skolealder.toString()) {
            "HELTID" + "UNDER" -> "HU"
            "HELTID" + "OVER" -> "HO"
            "DELTID" + "UNDER" -> "DU"
            "DELTID" + "OVER" -> "DO"
            else -> "Ukjent"
        }

        // Lager liste over gyldige alderTom-verdier
        val alderTomListe = hentAlderTomListe(underholdskostnadPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe)

        // Finner barnets beregnede alder. Alder regnes som om barnet er født 1. juli i fødselsåret.
        val beregnetAlder = Period.between(
            underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.withMonth(7).withDayOfMonth(1),
            bruddPeriode.fom.atDay(1),
        ).years

        // Finner den nærmeste alderTom som er større enn eller lik faktisk alder (til bruk for å hente ut sjablonverdi)
        val alderTom = alderTomListe.firstOrNull { beregnetAlder <= it } ?: alderTomListe.last()

        val resultat =
            UnderholdskostnadBeregningGrunnlag(
                søknadsbarn = SøknadsbarnBeregningGrunnlag(
                    referanse = underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    alder = alderTom,
                ),
                barnetilsynMedStønad = underholdskostnadPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
                    .firstOrNull {
                        ÅrMånedsperiode(it.barnetilsynMedStønadPeriode.periode.fom, it.barnetilsynMedStønadPeriode.periode.til).inneholder(
                            bruddPeriode,
                        )
                    }
                    ?.let {
                        BarnetilsynMedStønad(
                            referanse = it.referanse,
                            tilsynstype = it.barnetilsynMedStønadPeriode.tilsynstype!!,
                            skolealder = it.barnetilsynMedStønadPeriode.skolealder!!,
                        )
                    },

                nettoTilsynsutgiftBeregningGrunnlag = underholdskostnadPeriodeGrunnlag.nettoTilsynsutgiftPeriodeGrunnlagListe
                    .firstOrNull {
                        ÅrMånedsperiode(
                            it.nettoTilsynsutgiftPeriodeGrunnlag.periode.fom,
                            it.nettoTilsynsutgiftPeriodeGrunnlag.periode.til,
                        ).inneholder(bruddPeriode)
                    }
                    ?.let {
                        NettoTilsynsutgift(
                            referanse = it.referanse,
                            nettoTilsynsutgift = it.nettoTilsynsutgiftPeriodeGrunnlag.nettoTilsynsutgift,
                        )
                    },

                sjablonSjablontallBeregningGrunnlagListe = underholdskostnadPeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
                    .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
                    .map {
                        SjablonSjablontallBeregningGrunnlag(
                            referanse = it.referanse,
                            type = it.sjablonSjablontallPeriode.sjablon.navn,
                            verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                        )
                    },
                sjablonBarnetilsynBeregningGrunnlag = underholdskostnadPeriodeGrunnlag.sjablonBarnetilsynPeriodeGrunnlagListe
                    .filter { it.sjablonBarnetilsynPeriode.periode.inneholder(bruddPeriode) }
                    .filter { it.sjablonBarnetilsynPeriode.typeStønad == "64" }
                    .filter { it.sjablonBarnetilsynPeriode.typeTilsyn == typeTilsyn }
                    .map {
                        SjablonBarnetilsynBeregningGrunnlag(
                            referanse = it.referanse,
                            typeStønad = it.sjablonBarnetilsynPeriode.typeStønad,
                            typeTilsyn = it.sjablonBarnetilsynPeriode.typeTilsyn,
                            beløpBarnetilsyn = it.sjablonBarnetilsynPeriode.beløpBarnetilsyn,
                        )
                    }.first(),
                sjablonForbruksutgifterBeregningGrunnlag = underholdskostnadPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe
                    .filter { it.sjablonForbruksutgifterPeriode.periode.inneholder(bruddPeriode) }
                    .filter {
                        it.sjablonForbruksutgifterPeriode.alderTom ==
                            underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.plusYears(
                                1,
                            ).toString().substring(0, 4).toInt()
                    }
                    .map {
                        SjablonForbruksutgifterBeregningGrunnlag(
                            referanse = it.referanse,
                            alderTom = it.sjablonForbruksutgifterPeriode.alderTom,
                            beløpForbrukTotalt = it.sjablonForbruksutgifterPeriode.beløpForbruk,
                        )
                    }.first(),
            )
        return resultat
    }

    private fun mapUnderholdskostnadResultatGrunnlag(
        underholdskostnadBeregningResultatListe: List<UnderholdskostnadPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            underholdskostnadBeregningResultatListe
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

    // Lager liste over gyldige alderTom-verdier
    private fun hentAlderTomListe(sjablonForbruksutgifterPerioder: List<SjablonForbruksutgifterPeriodeGrunnlag>): List<Int> =
        sjablonForbruksutgifterPerioder
            .map { it.sjablonForbruksutgifterPeriode.alderTom }
            .distinct()
            .sorted()

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

    private fun mapDelberegninger(
        mottattGrunnlag: BeregnGrunnlag,
        underholdskostnadPeriodeGrunnlag: UnderholdskostnadPeriodeGrunnlag,
        underholdskostnadBeregningResultatListe: List<UnderholdskostnadPeriodeResultat>,
        referanseTilSøknadsbarn: String,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            underholdskostnadBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        resultatGrunnlagListe.addAll(
            mapDelberegningUnderholdskostnad(
                underholdskostnadPeriodeResultatListe = underholdskostnadBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene på laveste nivå og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            underholdskostnadBeregningResultatListe.flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        resultatGrunnlagListe.addAll(
            mottattGrunnlag.grunnlagListe
                .filter { it.referanse in delberegningReferanseListe }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = it.type,
                        innhold = it.innhold,
                        grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                        gjelderReferanse = it.gjelderReferanse,
                    )
                },
        )

        return resultatGrunnlagListe
    }

    // Mapper ut DelberegningUnderholdskostnad
    private fun mapDelberegningUnderholdskostnad(
        underholdskostnadPeriodeResultatListe: List<UnderholdskostnadPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = underholdskostnadPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = finnReferanseTilRolle(
                        grunnlagListe = mottattGrunnlag.grunnlagListe,
                        grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                    ),
                ),
                type = Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD,
                innhold = POJONode(
                    DelberegningUnderholdskostnad(
                        periode = it.periode,
                        forbruksutgift = it.resultat.forbruksutgift,
                        boutgift = it.resultat.boutgift,
                        barnetilsynMedStønad = it.resultat.barnetilsynMedStønad,
                        nettoTilsynsutgift = it.resultat.nettoTilsynsutgift,
                        barnetrygd = it.resultat.barnetrygd,
                        underholdskostnad = it.resultat.underholdskostnad,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.distinct().sorted(),
                gjelderReferanse = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
            )
        }
}