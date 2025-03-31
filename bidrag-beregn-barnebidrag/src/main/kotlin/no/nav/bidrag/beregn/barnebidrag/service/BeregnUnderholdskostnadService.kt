package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.UnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilsynMedStønad
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetrygdType
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBarnetilsynBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper.mapUnderholdskostnadGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.YearMonth

internal object BeregnUnderholdskostnadService : BeregnService() {

    fun delberegningUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(mottattGrunnlag.periode) { it.underholdskostnad }

        // Mapper ut grunnlag som skal brukes for å beregne underholdskostnad
        val underholdskostnadPeriodeGrunnlag = mapUnderholdskostnadGrunnlag(mottattGrunnlag = mottattGrunnlag, sjablonGrunnlag = sjablonGrunnlag)

        val søknadsbarnFødselsdatoÅrMåned =
            YearMonth.of(
                underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.year,
                underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.month,
            )
        val søknadsbarnSeksårsdag = søknadsbarnFødselsdatoÅrMåned.withMonth(7).plusYears(6)
        val datoInnføringForhøyetBarnetrygd = YearMonth.of(2021, 7)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeUnderholdskostnad(
            grunnlagListe = underholdskostnadPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
            søknadsbarnFødselsdatoÅrMåned = søknadsbarnFødselsdatoÅrMåned,
            søknadsbarnSeksårsdag = søknadsbarnSeksårsdag,
            datoInnføringUtvidetBarnetrygd = datoInnføringForhøyetBarnetrygd,
        )

