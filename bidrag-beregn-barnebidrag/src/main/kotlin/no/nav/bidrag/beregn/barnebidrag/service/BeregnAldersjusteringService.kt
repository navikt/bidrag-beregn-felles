package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndeligBidragBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.SamværsfradragBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.UnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.bo.AldersjusteringBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilsynMedStønad
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetrygdType
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.DeltBostedBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningAldersjusteringGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregningAldersjusteringResultat
import no.nav.bidrag.beregn.barnebidrag.bo.KopiBpAndelUnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.NettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsklasseBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonBarnetilsynBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonForbruksutgifterBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSamværsfradragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadBeregningResultat
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.AldersjusteringMapper.mapAldersjusteringGrunnlagFraVedtak
import no.nav.bidrag.beregn.barnebidrag.mapper.AldersjusteringMapper.mapSøknadsbarnGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.exception.AldersjusteringLavereEnnEllerLikLøpendeBidragException
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagAldersjustering
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagVedtak
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiBarnetilsynMedStønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiDelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiDelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.KopiSamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidragAldersjustering
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import java.time.YearMonth
import java.util.Collections.emptyList

class BeregnAldersjusteringService : BeregnService() {

    // Beregning av aldersjustering barnebidrag
    fun beregnAldersjusteringBarnebidrag(mottattGrunnlag: BeregnGrunnlagAldersjustering): BeregnetBarnebidragResultat {
        secureLogger.debug { "Beregning av aldersjustering barnebidrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av aldersjustering barnebidrag: " + e.message)
        }

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(periode = mottattGrunnlag.periode) { it.aldersjustering }

