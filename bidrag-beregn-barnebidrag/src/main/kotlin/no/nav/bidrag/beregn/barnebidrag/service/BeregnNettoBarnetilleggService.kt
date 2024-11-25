package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoBarnetilleggBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilsynMedStønad
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoBarnetilleggPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBarnetilsynBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoBarnetilleggMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoBarnetilleggMapper.mapNettoBarnetilleggGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.Period

internal object BeregnNettoBarnetilleggService : BeregnService() {

    fun delberegningNettoBarnetillegg(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne nettoBarnetillegg
        val nettoBarnetilleggPeriodeGrunnlag = mapNettoBarnetilleggGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeNettoBarnetillegg(nettoBarnetilleggPeriodeGrunnlag, mottattGrunnlag.periode)

        val nettoBarnetilleggBeregningResultatListe = mutableListOf<NettoBarnetilleggPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner nettoBarnetillegg
        bruddPeriodeListe.forEach { bruddPeriode ->
            val nettoBarnetilleggBeregningGrunnlag = lagNettoBarnetilleggBeregningGrunnlag(nettoBarnetilleggPeriodeGrunnlag, bruddPeriode)
            nettoBarnetilleggBeregningResultatListe.add(
                NettoBarnetilleggPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = NettoBarnetilleggBeregning.beregn(nettoBarnetilleggBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det (åpen sluttdato)
        if (nettoBarnetilleggBeregningResultatListe.isNotEmpty()) {
            val sisteElement = nettoBarnetilleggBeregningResultatListe.last()
            if (sisteElement.periode.til != null) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                nettoBarnetilleggBeregningResultatListe[nettoBarnetilleggBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapNettoBarnetilleggResultatGrunnlag(
            nettoBarnetilleggBeregningResultatListe = nettoBarnetilleggBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning nettoBarnetillegg
        resultatGrunnlagListe.addAll(
            mapDelberegningNettoBarnetillegg(
                nettoBarnetilleggPeriodeResultatListe = nettoBarnetilleggBeregningResultatListe,
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
    private fun lagBruddPeriodeListeNettoBarnetillegg(
        grunnlagListe: NettoBarnetilleggPeriodeGrunnlag,
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

    // Lager grunnlag for nettoBarnetilleggberegning som ligger innenfor bruddPeriode
    private fun lagNettoBarnetilleggBeregningGrunnlag(
        nettoBarnetilleggPeriodeGrunnlag: NettoBarnetilleggPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): NettoBarnetilleggBeregningGrunnlag {
        val barnetilsynMedStønad = nettoBarnetilleggPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
            .firstOrNull {
                ÅrMånedsperiode(it.barnetilsynMedStønadPeriode.periode.fom, it.barnetilsynMedStønadPeriode.periode.til).inneholder(
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
        val beregnetAlder = Period.between(
            nettoBarnetilleggPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.withMonth(7).withDayOfMonth(1),
            bruddPeriode.fom.atDay(1),
        ).years

        // Lager liste over gyldige alderTom-verdier
        val alderTomListe = hentAlderTomListe(nettoBarnetilleggPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe)

        // Finner den nærmeste alderTom som er større enn eller lik faktisk alder (til bruk for å hente ut sjablonverdi)
        val alderTom = alderTomListe.firstOrNull { beregnetAlder <= it } ?: alderTomListe.last()

        val typeTilsyn: String? = if (barnetilsynMedStønad != null) {
            when (barnetilsynMedStønad.tilsynstype.toString() + barnetilsynMedStønad.skolealder.toString()) {
                "HELTID" + "UNDER" -> "HU"
                "HELTID" + "OVER" -> "HO"
                "DELTID" + "UNDER" -> "DU"
                "DELTID" + "OVER" -> "DO"
                else -> "Ukjent"
            }
        } else {
            null
        }

        val resultat =
            NettoBarnetilleggBeregningGrunnlag(
                søknadsbarn = SøknadsbarnBeregningGrunnlag(
                    referanse = nettoBarnetilleggPeriodeGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    alder = beregnetAlder,
                ),
                skattFaktor = nettoBarnetilleggPeriodeGrunnlag.barnetilsynMedStønadPeriodeGrunnlagListe
                    .firstOrNull {
                        ÅrMånedsperiode(it.barnetilsynMedStønadPeriode.periode.fom, it.barnetilsynMedStønadPeriode.periode.til).inneholder(
                            bruddPeriode,
                        )
                    }
                    ?.let {
                        BarnetilsynMedStønad(
                            referanse = it.referanse,
                            tilsynstype = it.barnetilsynMedStønadPeriode.tilsynstype,
                            skolealder = it.barnetilsynMedStønadPeriode.skolealder,
                        )
                    },

                nettoTilsynsutgiftBeregningGrunnlag = nettoBarnetilleggPeriodeGrunnlag.nettoTilsynsutgiftPeriodeGrunnlagListe
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

                sjablonSjablontallBeregningGrunnlagListe = nettoBarnetilleggPeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
                    .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
                    .map {
                        SjablonSjablontallBeregningGrunnlag(
                            referanse = it.referanse,
                            type = it.sjablonSjablontallPeriode.sjablon.navn,
                            verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                        )
                    },
                sjablonBarnetilsynBeregningGrunnlag = if (typeTilsyn != null) {
                    nettoBarnetilleggPeriodeGrunnlag.sjablonBarnetilsynPeriodeGrunnlagListe
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
                sjablonForbruksutgifterBeregningGrunnlag = nettoBarnetilleggPeriodeGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe
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

    private fun mapNettoBarnetilleggResultatGrunnlag(
        nettoBarnetilleggBeregningResultatListe: List<NettoBarnetilleggPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            nettoBarnetilleggBeregningResultatListe
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
        nettoBarnetilleggPeriodeGrunnlag: NettoBarnetilleggPeriodeGrunnlag,
        delberegningNettoTilsynsutgiftResultat: List<NettoTilsynsutgiftPeriodeResultat>,
//        referanseTilSøknadsbarn: String,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            delberegningNettoTilsynsutgiftResultat
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        resultatGrunnlagListe.addAll(
            mapDelberegningNettoTilsynsutgift(
                nettoTilsynsutgiftPeriodeResultatListe = delberegningNettoTilsynsutgiftResultat,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene på laveste nivå og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            delberegningNettoTilsynsutgiftResultat.flatMap { it.resultat.grunnlagsreferanseListe }
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

    // Mapper ut DelberegningNettoTilsynsutgift
    private fun mapDelberegningNettoTilsynsutgift(
        nettoTilsynsutgiftPeriodeResultatListe: List<NettoTilsynsutgiftPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = nettoTilsynsutgiftPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_NETTO_TILSYNSUTGIFT,
                    periode = it.periode,
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                ),
                type = Grunnlagstype.DELBEREGNING_NETTO_TILSYNSUTGIFT,
                innhold = POJONode(
                    DelberegningNettoTilsynsutgift(
                        periode = it.periode,
                        totaltFaktiskUtgiftBeløp = it.resultat.totaltFaktiskUtgiftBeløp,
                        tilsynsutgiftBarnListe = it.resultat.tilsynsutgiftBarnListe,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderReferanse = finnReferanseTilRolle(
                    grunnlagListe = mottattGrunnlag.grunnlagListe,
                    grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                ),
            )
        }

    // Mapper ut DelberegningNettoBarnetillegg
    private fun mapDelberegningNettoBarnetillegg(
        nettoBarnetilleggPeriodeResultatListe: List<NettoBarnetilleggPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = nettoBarnetilleggPeriodeResultatListe
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
                    DelberegningNettoBarnetillegg(
                        periode = it.periode,
                        forbruksutgift = it.resultat.forbruksutgift,
                        boutgift = it.resultat.boutgift,
                        barnetilsynMedStønad = it.resultat.barnetilsynMedStønad,
                        nettoTilsynsutgift = it.resultat.nettoTilsynsutgift,
                        barnetrygd = it.resultat.barnetrygd,
                        nettoBarnetillegg = it.resultat.nettoBarnetillegg,
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
