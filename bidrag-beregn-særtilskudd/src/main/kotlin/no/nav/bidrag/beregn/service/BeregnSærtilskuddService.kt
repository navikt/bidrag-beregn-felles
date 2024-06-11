package no.nav.bidrag.beregn.service

import com.fasterxml.jackson.databind.JsonNode
import no.nav.bidrag.beregn.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.bidragsevne.BidragsevneCoreMapper
import no.nav.bidrag.beregn.bpsandelsaertilskudd.BPsAndelSaertilskuddCore
import no.nav.bidrag.beregn.exception.UgyldigInputException
import no.nav.bidrag.beregn.samvaersfradrag.SamvaersfradragCore
import no.nav.bidrag.beregn.service.CoreMapper.Companion.grunnlagTilObjekt
import no.nav.bidrag.beregn.særtilskudd.SaertilskuddCore
import no.nav.bidrag.beregn.særtilskudd.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.web.HttpResponse
import no.nav.bidrag.commons.web.HttpResponse.Companion.from
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.Samvaersklasse
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.SoknadsBarnInfo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.stream.Collectors

@Service
class BeregnSærtilskuddService(
//    private val sjablonConsumer: SjablonConsumer,
    private val bidragsevneCore: BidragsevneCore,
    private val bpAndelSaertilskuddCore: BPsAndelSaertilskuddCore,
    private val samvaersfradragCore: SamvaersfradragCore,
    private val saertilskuddCore: SaertilskuddCore,
) {
    fun beregn(beregnGrunnlag: BeregnGrunnlag): HttpResponse<BeregnetTotalSaertilskuddResultat> {
        // Kontroll av felles inputdata
        beregnGrunnlag.valider()

        // Validerer og henter ut soknadsbarn
        val soknadsBarnInfo = validerSoknadsbarn(beregnGrunnlag)

        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
        }

        // Henter sjabloner
        val sjablonListe = hentSjabloner()

        // Bygger grunnlag til core og utfører delberegninger
        return utfoerDelberegninger(beregnGrunnlag, sjablontallMap, sjablonListe, soknadsBarnInfo.id)
    }

    //  Validerer at det kun er oppgitt ett SoknadsbarnInfo-grunnlag og at mapping til SoknadsBarnInfo objekt ikke feiler
    private fun validerSoknadsbarn(beregnGrunnlag: BeregnGrunnlag): SoknadsBarnInfo {
        val soknadsbarnInfoGrunnlagListe =
            beregnGrunnlag.grunnlagListe
                ?.filter { grunnlag -> grunnlag.type == Grunnlagstype.PERSON_SØKNADSBARN }?.toList()
        if (soknadsbarnInfoGrunnlagListe?.size != 1) {
            throw UgyldigInputException("Det må være nøyaktig ett søknadsbarn i beregningsgrunnlaget")
        }
        val soknadsBarnInfo = grunnlagTilObjekt(soknadsbarnInfoGrunnlagListe[0], SoknadsBarnInfo::class.java)
        beregnGrunnlag.grunnlagListe!!
            .filter { grunnlag -> grunnlag.type == Grunnlagstype.SAMVÆRSKLASSE }
            .map { grunnlag: Grunnlag? ->
                grunnlagTilObjekt(
                    grunnlag!!,
                    Samvaersklasse::class.java,
                )
            }
            .forEach { samvaersklasse: Samvaersklasse ->
                samvaersklasse.valider()
                if (samvaersklasse.soknadsbarnId == soknadsBarnInfo.id && samvaersklasse.soknadsbarnFodselsdato != soknadsBarnInfo.fodselsdato) {
                    throw UgyldigInputException(
                        "Fødselsdato for søknadsbarn stemmer ikke overens med fødselsdato til barnet i Samværsklasse-grunnlaget",
                    )
                }
            }
        return soknadsBarnInfo
    }

    // ==================================================================================================================================================
    // Bygger grunnlag til core og kaller delberegninger
    private fun utfoerDelberegninger(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
        soknadsBarnId: Int,
    ): HttpResponse<BeregnetTotalSaertilskuddResultat> {
        val grunnlagReferanseListe = ArrayList<Grunnlag>()

        // ++ Bidragsevne
        val bidragsevneGrunnlagTilCore =
            BidragsevneCoreMapper.mapBidragsevneGrunnlagTilCore(
                beregnGrunnlag,
                sjablontallMap,
                sjablonListe,
            )
        val bidragsevneResultatFraCore = beregnBidragsevne(bidragsevneGrunnlagTilCore)
        grunnlagReferanseListe.addAll(
            lagGrunnlagListeForDelberegning(
                beregnGrunnlag,
                bidragsevneResultatFraCore.resultatPeriodeListe,
                bidragsevneResultatFraCore.sjablonListe,
            ),
        )

        // ++ BPs andel av særtilskudd
        val bpAndelSaertilskuddGrunnlagTilCore =
            BPAndelSaertilskuddCoreMapper.mapBPsAndelSaertilskuddGrunnlagTilCore(
                beregnGrunnlag,
                sjablontallMap,
                sjablonListe,
            )
        val bpAndelSaertilskuddResultatFraCore = beregnBPAndelSaertilskudd(bpAndelSaertilskuddGrunnlagTilCore)
        grunnlagReferanseListe.addAll(
            lagGrunnlagListeForDelberegning(
                beregnGrunnlag,
                bpAndelSaertilskuddResultatFraCore.resultatPeriodeListe,
                bpAndelSaertilskuddResultatFraCore.sjablonListe,
            ),
        )
        grunnlagReferanseListe.addAll(
            lagGrunnlagListeForBeregnedeGrunnlagBPsAndelSaertilskudd(bpAndelSaertilskuddResultatFraCore.resultatPeriodeListe),
        )

        // ++ Samværsfradrag
        val samvaersfradragGrunnlagTilCore =
            SamvaersfradragCoreMapper.mapSamvaersfradragGrunnlagTilCore(beregnGrunnlag, sjablonListe)
        val samvaersfradragResultatFraCore = beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore)
        grunnlagReferanseListe.addAll(
            lagGrunnlagListeForDelberegning(
                beregnGrunnlag,
                samvaersfradragResultatFraCore.resultatPeriodeListe,
                samvaersfradragResultatFraCore.sjablonListe,
            ),
        )

        // ++ Særtilskudd (totalberegning)
        val saertilskuddGrunnlagTilCore =
            SaertilskuddCoreMapper.mapSaertilskuddGrunnlagTilCore(
                beregnGrunnlag,
                bidragsevneResultatFraCore,
                bpAndelSaertilskuddResultatFraCore,
                samvaersfradragResultatFraCore,
                soknadsBarnId,
                sjablonListe,
            )
        val saertilskuddResultatFraCore = beregnSaertilskudd(saertilskuddGrunnlagTilCore)
        grunnlagReferanseListe.addAll(
            lagGrunnlagReferanseListeSaertilskudd(
                beregnGrunnlag,
                saertilskuddResultatFraCore,
                saertilskuddGrunnlagTilCore,
                bidragsevneResultatFraCore,
                bpAndelSaertilskuddResultatFraCore,
                samvaersfradragResultatFraCore,
            ),
        )
        val unikeReferanserListe = grunnlagReferanseListe.sortedBy { it.referanse }.distinct().toList()

        // Bygger responsobjekt
        return from(
            HttpStatus.OK,
            BeregnetTotalSaertilskuddResultat(
                mapFraResultatPeriodeCore(saertilskuddResultatFraCore.resultatPeriodeListe),
                unikeReferanserListe,
            ),
        )
    }

    private fun mapFraResultatPeriodeCore(resultatPeriodeCoreListe: List<ResultatPeriodeCore>): List<ResultatPeriode> {
        return resultatPeriodeCoreListe.map {
            ResultatPeriode(
                barn = it.soknadsbarnPersonId,
                periode = Periode(it.periode.datoFom, it.periode.datoTil),
                resultat = ResultatBeregning(it.resultatBeregning.belop, ResultatKodeSaertilskudd.valueOf(it.resultatBeregning.kode)),
                grunnlagReferanseListe = it.grunnlagReferanseListe,
            )
        }.toList()
    }

    private fun lagGrunnlagListeForDelberegning(
        beregnGrunnlag: BeregnGrunnlag,
        resultatPeriodeListe: List<IResultatPeriode?>,
        sjablonListe: List<SjablonResultatGrunnlagCore>,
    ): List<Grunnlag> {
        val resultatGrunnlagListe = ArrayList<Grunnlag>()

        // Bygger opp oversikt over alle grunnlag som er brukt i beregningen
        val grunnlagReferanseListe =
            resultatPeriodeListe
                .flatMap { resultatPeriodeCore ->
                    resultatPeriodeCore?.grunnlagReferanseListe?.map { it } ?: emptyList()
                }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
        beregnGrunnlag.grunnlagListe
            ?.filter { grunnlag -> grunnlagReferanseListe.contains(grunnlag.referanse) }
            ?.map { grunnlag -> Grunnlag(grunnlag.referanse, grunnlag.type, grunnlag.innhold) }?.let {
                resultatGrunnlagListe.addAll(it)
            }

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjabloner(sjablonListe))
        return resultatGrunnlagListe
    }

    private fun lagGrunnlagListeForBeregnedeGrunnlagBPsAndelSaertilskudd(resultatPeriodeCoreListe: List<ResultatPeriodeCore>): List<Grunnlag> {
        val beregnedeGrunnlagListe: MutableList<Grunnlag> = ArrayList()
        for ((periode, _, beregnedeGrunnlag) in resultatPeriodeCoreListe) {
            beregnedeGrunnlagListe.addAll(
                beregnedeGrunnlag.inntektBPListe.stream().map { (referanse, inntektType, inntektBelop): Inntekt ->
                    Grunnlag(
                        referanse,
                        GrunnlagType.INNTEKT,
                        tilJsonNode(
                            BPInntekt(
                                periode.datoFom,
                                periode.datoTil!!,
                                Rolle.BIDRAGSPLIKTIG,
                                inntektType.toString(),
                                inntektBelop,
                            ),
                        ),
                    )
                }.toList(),
            )
            beregnedeGrunnlagListe.addAll(
                beregnedeGrunnlag.inntektBMListe.stream().map {
                        (referanse, inntektType, inntektBelop, deltFordel, skatteklasse2): Inntekt ->
                    Grunnlag(
                        referanse,
                        GrunnlagType.INNTEKT,
                        tilJsonNode(
                            BMInntekt(
                                periode.datoFom,
                                periode.datoTil!!,
                                inntektType.toString(),
                                inntektBelop,
                                Rolle.BIDRAGSMOTTAKER,
                                deltFordel,
                                skatteklasse2,
                            ),
                        ),
                    )
                }.toList(),
            )
            beregnedeGrunnlagListe.addAll(
                beregnedeGrunnlag.inntektBBListe.stream().map { (referanse, inntektType, inntektBelop): Inntekt ->
                    Grunnlag(
                        referanse,
                        GrunnlagType.INNTEKT,
                        tilJsonNode(
                            SBInntekt(
                                periode.datoFom,
                                periode.datoTil!!,
                                Rolle.SOKNADSBARN,
                                inntektType.toString(),
                                inntektBelop,
                                null,
                            ),
                        ),
                    )
                }.toList(),
            )
        }
        return beregnedeGrunnlagListe
    }

    // Barnebidrag
    private fun lagGrunnlagReferanseListeSaertilskudd(
        beregnGrunnlag: BeregnGrunnlag,
        beregnSaertilskuddResultatCore: BeregnSaertilskuddResultatCore,
        beregnSaertilskuddGrunnlagCore: BeregnSaertilskuddGrunnlagCore,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
        samvaersfradragResultatFraCore: BeregnSamvaersfradragResultatCore,
    ): List<Grunnlag> {
        val resultatGrunnlagListe = ArrayList<Grunnlag>()

        // Bygger opp oversikt over alle grunnlag som er brukt i beregningen
        val grunnlagReferanseListe =
            beregnSaertilskuddResultatCore.resultatPeriodeListe
                .flatMap { resultatPeriodeCore ->
                    resultatPeriodeCore.grunnlagReferanseListe.map { it }
                }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            beregnGrunnlag.grunnlagListe!!
                .filter { (referanse): Grunnlag -> grunnlagReferanseListe.contains(referanse) }
                .map { (referanse, type, innhold): Grunnlag -> Grunnlag(referanse, type, innhold) }
                .toList(),
        )

        // Mapper ut delberegninger som er brukt som grunnlag
        resultatGrunnlagListe.addAll(
            beregnSaertilskuddGrunnlagCore.bidragsevnePeriodeListe
                .filter { (referanse): BidragsevnePeriodeCore -> grunnlagReferanseListe.contains(referanse) }
                .map { grunnlag: BidragsevnePeriodeCore ->
                    Grunnlag(
                        grunnlag.referanse,
                        GrunnlagType.BIDRAGSEVNE,
                        lagInnholdBidragsevne(grunnlag, bidragsevneResultatFraCore),
                    )
                }
                .toList(),
        )
        resultatGrunnlagListe.addAll(
            beregnSaertilskuddGrunnlagCore.bPsAndelSaertilskuddPeriodeListe
                .filter { (referanse): BPsAndelSaertilskuddPeriodeCore -> grunnlagReferanseListe.contains(referanse) }
                .map { grunnlag: BPsAndelSaertilskuddPeriodeCore ->
                    Grunnlag(
                        grunnlag.referanse,
                        GrunnlagType.BPS_ANDEL_SAERTILSKUDD,
                        lagInnholdBPsAndelSaertilskudd(grunnlag, beregnBPsAndelSaertilskuddResultatCore),
                    )
                }
                .toList(),
        )
        resultatGrunnlagListe.addAll(
            beregnSaertilskuddGrunnlagCore.samvaersfradragPeriodeListe
                .filter { (referanse): SamvaersfradragPeriodeCore -> grunnlagReferanseListe.contains(referanse) }
                .map { grunnlag: SamvaersfradragPeriodeCore ->
                    Grunnlag(
                        grunnlag.referanse,
                        GrunnlagType.SAMVAERSFRADRAG,
                        lagInnholdSamvaersfradrag(grunnlag, samvaersfradragResultatFraCore),
                    )
                }
                .toList(),
        )
        return resultatGrunnlagListe
    }

    // Mapper ut innhold fra delberegning Bidragsevne
    private fun lagInnholdBidragsevne(
        bidragsevnePeriodeCore: BidragsevnePeriodeCore,
        beregnBidragsevneResultatCore: BeregnBidragsevneResultatCore,
    ): JsonNode {
        val grunnlagReferanseListe =
            getReferanseListeFromResultatPeriodeCore(
                beregnBidragsevneResultatCore.resultatPeriodeListe,
                bidragsevnePeriodeCore.periodeDatoFraTil.datoFom,
            )
        val bidragsevne =
            BidragsevneResultatPeriode(
                bidragsevnePeriodeCore.periodeDatoFraTil.datoFom,
                bidragsevnePeriodeCore.periodeDatoFraTil.datoTil!!,
                bidragsevnePeriodeCore.bidragsevneBelop,
                grunnlagReferanseListe,
            )
        return tilJsonNode(bidragsevne)
    }

    // Mapper ut innhold fra delberegning BPsAndelSaertilskudd
    private fun lagInnholdBPsAndelSaertilskudd(
        bPsAndelSaertilskuddPeriodeCore: BPsAndelSaertilskuddPeriodeCore,
        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
    ): JsonNode {
        val grunnlagReferanseListe =
            getReferanseListeFromResultatPeriodeCore(
                beregnBPsAndelSaertilskuddResultatCore.resultatPeriodeListe,
                bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoFom,
            )
        val bPsAndelSaertilskudd =
            BPsAndelSaertilskuddResultatPeriode(
                mapDato(bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoFom),
                mapDato(bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoTil),
                bPsAndelSaertilskuddPeriodeCore.bPsAndelSaertilskuddBelop,
                bPsAndelSaertilskuddPeriodeCore.bPsAndelSaertilskuddProsent,
                bPsAndelSaertilskuddPeriodeCore.barnetErSelvforsorget,
                grunnlagReferanseListe,
            )
        return tilJsonNode(bPsAndelSaertilskudd)
    }

    // Mapper ut innhold fra delberegning Samvaersfradrag
    private fun lagInnholdSamvaersfradrag(
        samvaersfradragPeriodeCore: SamvaersfradragPeriodeCore,
        beregnSamvaersfradragResultatCore: BeregnSamvaersfradragResultatCore,
    ): JsonNode {
        val grunnlagReferanseListe =
            getReferanseListeFromResultatPeriodeCore(
                beregnSamvaersfradragResultatCore.resultatPeriodeListe,
                samvaersfradragPeriodeCore.periodeDatoFraTil.datoFom,
            )
        val samvaersfradrag =
            SamvaersfradragResultatPeriode(
                mapDato(samvaersfradragPeriodeCore.periodeDatoFraTil.datoFom),
                mapDato(samvaersfradragPeriodeCore.periodeDatoFraTil.datoTil),
                samvaersfradragPeriodeCore.samvaersfradragBelop,
                samvaersfradragPeriodeCore.barnPersonId,
                grunnlagReferanseListe,
            )
        return tilJsonNode(samvaersfradrag)
    }

    private fun mapSjabloner(sjablonResultatGrunnlagCoreListe: List<SjablonResultatGrunnlagCore>): List<Grunnlag> {
        return sjablonResultatGrunnlagCoreListe.stream()
            .map { (referanse, periode, navn, verdi): SjablonResultatGrunnlagCore ->
                val sjablonPeriode =
                    SjablonResultatPeriode(
                        mapDato(
                            periode.datoFom,
                        ),
                        mapDato(periode.datoTil),
                        navn,
                        verdi.toInt(),
                    )
                Grunnlag(referanse, GrunnlagType.SJABLON, tilJsonNode(sjablonPeriode))
            }
            .toList()
    }

    private fun getReferanseListeFromResultatPeriodeCore(resultatPeriodeListe: List<IResultatPeriode?>, datoFom: LocalDate): List<String> {
        return resultatPeriodeListe.stream()
            .filter { resultatPeriodeCore: IResultatPeriode? -> datoFom == resultatPeriodeCore!!.periode.datoFom }
            .findFirst()
            .map { resultatperiodeCore -> resultatperiodeCore?.grunnlagReferanseListe ?: emptyList() }
            .orElse(emptyList())
    }

    // ==================================================================================================================================================
    // Kaller core for beregning av bidragsevne
    private fun beregnBidragsevne(bidragsevneGrunnlagTilCore: BeregnBidragsevneGrunnlagCore): BeregnBidragsevneResultatCore {
        val bidragsevneResultatFraCore: BeregnBidragsevneResultatCore
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Bidragsevne - grunnlag for beregning: {}", bidragsevneGrunnlagTilCore)
        }

        // Kaller core-modulen for beregning av bidragsevne
        bidragsevneResultatFraCore =
            try {
                bidragsevneCore.beregnBidragsevne(bidragsevneGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av bidragsevne: " + e.message)
            }
        if (!bidragsevneResultatFraCore.avvikListe.isEmpty()) {
            LOGGER.warn(
                "Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet: " + System.lineSeparator() +
                    bidragsevneResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst).collect(
                        Collectors.joining(
                            System.lineSeparator(),
                        ),
                    ),
            )
            LOGGER.info(
                "Bidragsevne - grunnlag for beregning:" + System.lineSeparator() +
                    "beregnDatoFra= " + bidragsevneGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + bidragsevneGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "antallBarnIEgetHusholdPeriodeListe= " + bidragsevneGrunnlagTilCore.antallBarnIEgetHusholdPeriodeListe + System.lineSeparator() +
                    "bostatusPeriodeListe= " + bidragsevneGrunnlagTilCore.bostatusPeriodeListe + System.lineSeparator() +
                    "inntektPeriodeListe= " + bidragsevneGrunnlagTilCore.inntektPeriodeListe + System.lineSeparator() +
                    "særfradragPeriodeListe= " + bidragsevneGrunnlagTilCore.saerfradragPeriodeListe + System.lineSeparator() +
                    "skatteklassePeriodeListe= " + bidragsevneGrunnlagTilCore.skatteklassePeriodeListe,
            )
            throw UgyldigInputException(
                "Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet: " +
                    bidragsevneResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining("; ")),
            )
        }
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Bidragsevne - resultat av beregning: {}", bidragsevneResultatFraCore.resultatPeriodeListe)
        }
        return bidragsevneResultatFraCore
    }

    // Kaller core for beregning av BPs andel av særtilskudd
    private fun beregnBPAndelSaertilskudd(
        bpAndelSaertilskuddGrunnlagTilCore: BeregnBPsAndelSaertilskuddGrunnlagCore,
    ): BeregnBPsAndelSaertilskuddResultatCore {
        val bpAndelSaertilskuddResultatFraCore: BeregnBPsAndelSaertilskuddResultatCore
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug("BPs andel av særtilskudd - grunnlag for beregning: {}", bpAndelSaertilskuddGrunnlagTilCore)
        }

        // Kaller core-modulen for beregning av BPs andel av særtilskudd
        bpAndelSaertilskuddResultatFraCore =
            try {
                bpAndelSaertilskuddCore.beregnBPsAndelSaertilskudd(bpAndelSaertilskuddGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av BPs andel av særtilskudd: " + e.message)
            }
        if (!bpAndelSaertilskuddResultatFraCore.avvikListe.isEmpty()) {
            LOGGER.warn(
                "Ugyldig input ved beregning av BPs andel av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
                    bpAndelSaertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.warn(
                "Ugyldig input ved beregning av BPs andel av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
                    bpAndelSaertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.info(
                "BPs andel av særtilskudd - grunnlag for beregning:" + System.lineSeparator() +
                    "beregnDatoFra= " + bpAndelSaertilskuddGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + bpAndelSaertilskuddGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "nettoSaertilskuddPeriodeListe= " + bpAndelSaertilskuddGrunnlagTilCore.nettoSaertilskuddPeriodeListe + System.lineSeparator() +
                    "inntektBPPeriodeListe= " + bpAndelSaertilskuddGrunnlagTilCore.inntektBPPeriodeListe + System.lineSeparator() +
                    "inntektBMPeriodeListe= " + bpAndelSaertilskuddGrunnlagTilCore.inntektBMPeriodeListe + System.lineSeparator() +
                    "inntektBBPeriodeListe= " + bpAndelSaertilskuddGrunnlagTilCore.inntektBBPeriodeListe + System.lineSeparator(),
            )
            throw UgyldigInputException(
                "Ugyldig input ved beregning av BPs andel av særtilskudd. Følgende avvik ble funnet: " +
                    bpAndelSaertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining("; ")),
            )
        }
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug(
                "BPs andel av særtilskudd - resultat av beregning: {}",
                bpAndelSaertilskuddResultatFraCore.resultatPeriodeListe,
            )
        }
        return bpAndelSaertilskuddResultatFraCore
    }

    // Kaller core for beregning av samværsfradrag
    private fun beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore: BeregnSamvaersfradragGrunnlagCore): BeregnSamvaersfradragResultatCore {
        val samvaersfradragResultatFraCore: BeregnSamvaersfradragResultatCore
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug("Samværsfradrag - grunnlag for beregning: {}", samvaersfradragGrunnlagTilCore)
        }

        // Kaller core-modulen for beregning av samværsfradrag
        samvaersfradragResultatFraCore =
            try {
                samvaersfradragCore.beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av samværsfradrag: " + e.message)
            }
        if (!samvaersfradragResultatFraCore.avvikListe.isEmpty()) {
            LOGGER.warn(
                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " + System.lineSeparator() +
                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.warn(
                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " + System.lineSeparator() +
                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.info(
                "Samværsfradrag - grunnlag for beregning: " + System.lineSeparator() +
                    "beregnDatoFra= " + samvaersfradragGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + samvaersfradragGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "samvaersklassePeriodeListe= " + samvaersfradragGrunnlagTilCore.samvaersklassePeriodeListe + System.lineSeparator(),
            )
            throw UgyldigInputException(
                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " +
                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining("; ")),
            )
        }
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug(
                "Samværsfradrag - resultat av beregning: {}",
                samvaersfradragResultatFraCore.resultatPeriodeListe,
            )
        }
        return samvaersfradragResultatFraCore
    }

    // Kaller core for beregning av særtilskudd
    private fun beregnSaertilskudd(saertilskuddGrunnlagTilCore: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore {
        val saertilskuddResultatFraCore: BeregnSaertilskuddResultatCore
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug("Særtilskudd - grunnlag for beregning: {}", saertilskuddGrunnlagTilCore)
        }

        // Kaller core-modulen for beregning av særtilskudd
        saertilskuddResultatFraCore =
            try {
                saertilskuddCore.beregnSaertilskudd(saertilskuddGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av særtilskudd: " + e.message)
            }
        if (!saertilskuddResultatFraCore.avvikListe.isEmpty()) {
            LOGGER.warn(
                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.warn(
                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining(System.lineSeparator())),
            )
            SECURE_LOGGER.info(
                "Særtilskudd - grunnlag for beregning: " + System.lineSeparator() +
                    "beregnDatoFra= " + saertilskuddGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
                    "beregnDatoTil= " + saertilskuddGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
                    "soknadsbarnPersonId= " + saertilskuddGrunnlagTilCore.soknadsbarnPersonId + System.lineSeparator() +
                    "bidragsevnePeriodeListe= " + saertilskuddGrunnlagTilCore.bidragsevnePeriodeListe + System.lineSeparator() +
                    "bPsAndelSaertilskuddPeriodeListe= " + saertilskuddGrunnlagTilCore.bPsAndelSaertilskuddPeriodeListe + System.lineSeparator() +
                    "samvaersfradragPeriodeListe= " + saertilskuddGrunnlagTilCore.samvaersfradragPeriodeListe + System.lineSeparator() +
                    "lopendeBidragPeriodeListe= " + saertilskuddGrunnlagTilCore.lopendeBidragPeriodeListe + System.lineSeparator(),
            )
            throw UgyldigInputException(
                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " +
                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
                        .collect(Collectors.joining("; ")),
            )
        }
        if (SECURE_LOGGER.isDebugEnabled) {
            SECURE_LOGGER.debug("Særtilskudd - resultat av beregning: {}", saertilskuddResultatFraCore.resultatPeriodeListe)
        }
        return saertilskuddResultatFraCore
    }

    // ==================================================================================================================================================
    // Henter sjabloner
    private fun hentSjabloner(): SjablonListe {
        // Henter sjabloner for sjablontall
        val sjablonSjablontallListe = sjablonConsumer.hentSjablonSjablontall().responseEntity.body ?: emptyList()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Sjablontall: ${sjablonSjablontallListe.size}")
        }

        // Henter sjabloner for samværsfradrag
        val sjablonSamvaersfradragListe = sjablonConsumer.hentSjablonSamvaersfradrag().responseEntity.body ?: emptyList()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Samværsfradrag: ${sjablonSamvaersfradragListe.size}")
        }

        // Henter sjabloner for bidragsevne
        val sjablonBidragsevneListe = sjablonConsumer.hentSjablonBidragsevne().responseEntity.body ?: emptyList()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Bidragsevne: ${sjablonBidragsevneListe.size}")
        }

        // Henter sjabloner for trinnvis skattesats
        val sjablonTrinnvisSkattesatsListe = sjablonConsumer.hentSjablonTrinnvisSkattesats().responseEntity.body ?: emptyList()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Trinnvis skattesats: ${sjablonTrinnvisSkattesatsListe.size}")
        }

        return SjablonListe(sjablonSjablontallListe, sjablonSamvaersfradragListe, sjablonBidragsevneListe, sjablonTrinnvisSkattesatsListe)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BeregnSærtilskuddService::class.java)

        private fun mapDato(dato: LocalDate?): LocalDate {
            return if (dato!!.isAfter(LocalDate.parse("9999-12-31"))) LocalDate.parse("9999-12-31") else dato
        }
    }
}

/*
fun BeregnGrunnlag.valider() {
    if (beregnDatoFra == null) throw UgyldigInputException("beregnDatoFra kan ikke være null")
    if (beregnDatoTil == null) throw UgyldigInputException("beregnDatoTil kan ikke være null")
    grunnlagListe?.map { it.valider() } ?: throw UgyldigInputException("grunnlagListe kan ikke være null")
}

fun Grunnlag.valider() {
    if (referanse == null) throw UgyldigInputException("referanse kan ikke være null")
    if (type == null) throw UgyldigInputException("type kan ikke være null")
    if (innhold == null) throw UgyldigInputException("innhold kan ikke være null")
}

fun Samvaersklasse.valider() {
    if (soknadsbarnId == null) throw UgyldigInputException("soknadsbarnId kan ikke være null")
    if (soknadsbarnFodselsdato == null) throw UgyldigInputException("soknadsbarnFodselsdato kan ikke være null")
    if (samvaersklasseId == null) throw UgyldigInputException("samvaersklasseId kan ikke være null")
}*/