        val underholdskostnadBeregningResultatListe = mutableListOf<UnderholdskostnadPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner underholdskostnad
        bruddPeriodeListe.forEach { bruddPeriode ->
            val underholdskostnadBeregningGrunnlag = lagUnderholdskostnadBeregningGrunnlag(
                underholdskostnadPeriodeGrunnlag = underholdskostnadPeriodeGrunnlag,
                bruddPeriode = bruddPeriode,
            )

            val barnetrygdType = when {
                mottattGrunnlag.stønadstype == Stønadstype.BIDRAG18AAR -> BarnetrygdType.INGEN
                bruddPeriode.fom == søknadsbarnFødselsdatoÅrMåned -> BarnetrygdType.INGEN
                bruddPeriode.fom.isBefore(datoInnføringForhøyetBarnetrygd) -> BarnetrygdType.ORDINÆR
                bruddPeriode.fom.isBefore(søknadsbarnSeksårsdag) -> BarnetrygdType.FORHØYET
                else -> BarnetrygdType.ORDINÆR
            }

            underholdskostnadBeregningResultatListe.add(
                UnderholdskostnadPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = UnderholdskostnadBeregning.beregn(grunnlag = underholdskostnadBeregningGrunnlag, barnetrygdType = barnetrygdType),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (underholdskostnadBeregningResultatListe.isNotEmpty()) {
            val sisteElement = underholdskostnadBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                underholdskostnadBeregningResultatListe[underholdskostnadBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = underholdskostnadBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning underholdskostnad
        resultatGrunnlagListe.addAll(
            mapDelberegningUnderholdskostnad(
                underholdskostnadPeriodeResultatListe = underholdskostnadBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe
            )
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
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
        søknadsbarnFødselsdatoÅrMåned: YearMonth,
        søknadsbarnSeksårsdag: YearMonth,
        datoInnføringUtvidetBarnetrygd: YearMonth,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.barnetilsynMedStønadPeriodeGrunnlagListe.asSequence().map { it.barnetilsynMedStønadPeriode.periode })
            .plus(grunnlagListe.nettoTilsynsutgiftPeriodeGrunnlagListe.asSequence().map { it.nettoTilsynsutgiftPeriodeGrunnlag.periode })
            .plus(grunnlagListe.sjablonSjablontallPeriodeGrunnlagListe.asSequence().map { it.sjablonSjablontallPeriode.periode })
            .plus(grunnlagListe.sjablonBarnetilsynPeriodeGrunnlagListe.asSequence().map { it.sjablonBarnetilsynPeriode.periode })
            .plus(grunnlagListe.sjablonForbruksutgifterPeriodeGrunnlagListe.asSequence().map { it.sjablonForbruksutgifterPeriode.periode })
            .plus(ÅrMånedsperiode(fom = søknadsbarnFødselsdatoÅrMåned, til = søknadsbarnFødselsdatoÅrMåned.plusMonths(1)))
            .plus(ÅrMånedsperiode(fom = søknadsbarnSeksårsdag, til = søknadsbarnSeksårsdag))
            .plus(ÅrMånedsperiode(fom = datoInnføringUtvidetBarnetrygd, til = datoInnføringUtvidetBarnetrygd))

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for underholdskostnadberegning som ligger innenfor bruddPeriode
    private fun lagUnderholdskostnadBeregningGrunnlag(
        underholdskostnadPeriodeGrunnlag: UnderholdskostnadPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): UnderholdskostnadBeregningGrunnlag {
        val barnetilsynMedStønad = underholdskostnadPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
            .firstOrNull {
                ÅrMånedsperiode(fom = it.barnetilsynMedStønadPeriode.periode.fom, til = it.barnetilsynMedStønadPeriode.periode.til).inneholder(
                    bruddPeriode,
                )
            }
            ?.let {
                BarnetilsynMedStønad(
                    referanse = it.referanse,
                    tilsynstype = it.barnetilsynMedStønadPeriode.tilsynstype,
                    skolealder = it.barnetilsynMedStønadPeriode.skolealder,
                )
            }

        // Finner barnets beregnede alder. Alder regnes som om barnet er født 1. juli i fødselsåret.
        val beregnetAlder = finnBarnetsAlder(
            fødselsdato = underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato,
            årMåned = bruddPeriode.fom,
        )

        // Lager liste over gyldige alderTom-verdier
        val alderTomListe = hentAlderTomListeForbruksutgifter(underholdskostnadPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe)

        // Finner den nærmeste alderTom som er større enn eller lik faktisk alder (til bruk for å hente ut sjablonverdi)
        val alderTom = alderTomListe.firstOrNull { beregnetAlder <= it } ?: alderTomListe.last()

        val typeTilsyn =
            if (barnetilsynMedStønad != null) {
                bestemTilsynskode(
                    tilsynstype = barnetilsynMedStønad.tilsynstype,
                    skolealder = barnetilsynMedStønad.skolealder,
                )
            } else {
                null
            }

        val resultat =
            UnderholdskostnadBeregningGrunnlag(
                søknadsbarn = SøknadsbarnBeregningGrunnlag(
                    referanse = underholdskostnadPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    alder = beregnetAlder,
                ),
                barnetilsynMedStønad = underholdskostnadPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
                    .firstOrNull {
                        ÅrMånedsperiode(
                            fom = it.barnetilsynMedStønadPeriode.periode.fom,
                            til = it.barnetilsynMedStønadPeriode.periode.til,
                        ).inneholder(bruddPeriode)
                    }
                    ?.let {
                        BarnetilsynMedStønad(
                            referanse = it.referanse,
                            tilsynstype = it.barnetilsynMedStønadPeriode.tilsynstype,
                            skolealder = it.barnetilsynMedStønadPeriode.skolealder,
                        )
                    },

                nettoTilsynsutgiftBeregningGrunnlag = underholdskostnadPeriodeGrunnlag.nettoTilsynsutgiftPeriodeGrunnlagListe
                    .firstOrNull {
                        ÅrMånedsperiode(
                            fom = it.nettoTilsynsutgiftPeriodeGrunnlag.periode.fom,
                            til = it.nettoTilsynsutgiftPeriodeGrunnlag.periode.til,
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
                    }.toMutableList(),
                sjablonBarnetilsynBeregningGrunnlag = if (typeTilsyn != null) {
                    underholdskostnadPeriodeGrunnlag.sjablonBarnetilsynPeriodeGrunnlagListe
                        .asSequence()
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
                        }.first()
                } else {
                    null
                },
                sjablonForbruksutgifterBeregningGrunnlag = underholdskostnadPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe
                    .filter { it.sjablonForbruksutgifterPeriode.periode.inneholder(bruddPeriode) }
                    .filter { it.sjablonForbruksutgifterPeriode.alderTom == alderTom }
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
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }
}
