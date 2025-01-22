package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoTilsynsutgiftBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.BarnBM
import no.nav.bidrag.beregn.barnebidrag.bo.FaktiskUtgift
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgiftPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksFradragsbeløpBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonMaksTilsynsbeløpBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Tilleggsstønad
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper.beregnAntallBarnBM
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
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningFaktiskTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningNettoTilsynsutgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningTilleggsstønad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

internal object BeregnNettoTilsynsutgiftService : BeregnService() {

    fun delberegningNettoTilsynsutgift(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
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
            // Teller antall barn i perioden. Hvis antall er null så gjøres det ingen beregning
            val barnMedUtgifterIPerioden = nettoTilsynsutgiftPeriodeGrunnlag.faktiskUtgiftPeriodeCoreListe
                .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                .distinctBy { it.gjelderBarn }
            if (barnMedUtgifterIPerioden.isNotEmpty()) {
                val nettoTilsynsutgiftBeregningGrunnlag =
                    lagNettoTilsynsutgiftBeregningGrunnlag(
                        nettoTilsynsutgiftPeriodeGrunnlag,
                        bruddPeriode,
                        barnMedUtgifterIPerioden,
                    )
                nettoTilsynsutgiftBeregningResultatListe.add(
                    NettoTilsynsutgiftPeriodeResultat(
                        periode = bruddPeriode,
                        resultat = NettoTilsynsutgiftBeregning.beregn(nettoTilsynsutgiftBeregningGrunnlag),
                    ),
                )
            }
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true. Gjøres ikke hvis tildato er før
        // tildato på beregningsperioden. Delberegning netto tilsynsutgifter kan gjelde deler av beregningsperioden.
        if (nettoTilsynsutgiftBeregningResultatListe.isNotEmpty()) {
            val sisteElement = nettoTilsynsutgiftBeregningResultatListe.last()
            if (sisteElement.periode.til != null && sisteElement.periode.til!! == mottattGrunnlag.periode.til && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                nettoTilsynsutgiftBeregningResultatListe[nettoTilsynsutgiftBeregningResultatListe.size - 1] = oppdatertSisteElement
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
        // Barn som er født etter beregningsperiodens start skal ikke regnes med i antall barn under 12 år, det må lages en bruddperiode i disse
        // barnenes fødselsmåned
        val fødselsmånederForBarnFødtEtterberegningsperiodeFra = grunnlagListe.barnBMListe
            .filter {
                it.fødselsdato.withDayOfMonth(1).isAfter(LocalDate.of(beregningsperiode.fom.year, beregningsperiode.fom.month, 1).minusDays(1))
            }
            .map { ÅrMånedsperiode(YearMonth.from(it.fødselsdato), YearMonth.from(it.fødselsdato)) }
            .distinct()

        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.faktiskUtgiftPeriodeCoreListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.tilleggsstønadPeriodeCoreListe.asSequence().map { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil) })
            .plus(grunnlagListe.sjablonMaksTilsynsbeløpPeriodeGrunnlagListe.asSequence().map { it.sjablonMaksTilsynsbeløpPeriode.periode })
            .plus(grunnlagListe.sjablonMaksFradragsbeløpPeriodeGrunnlagListe.asSequence().map { it.sjablonMaksFradragsbeløpPeriode.periode })
            .plus(fødselsmånederForBarnFødtEtterberegningsperiodeFra.asSequence())

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for nettoTilsynsutgiftberegning som ligger innenfor bruddPeriode
    private fun lagNettoTilsynsutgiftBeregningGrunnlag(
        grunnlag: NettoTilsynsutgiftPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
        barnMedUtgifterIPerioden: List<FaktiskUtgiftPeriodeCore>,
    ): NettoTilsynsutgiftBeregningGrunnlag {
        val barnBMListeUnderTolvÅr = barnUnderTolvÅr(grunnlag.barnBMListe, bruddPeriode.fom)
        val barnMedUtgifterReferanser = barnMedUtgifterIPerioden.map { it.gjelderBarn }
        val antallBarnBMBeregnet = beregnAntallBarnBM(barnBMListeUnderTolvÅr, barnMedUtgifterReferanser)

        val respons = NettoTilsynsutgiftBeregningGrunnlag(
            søknadsbarnReferanse = grunnlag.søknadsbarnReferanse,
            barnBMListe = grunnlag.barnBMListe,
            antallBarnBMBeregnet = antallBarnBMBeregnet,
            antallBarnMedTilsynsutgifter = barnMedUtgifterReferanser.size,
            barnBMListeUnderTolvÅr = barnBMListeUnderTolvÅr,
            faktiskUtgiftListe = grunnlag.faktiskUtgiftPeriodeCoreListe
                .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                .map {
                    FaktiskUtgift(
                        referanse = it.referanse,
                        gjelderBarn = it.gjelderBarn,
                        beregnetMånedsbeløp = it.beregnetBeløp,
                    )
                }.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Ingen faktisk utgift funnet for periode $bruddPeriode"),

            tilleggsstønad = grunnlag.tilleggsstønadPeriodeCoreListe
                .filter { ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil).inneholder(bruddPeriode) }
                .map {
                    Tilleggsstønad(
                        referanse = it.referanse,
                        gjelderBarn = it.gjelderBarn,
                        beregnetMånedsbeløp = it.beregnetBeløp,
                    )
                }.firstOrNull(),

            sjablonSjablontallBeregningGrunnlagListe = grunnlag.sjablonSjablontallPeriodeGrunnlagListe
                .filter { it.sjablonSjablontallPeriode.periode.inneholder(bruddPeriode) }
                .map {
                    SjablonSjablontallBeregningGrunnlag(
                        referanse = it.referanse,
                        type = it.sjablonSjablontallPeriode.sjablon.navn,
                        verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                    )
                },

            sjablonMaksTilsynsbeløpBeregningGrunnlag = grunnlag.sjablonMaksTilsynsbeløpPeriodeGrunnlagListe
                .asSequence()
                .filter { it.sjablonMaksTilsynsbeløpPeriode.periode.inneholder(bruddPeriode) }
                .sortedBy { it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom }
                .filter { it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom >= antallBarnBMBeregnet }
                .map {
                    SjablonMaksTilsynsbeløpBeregningGrunnlag(
                        referanse = it.referanse,
                        antallBarnTom = it.sjablonMaksTilsynsbeløpPeriode.antallBarnTom,
                        maxBeløpTilsyn = it.sjablonMaksTilsynsbeløpPeriode.maksBeløpTilsyn,
                    )
                }.first(),

            sjablonMaksFradragsbeløpBeregningGrunnlag = grunnlag.sjablonMaksFradragsbeløpPeriodeGrunnlagListe
                .asSequence()
                .filter { it.sjablonMaksFradragsbeløpPeriode.periode.inneholder(bruddPeriode) }
                .sortedBy { it.sjablonMaksFradragsbeløpPeriode.antallBarnTom }
                .filter { it.sjablonMaksFradragsbeløpPeriode.antallBarnTom >= antallBarnBMBeregnet }
                .map {
                    SjablonMaksFradragsbeløpBeregningGrunnlag(
                        referanse = it.referanse,
                        antallBarnTom = it.sjablonMaksFradragsbeløpPeriode.antallBarnTom,
                        maxBeløpFradrag = it.sjablonMaksFradragsbeløpPeriode.maksBeløpFradrag,
                    )
                }.first(),
        )
        return respons
    }

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
                gjelderBarnReferanse = it.gjelderBarnReferanse,
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
                        totalTilsynsutgift = it.resultat.totalTilsynsutgift,
                        bruttoTilsynsutgift = it.resultat.bruttoTilsynsutgift,
                        justertBruttoTilsynsutgift = it.resultat.justertBruttoTilsynsutgift,
                        andelTilsynsutgiftFaktor = it.resultat.andelTilsynsutgiftFaktor,
                        skattefradrag = it.resultat.skattefradrag,
                        antallBarnBMUnderTolvÅr = it.resultat.antallBarnBMUnderTolvÅr,
                        antallBarnBMBeregnet = it.resultat.antallBarnBMBeregnet,
                        antallBarnMedTilsynsutgifter = it.resultat.antallBarnMedTilsynsutgifter,
                        nettoTilsynsutgift = it.resultat.nettoTilsynsutgift,
                        tilsynsutgiftBarnListe = it.resultat.tilsynsutgiftBarnListe,
                        erBegrensetAvMaksTilsyn = it.resultat.erBegrensetAvMaksTilsyn,
                        skattefradragMaksfradrag = it.resultat.skattefradragMaksfradrag,
                        skattefradragTotalTilsynsutgift = it.resultat.skattefradragTotalTilsynsutgift,
                        skattefradragPerBarn = it.resultat.skattefradragPerBarn,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
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
                        gjelderBarnReferanse = it.gjelderBarnReferanse,
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
                    type = Grunnlagstype.DELBEREGNING_FAKTISK_UTGIFT,
                    innhold = POJONode(
                        DelberegningFaktiskTilsynsutgift(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            beregnetBeløp = it.beregnetBeløp,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = bidragsmottakerReferanse,
                    gjelderBarnReferanse = it.gjelderBarn,
                )
            }

