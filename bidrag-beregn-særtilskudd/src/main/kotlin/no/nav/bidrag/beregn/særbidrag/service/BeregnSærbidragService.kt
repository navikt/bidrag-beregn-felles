package no.nav.bidrag.beregn.særbidrag.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.core.util.InntektUtil.erKapitalinntekt
import no.nav.bidrag.beregn.core.util.InntektUtil.justerKapitalinntekt
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.BPsAndelSærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.IResultatPeriode
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.felles.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.SærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.exception.UgyldigInputException
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPAndelSærbidragCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.BidragsevneCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.CoreMapper.Companion.tilJsonNode
import no.nav.bidrag.beregn.særbidrag.service.mapper.SærbidragCoreMapper
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.inntekt.Inntektstype
import no.nav.bidrag.domene.enums.inntekt.Inntektstype.Companion.inngårIInntektRapporteringer
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.særbidrag.BeregnetSærbidragResultat
import no.nav.bidrag.transport.behandling.beregning.særbidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.særbidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndelSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningVoksneIHustand
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSjablonreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate

@Service
internal class BeregnSærbidragService(
    private val bidragsevneCore: BidragsevneCore = BidragsevneCore(),
    private val bpAndelSærbidragCore: BPsAndelSærbidragCore = BPsAndelSærbidragCore(),
    private val særbidragCore: SærbidragCore = SærbidragCore(),
) {
    fun beregn(grunnlag: BeregnGrunnlag): BeregnetSærbidragResultat {
        secureLogger.debug { "Særbidragberegning - følgende request mottatt: ${tilJson(grunnlag)}" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av særbidrag: " + e.message)
        }

        // Lager en map for sjablontall (id og navn)
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
        }

        // Henter sjabloner
        val sjablonListe = hentSjabloner()

        // Bygger grunnlag til core og utfører delberegninger
        return utførDelberegninger(beregnGrunnlag = grunnlag, sjablontallMap = sjablontallMap, sjablonListe = sjablonListe)
    }

    // ==================================================================================================================================================
    // Bygger grunnlag til core og kaller delberegninger
    private fun utførDelberegninger(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
    ): BeregnetSærbidragResultat {
        val grunnlagReferanseListe = mutableListOf<GrunnlagDto>()

        // ++ Bidragsevne
        val bidragsevneGrunnlagTilCore =
            BidragsevneCoreMapper.mapBidragsevneGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                sjablontallMap = sjablontallMap,
                sjablonListe = sjablonListe,
            )

        val bidragsevneResultatFraCore = beregnBidragsevne(bidragsevneGrunnlagTilCore)

        val innslagKapitalinntektSjablon =
            sjablonListe.sjablonSjablontallResponse.firstOrNull { it.typeSjablon == SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP.id }

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteBidragsevne(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = bidragsevneResultatFraCore,
                grunnlagTilCore = bidragsevneGrunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            ),
        )
//        grunnlagReferanseListe.addAll(
//            lagGrunnlagListeForDelberegning(
//                beregnGrunnlag = beregnGrunnlag,
//                resultatPeriodeListe = bidragsevneResultatFraCore.resultatPeriodeListe,
//                sjablonListe = bidragsevneResultatFraCore.sjablonListe,
//            ),
//        )

        // ++ BPs andel av særbidrag
        val bpAndelSærbidragGrunnlagTilCore =
            BPAndelSærbidragCoreMapper.mapBPsAndelSærbidragGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                sjablontallMap = sjablontallMap,
                sjablonListe = sjablonListe,
            )

        val bpAndelSærbidragResultatFraCore = beregnBPAndelSærbidrag(bpAndelSærbidragGrunnlagTilCore)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteBpAndelSærbidrag(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = bpAndelSærbidragResultatFraCore,
                grunnlagTilCore = bpAndelSærbidragGrunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            ),
        )

