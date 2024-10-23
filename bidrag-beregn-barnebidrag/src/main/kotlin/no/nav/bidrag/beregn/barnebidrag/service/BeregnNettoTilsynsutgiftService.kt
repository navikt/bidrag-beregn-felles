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
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.finnFødselsdatoBarn
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.mapNettoTilsynsutgiftGrunnlag
import no.nav.bidrag.beregn.core.dto.FaktiskUtgiftPeriodeCore
import no.nav.bidrag.beregn.core.dto.TilleggsstønadPeriodeCore
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
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
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode)

        val nettoTilsynsutgiftPeriodeGrunnlag = mapNettoTilsynsutgiftGrunnlag(mottattGrunnlag, sjablonGrunnlag)

        val bruddPeriodeListe = lagBruddPeriodeListeNettoTilsynsutgift(nettoTilsynsutgiftPeriodeGrunnlag, mottattGrunnlag.periode)

        val nettoTilsynsutgiftBeregningResultatListe = mutableListOf<NettoTilsynsutgiftPeriodeResultat>()

        bruddPeriodeListe.forEach { bruddPeriode ->
            val nettoTilsynsutgiftBeregningGrunnlag =
                lagNettoTilsynsutgiftBeregningGrunnlag(mottattGrunnlag.grunnlagListe, nettoTilsynsutgiftPeriodeGrunnlag, bruddPeriode)
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

    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode): List<GrunnlagDto> =
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
        mottattGrunnlag: List<GrunnlagDto>,
        nettoTilsynsutgiftPeriodeGrunnlag: NettoTilsynsutgiftPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): NettoTilsynsutgiftBeregningGrunnlag = NettoTilsynsutgiftBeregningGrunnlag(
        faktiskUtgiftListe = nettoTilsynsutgiftPeriodeGrunnlag.faktiskUtgiftPeriodeCoreListe
            .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
            .filter { finnAlderBarn(mottattGrunnlag, it.referanseBarn, bruddPeriode.fom.atDay(1)) < 13 }.map {
                FaktiskUtgift(
                    referanseBarn = it.referanseBarn,
                    beregnetBeløp = it.beregnetBeløp,
                )
            }.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Ingen faktisk utgift funnet for periode $bruddPeriode"),

        sjablonMaksTilsynsbeløpBeregningGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag.sjablonMaksTilsynsbeløpPeriodeGrunnlagListe
            .filter { it.sjablonMaksTilsynsbeløpPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonMaksTilsynsbeløpBeregningGrunnlag(
                    referanse = it.referanse,
                    antallBarnTom = it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom,
                    maxBeløpTilsyn = it.sjablonMaksTilsynsbeløpPeriode.maksBeløpTilsyn,
                )
            },

        sjablonMaksFradragsbeløpBeregningGrunnlag = nettoTilsynsutgiftPeriodeGrunnlag.sjablonMaksFradragsbeløpPeriodeGrunnlagListe
            .filter { it.sjablonMaksFradragsbeløpPeriode.periode.inneholder(bruddPeriode) }
            .map {
                SjablonMaksFradragsbeløpBeregningGrunnlag(
                    referanse = it.referanse,
                    antallBarnTom = it.sjablonMaksFradragsbeløpPeriode.antallBarnTom,
                    maxBeløpFradrag = it.sjablonMaksFradragsbeløpPeriode.maksBeløpFradrag,
                )
            },
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
                        beløp = it.resultat.beløp,
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
                    gjelderReferanse = bidragsmottakerReferanse,
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

//    private fun mapSjablonMaksTilsynsbeløpGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksTilsyn>): List<GrunnlagDto> = sjablonListe
//        // TODO Sjekk om periode.overlapper er dekkende
//        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
//        .map {
//            GrunnlagDto(
//                referanse = lagSjablonReferanse(SjablonNavn.MAKS_TILSYN.navn, it.datoFom!!),
//                type = Grunnlagstype.SJABLON,
//                innhold = POJONode(
//                    SjablonMaksTilsynPeriode(
//                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
//                        antallBarnTom = it.antallBarnTom!!,
//                        maksBeløpTilsyn = it.maksBeløpTilsyn!!,
//                    ),
//                ),
//            )
//        }
//
//    private fun mapSjablonMaksFradragGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksFradrag>): List<GrunnlagDto> = sjablonListe
//        // TODO Sjekk om periode.overlapper er dekkende
//        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
//        .map {
//            GrunnlagDto(
//                referanse = lagSjablonReferanse(SjablonNavn.MAKS_FRADRAG.navn, it.datoFom!!),
//                type = Grunnlagstype.SJABLON,
//                innhold = POJONode(
//                    SjablonMaksFradragPeriode(
//                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
//                        antallBarnTom = it.antallBarnTom!!,
//                        maksBeløpFradrag = it.maksBeløpFradrag!!,
//                    ),
//                ),
//            )
//        }

    private fun finnAlderBarn(beregnGrunnlag: List<GrunnlagDto>, referanseBarn: Grunnlagsreferanse, dato: LocalDate): Int {
        val fødselsdatoBarn = finnFødselsdatoBarn(beregnGrunnlag, referanseBarn)
        return Period.between(
            fødselsdatoBarn,
            dato,
        ).years
    }
}