    // Mapper ut DelberegningTilleggsstønad
    private fun mapDelberegningTilleggsstønad(tilleggsstønadPeriodeCoreListe: List<TilleggsstønadPeriodeCore>, bidragsmottakerReferanse: String) =
        tilleggsstønadPeriodeCoreListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = Grunnlagstype.DELBEREGNING_TILLEGGSSTØNAD,
                    innhold = POJONode(
                        DelberegningTilleggsstønad(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            beregnetBeløp = it.beregnetBeløp,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = bidragsmottakerReferanse,
                    gjelderBarnReferanse = it.gjelderBarn,
                )
            }

    fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
        try {
            return sjablonGrunnlag
                .filter { it.type == Grunnlagstype.SJABLON_SJABLONTALL }
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

    private fun barnUnderTolvÅr(barnBMListe: List<BarnBM>, fom: YearMonth): List<BarnBM> = barnBMListe
        // Filtrer først bort barn som er født etter periodens start
        .filter { YearMonth.from(it.fødselsdato).isBefore(fom.plusMonths(1)) }
        .filter {
            it.fødselsdato.let { fødselsdato ->
                Period.between(
                    fødselsdato.withMonth(7).withDayOfMonth(1),
                    LocalDate.of(fom.year, fom.month, 1),
                ).years < 12
            }
        }
}
