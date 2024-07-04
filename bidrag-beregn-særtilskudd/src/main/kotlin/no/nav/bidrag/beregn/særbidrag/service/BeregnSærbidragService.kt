package no.nav.bidrag.beregn.særbidrag.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.BPsAndelSærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærtilskuddResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.samvaersfradrag.SamvaersfradragCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.SærbidragCore
import no.nav.bidrag.beregn.særbidrag.exception.UgyldigInputException
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPAndelSærbidragCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.BidragsevneCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.CoreMapper.Companion.tilJsonNode
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.saertilskudd.SjablonResultatPeriode
import no.nav.bidrag.transport.behandling.beregning.særtilskudd.BeregnetSærtilskuddResultat
import no.nav.bidrag.transport.behandling.beregning.særtilskudd.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.særtilskudd.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate

@Service
internal class BeregnSærbidragService(
    private val bidragsevneCore: BidragsevneCore = BidragsevneCore(),
    private val bpAndelSaertilskuddCore: BPsAndelSærbidragCore = BPsAndelSærbidragCore(),
    private val samvaersfradragCore: SamvaersfradragCore = SamvaersfradragCore(),
    private val saertilskuddCore: SærbidragCore = SærbidragCore(),
) {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetSærtilskuddResultat {
        secureLogger.debug { "Beregning særtilskudd - følgende request mottatt: ${tilJson(grunnlag)}" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av særtilskudd: " + e.message)
        }

        // Validerer og henter ut søknadsbarn
//        val søknadsbarnInfo = validerSøknadsbarn(grunnlag)

        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
        }

        // Henter sjabloner
        val sjablonListe = hentSjabloner()

        // Bygger grunnlag til core og utfører delberegninger
        return utførDelberegninger(grunnlag, sjablontallMap, sjablonListe)
    }

    //  Validerer at det kun er oppgitt ett SoknadsbarnInfo-grunnlag og at mapping til SoknadsBarnInfo objekt ikke feiler