//        grunnlagReferanseListe.addAll(
//            lagGrunnlagListeForDelberegning(
//                beregnGrunnlag = beregnGrunnlag,
//                resultatPeriodeListe = bpAndelSærbidragResultatFraCore.resultatPeriodeListe,
//                sjablonListe = bpAndelSærbidragResultatFraCore.sjablonListe,
//            ),
//        )

        // ++ Særbidrag (totalberegning)
        val særbidragGrunnlagTilCore =
            SærbidragCoreMapper.mapSærbidragGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                beregnBidragsevneResultatCore = bidragsevneResultatFraCore,
                beregnBPsAndelSærbidragResultatCore = bpAndelSærbidragResultatFraCore,
            )

        val særbidragResultatFraCore = beregnSærbidrag(særbidragGrunnlagTilCore)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteSærbidrag(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = særbidragResultatFraCore,
                grunnlagTilCore = særbidragGrunnlagTilCore,
                bidragsevneResultatFraCore = bidragsevneResultatFraCore,
                bpAndelSærbidragResultatFraCore = bpAndelSærbidragResultatFraCore,
            ),
        )

//        grunnlagReferanseListe.addAll(
//            lagGrunnlagReferanseListeSærbidrag(
//                beregnGrunnlag = beregnGrunnlag,
//                beregnSærbidragResultatCore = særbidragResultatFraCore,
//                beregnSærbidragGrunnlagCore = særbidragGrunnlagTilCore,
//                bidragsevneResultatFraCore = bidragsevneResultatFraCore,
//                beregnBPsAndelSærbidragResultatCore = bpAndelSærbidragResultatFraCore,
//            ),
//        )

        val resultatPeriodeListe = lagSluttperiodeOgResultatperioder(
            resultatPeriodeCoreListe = særbidragResultatFraCore.resultatPeriodeListe,
            grunnlagReferanseListe = grunnlagReferanseListe,
            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse
        )

        // Bygger responsobjekt
        val respons =
            BeregnetSærbidragResultat(
                beregnetSærbidragPeriodeListe = resultatPeriodeListe,
//                beregnetSærbidragPeriodeListe = mapFraResultatPeriodeCore(særbidragResultatFraCore.resultatPeriodeListe),
                grunnlagListe = grunnlagReferanseListe
                    .distinctBy { it.referanse }
                    .sortedWith(compareBy<GrunnlagDto> { it.type.toString() }.thenBy { it.referanse }),
            )

        secureLogger.debug { "Særbidragberegning - returnerer følgende respons: ${tilJson(respons)}" }

        return respons
    }

    private fun mapFraResultatPeriodeCore(resultatPeriodeCoreListe: List<ResultatPeriodeCore>) =
        resultatPeriodeCoreListe
            .map {
                ResultatPeriode(
                    periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                    //TODO Null check
                    resultat = ResultatBeregning(it.resultat.beløp, Resultatkode.fraKode(it.resultat.kode)!!),
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

    // Særbidrag
    private fun lagGrunnlagReferanseListeSærbidrag(
        beregnGrunnlag: BeregnGrunnlag,
        beregnSærbidragResultatCore: BeregnSærbidragResultatCore,
        beregnSærbidragGrunnlagCore: BeregnSærbidragGrunnlagCore,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
        beregnBPsAndelSærbidragResultatCore: BeregnBPsAndelSærbidragResultatCore,
    ): List<GrunnlagDto> {
        val resultatGrunnlagListe = ArrayList<GrunnlagDto>()

        // Bygger opp oversikt over alle grunnlag som er brukt i beregningen
        val grunnlagReferanseListe = beregnSærbidragResultatCore.resultatPeriodeListe
            .flatMap { it.grunnlagsreferanseListe }
            .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen
        resultatGrunnlagListe.addAll(
            beregnGrunnlag.grunnlagListe
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map { GrunnlagDto(referanse = it.referanse, type = it.type, innhold = it.innhold) }
        )

        // Mapper ut delberegninger som er brukt som grunnlag
        resultatGrunnlagListe.addAll(
            beregnSærbidragGrunnlagCore.bidragsevnePeriodeListe
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                        innhold = lagInnholdBidragsevne(bidragsevnePeriodeCore = it, beregnBidragsevneResultatCore = bidragsevneResultatFraCore),
                    )
                }
        )
        resultatGrunnlagListe.addAll(
            beregnSærbidragGrunnlagCore.bPsAndelSærbidragPeriodeListe
                .filter { grunnlagReferanseListe.contains(it.referanse) }
                .map {
                    GrunnlagDto(
                        referanse = it.referanse,
                        type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRBIDRAG,
                        innhold = lagInnholdBPsAndelSærbidrag(
                            bPsAndelSærbidragPeriodeCore = it,
                            beregnBPsAndelSærbidragResultatCore = beregnBPsAndelSærbidragResultatCore
                        ),
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
            hentReferanseListeFraResultatPeriodeCore(
                resultatPeriodeListe = beregnBidragsevneResultatCore.resultatPeriodeListe,
                datoFom = bidragsevnePeriodeCore.periode.datoFom,
            )
        val bidragsevne =
            DelberegningBidragsevne(
                periode = ÅrMånedsperiode(fom = bidragsevnePeriodeCore.periode.datoFom, til = bidragsevnePeriodeCore.periode.datoTil),
                beløp = bidragsevnePeriodeCore.beløp,
            )
        return tilJsonNode(bidragsevne)
    }

    // Mapper ut innhold fra delberegning BPsAndelSærbidrag
    private fun lagInnholdBPsAndelSærbidrag(
        bPsAndelSærbidragPeriodeCore: BPsAndelSærbidragPeriodeCore,
        beregnBPsAndelSærbidragResultatCore: BeregnBPsAndelSærbidragResultatCore,
    ): JsonNode {
        val grunnlagReferanseListe =
            hentReferanseListeFraResultatPeriodeCore(
                resultatPeriodeListe = beregnBPsAndelSærbidragResultatCore.resultatPeriodeListe,
                datoFom = bPsAndelSærbidragPeriodeCore.periode.datoFom,
            )
        val bPsAndelSærbidrag =
            DelberegningBidragspliktigesAndelSærbidrag(
                periode = ÅrMånedsperiode(fom = bPsAndelSærbidragPeriodeCore.periode.datoFom, til = bPsAndelSærbidragPeriodeCore.periode.datoTil),
                andelProsent = bPsAndelSærbidragPeriodeCore.andelProsent,
                andelBeløp = bPsAndelSærbidragPeriodeCore.andelBeløp,
                barnetErSelvforsørget = bPsAndelSærbidragPeriodeCore.barnetErSelvforsørget,
            )
        return tilJsonNode(bPsAndelSærbidrag)
    }

    //TODO Må gåes gjennom. Må mappe også andre typer sjabloner enn SjablonNavn
    private fun mapSjabloner(sjablonResultatGrunnlagCoreListe: List<SjablonResultatGrunnlagCore>) =
        sjablonResultatGrunnlagCoreListe
            .map {
                val sjablonPeriode =
                    SjablonSjablontallPeriode(
                        periode = ÅrMånedsperiode(fom = mapDato(it.periode.datoFom), til = mapDato(it.periode.datoTil)),
                        sjablon = SjablonTallNavn.from(it.navn),
                        verdi = it.verdi,
                    )
                GrunnlagDto(referanse = it.referanse, type = Grunnlagstype.SJABLON, innhold = tilJsonNode(sjablonPeriode))
            }

    private fun hentReferanseListeFraResultatPeriodeCore(resultatPeriodeListe: List<IResultatPeriode?>, datoFom: LocalDate) =
        resultatPeriodeListe
            .firstOrNull { it?.periode?.datoFom == datoFom }
            ?.grunnlagsreferanseListe
            ?: emptyList()

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
    private fun beregnBPAndelSærbidrag(bpAndelSærbidragGrunnlagTilCore: BeregnBPsAndelSærbidragGrunnlagCore): BeregnBPsAndelSærbidragResultatCore {
        secureLogger.debug { "BP's andel av særbidrag - grunnlag for beregning: ${tilJson(bpAndelSærbidragGrunnlagTilCore)}" }

        // Kaller core-modulen for beregning av BPs andel av særbidrag
        val bpAndelSærbidragResultatFraCore =
            try {
                bpAndelSærbidragCore.beregnBPsAndelSærbidrag(bpAndelSærbidragGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av BPs andel av særbidrag: " + e.message)
            }

        if (bpAndelSærbidragResultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = bpAndelSærbidragResultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            secureLogger.warn { "Ugyldig input ved beregning av BPs andel av særbidrag. Følgende avvik ble funnet: $avviktekst" }
            throw UgyldigInputException("Ugyldig input ved beregning av BPs andel av særbidrag. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.debug { "BPs andel av særbidrag - resultat av beregning: ${tilJson(bpAndelSærbidragResultatFraCore.resultatPeriodeListe)}" }

        return bpAndelSærbidragResultatFraCore
    }

    // Kaller core for beregning av særbidrag
    private fun beregnSærbidrag(særbidragGrunnlagTilCore: BeregnSærbidragGrunnlagCore): BeregnSærbidragResultatCore {
        secureLogger.debug { "Særbidrag - grunnlag for beregning: ${tilJson(særbidragGrunnlagTilCore)}" }

        // Kaller core-modulen for beregning av særbidrag
        val særbidragResultatFraCore =
            try {
                særbidragCore.beregnSærbidrag(særbidragGrunnlagTilCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av særbidrag: " + e.message)
            }

        if (særbidragResultatFraCore.avvikListe.isNotEmpty()) {
            val avviktekst = særbidragResultatFraCore.avvikListe.joinToString("; ") { it.avvikTekst }
            secureLogger.warn { "Ugyldig input ved beregning av særbidrag. Følgende avvik ble funnet: $avviktekst" }
            throw UgyldigInputException("Ugyldig input ved beregning av særbidrag. Følgende avvik ble funnet: $avviktekst")
        }

        secureLogger.debug { "Særbidrag - resultat av beregning: ${tilJson(særbidragResultatFraCore.resultatPeriodeListe)}" }

        return særbidragResultatFraCore
    }

    // ==================================================================================================================================================
    // Henter sjabloner
    private fun hentSjabloner(): SjablonListe {
        // Henter sjabloner for sjablontall
        val sjablontallListe = SjablonProvider.hentSjablontall()
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Antall sjabloner hentet av type Sjablontall: ${sjablontallListe.size}")
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

        return SjablonListe(
            sjablonSjablontallResponse = sjablontallListe,
            sjablonBidragsevneResponse = sjablonBidragsevneListe,
            sjablonTrinnvisSkattesatsResponse = sjablonTrinnvisSkattesatsListe
        )
    }

    // ==================================================================================================================================================

    // Lager en liste over resultatgrunnlag for delberegning bidragsevne som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagslisteBidragsevne(
        beregnGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnBidragsevneResultatCore,
        grunnlagTilCore: BeregnBidragsevneGrunnlagCore,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): MutableList<GrunnlagDto> {

        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.resultatPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()
                .sorted()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapMottatteGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe
            )
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektListe = grunnlagTilCore.inntektPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumAntallBarnListe = grunnlagTilCore.barnIHusstandenPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val voksneIHusstandenListe = grunnlagTilCore.voksneIHusstandenPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            )
        )

        // Mapper ut DelberegningBarnIHusstand
        resultatGrunnlagListe.addAll(mapDelberegningBarnIHusstand(sumAntallBarnListe))

        // Mapper ut DelberegningVoksneIHusstand
        resultatGrunnlagListe.addAll(mapDelberegningVoksneIHusstand(voksneIHusstandenListe))

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe =
            sumInntektListe.flatMap { it.grunnlagsreferanseListe }
                .union(sumAntallBarnListe.flatMap { it.grunnlagsreferanseListe }
                    .union(voksneIHusstandenListe.flatMap { it.grunnlagsreferanseListe }
                    )
                )
                .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            mapDelberegningGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                delberegningReferanseListe = delberegningReferanseListe
            )
        )

        // Mapper ut grunnlag basert på liste over sjabloner av type Sjablontall som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjablonSjablontallGrunnlag(resultatFraCore.sjablonListe))
        resultatGrunnlagListe.addAll(mapSjablonTrinnvisSkattesatsGrunnlag(resultatFraCore.sjablonListe))

        //TODO Legge til andre typer sjabloner enn sjablontall

        // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
        if (delberegningReferanseListe.any { it.contains("kapitalinntekt", ignoreCase = true) } && innslagKapitalinntektSjablon != null) {
            resultatGrunnlagListe.add(mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon))
        }

        return resultatGrunnlagListe
    }

    // Lager en liste over resultatgrunnlag for delberegning BPs andel særbidrag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagslisteBpAndelSærbidrag(
        beregnGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnBPsAndelSærbidragResultatCore,
        grunnlagTilCore: BeregnBPsAndelSærbidragGrunnlagCore,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): MutableList<GrunnlagDto> {

        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.resultatPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapMottatteGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe
            )
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektBPListe = grunnlagTilCore.inntektBPPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumInntektBMListe = grunnlagTilCore.inntektBMPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumInntektSBListe = grunnlagTilCore.inntektSBPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektBPListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            )
        )
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektBMListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            )
        )
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektSBListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
            )
        )

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe =
            sumInntektBPListe.flatMap { it.grunnlagsreferanseListe }
                .union(sumInntektBMListe.flatMap { it.grunnlagsreferanseListe }
                    .union(sumInntektSBListe.flatMap { it.grunnlagsreferanseListe }
                    )
                )
                .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            mapDelberegningGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                delberegningReferanseListe = delberegningReferanseListe
            )
        )

        // Mapper ut grunnlag basert på liste over sjabloner av type Sjablontall som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjablonSjablontallGrunnlag(resultatFraCore.sjablonListe))

        // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
        if (delberegningReferanseListe.any { it.contains("kapitalinntekt", ignoreCase = true) } && innslagKapitalinntektSjablon != null) {
            resultatGrunnlagListe.add(mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon))
        }

        return resultatGrunnlagListe
    }

    // Lager en liste over resultatgrunnlag for delberegning særbidrag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagslisteSærbidrag(
        beregnGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnSærbidragResultatCore,
        grunnlagTilCore: BeregnSærbidragGrunnlagCore,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
        bpAndelSærbidragResultatFraCore: BeregnBPsAndelSærbidragResultatCore
    ): MutableList<GrunnlagDto> {

        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.resultatPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

//        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
//        resultatGrunnlagListe.addAll(
//            mapMottatteGrunnlag(
//                grunnlagListe = beregnGrunnlag.grunnlagListe,
//                grunnlagReferanseListe = grunnlagReferanseListe
//            )
//        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val bidragsevneListe = grunnlagTilCore.bidragsevnePeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val bPsAndelSærbidragListe = grunnlagTilCore.bPsAndelSærbidragPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut delberegning bidragsevne
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragsevne(
                bidragsevneListe = bidragsevneListe,
                bidragsevneResultatFraCore = bidragsevneResultatFraCore
            )
        )

        // Mapper ut delberegning BPs andel særbidrag
        resultatGrunnlagListe.addAll(
            mapDelberegningBpsAndelSærbidrag(
                bPsAndelSærbidragListe = bPsAndelSærbidragListe,
                bpAndelSærbidragResultatFraCore = bpAndelSærbidragResultatFraCore
            )
        )

        // Lager en liste av referanser som refereres til av delberegningene
        // TODO?