        val søknadsbarnGrunnlagListe = mapSøknadsbarnGrunnlag(mottattGrunnlag.personObjektListe)
        val bidragsmottakerReferanse = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.personObjektListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )
        val bidragspliktigReferanse = finnReferanseTilRolle(
            grunnlagListe = mottattGrunnlag.personObjektListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )

        var beregnetBarnebidragResultat = BeregnetBarnebidragResultat(emptyList(), emptyList())

        // Utfører aldersjustering for hvert søknadsbarn. Forutsetter at kontroll på om barnet skal aldersjusteres er gjort på utsiden og at alle
        // kriterier er oppfylt. Siste manuelle vedtak (som aldersjusteringen skal bygge på) skal være vedlagt i grunnlaget. Hvis det ikke finnes
        // vedtak eller det er mer enn ett vedtak for det aktuelle søknadsbarnet kastes exception.
        søknadsbarnGrunnlagListe.forEach { søknadsbarn ->

            // Henter ut vedtak fra innsendt grunnlag
            val vedtak = hentVedtakFraGrunnlag(mottattGrunnlag, søknadsbarn.referanse)
            val beløpshistorikkGrunnlagListe = mottattGrunnlag.beløpshistorikkListe.filter { it.gjelderBarnReferanse == søknadsbarn.referanse }

            // Mapper ut data som skal brukes i beregningene
            val aldersjusteringGrunnlag = mapAldersjusteringGrunnlagFraVedtak(
                beregningsperiode = mottattGrunnlag.periode,
                grunnlagsperiode = ÅrMånedsperiode(
                    fom = mottattGrunnlag.periode.til!!.minusMonths(2),
                    til = mottattGrunnlag.periode.til!!.minusMonths(1),
                ),
                søknadsbarn = søknadsbarn,
                bidragsmottakerReferanse = bidragsmottakerReferanse,
                bidragspliktigReferanse = bidragspliktigReferanse,
                vedtak = vedtak,
                sjablonGrunnlagListe = sjablonGrunnlag,
                beløpshistorikkGrunnlagListe = mottattGrunnlag.beløpshistorikkListe,
            )

            // Oppretter kopi-objekter av objekter fra siste manuelle vedtak som inneholder data som skal brukes i beregningene
            val grunnlagFraVedtakListe = opprettKopiObjekter(aldersjusteringGrunnlag = aldersjusteringGrunnlag, mottattGrunnlag = mottattGrunnlag)

            // Beregner ny underholdskostnad og mapper ut resultat + grunnlag
            val underholdskostnadBeregningResultat = beregnNyUnderholdskostnad(
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
            )
            val underholdskostnadResultatGrunnlagListe = mapUnderholdskostnadResultatGrunnlagListe(
                underholdskostnadBeregningResultat = underholdskostnadBeregningResultat,
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
                sjablonGrunnlag = sjablonGrunnlag,
            )

            // Beregner nytt samværsfradrag og mapper ut resultat + grunnlag
            val samværsfradragBeregningResultat = beregnNyttSamværsfradrag(
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
            )
            val samværsfradragResultatGrunnlagListe = mapSamværsfradragResultatGrunnlagListe(
                samværsfradragBeregningResultat = samværsfradragBeregningResultat,
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
                sjablonGrunnlag = sjablonGrunnlag,
            )

            // Beregner nytt endelig bidrag (sluttberegning) og mapper ut resultat + grunnlag
            val endeligBidragBeregningResultat = beregnNyttEndeligBidrag(
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
                underholdskostnadBeregningResultat = underholdskostnadBeregningResultat,
                underholdskostnadResultatGrunnlagListe = underholdskostnadResultatGrunnlagListe,
                samværsfradragBeregningResultat = samværsfradragBeregningResultat,
                samværsfradragResultatGrunnlagListe = samværsfradragResultatGrunnlagListe,
            )
            val endeligBidragResultatGrunnlagListe = mapEndeligBidragResultatGrunnlagListe(
                endeligBidragBeregningResultat = endeligBidragBeregningResultat,
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                grunnlagFraVedtakListe = grunnlagFraVedtakListe,
                grunnlagFraDelberegningerListe = underholdskostnadResultatGrunnlagListe + samværsfradragResultatGrunnlagListe,
                sjablonGrunnlag = sjablonGrunnlag,
                personobjektGrunnlagListe = mottattGrunnlag.personObjektListe,
            )

            // Lager resultatperioder. Det vil bare være en resultatperiode, med åpen til-dato
            val resultatPeriodeListe = lagResultatPerioder(
                delberegningEndeligBidragResultat = endeligBidragResultatGrunnlagListe,
                beløpshistorikkReferanse = aldersjusteringGrunnlag.beløpshistorikk?.referanse,
            )
            beregnetBarnebidragResultat = BeregnetBarnebidragResultat(
                beregnetBarnebidragPeriodeListe = resultatPeriodeListe,
                grunnlagListe = (
                    grunnlagFraVedtakListe + underholdskostnadResultatGrunnlagListe + samværsfradragResultatGrunnlagListe +
                        endeligBidragResultatGrunnlagListe + beløpshistorikkGrunnlagListe
                    ).distinct().sortedBy { it.referanse },
            )

            // Sjekker om beregnet beløp er lavere enn løpende beløp fra beløpshistorikken. I så fall kastes exception.
            if (erAldersjustertBeløpLavereEnnEllerLikLøpendeBeløp(
                    beregnetBarnebidragResultat = beregnetBarnebidragResultat.beregnetBarnebidragPeriodeListe.first(),
                    beløpshistorikk = aldersjusteringGrunnlag.beløpshistorikk,
                    beregningsperiode = aldersjusteringGrunnlag.beregningsperiode,
                )
            ) {
                throw AldersjusteringLavereEnnEllerLikLøpendeBidragException(
                    melding = "Alderjustert beløp er lavere enn løpende beløp fra beløpshistorikken for søknadsbarn med referanse " +
                        søknadsbarn.referanse,
                    data = beregnetBarnebidragResultat,
                )
            }
        }

        // TODO Håndtere flere søknadsbarn. Må returnere et nytt responsobjekt (liste)

        return beregnetBarnebidragResultat
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode, delberegning: (SjablonTallNavn) -> Boolean): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablontall(), delberegning = delberegning) +
            mapSjablonBarnetilsynGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonBarnetilsyn()) +
            mapSjablonForbruksutgifterGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonForbruksutgifter()) +
            mapSjablonSamværsfradragGrunnlag(periode = periode, sjablonListe = SjablonProvider.hentSjablonSamværsfradrag())

    // Henter vedtak for et søknadsbarn fra grunnlag. Hvis det ikke finnes noe vedtak eller det er mer enn ett vedtak kastes exception.
    private fun hentVedtakFraGrunnlag(mottattGrunnlag: BeregnGrunnlagAldersjustering, søknadsbarnReferanse: String): BeregnGrunnlagVedtak {
        val vedtakListe = mottattGrunnlag.vedtakListe.filter { it.gjelderBarnReferanse == søknadsbarnReferanse }
        when {
            vedtakListe.isEmpty() -> {
                throw UgyldigInputException("Aldersjustering: Ingen vedtak funnet for søknadsbarn med referanse $søknadsbarnReferanse")
            }

            vedtakListe.size > 1 -> {
                throw UgyldigInputException("Aldersjustering: Flere vedtak funnet for søknadsbarn med referanse $søknadsbarnReferanse")
            }

            else -> return vedtakListe.first()
        }
    }

    // Beregner ny underholdskostnad
    private fun beregnNyUnderholdskostnad(
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
    ): UnderholdskostnadBeregningResultat {
        val tilsynskode = bestemTilsynskode(tilsynstype = aldersjusteringGrunnlag.tilsynstype, skolealder = aldersjusteringGrunnlag.skolealder)
        val beregnetAlder = finnBarnetsAlder(
            fødselsdato = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato,
            årMåned = aldersjusteringGrunnlag.beregningsperiode.fom,
        )
        val alderTomListe = hentAlderTomListeForbruksutgifter(aldersjusteringGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe)
        val alderTom = alderTomListe.firstOrNull { beregnetAlder <= it } ?: alderTomListe.last()

        // Lager grunnlag til beregning av underholdskostnad
        val grunnlag = UnderholdskostnadBeregningGrunnlag(
            søknadsbarn = SøknadsbarnBeregningGrunnlag(
                referanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                alder = aldersjusteringGrunnlag.søknadsbarnAlder,
            ),
            barnetilsynMedStønad =
                if (aldersjusteringGrunnlag.tilsynstype == null || aldersjusteringGrunnlag.skolealder == null) {
                    null
                } else {
                    BarnetilsynMedStønad(
                        referanse = grunnlagFraVedtakListe
                            .filtrerOgKonverterBasertPåEgenReferanse<KopiBarnetilsynMedStønadPeriode>(Grunnlagstype.KOPI_BARNETILSYN_MED_STØNAD_PERIODE)
                            .map { it.referanse }
                            .firstOrNull() ?: "",
                        tilsynstype = aldersjusteringGrunnlag.tilsynstype,
                        skolealder = aldersjusteringGrunnlag.skolealder,
                    )
                },
            nettoTilsynsutgiftBeregningGrunnlag =
                if (aldersjusteringGrunnlag.nettoTilsynsutgift == null) {
                    null
                } else {
                    NettoTilsynsutgift(
                        referanse = grunnlagFraVedtakListe
                            .filtrerOgKonverterBasertPåEgenReferanse<KopiDelberegningUnderholdskostnad>(Grunnlagstype.KOPI_DELBEREGNING_UNDERHOLDSKOSTNAD)
                            .map { it.referanse }
                            .firstOrNull() ?: "",
                        nettoTilsynsutgift = aldersjusteringGrunnlag.nettoTilsynsutgift,
                    )
                },
            sjablonSjablontallBeregningGrunnlagListe = aldersjusteringGrunnlag.sjablonSjablontallPeriodeGrunnlagListe
                .filter { it.sjablonSjablontallPeriode.periode.inneholder(aldersjusteringGrunnlag.beregningsperiode) }
                .map {
                    SjablonSjablontallBeregningGrunnlag(
                        referanse = it.referanse,
                        type = it.sjablonSjablontallPeriode.sjablon.navn,
                        verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                    )
                }.toMutableList(),
            sjablonBarnetilsynBeregningGrunnlag =
                if (tilsynskode != null) {
                    aldersjusteringGrunnlag.sjablonBarnetilsynPeriodeGrunnlagListe
                        .asSequence()
                        .filter { it.sjablonBarnetilsynPeriode.periode.inneholder(aldersjusteringGrunnlag.beregningsperiode) }
                        .filter { it.sjablonBarnetilsynPeriode.typeStønad == "64" }
                        .filter { it.sjablonBarnetilsynPeriode.typeTilsyn == tilsynskode }
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
            sjablonForbruksutgifterBeregningGrunnlag = aldersjusteringGrunnlag.sjablonForbruksutgifterPeriodeGrunnlagListe
                .filter { it.sjablonForbruksutgifterPeriode.periode.inneholder(aldersjusteringGrunnlag.beregningsperiode) }
                .filter { it.sjablonForbruksutgifterPeriode.alderTom == alderTom }
                .map {
                    SjablonForbruksutgifterBeregningGrunnlag(
                        referanse = it.referanse,
                        alderTom = it.sjablonForbruksutgifterPeriode.alderTom,
                        beløpForbrukTotalt = it.sjablonForbruksutgifterPeriode.beløpForbruk,
                    )
                }.first(),
        )

        // Beregner ny underholdskostnad
        val underholdskostnadBeregningResultat = UnderholdskostnadBeregning.beregn(
            grunnlag = grunnlag,
            barnetrygdType = bestemBarnetrygdType(
                aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                beregnetAlder = beregnetAlder,
            ),
        )

        return underholdskostnadBeregningResultat
    }

    // Mapper ut grunnlag for underholdskostnad
    private fun mapUnderholdskostnadResultatGrunnlagListe(
        underholdskostnadBeregningResultat: UnderholdskostnadBeregningResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): List<GrunnlagDto> {
        // Mapper ut grunnlag som er brukt i underholdskostnadberegningen (mottatte grunnlag og sjabloner)
        val underholdskostnadResultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = underholdskostnadBeregningResultat.grunnlagsreferanseListe.distinct(),
            mottattGrunnlag = grunnlagFraVedtakListe,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning underholdskostnad
        underholdskostnadResultatGrunnlagListe.addAll(
            listOf(
                mapDelberegningUnderholdskostnad(
                    underholdskostnadBeregningResultat = underholdskostnadBeregningResultat,
                    aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                ),
            ),
        )

        return underholdskostnadResultatGrunnlagListe
    }

    // Mapper ut DelberegningUnderholdskostnad
    private fun mapDelberegningUnderholdskostnad(
        underholdskostnadBeregningResultat: UnderholdskostnadBeregningResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
    ): GrunnlagDto = GrunnlagDto(
        referanse = opprettDelberegningreferanse(
            type = Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD,
            periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
            søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
            gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
        ),
        type = Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD,
        innhold = POJONode(
            DelberegningUnderholdskostnad(
                periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                forbruksutgift = underholdskostnadBeregningResultat.forbruksutgift,
                boutgift = underholdskostnadBeregningResultat.boutgift,
                barnetilsynMedStønad = underholdskostnadBeregningResultat.barnetilsynMedStønad,
                nettoTilsynsutgift = underholdskostnadBeregningResultat.nettoTilsynsutgift,
                barnetrygd = underholdskostnadBeregningResultat.barnetrygd,
                underholdskostnad = underholdskostnadBeregningResultat.underholdskostnad,
            ),
        ),
        grunnlagsreferanseListe = underholdskostnadBeregningResultat.grunnlagsreferanseListe.distinct().sorted(),
        gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
        gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
    )

    // Beregner nytt samværsfradrag
    private fun beregnNyttSamværsfradrag(
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
    ): SamværsfradragBeregningResultat {
        val grunnlag = SamværsfradragBeregningGrunnlag(
            søknadsbarn = SøknadsbarnBeregningGrunnlag(
                referanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                alder = aldersjusteringGrunnlag.søknadsbarnAlder,
            ),
            samværsklasseBeregningGrunnlag = SamværsklasseBeregningGrunnlag(
                referanse = grunnlagFraVedtakListe
                    .filtrerOgKonverterBasertPåEgenReferanse<KopiSamværsperiodeGrunnlag>(Grunnlagstype.KOPI_SAMVÆRSPERIODE)
                    .map { it.referanse }
                    .firstOrNull() ?: "",
                samværsklasse = aldersjusteringGrunnlag.samværsklasse,
            ),
            sjablonSamværsfradragBeregningGrunnlagListe = aldersjusteringGrunnlag.sjablonSamværsfradragPeriodeGrunnlagListe
                .filter { it.sjablonSamværsfradragPeriode.periode.inneholder(aldersjusteringGrunnlag.beregningsperiode) }
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

        return SamværsfradragBeregning.beregn(grunnlag)
    }

    // Mapper ut grunnlag for samværsfradrag
    private fun mapSamværsfradragResultatGrunnlagListe(
        samværsfradragBeregningResultat: SamværsfradragBeregningResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
        sjablonGrunnlag: List<GrunnlagDto>,
    ): List<GrunnlagDto> {
        // Mapper ut grunnlag som er brukt i samværsfradragberegningen (mottatte grunnlag og sjabloner)
        val samværsfradragResultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = samværsfradragBeregningResultat.grunnlagsreferanseListe.distinct(),
            mottattGrunnlag = grunnlagFraVedtakListe,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning samværsfradrag
        samværsfradragResultatGrunnlagListe.addAll(
            listOf(
                mapDelberegningSamværsfradrag(
                    samværsfradragBeregningResultat = samværsfradragBeregningResultat,
                    aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                ),
            ),
        )

        return samværsfradragResultatGrunnlagListe
    }

    // Mapper ut DelberegningSamværsfradrag
    private fun mapDelberegningSamværsfradrag(
        samværsfradragBeregningResultat: SamværsfradragBeregningResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
    ): GrunnlagDto = GrunnlagDto(
        referanse = opprettDelberegningreferanse(
            type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
            periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
            søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
            gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
        ),
        type = Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG,
        innhold = POJONode(
            DelberegningSamværsfradrag(
                periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                beløp = samværsfradragBeregningResultat.beløpFradrag,
            ),
        ),
        grunnlagsreferanseListe = samværsfradragBeregningResultat.grunnlagsreferanseListe.distinct().sorted(),
        gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
        gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
    )

    // Beregner nytt endelig bidrag (sluttberegning)
    private fun beregnNyttEndeligBidrag(
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
        underholdskostnadBeregningResultat: UnderholdskostnadBeregningResultat,
        underholdskostnadResultatGrunnlagListe: List<GrunnlagDto>,
        samværsfradragBeregningResultat: SamværsfradragBeregningResultat,
        samværsfradragResultatGrunnlagListe: List<GrunnlagDto>,
    ): EndeligBidragBeregningAldersjusteringResultat {
        // Lager grunnlag til beregning av underholdskostnad
        val grunnlag = EndeligBidragBeregningAldersjusteringGrunnlag(
            underholdskostnad = UnderholdskostnadDelberegningBeregningGrunnlag(
                referanse = underholdskostnadResultatGrunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
                    .map { it.referanse }
                    .firstOrNull() ?: "",
                beløp = underholdskostnadBeregningResultat.underholdskostnad,
            ),
            bpAndelFaktor = KopiBpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
                referanse = grunnlagFraVedtakListe
                    .filtrerOgKonverterBasertPåEgenReferanse<KopiDelberegningBidragspliktigesAndel>(
                        Grunnlagstype.KOPI_DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                    )
                    .map { it.referanse }
                    .firstOrNull() ?: "",
                andelFaktor = aldersjusteringGrunnlag.bpAndelFaktor,
            ),
            samværsfradrag = SamværsfradragDelberegningBeregningGrunnlag(
                referanse = samværsfradragResultatGrunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<DelberegningSamværsfradrag>(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG)
                    .map { it.referanse }
                    .firstOrNull() ?: "",
                beløp = samværsfradragBeregningResultat.beløpFradrag,
            ),
            deltBosted = DeltBostedBeregningGrunnlag(
                referanse = grunnlagFraVedtakListe
                    .filtrerOgKonverterBasertPåEgenReferanse<KopiSamværsperiodeGrunnlag>(Grunnlagstype.KOPI_SAMVÆRSPERIODE)
                    .map { it.referanse }
                    .firstOrNull() ?: "",
                deltBosted = aldersjusteringGrunnlag.samværsklasse == Samværsklasse.DELT_BOSTED,
            )
        )

        return EndeligBidragBeregning.beregnAldersjustering(grunnlag)
    }

    // Mapper ut grunnlag for endelig bidrag
    private fun mapEndeligBidragResultatGrunnlagListe(
        endeligBidragBeregningResultat: EndeligBidragBeregningAldersjusteringResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        grunnlagFraVedtakListe: List<GrunnlagDto>,
        grunnlagFraDelberegningerListe: List<GrunnlagDto>,
        sjablonGrunnlag: List<GrunnlagDto>,
        personobjektGrunnlagListe: List<GrunnlagDto>
    ): List<GrunnlagDto> {
        // Mapper ut grunnlag som er brukt i endelig bidrag beregningen (mottatte grunnlag og sjabloner)
        val endeligBidragResultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = endeligBidragBeregningResultat.grunnlagsreferanseListe.distinct(),
            mottattGrunnlag = grunnlagFraVedtakListe + grunnlagFraDelberegningerListe,
            sjablonGrunnlag = sjablonGrunnlag,
        )

        // Mapper ut grunnlag for delberegning endelig bidrag
        endeligBidragResultatGrunnlagListe.addAll(
            listOf(
                mapDelberegningEndeligBidrag(
                    endeligBidragBeregningResultat = endeligBidragBeregningResultat,
                    aldersjusteringGrunnlag = aldersjusteringGrunnlag,
                ),
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        endeligBidragResultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = endeligBidragResultatGrunnlagListe,
                personobjektGrunnlagListe = personobjektGrunnlagListe
            )
        )

        return endeligBidragResultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    // Mapper ut DelberegningEndeligBidrag
    private fun mapDelberegningEndeligBidrag(
        endeligBidragBeregningResultat: EndeligBidragBeregningAldersjusteringResultat,
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
    ): GrunnlagDto = GrunnlagDto(
        referanse = opprettSluttberegningreferanse(
            barnreferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
            periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
        ),
        type = Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG_ALDERSJUSTERING,
        innhold = POJONode(
            SluttberegningBarnebidragAldersjustering(
                periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                beregnetBeløp = endeligBidragBeregningResultat.beregnetBeløp,
                resultatBeløp = endeligBidragBeregningResultat.resultatBeløp,
                bpAndelBeløp = endeligBidragBeregningResultat.bpAndelBeløp,
                bpAndelFaktorVedDeltBosted = endeligBidragBeregningResultat.bpAndelFaktorVedDeltBosted,
                deltBosted = endeligBidragBeregningResultat.deltBosted,
            ),
        ),
        grunnlagsreferanseListe = endeligBidragBeregningResultat.grunnlagsreferanseListe.distinct().sorted(),
        gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnReferanse,
    )

    // Oppretter kopiobjekter for grunnlag fra vedtak
    private fun opprettKopiObjekter(
        aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag,
        mottattGrunnlag: BeregnGrunnlagAldersjustering,
    ): List<GrunnlagDto> {
        val grunnlagListe = mutableListOf<GrunnlagDto>()

        grunnlagListe.add(
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.KOPI_DELBEREGNING_UNDERHOLDSKOSTNAD,
                    periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                    søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
                ),
                type = Grunnlagstype.KOPI_DELBEREGNING_UNDERHOLDSKOSTNAD,
                innhold = POJONode(
                    KopiDelberegningUnderholdskostnad(
                        periode = aldersjusteringGrunnlag.beregningsperiode,
                        fraVedtakId = aldersjusteringGrunnlag.vedtakId,
                        nettoTilsynsutgift = aldersjusteringGrunnlag.nettoTilsynsutgift,
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
                gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
            ),
        )

        grunnlagListe.add(
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.KOPI_DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                    periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                    søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
                ),
                type = Grunnlagstype.KOPI_DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                innhold = POJONode(
                    KopiDelberegningBidragspliktigesAndel(
                        periode = mottattGrunnlag.periode,
                        fraVedtakId = aldersjusteringGrunnlag.vedtakId,
                        endeligAndelFaktor = aldersjusteringGrunnlag.bpAndelFaktor,
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
                gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
            ),
        )

        if (aldersjusteringGrunnlag.tilsynstype != null && aldersjusteringGrunnlag.skolealder != null) {
            grunnlagListe.add(
                GrunnlagDto(
                    referanse = opprettDelberegningreferanse(
                        type = Grunnlagstype.KOPI_BARNETILSYN_MED_STØNAD_PERIODE,
                        periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                        søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                        gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
                    ),
                    type = Grunnlagstype.KOPI_BARNETILSYN_MED_STØNAD_PERIODE,
                    innhold = POJONode(
                        KopiBarnetilsynMedStønadPeriode(
                            periode = mottattGrunnlag.periode,
                            fraVedtakId = aldersjusteringGrunnlag.vedtakId,
                            tilsynstype = aldersjusteringGrunnlag.tilsynstype,
                            skolealder = aldersjusteringGrunnlag.skolealder,
                            manueltRegistrert = aldersjusteringGrunnlag.barnetilsynMedStønadManueltRegistrert ?: false,
                        ),
                    ),
                    grunnlagsreferanseListe = emptyList(),
                    gjelderReferanse = aldersjusteringGrunnlag.bidragsmottakerReferanse,
                    gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                ),
            )
        }

        grunnlagListe.add(
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.KOPI_SAMVÆRSPERIODE,
                    periode = ÅrMånedsperiode(fom = aldersjusteringGrunnlag.beregningsperiode.fom, til = null),
                    søknadsbarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
                    gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
                ),
                type = Grunnlagstype.KOPI_SAMVÆRSPERIODE,
                innhold = POJONode(
                    KopiSamværsperiodeGrunnlag(
                        periode = mottattGrunnlag.periode,
                        fraVedtakId = aldersjusteringGrunnlag.vedtakId,
                        samværsklasse = aldersjusteringGrunnlag.samværsklasse,
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = aldersjusteringGrunnlag.bidragspliktigReferanse,
                gjelderBarnReferanse = aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.referanse,
            ),
        )

        return grunnlagListe
    }

    // Bestemmer barnetrygd-type
    private fun bestemBarnetrygdType(aldersjusteringGrunnlag: AldersjusteringBeregningGrunnlag, beregnetAlder: Int): BarnetrygdType {
        val søknadsbarnFødselsdatoÅrMåned =
            YearMonth.of(
                aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.year,
                aldersjusteringGrunnlag.søknadsbarnPeriodeGrunnlag.fødselsdato.month,
            )
        val søknadsbarnSeksårsdag = søknadsbarnFødselsdatoÅrMåned.withMonth(7).plusYears(6)
        val datoInnføringForhøyetBarnetrygd = YearMonth.of(2021, 7)
        val beregningsperiodeFom = aldersjusteringGrunnlag.beregningsperiode.fom

        return when {
            beregnetAlder >= 18 -> BarnetrygdType.INGEN
            beregningsperiodeFom == søknadsbarnFødselsdatoÅrMåned -> BarnetrygdType.INGEN
            beregningsperiodeFom.isBefore(datoInnføringForhøyetBarnetrygd) -> BarnetrygdType.ORDINÆR
            beregningsperiodeFom.isBefore(søknadsbarnSeksårsdag) -> BarnetrygdType.FORHØYET
            else -> BarnetrygdType.ORDINÆR
        }
    }

    // Standardlogikk for å lage resultatperioder
    private fun lagResultatPerioder(delberegningEndeligBidragResultat: List<GrunnlagDto>, beløpshistorikkReferanse: String?): List<ResultatPeriode> =
        delberegningEndeligBidragResultat
            .filtrerOgKonverterBasertPåEgenReferanse<SluttberegningBarnebidragAldersjustering>(
                Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG_ALDERSJUSTERING,
            )
            .map {
                ResultatPeriode(
                    periode = it.innhold.periode,
                    resultat = ResultatBeregning(
                        beløp = it.innhold.resultatBeløp,
                    ),
                    grunnlagsreferanseListe = listOfNotNull(it.referanse, beløpshistorikkReferanse),
                )
            }

    // Sjekker om aldersjustert beløp er lavere enn løpende beløp fra beløpshistorikken
    private fun erAldersjustertBeløpLavereEnnEllerLikLøpendeBeløp(
        beregnetBarnebidragResultat: ResultatPeriode,
        beløpshistorikk: BeløpshistorikkPeriodeGrunnlag?,
        beregningsperiode: ÅrMånedsperiode,
    ): Boolean {
        val aldersjustertBeløp = beregnetBarnebidragResultat.resultat.beløp
        val beløpshistorikkBeløp = beløpshistorikk?.beløpshistorikkPeriode?.beløpshistorikk
            ?.firstOrNull { it.periode.inneholder(beregningsperiode) }?.beløp

        return beløpshistorikkBeløp != null && aldersjustertBeløp != null && aldersjustertBeløp <= beløpshistorikkBeløp
    }
}