//    private fun validerSøknadsbarn(beregnGrunnlag: BeregnGrunnlag): SoknadsBarnInfo {
//        val soknadsbarnInfoGrunnlagListe =
//            beregnGrunnlag.grunnlagListe
//                ?.filter { grunnlag -> grunnlag.type == Grunnlagstype.PERSON_SØKNADSBARN }?.toList()
//        if (soknadsbarnInfoGrunnlagListe?.size != 1) {
//            throw UgyldigInputException("Det må være nøyaktig ett søknadsbarn i beregningsgrunnlaget")
//        }
//        val soknadsBarnInfo = grunnlagTilObjekt(soknadsbarnInfoGrunnlagListe[0], SoknadsBarnInfo::class.java)
//
//        beregnGrunnlag.grunnlagListe!!
//            .filter { grunnlag -> grunnlag.type == Grunnlagstype.SAMVÆRSKLASSE }
//            .map { grunnlag: Grunnlag? ->
//                grunnlagTilObjekt(
//                    grunnlag!!,
//                    Samvaersklasse::class.java,
//                )
//            }
//            .forEach { samvaersklasse: Samvaersklasse ->
//                samvaersklasse.valider()
//                if (samvaersklasse.soknadsbarnId == soknadsBarnInfo.id && samvaersklasse.soknadsbarnFodselsdato != soknadsBarnInfo.fodselsdato) {
//                    throw UgyldigInputException(
//                        "Fødselsdato for søknadsbarn stemmer ikke overens med fødselsdato til barnet i Samværsklasse-grunnlaget",
//                    )
//                }
//            }
//        return soknadsBarnInfo
//    }

    // ==================================================================================================================================================
    // Bygger grunnlag til core og kaller delberegninger
    private fun utførDelberegninger(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
//        soknadsBarnId: Int,
    ): BeregnetSærtilskuddResultat {
        val grunnlagReferanseListe = ArrayList<GrunnlagDto>()

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
            BPAndelSærbidragCoreMapper.mapBPsAndelSaertilskuddGrunnlagTilCore(
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

//        // ++ Samværsfradrag
//        val samvaersfradragGrunnlagTilCore =
//            SamvaersfradragCoreMapper.mapSamvaersfradragGrunnlagTilCore(beregnGrunnlag, sjablonListe)
//        val samvaersfradragResultatFraCore = beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore)
// //        grunnlagReferanseListe.addAll(
// //            lagGrunnlagListeForDelberegning(
// //                beregnGrunnlag,
// //                samvaersfradragResultatFraCore.resultatPeriodeListe,
// //                samvaersfradragResultatFraCore.sjablonListe,
// //            ),
// //        )
//
//        // ++ Særtilskudd (totalberegning)
//        val saertilskuddGrunnlagTilCore =
//            SaertilskuddCoreMapper.mapSaertilskuddGrunnlagTilCore(
//                beregnGrunnlag,
//                bidragsevneResultatFraCore,
//                bpAndelSaertilskuddResultatFraCore,
//                samvaersfradragResultatFraCore,
//                soknadsBarnId,
//                sjablonListe,
//            )
//        val saertilskuddResultatFraCore = beregnSaertilskudd(saertilskuddGrunnlagTilCore)
// //        grunnlagReferanseListe.addAll(
// //            lagGrunnlagReferanseListeSaertilskudd(
// //                beregnGrunnlag,
// //                saertilskuddResultatFraCore,
// //                saertilskuddGrunnlagTilCore,
// //                bidragsevneResultatFraCore,
// //                bpAndelSaertilskuddResultatFraCore,
// //                samvaersfradragResultatFraCore,
// //            ),
// //        )

        val unikeReferanserListe = grunnlagReferanseListe.sortedBy { it.referanse }.distinct()

        // Bygger responsobjekt
        return BeregnetSærtilskuddResultat(
//            mapFraResultatPeriodeCore(saertilskuddResultatFraCore.resultatPeriodeListe),
            mapFraResultatPeriodeCore(bpAndelSaertilskuddResultatFraCore.resultatPeriodeListe),
            unikeReferanserListe,
        )
    }

    private fun mapFraResultatPeriodeCore(resultatPeriodeCoreListe: List<ResultatPeriodeCore>) =
        resultatPeriodeCoreListe
            .map {
                ResultatPeriode(
                    periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                    resultat = ResultatBeregning(it.resultat.resultatAndelBeløp, Resultatkode.SÆRTILSKUDD_INNVILGET),
//            resultat = ResultatBeregning(it.resultat.belop, Resultatkode.valueOf(it.resultat.kode)),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                )
            }

    private fun lagGrunnlagListeForDelberegning(
        beregnGrunnlag: BeregnGrunnlag,
        resultatPeriodeListe: List<IResultatPeriode>,
        sjablonListe: List<SjablonResultatGrunnlagCore>,
    ): List<GrunnlagDto> {
        // Bygger opp oversikt over alle grunnlag som er brukt i beregningen
        val grunnlagReferanseListe = resultatPeriodeListe
            .flatMap { it.grunnlagsreferanseListe }
            .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
        val resultatGrunnlagListe = beregnGrunnlag.grunnlagListe
            .filter { it.referanse in grunnlagReferanseListe }
            .map { GrunnlagDto(referanse = it.referanse, type = it.type, innhold = it.innhold) }
            .toMutableList()

        // Danner grunnlag basert på liste over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjabloner(sjablonListe))
        return resultatGrunnlagListe
    }

//    private fun lagGrunnlagListeForBeregnedeGrunnlagBPsAndelSaertilskudd(resultatPeriodeCoreListe: List<ResultatPeriodeCore>): List<Grunnlag> {
//        val beregnedeGrunnlagListe: MutableList<Grunnlag> = ArrayList()
//        for ((periode, _, beregnedeGrunnlag) in resultatPeriodeCoreListe) {
//            beregnedeGrunnlagListe.addAll(
//                beregnedeGrunnlag.inntektBPListe.stream().map { (referanse, inntektType, inntektBelop): Inntekt ->
//                    Grunnlag(
//                        referanse,
//                        GrunnlagType.INNTEKT,
//                        tilJsonNode(
//                            BPInntekt(
//                                periode.datoFom,
//                                periode.datoTil!!,
//                                Rolle.BIDRAGSPLIKTIG,
//                                inntektType.toString(),
//                                inntektBelop,
//                            ),
//                        ),
//                    )
//                }.toList(),
//            )
//            beregnedeGrunnlagListe.addAll(
//                beregnedeGrunnlag.inntektBMListe.stream().map { (referanse, inntektType, inntektBelop, deltFordel, skatteklasse2): Inntekt ->
//                    Grunnlag(
//                        referanse,
//                        GrunnlagType.INNTEKT,
//                        tilJsonNode(
//                            BMInntekt(
//                                periode.datoFom,
//                                periode.datoTil!!,
//                                inntektType.toString(),
//                                inntektBelop,
//                                Rolle.BIDRAGSMOTTAKER,
//                                deltFordel,
//                                skatteklasse2,
//                            ),
//                        ),
//                    )
//                }.toList(),
//            )
//            beregnedeGrunnlagListe.addAll(
//                beregnedeGrunnlag.inntektBBListe.stream().map { (referanse, inntektType, inntektBelop): Inntekt ->
//                    Grunnlag(
//                        referanse,
//                        GrunnlagType.INNTEKT,
//                        tilJsonNode(
//                            SBInntekt(
//                                periode.datoFom,
//                                periode.datoTil!!,
//                                Rolle.SOKNADSBARN,
//                                inntektType.toString(),
//                                inntektBelop,
//                                null,
//                            ),
//                        ),
//                    )
//                }.toList(),
//            )
//        }
//        return beregnedeGrunnlagListe
//    }

    // Særtilskudd
//    private fun lagGrunnlagReferanseListeSaertilskudd(
//        beregnGrunnlag: BeregnGrunnlag,
//        beregnSaertilskuddResultatCore: BeregnSaertilskuddResultatCore,
//        beregnSaertilskuddGrunnlagCore: BeregnSaertilskuddGrunnlagCore,
//        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
//        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
//        samvaersfradragResultatFraCore: BeregnSamvaersfradragResultatCore,
//    ): List<Grunnlag> {
//        val resultatGrunnlagListe = ArrayList<Grunnlag>()
//
//        // Bygger opp oversikt over alle grunnlag som er brukt i beregningen
//        val grunnlagReferanseListe =
//            beregnSaertilskuddResultatCore.resultatPeriodeListe
//                .flatMap { resultatPeriodeCore ->
//                    resultatPeriodeCore.grunnlagReferanseListe.map { it }
//                }
//                .distinct()
//
//        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
//        resultatGrunnlagListe.addAll(
//            beregnGrunnlag.grunnlagListe!!
//                .filter { (referanse): Grunnlag -> grunnlagReferanseListe.contains(referanse) }
//                .map { (referanse, type, innhold): Grunnlag -> Grunnlag(referanse, type, innhold) }
//                .toList(),
//        )
//
//        // Mapper ut delberegninger som er brukt som grunnlag
//        resultatGrunnlagListe.addAll(
//            beregnSaertilskuddGrunnlagCore.bidragsevnePeriodeListe
//                .filter { (referanse): BidragsevnePeriodeCore -> grunnlagReferanseListe.contains(referanse) }
//                .map { grunnlag: BidragsevnePeriodeCore ->
//                    Grunnlag(
//                        grunnlag.referanse,
//                        GrunnlagType.BIDRAGSEVNE,
//                        lagInnholdBidragsevne(grunnlag, bidragsevneResultatFraCore),
//                    )
//                }
//                .toList(),
//        )
//        resultatGrunnlagListe.addAll(
//            beregnSaertilskuddGrunnlagCore.bPsAndelSaertilskuddPeriodeListe
//                .filter { (referanse): BPsAndelSaertilskuddPeriodeCore -> grunnlagReferanseListe.contains(referanse) }
//                .map { grunnlag: BPsAndelSaertilskuddPeriodeCore ->
//                    Grunnlag(
//                        grunnlag.referanse,
//                        GrunnlagType.BPS_ANDEL_SAERTILSKUDD,
//                        lagInnholdBPsAndelSaertilskudd(grunnlag, beregnBPsAndelSaertilskuddResultatCore),
//                    )
//                }
//                .toList(),
//        )
//        resultatGrunnlagListe.addAll(
//            beregnSaertilskuddGrunnlagCore.samvaersfradragPeriodeListe
//                .filter { (referanse): SamvaersfradragPeriodeCore -> grunnlagReferanseListe.contains(referanse) }
//                .map { grunnlag: SamvaersfradragPeriodeCore ->
//                    Grunnlag(
//                        grunnlag.referanse,
//                        GrunnlagType.SAMVAERSFRADRAG,
//                        lagInnholdSamvaersfradrag(grunnlag, samvaersfradragResultatFraCore),
//                    )
//                }
//                .toList(),
//        )
//        return resultatGrunnlagListe
//    }

    // Mapper ut innhold fra delberegning Bidragsevne
//    private fun lagInnholdBidragsevne(
//        bidragsevnePeriodeCore: BidragsevnePeriodeCore,
//        beregnBidragsevneResultatCore: BeregnBidragsevneResultatCore,
//    ): JsonNode {
//        val grunnlagReferanseListe =
//            getReferanseListeFromResultatPeriodeCore(
//                beregnBidragsevneResultatCore.resultatPeriodeListe,
//                bidragsevnePeriodeCore.periodeDatoFraTil.datoFom,
//            )
//        val bidragsevne =
//            BidragsevneResultatPeriode(
//                bidragsevnePeriodeCore.periodeDatoFraTil.datoFom,
//                bidragsevnePeriodeCore.periodeDatoFraTil.datoTil!!,
//                bidragsevnePeriodeCore.bidragsevneBelop,
//                grunnlagReferanseListe,
//            )
//        return tilJsonNode(bidragsevne)
//    }

    // Mapper ut innhold fra delberegning BPsAndelSaertilskudd
//    private fun lagInnholdBPsAndelSaertilskudd(
//        bPsAndelSaertilskuddPeriodeCore: BPsAndelSaertilskuddPeriodeCore,
//        beregnBPsAndelSaertilskuddResultatCore: BeregnBPsAndelSaertilskuddResultatCore,
//    ): JsonNode {
//        val grunnlagReferanseListe =
//            getReferanseListeFromResultatPeriodeCore(
//                beregnBPsAndelSaertilskuddResultatCore.resultatPeriodeListe,
//                bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoFom,
//            )
//        val bPsAndelSaertilskudd =
//            BPsAndelSaertilskuddResultatPeriode(
//                mapDato(bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoFom),
//                mapDato(bPsAndelSaertilskuddPeriodeCore.periodeDatoFraTil.datoTil),
//                bPsAndelSaertilskuddPeriodeCore.bPsAndelSaertilskuddBelop,
//                bPsAndelSaertilskuddPeriodeCore.bPsAndelSaertilskuddProsent,
//                bPsAndelSaertilskuddPeriodeCore.barnetErSelvforsorget,
//                grunnlagReferanseListe,
//            )
//        return tilJsonNode(bPsAndelSaertilskudd)
//    }

    // Mapper ut innhold fra delberegning Samvaersfradrag
//    private fun lagInnholdSamvaersfradrag(
//        samvaersfradragPeriodeCore: SamvaersfradragPeriodeCore,
//        beregnSamvaersfradragResultatCore: BeregnSamvaersfradragResultatCore,
//    ): JsonNode {
//        val grunnlagReferanseListe =
//            getReferanseListeFromResultatPeriodeCore(
//                beregnSamvaersfradragResultatCore.resultatPeriodeListe,
//                samvaersfradragPeriodeCore.periodeDatoFraTil.datoFom,
//            )
//        val samvaersfradrag =
//            SamvaersfradragResultatPeriode(
//                mapDato(samvaersfradragPeriodeCore.periodeDatoFraTil.datoFom),
//                mapDato(samvaersfradragPeriodeCore.periodeDatoFraTil.datoTil),
//                samvaersfradragPeriodeCore.samvaersfradragBelop,
//                samvaersfradragPeriodeCore.barnPersonId,
//                grunnlagReferanseListe,
//            )
//        return tilJsonNode(samvaersfradrag)
//    }

    private fun mapSjabloner(sjablonResultatGrunnlagCoreListe: List<SjablonResultatGrunnlagCore>) =
        sjablonResultatGrunnlagCoreListe
            .map {
                val sjablonPeriode =
                    SjablonResultatPeriode(
                        datoFom = mapDato(it.periode.datoFom),
                        datoTil = mapDato(it.periode.datoTil),
                        sjablonNavn = it.navn,
                        sjablonVerdi = it.verdi.toInt(),
                    )
                GrunnlagDto(it.referanse, Grunnlagstype.SJABLON, tilJsonNode(sjablonPeriode))
            }

//    private fun getReferanseListeFromResultatPeriodeCore(resultatPeriodeListe: List<IResultatPeriode?>, datoFom: LocalDate): List<String> =
//        resultatPeriodeListe.stream()
//            .filter { resultatPeriodeCore: IResultatPeriode? -> datoFom == resultatPeriodeCore!!.periode.datoFom }
//            .findFirst()
//            .map { resultatperiodeCore -> resultatperiodeCore?.grunnlagReferanseListe ?: emptyList() }
//            .orElse(emptyList())

    // ==================================================================================================================================================
    // Kaller core for beregning av bidragsevne
    private fun beregnBidragsevne(bidragsevneGrunnlagTilCore: BeregnBidragsevneGrunnlagCore): BeregnBidragsevneResultatCore {
        secureLogger.debug { "Bidragsevne - grunnlag for beregning: ${tilJson(bidragsevneGrunnlagTilCore)}" }

        // Kaller core-modulen for beregning av bidragsevne
        val bidragsevneResultatFraCore =
            try {
                bidragsevneCore.beregnBidragsevne(bidragsevneGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av bidragsevne: " + e.message)
            }

        if (bidragsevneResultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = bidragsevneResultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            secureLogger.warn { "Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet: $avviktekst" }
            throw UgyldigInputException("Ugyldig input ved beregning av bidragsevne. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.debug { "Bidragsevne - resultat av beregning: ${tilJson(bidragsevneResultatFraCore.resultatPeriodeListe)}" }

        return bidragsevneResultatFraCore
    }

    // Kaller core for beregning av BPs andel av særbidrag
    private fun beregnBPAndelSaertilskudd(
        bpAndelSærtilskuddGrunnlagTilCore: BeregnBPsAndelSærtilskuddGrunnlagCore
    ): BeregnBPsAndelSærtilskuddResultatCore {
        secureLogger.debug { "BP's andel av særbidrag - grunnlag for beregning: ${tilJson(bpAndelSærtilskuddGrunnlagTilCore)}" }

        // Kaller core-modulen for beregning av BPs andel av særbidrag
        val bpAndelSærbidragResultatFraCore =
            try {
                bpAndelSaertilskuddCore.beregnBPsAndelSaertilskudd(bpAndelSærtilskuddGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av BPs andel av særbidrag: " + e.message)
            }

        if (bpAndelSærbidragResultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = bpAndelSærbidragResultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            secureLogger.warn { "Ugyldig input ved beregning av BPs andel av særbidrag. Følgende avvik ble funnet: $avviktekst" }
            throw UgyldigInputException("Ugyldig input ved beregning av BPs andel av særtilskudd. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.debug { "BPs andel av særbidrag - resultat av beregning: ${tilJson(bpAndelSærbidragResultatFraCore.resultatPeriodeListe)}" }

        return bpAndelSærbidragResultatFraCore
    }

    // Kaller core for beregning av samværsfradrag
//    private fun beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore: BeregnSamvaersfradragGrunnlagCore): BeregnSamvaersfradragResultatCore {
//        val samvaersfradragResultatFraCore: BeregnSamvaersfradragResultatCore
//        if (secureLogger.isDebugEnabled()) {
//            secureLogger.debug { "${"Samværsfradrag - grunnlag for beregning: {}"} $samvaersfradragGrunnlagTilCore" }
//        }
//
//        // Kaller core-modulen for beregning av samværsfradrag
//        samvaersfradragResultatFraCore =
//            try {
//                samvaersfradragCore.beregnSamvaersfradrag(samvaersfradragGrunnlagTilCore)
//            } catch (e: Exception) {
//                throw UgyldigInputException("Ugyldig input ved beregning av samværsfradrag: " + e.message)
//            }
//        if (!samvaersfradragResultatFraCore.avvikListe.isEmpty()) {
//            LOGGER.warn(
//                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " + System.lineSeparator() +
//                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining(System.lineSeparator())),
//            )
//            secureLogger.warn {
//                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " + System.lineSeparator() +
//                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining(System.lineSeparator()))
//            }
//            secureLogger.info {
//                "Samværsfradrag - grunnlag for beregning: " + System.lineSeparator() +
//                    "beregnDatoFra= " + samvaersfradragGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
//                    "beregnDatoTil= " + samvaersfradragGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
//                    "samvaersklassePeriodeListe= " + samvaersfradragGrunnlagTilCore.samvaersklassePeriodeListe + System.lineSeparator()
//            }
//            throw UgyldigInputException(
//                "Ugyldig input ved beregning av samværsfradrag. Følgende avvik ble funnet: " +
//                    samvaersfradragResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining("; ")),
//            )
//        }
//        if (secureLogger.isDebugEnabled()) {
//            secureLogger.debug { "${"Samværsfradrag - resultat av beregning: {}"} ${samvaersfradragResultatFraCore.resultatPeriodeListe}" }
//        }
//        return samvaersfradragResultatFraCore
//    }

    // Kaller core for beregning av særtilskudd
//    private fun beregnSaertilskudd(saertilskuddGrunnlagTilCore: BeregnSaertilskuddGrunnlagCore): BeregnSaertilskuddResultatCore {
//        val saertilskuddResultatFraCore: BeregnSaertilskuddResultatCore
//        if (secureLogger.isDebugEnabled()) {
//            secureLogger.debug { "${"Særtilskudd - grunnlag for beregning: {}"} $saertilskuddGrunnlagTilCore" }
//        }
//
//        // Kaller core-modulen for beregning av særtilskudd
//        saertilskuddResultatFraCore =
//            try {
//                saertilskuddCore.beregnSaertilskudd(saertilskuddGrunnlagTilCore)
//            } catch (e: Exception) {
//                throw UgyldigInputException("Ugyldig input ved beregning av særtilskudd: " + e.message)
//            }
//        if (!saertilskuddResultatFraCore.avvikListe.isEmpty()) {
//            LOGGER.warn(
//                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
//                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining(System.lineSeparator())),
//            )
//            secureLogger.warn(
//                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " + System.lineSeparator() +
//                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining(System.lineSeparator())),
//            )
//            secureLogger.info(
//                "Særtilskudd - grunnlag for beregning: " + System.lineSeparator() +
//                    "beregnDatoFra= " + saertilskuddGrunnlagTilCore.beregnDatoFra + System.lineSeparator() +
//                    "beregnDatoTil= " + saertilskuddGrunnlagTilCore.beregnDatoTil + System.lineSeparator() +
//                    "soknadsbarnPersonId= " + saertilskuddGrunnlagTilCore.soknadsbarnPersonId + System.lineSeparator() +
//                    "bidragsevnePeriodeListe= " + saertilskuddGrunnlagTilCore.bidragsevnePeriodeListe + System.lineSeparator() +
//                    "bPsAndelSaertilskuddPeriodeListe= " + saertilskuddGrunnlagTilCore.bPsAndelSaertilskuddPeriodeListe + System.lineSeparator() +
//                    "samvaersfradragPeriodeListe= " + saertilskuddGrunnlagTilCore.samvaersfradragPeriodeListe + System.lineSeparator() +
//                    "lopendeBidragPeriodeListe= " + saertilskuddGrunnlagTilCore.lopendeBidragPeriodeListe + System.lineSeparator(),
//            )
//            throw UgyldigInputException(
//                "Ugyldig input ved beregning av særtilskudd. Følgende avvik ble funnet: " +
//                    saertilskuddResultatFraCore.avvikListe.stream().map(AvvikCore::avvikTekst)
//                        .collect(Collectors.joining("; ")),
//            )
//        }
//        if (secureLogger.isDebugEnabled()) {
//            secureLogger.debug { "${"Særtilskudd - resultat av beregning: {}"} ${saertilskuddResultatFraCore.resultatPeriodeListe}" }
//        }
//        return saertilskuddResultatFraCore
//    }

    // ==================================================================================================================================================
    // Henter sjabloner
    private fun hentSjabloner(): SjablonListe {
        // Henter sjabloner for sjablontall
        val sjablontallListe = SjablonProvider.hentSjablontall()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Sjablontall: ${sjablontallListe.size}")
        }

        // Henter sjabloner for samværsfradrag
        val sjablonSamvaersfradragListe = SjablonProvider.hentSjablonSamværsfradrag()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Samværsfradrag: ${sjablonSamvaersfradragListe.size}")
        }

        // Henter sjabloner for bidragsevne
        val sjablonBidragsevneListe = SjablonProvider.hentSjablonBidragsevne()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Bidragsevne: ${sjablonBidragsevneListe.size}")
        }

        // Henter sjabloner for trinnvis skattesats
        val sjablonTrinnvisSkattesatsListe = SjablonProvider.hentSjablonTrinnvisSkattesats()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Trinnvis skattesats: ${sjablonTrinnvisSkattesatsListe.size}")
        }

        return SjablonListe(sjablontallListe, sjablonSamvaersfradragListe, sjablonBidragsevneListe, sjablonTrinnvisSkattesatsListe)
    }

    private fun tilJson(json: Any): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.writerWithDefaultPrettyPrinter()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper.writeValueAsString(json)
    }

    private fun bestemGrunnlagstype(referanse: String) = when {
        referanse.contains(Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_SUM_INNTEKT
        referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE.name) -> Grunnlagstype.BIDRAGSEVNE
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRTILSKUDD.name) -> Grunnlagstype.BPS_ANDEL_SÆRTILSKUDD
        referanse.contains(Grunnlagstype.SAMVÆRSFRADRAG.name) -> Grunnlagstype.SAMVÆRSFRADRAG
        else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BeregnSærbidragService::class.java)

        private fun mapDato(dato: LocalDate?): LocalDate {
            return if (dato == null || dato.isAfter(LocalDate.parse("9999-12-31"))) {
                LocalDate.parse("9999-12-31")
            } else {
                dato
            }
        }
    }
}