//        val delberegningReferanseListe =
//            bidragsevneListe.flatMap { it.grunnlagsreferanseListe }
//                .union(bPsAndelSærbidragListe.flatMap { it.grunnlagsreferanseListe }
//                )
//                .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        // TODO?
//        resultatGrunnlagListe.addAll(
//            mapDelberegningGrunnlag(
//                grunnlagListe = beregnGrunnlag.grunnlagListe,
//                delberegningReferanseListe = delberegningReferanseListe
//            )
//        )

        return resultatGrunnlagListe
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapMottatteGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) =
        grunnlagListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = it.type,
                    innhold = it.innhold,
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = it.gjelderReferanse,
                )
            }

    // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
    private fun mapDelberegningSumInntekt(
        sumInntektListe: List<InntektPeriodeCore>,
        beregnGrunnlag: BeregnGrunnlag,
        innslagKapitalinntektSjablon: Sjablontall?
    ) =
        sumInntektListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningSumInntekt(
                            periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                            totalinntekt = it.beløp,
                            kontantstøtte = summerInntekter(
                                beregnGrunnlag = beregnGrunnlag,
                                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer(),
                            ),
                            skattepliktigInntekt = summerInntekter(
                                beregnGrunnlag = beregnGrunnlag,
                                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                inntektsrapporteringListe = Inntektstype.KONTANTSTØTTE.inngårIInntektRapporteringer() +
                                    Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer() +
                                    Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer() +
                                    Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                                ekskluderInntekter = true,
                                innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablon?.verdi ?: BigDecimal.ZERO,
                            ),
                            barnetillegg = summerInntekter(
                                beregnGrunnlag = beregnGrunnlag,
                                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                inntektsrapporteringListe = Inntektstype.BARNETILLEGG_PENSJON.inngårIInntektRapporteringer(),
                            ),
                            utvidetBarnetrygd = summerInntekter(
                                beregnGrunnlag = beregnGrunnlag,
                                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                inntektsrapporteringListe = Inntektstype.UTVIDET_BARNETRYGD.inngårIInntektRapporteringer(),
                            ),
                            småbarnstillegg = summerInntekter(
                                beregnGrunnlag = beregnGrunnlag,
                                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                                inntektsrapporteringListe = Inntektstype.SMÅBARNSTILLEGG.inngårIInntektRapporteringer(),
                            ),
                        ),
                    ),
                    grunnlagsreferanseListe = lagGrunnlagsreferanselisteInntekt(
                        grunnlagsreferanseliste = it.grunnlagsreferanseListe,
                        innslagKapitalinntektSjablon = innslagKapitalinntektSjablon
                    ).sorted(),
                )
            }

    // Mapper ut DelberegningBarnIHusstand
    private fun mapDelberegningBarnIHusstand(sumAntallBarnListe: List<BarnIHusstandenPeriodeCore>) =
        sumAntallBarnListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningBarnIHusstand(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            antallBarn = it.antall,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                )
            }

    // Mapper ut DelberegningVoksneIHusstand
    private fun mapDelberegningVoksneIHusstand(voksneIHusstandenListe: List<VoksneIHusstandenPeriodeCore>) =
        voksneIHusstandenListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningVoksneIHustand(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            borMedAndreVoksne = it.borMedAndre,
                        ),
                    ),
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                )
            }

    // Mapper ut DelberegningBidragsevne
    private fun mapDelberegningBidragsevne(
        bidragsevneListe: List<BidragsevnePeriodeCore>,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore
    ) =
        bidragsevneListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningBidragsevne(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            beløp = it.beløp,
                        ),
                    ),
