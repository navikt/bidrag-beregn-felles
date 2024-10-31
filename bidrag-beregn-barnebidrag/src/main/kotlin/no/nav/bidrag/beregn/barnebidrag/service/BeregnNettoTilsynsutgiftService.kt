package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoTilsynsutgiftBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.DelberegningFaktiskTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.DelberegningTilleggsstønad
import no.nav.bidrag.beregn.barnebidrag.bo.FaktiskUtgift
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksFradragsbeløpBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksTilsynsbeløpBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Tilleggsstønad
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.finnFødselsdatoBarn
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.mapNettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.LocalDate
import java.time.Period

internal object BeregnNettoTilsynsutgiftService : BeregnService() {

    fun delberegningNettoTilsynsutgift(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        val referanseBm = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag =
            lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.nettoBarnetilsyn }

        val nettoTilsynsutgiftPeriodeGrunnlag = mapNettoTilsynsutgiftPeriodeGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        val bruddPeriodeListe = lagBruddPeriodeListeNettoTilsynsutgift(nettoTilsynsutgiftPeriodeGrunnlag, mottattGrunnlag.periode)

        val nettoTilsynsutgiftBeregningResultatListe = mutableListOf<NettoTilsynsutgiftPeriodeResultat>()

        bruddPeriodeListe.forEach { bruddPeriode ->
            // Teller antall barn under 13 år i perioden og filterer bort resten. Hvis antall er null så gjøres det ingen beregning
            val antallBarnIPerioden = nettoTilsynsutgiftPeriodeGrunnlag.faktiskUtgiftPeriodeCoreListe
                .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                .filter { finnAlderBarn(mottattGrunnlag.grunnlagListe, it.gjelderBarn, bruddPeriode.fom.atDay(1)) < 13 }
                .size
            if (antallBarnIPerioden > 0) {
                val nettoTilsynsutgiftBeregningGrunnlag =
                    lagNettoTilsynsutgiftBeregningGrunnlag(
                        nettoTilsynsutgiftPeriodeGrunnlag,
                        bruddPeriode,
                        antallBarnIPerioden,
                    )
                nettoTilsynsutgiftBeregningResultatListe.add(
                    NettoTilsynsutgiftPeriodeResultat(
                        periode = bruddPeriode,
                        resultat = NettoTilsynsutgiftBeregning.beregn(nettoTilsynsutgiftBeregningGrunnlag),
                    ),
                )
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapNettoTilsynsutgiftResultatGrunnlag(
            nettoTilsynsutgiftBeregningResultatListe = nettoTilsynsutgiftBeregningResultatListe,
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut "sub"-delberegninger
        resultatGrunnlagListe.addAll(
            mapDelberegninger(
                mottattGrunnlag = mottattGrunnlag,
                nettoTilsynsutgiftPeriodeGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag,
                nettoTilsynsutgiftPeriodeResultat = nettoTilsynsutgiftBeregningResultatListe,
                referanseBm = referanseBm,
            ),
        )

        // Mapper ut grunnlag for delberegning nettoTilsynsutgift
        resultatGrunnlagListe.addAll(
            mapDelberegningNettoTilsynsutgift(
                nettoTilsynsutgiftPeriodeResultatListe = nettoTilsynsutgiftBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        return resultatGrunnlagListe.sortedBy { it.referanse }
    }

    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning) +
            mapSjablonMaksTilsynsbeløpGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonMaksTilsyn()) +
            mapSjablonMaksFradragGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonMaksFradrag())

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeNettoTilsynsutgift(
        grunnlagListe: NettoTilsynsutgiftPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.faktiskUtgiftPeriodeCoreListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.tilleggsstønadPeriodeCoreListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.sjablonMaksTilsynsbeløpPeriodeGrunnlagListe.asSequence().map { it.sjablonMaksTilsynsbeløpPeriode.periode })
            .plus(grunnlagListe.sjablonMaksFradragsbeløpPeriodeGrunnlagListe.asSequence().map { it.sjablonMaksFradragsbeløpPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for nettoTilsynsutgiftberegning som ligger innenfor bruddPeriode
    private fun lagNettoTilsynsutgiftBeregningGrunnlag(
        nettoTilsynsutgiftPeriodeGrunnlag: NettoTilsynsutgiftPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
        antallBarnIPerioden: Int,
    ): NettoTilsynsutgiftBeregningGrunnlag = NettoTilsynsutgiftBeregningGrunnlag(
        faktiskUtgiftListe = nettoTilsynsutgiftPeriodeGrunnlag.faktiskUtgiftPeriodeCoreListe
            .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            .map {
                FaktiskUtgift(
                    referanse = it.referanse,
                    gjelderBarn = it.gjelderBarn,
                    beregnetBeløp = it.beregnetBeløp,
                )
            }.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Ingen faktisk utgift funnet for periode $bruddPeriode"),

        tilleggsstønadListe = nettoTilsynsutgiftPeriodeGrunnlag.tilleggsstønadPeriodeCoreListe
            .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            .map {
                Tilleggsstønad(
                    referanse = it.referanse,
                    gjelderBarn = it.gjelderBarn,
                    beregnetBeløp = it.beregnetBeløp,
                )
            },

        sjablonSjablontallBeregningGrunnlagListe = nettoTilsynsutgiftPeriodeGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
            .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonSjablontallBeregningGrunnlag(
                    referanse = it.referanse,
                    type = it.sjablonSjablontallPeriode.sjablon.navn,
                    verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                )
            },

        sjablonMaksTilsynsbeløpBeregningGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag.sjablonMaksTilsynsbeløpPeriodeGrunnlagListe
            .asSequence()
            .filter { it.sjablonMaksTilsynsbeløpPeriode.periode.inneholder(bruddPeriode) }
            .sortedBy { it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom }
            .filter { it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom >= antallBarnIPerioden }
            .map {
                SjablonMaksTilsynsbeløpBeregningGrunnlag(
                    referanse = it.referanse,
                    antallBarnTom = it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom,
                    maxBeløpTilsyn = it.sjablonMaksTilsynsbeløpPeriode.maksBeløpTilsyn,
                )
            }.first(),

        sjablonMaksFradragsbeløpBeregningGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag.sjablonMaksFradragsbeløpPeriodeGrunnlagListe
            .asSequence()
            .filter { it.sjablonMaksFradragsbeløpPeriode.periode.inneholder(bruddPeriode) }
            .sortedBy { it.sjablonMaksFradragsbeløpPeriode.antallBarnTom }
            .filter { it.sjablonMaksFradragsbeløpPeriode.antallBarnTom >= antallBarnIPerioden }
            .map {
                SjablonMaksFradragsbeløpBeregningGrunnlag(
                    referanse = it.referanse,
                    antallBarnTom = it.sjablonMaksFradragsbeløpPeriode.antallBarnTom,
                    maxBeløpFradrag = it.sjablonMaksFradragsbeløpPeriode.maksBeløpFradrag,
                )
            }.first(),
    )

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

    private fun mapDelberegninger(
        mottattGrunnlag: BeregnGrunnlag,
        nettoTilsynsutgiftPeriodeGrunnlag: NettoTilsynsutgiftPeriodeGrunnlag,
        nettoTilsynsutgiftPeriodeResultat: List<NettoTilsynsutgiftPeriodeResultat>,
        referanseBm: String,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            nettoTilsynsutgiftPeriodeResultat
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct()

        // Mapper ut DelberegningFaktiskUtgift
        val faktiskUtgiftPeriodeCoreListe = nettoTilsynsutgiftPeriodeGrunnlag.faktiskUtgiftPeriodeCoreListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningFaktiskTilsynsutgift(
                faktiskUtgiftPeriodeCoreListe = faktiskUtgiftPeriodeCoreListe,
                bidragsmottakerReferanse = referanseBm,
            ),
        )

        // Mapper ut DelberegningTilleggsstønad
        val tilleggsstønadPeriodeCoreListe = nettoTilsynsutgiftPeriodeGrunnlag.tilleggsstønadPeriodeCoreListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        resultatGrunnlagListe.addAll(
            mapDelberegningTilleggsstønad(
                tilleggsstønadPeriodeCoreListe = tilleggsstønadPeriodeCoreListe,
                bidragsmottakerReferanse = referanseBm,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene og mapper ut tilhørende grunnlag
        val delberegningReferanseListe =
            faktiskUtgiftPeriodeCoreListe.flatMap { it.grunnlagsreferanseListe }
                .union(
                    tilleggsstønadPeriodeCoreListe.flatMap { it.grunnlagsreferanseListe },
                )
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

    // Mapper ut DelberegningFaktiskUtgift
    private fun mapDelberegningFaktiskTilsynsutgift(faktiskUtgiftPeriodeCoreListe: List<FaktiskUtgiftPeriodeCore>, bidragsmottakerReferanse: String) =
        faktiskUtgiftPeriodeCoreListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningFaktiskTilsynsutgift(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            beregnetBeløp = it.beregnetBeløp,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = it.gjelderBarn,
                )
            }

    // Mapper ut DelberegningTilleggsstønad
    private fun mapDelberegningTilleggsstønad(tilleggsstønadPeriodeCoreListe: List<TilleggsstønadPeriodeCore>, bidragsmottakerReferanse: String) =
        tilleggsstønadPeriodeCoreListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningTilleggsstønad(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            beregnetBeløp = it.beregnetBeløp,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = bidragsmottakerReferanse,
                )
            }

    fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.referanse.uppercase().contains("SJABLONTALL") }
                .filtrerOgKonverterBasertPåEgenReferanse<SjablonSjablontallPeriode>()
                .map {
                    SjablonSjablontallPeriodeGrunnlag(
                        referanse = it.referanse,
                        sjablonSjablontallPeriode = it.innhold,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Feil ved uthenting av sjablon for sjablontall: " + e.message,
            )
        }
    }

    private fun finnAlderBarn(beregnGrunnlag: List<GrunnlagDto>, referanseBarn: Grunnlagsreferanse, dato: LocalDate): Int {
        val fødselsdatoBarn = finnFødselsdatoBarn(beregnGrunnlag, referanseBarn)
        return Period.between(
            fødselsdatoBarn,
            dato,
        ).years
    }
}