//TODO Må testes nøye
                    grunnlagsreferanseListe = bidragsevneResultatFraCore.resultatPeriodeListe
                        .firstOrNull { resultatPeriode -> resultatPeriode.periode.datoFom == it.periode.datoFom }
                        ?.grunnlagsreferanseListe ?: emptyList<Grunnlagsreferanse>()
                        .sorted(),
                )
            }

    // Mapper ut DelberegningBpsAndelSærbidrag
    private fun mapDelberegningBpsAndelSærbidrag(
        bPsAndelSærbidragListe: List<BPsAndelSærbidragPeriodeCore>,
        bpAndelSærbidragResultatFraCore: BeregnBPsAndelSærbidragResultatCore
    ) =
        bPsAndelSærbidragListe
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = bestemGrunnlagstype(it.referanse),
                    innhold = POJONode(
                        DelberegningBidragspliktigesAndelSærbidrag(
                            periode = ÅrMånedsperiode(fom = it.periode.datoFom, til = it.periode.datoTil),
                            andelProsent = it.andelProsent,
                            andelBeløp = it.andelBeløp,
                            barnetErSelvforsørget = it.barnetErSelvforsørget
                        ),
                    ),
//TODO Må testes nøye
                    grunnlagsreferanseListe = bpAndelSærbidragResultatFraCore.resultatPeriodeListe
                        .firstOrNull { resultatPeriode -> resultatPeriode.periode.datoFom == it.periode.datoFom }
                        ?.grunnlagsreferanseListe ?: emptyList<Grunnlagsreferanse>()
                        .sorted(),
                )
            }

    // Mapper ut grunnlag som er brukt av delberegningene
    private fun mapDelberegningGrunnlag(grunnlagListe: List<GrunnlagDto>, delberegningReferanseListe: List<String>) =
        grunnlagListe
            .filter { it.referanse in delberegningReferanseListe }
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = it.type,
                    innhold = it.innhold,
                    grunnlagsreferanseListe = it.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = it.gjelderReferanse,
                )
            }

    // Mapper ut grunnlag basert på liste over sjabloner av type Sjablontall som er brukt i beregningen
    private fun mapSjablonSjablontallGrunnlag(sjablonListe: List<SjablonResultatGrunnlagCore>) =
        sjablonListe
            .filter { sjablon -> SjablonTallNavn.entries.any { it.navn == sjablon.navn } }
            .map { sjablon ->
                GrunnlagDto(
                    referanse = sjablon.referanse,
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonSjablontallPeriode(
                            periode = ÅrMånedsperiode(sjablon.periode.datoFom, sjablon.periode.datoTil),
                            sjablon = SjablonTallNavn.from(sjablon.navn),
                            verdi = sjablon.verdi,
                        ),
                    ),
                )
            }

    // Mapper ut grunnlag basert på liste over sjabloner av type Sjablontall som er brukt i beregningen
    private fun mapSjablonTrinnvisSkattesatsGrunnlag(sjablonListe: MutableList<SjablonResultatGrunnlagCore>): List<GrunnlagDto> {
//TODO Spørre ChatGPT om forbedring
        val grunnlagDtoListe = mutableListOf<GrunnlagDto>()

        val unikePerioderListe = sjablonListe
            .filter { it.navn.contains(SjablonNavn.TRINNVIS_SKATTESATS.navn + "InntektGrense") }
            .map { it.periode }.distinct()

        unikePerioderListe.forEach { periode ->
            val filtrertListeInntektGrense = sjablonListe
                .filter { it.periode.datoFom == periode.datoFom }
                .filter { it.navn.contains(SjablonNavn.TRINNVIS_SKATTESATS.navn + "InntektGrense") }
                .sortedWith(compareBy<SjablonResultatGrunnlagCore> { it.periode.datoFom.toString() }.thenBy { it.navn })
            val filtrertListeSats = sjablonListe
                .filter { it.periode.datoFom == periode.datoFom }
                .filter { it.navn.contains(SjablonNavn.TRINNVIS_SKATTESATS.navn + "Sats") }
                .sortedWith(compareBy<SjablonResultatGrunnlagCore> { it.periode.datoFom.toString() }.thenBy { it.navn })

            var indeks = 0
            val sjablonTrinnvisSkattesatsListe = mutableListOf<SjablonTrinnvisSkattesats>()

            filtrertListeInntektGrense.forEach {
                sjablonTrinnvisSkattesatsListe.add(
                    SjablonTrinnvisSkattesats(
//TODO
                        inntekstgrense = it.verdi.intValueExact(),
                        sats = filtrertListeSats[indeks].verdi,
                    )
                )
                indeks++
            }

            grunnlagDtoListe.add(
                GrunnlagDto(
                    referanse = "Sjablon_TrinnvisSkattesats_${periode.datoFom}",
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonTrinnvisSkattesatsPeriode(
                            periode = ÅrMånedsperiode(periode.datoFom, periode.datoTil),
                            trinnliste = sjablonTrinnvisSkattesatsListe,
                        ),
                    )
                )
            )
        }

        // Fjerner gamle referanser til trinnvis skattesats og legger til nye referanse
        sjablonListe.removeAll { it.navn.contains ("TrinnvisSkattesats", ignoreCase = true)}

        return grunnlagDtoListe
    }


    // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
    private fun mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon: Sjablontall) =
        GrunnlagDto(
            referanse = opprettSjablonreferanse(
                navn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!).navn,
                periode = ÅrMånedsperiode(fom = innslagKapitalinntektSjablon.datoFom!!, til = innslagKapitalinntektSjablon.datoTom),
            ),
            type = Grunnlagstype.SJABLON,
            innhold = POJONode(
                SjablonSjablontallPeriode(
                    periode = ÅrMånedsperiode(innslagKapitalinntektSjablon.datoFom!!, innslagKapitalinntektSjablon.datoTom),
                    sjablon = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!),
                    verdi = innslagKapitalinntektSjablon.verdi!!,
                ),
            ),
        )

    private fun bestemGrunnlagstype(referanse: String) = when {
        referanse.contains(Grunnlagstype.DELBEREGNING_SUM_INNTEKT.name) -> Grunnlagstype.DELBEREGNING_SUM_INNTEKT
        referanse.contains(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
        referanse.contains(Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND.name) -> Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSEVNE
//TODO Må fikses
        referanse.contains("Delberegning_BP_Bidragsevne") -> Grunnlagstype.DELBEREGNING_BIDRAGSEVNE
        referanse.contains(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRBIDRAG.name) -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRBIDRAG
//TODO Må fikses
        referanse.contains("Delberegning_BP_AndelSærbidrag") -> Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL_SÆRBIDRAG
        else -> throw IllegalArgumentException("Ikke i stand til å utlede grunnlagstype for referanse: $referanse")
    }

    // Summerer inntekter som matcher med en liste over referanser og som inkluderer eller ekskluderer en liste over inntektsrapporteringstyper
    // (basert på om inputparameter ekskluderInntekter er satt til true eller false). Hvis den filtrerte inntektslisten er tom, returneres null.
    private fun summerInntekter(
        beregnGrunnlag: BeregnGrunnlag,
        grunnlagsreferanseListe: List<String>,
        inntektsrapporteringListe: List<Inntektsrapportering>,
        ekskluderInntekter: Boolean = false,
        innslagKapitalinntektSjablonverdi: BigDecimal = BigDecimal.ZERO,
    ): BigDecimal? {
        var summertInntekt: BigDecimal? = BigDecimal.ZERO
        beregnGrunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<InntektsrapporteringPeriode>(grunnlagType = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE)
            .filter { it.referanse in grunnlagsreferanseListe }
            .let { filtrertListe ->
                if (ekskluderInntekter) {
                    filtrertListe.filterNot { it.innhold.inntektsrapportering in inntektsrapporteringListe }
                } else {
                    filtrertListe.filter { it.innhold.inntektsrapportering in inntektsrapporteringListe }
                }
            }
            .let { filtrertListe ->
                if (filtrertListe.isNotEmpty()) {
                    filtrertListe.forEach {
                        summertInntekt = summertInntekt?.plus(
                            if (erKapitalinntekt(it.innhold.inntektsrapportering)) {
                                justerKapitalinntekt(
                                    beløp = it.innhold.beløp,
                                    innslagKapitalinntektSjablonverdi = innslagKapitalinntektSjablonverdi,
                                )
                            } else {
                                it.innhold.beløp
                            },
                        )
                    }
                } else {
                    summertInntekt = null
                }
            }
        return summertInntekt
    }

    // Legger til referanse for sjablon 0006 (innslag kapitalinntekt) hvis det er kapitalinntekt i grunnlagsreferanseliste
    private fun lagGrunnlagsreferanselisteInntekt(
        grunnlagsreferanseliste: List<String>,
        innslagKapitalinntektSjablon: Sjablontall?,
    ): List<Grunnlagsreferanse> {
        return if (grunnlagsreferanseliste.any { it.contains("kapitalinntekt", ignoreCase = true) }) {
            if (innslagKapitalinntektSjablon != null) {
                grunnlagsreferanseliste + opprettSjablonreferanse(
                    navn = SjablonTallNavn.fromId(innslagKapitalinntektSjablon.typeSjablon!!).navn,
                    periode = ÅrMånedsperiode(fom = innslagKapitalinntektSjablon.datoFom!!, til = innslagKapitalinntektSjablon.datoTom),
                )
            } else {
                grunnlagsreferanseliste
            }
        } else {
            grunnlagsreferanseliste
        }
    }

    // Oppretter resultatperioder som refererer til sluttberegning, som igjen refererer til delberegninger og grunnlag
    private fun lagSluttperiodeOgResultatperioder(
        resultatPeriodeCoreListe: List<ResultatPeriodeCore>,
        grunnlagReferanseListe: MutableList<GrunnlagDto>,
//TODO Skal denne brukes?
        søknadsbarnReferanse: String
    ): List<ResultatPeriode> {
        return resultatPeriodeCoreListe.map { resultatPeriode ->

            val sluttberegningReferanse = opprettSluttberegningreferanse(
                barnreferanse = søknadsbarnReferanse,
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
            )

            // Oppretter sluttberegning, som legges til i grunnlagslista
            grunnlagReferanseListe.add(
                0,
                GrunnlagDto(
                    referanse = sluttberegningReferanse,
                    type = Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG,
                    innhold = POJONode(
                        SluttberegningSærbidrag(
                            periode = ÅrMånedsperiode(resultatPeriode.periode.datoFom, resultatPeriode.periode.datoTil),
                            beløp = resultatPeriode.resultat.beløp,
//TODO Null check. Vurdere å endre kode i ResultatPeriodeCore til Resultatkode (tilsvarende forskudd)?
                            resultatKode = Resultatkode.fraKode(resultatPeriode.resultat.kode)!!,
                        ),
                    ),
                    grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe.sorted(),
                    gjelderReferanse = søknadsbarnReferanse,
                ),
            )

            // Oppretter resultatperioder, som refererer til sluttberegning
            ResultatPeriode(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
                resultat =
                ResultatBeregning(
                    beløp = resultatPeriode.resultat.beløp,
//TODO Null check
                    resultatkode = Resultatkode.fraKode(resultatPeriode.resultat.kode)!!,
                ),
                grunnlagsreferanseListe = listOf(sluttberegningReferanse).sorted(),
            )
        }
    }

    // ==================================================================================================================================================

    private fun tilJson(json: Any): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.writerWithDefaultPrettyPrinter()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return objectMapper.writeValueAsString(json)
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
