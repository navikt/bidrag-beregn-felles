package no.nav.bidrag.beregn.særbidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.dto.SjablonResultatGrunnlagCore
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.beregn.core.mapping.mapTilGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.BidragsevneCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.BPsAndelSærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.BPsBeregnedeTotalbidragCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.BeregnBPsBeregnedeTotalbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.LøpendeBidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.felles.bo.SjablonListe
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.SærbidragCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsBeregnedeTotalbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPAndelSærbidragCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPAndelSærbidragCoreMapper.finnInnslagKapitalinntektFraSjablontallListe
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPAndelSærbidragCoreMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.særbidrag.service.mapper.BPsBeregnedeTotalbidragCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.BidragsevneCoreMapper
import no.nav.bidrag.beregn.særbidrag.service.mapper.SærbidragCoreMapper
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.beregning.særbidrag.BeregnetSærbidragResultat
import no.nav.bidrag.transport.behandling.beregning.særbidrag.ResultatBeregning
import no.nav.bidrag.transport.behandling.beregning.særbidrag.ResultatPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesBeregnedeTotalbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesats
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonTrinnvisSkattesatsPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList

@Service
internal class BeregnSærbidragService(
    private val bidragsevneCore: BidragsevneCore = BidragsevneCore(),
    private val bPsBeregnedeTotalbidragCore: BPsBeregnedeTotalbidragCore = BPsBeregnedeTotalbidragCore(),
    private val bpAndelSærbidragCore: BPsAndelSærbidragCore = BPsAndelSærbidragCore(),
    private val særbidragCore: SærbidragCore = SærbidragCore(),
) : BeregnService() {
    fun beregn(grunnlag: BeregnGrunnlag, vedtakstype: Vedtakstype): BeregnetSærbidragResultat {
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
        return utførDelberegninger(beregnGrunnlag = grunnlag, sjablontallMap = sjablontallMap, sjablonListe = sjablonListe, vedtakstype = vedtakstype)
    }

    fun validerForBeregning(
        vedtakstype: Vedtakstype,
        delberegningUtgift: DelberegningUtgift,
        sjablonListe: SjablonListe = hentSjabloner(),
    ): Resultatkode? = if (vedtakstype == Vedtakstype.FASTSETTELSE) {
        val forskuddssats = sjablonListe.sjablonSjablontallResponse
            .filter { it.typeSjablon == SjablonTallNavn.FORSKUDDSSATS_BELØP.id }
            .maxBy { it.datoTom ?: it.datoFom!! }
        if (delberegningUtgift.sumGodkjent < forskuddssats.verdi) Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS else null
    } else {
        null
    }

    fun validerValutakode(LøpendeBidragGrunnlag: LøpendeBidragGrunnlag): Boolean =
        LøpendeBidragGrunnlag.løpendeBidragListe.all { it.valutakode == "NOK" }

    // ==================================================================================================================================================
    // Bygger grunnlag til core og kaller delberegninger
    private fun utførDelberegninger(
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
        sjablonListe: SjablonListe,
        vedtakstype: Vedtakstype,
    ): BeregnetSærbidragResultat {
        val grunnlagReferanseListe = mutableListOf<GrunnlagDto>()

        val bidragspliktigReferanse = finnReferanseTilRolle(
            grunnlagListe = beregnGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
        )
        val bidragsmottakerReferanse = finnReferanseTilRolle(
            grunnlagListe = beregnGrunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        // For fastsettelse så skal det Sjekkes om godkjent beløp er mindre enn sats for fullt forskudd. Det gjøres ingen beregning hvis det er tilfelle.
        if (vedtakstype == Vedtakstype.FASTSETTELSE) {
            val delberegningUtgift = try {
                beregnGrunnlag.grunnlagListe
                    .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUtgift>(Grunnlagstype.DELBEREGNING_UTGIFT)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.DELBEREGNING_UTGIFT er ikke gyldig: " + e.message,
                )
            }.first()

            validerForBeregning(vedtakstype, delberegningUtgift.innhold, sjablonListe).takeIf {
                it == Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS
            }?.let {
                val forskuddssats = sjablonListe.sjablonSjablontallResponse
                    .filter { it.typeSjablon == SjablonTallNavn.FORSKUDDSSATS_BELØP.id }
                    .maxBy { it.datoTom ?: it.datoFom!! }
                return lagResponsGodkjentBeløpUnderForskuddssats(beregnGrunnlag, forskuddssats)
            }
        }

        // Bidragsevne
        val bidragsevneGrunnlagTilCore =
            BidragsevneCoreMapper.mapBidragsevneGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                sjablontallMap = sjablontallMap,
                sjablonListe = sjablonListe,
            )

        val bidragsevneResultatFraCore = beregnBidragsevne(bidragsevneGrunnlagTilCore)

        // Henter sjablonverdi for kapitalinntekt
        val innslagKapitalinntektSjablon = finnInnslagKapitalinntektFraSjablontallListe(sjablonListe.sjablonSjablontallResponse)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteBidragsevne(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = bidragsevneResultatFraCore,
                grunnlagTilCore = bidragsevneGrunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        // Sum løpende bidrag
        val bPsBeregnedeTotalbidragGrunnlagCore =
            BPsBeregnedeTotalbidragCoreMapper.mapBPsBeregnedeTotalbidragGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                sjablonListe = sjablonListe,
            )

        val bPsBeregnedeTotalbidragResultatFraCore = beregnBPsBeregnedeTotalbidrag(bPsBeregnedeTotalbidragGrunnlagCore)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteBPsBeregnedeTotalbidrag(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = bPsBeregnedeTotalbidragResultatFraCore,
            ),
        )

        // BPs andel av særbidrag
        val bpAndelSærbidragGrunnlagTilCore =
            BPAndelSærbidragCoreMapper.mapBPsAndelSærbidragGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                sjablontallMap = sjablontallMap,
                sjablonListe = sjablonListe,
                innslagKapitalinntekt = innslagKapitalinntektSjablon?.verdi ?: BigDecimal.ZERO,
            )

        val bpAndelSærbidragResultatFraCore = beregnBPAndelSærbidrag(bpAndelSærbidragGrunnlagTilCore)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteBpAndelSærbidrag(
                beregnGrunnlag = beregnGrunnlag,
                resultatFraCore = bpAndelSærbidragResultatFraCore,
                grunnlagTilCore = bpAndelSærbidragGrunnlagTilCore,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                bidragsmottakerReferanse = bidragsmottakerReferanse,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        // Særbidrag (totalberegning)
        val særbidragGrunnlagTilCore =
            SærbidragCoreMapper.mapSærbidragGrunnlagTilCore(
                beregnGrunnlag = beregnGrunnlag,
                beregnBidragsevneResultatCore = bidragsevneResultatFraCore,
                beregnBPsBeregnedeTotalbidragResultatCore = bPsBeregnedeTotalbidragResultatFraCore,
                beregnBPsAndelSærbidragResultatCore = bpAndelSærbidragResultatFraCore,
            )

        val særbidragResultatFraCore = beregnSærbidrag(særbidragGrunnlagTilCore)

        grunnlagReferanseListe.addAll(
            lagGrunnlagslisteSærbidrag(
                resultatFraCore = særbidragResultatFraCore,
                grunnlagTilCore = særbidragGrunnlagTilCore,
                bidragsevneResultatFraCore = bidragsevneResultatFraCore,
                bPsBeregnedeTotalbidragResultatCore = bPsBeregnedeTotalbidragResultatFraCore,
                bpAndelSærbidragResultatFraCore = bpAndelSærbidragResultatFraCore,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        val resultatPeriodeListe = lagSluttperiodeOgResultatperioder(
            resultatPeriodeCoreListe = særbidragResultatFraCore.resultatPeriodeListe,
            grunnlagReferanseListe = grunnlagReferanseListe,
            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
        )

        // Sjekker om det er grunnlagsobjekter som refereres av gjelderReferanse og som mangler i outputen
        grunnlagReferanseListe.addAll(
            sjekkGrunnlagRefereranseListeGrunnlag(
                grunnlagReferanseListe = grunnlagReferanseListe,
                beregnGrunnlag = beregnGrunnlag,
            ),
        )

        // Bygger responsobjekt
        val respons =
            BeregnetSærbidragResultat(
                beregnetSærbidragPeriodeListe = resultatPeriodeListe,
                grunnlagListe = grunnlagReferanseListe
                    .distinctBy { it.referanse }
                    .sortedWith(compareBy<GrunnlagDto> { it.type.toString() }.thenBy { it.referanse }),
            )

        secureLogger.debug { "Særbidragberegning - returnerer følgende respons: ${tilJson(respons)}" }

        return respons
    }

    // ===============================================================================================================================================
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

        håndterAvvik(bidragsevneResultatFraCore.avvikListe, "bidragsevne")

        secureLogger.debug { "Bidragsevne - resultat av beregning: ${tilJson(bidragsevneResultatFraCore.resultatPeriodeListe)}" }

        return bidragsevneResultatFraCore
    }

    // Kaller core for beregning av bPsBeregnedeTotalbidrag
    private fun beregnBPsBeregnedeTotalbidrag(løpendeBidragGrunnlagCore: LøpendeBidragGrunnlagCore): BeregnBPsBeregnedeTotalbidragResultatCore {
        secureLogger.debug { "BPs Beregnede Totalbidrag - grunnlag for beregning: ${tilJson(løpendeBidragGrunnlagCore)}" }

        // Kaller core-modulen for beregning av bPsBeregnedeTotalbidrag
        val bPsBeregnedeTotalbidragResultatFraCore =
            try {
                bPsBeregnedeTotalbidragCore.beregnBPsBeregnedeTotalbidrag(løpendeBidragGrunnlagCore)
            } catch (e: Exception) {
                throw UgyldigInputException("Ugyldig input ved beregning av bidragsevne: " + e.message)
            }

        secureLogger.debug { "BPs Beregnede Totalbidrag - resultat av beregning: ${tilJson(bPsBeregnedeTotalbidragResultatFraCore.resultatPeriode)}" }

        return bPsBeregnedeTotalbidragResultatFraCore
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

        håndterAvvik(bpAndelSærbidragResultatFraCore.avvikListe, "BPs andel av særbidrag")

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

        håndterAvvik(særbidragResultatFraCore.avvikListe, "særbidrag")

        secureLogger.debug { "Særbidrag - resultat av beregning: ${tilJson(særbidragResultatFraCore.resultatPeriodeListe)}" }

        return særbidragResultatFraCore
    }

    // ===============================================================================================================================================
    // Henter sjabloner
    private fun hentSjabloner(): SjablonListe {
        // Henter sjabloner for sjablontall
        val sjablontallListe = SjablonProvider.hentSjablontall()

        // Henter sjabloner for bidragsevne
        val sjablonBidragsevneListe = SjablonProvider.hentSjablonBidragsevne()

        // Henter sjabloner for samværsfradrag
        val sjablonSamværsfradragListe = SjablonProvider.hentSjablonSamværsfradrag()

        // Henter sjabloner for trinnvis skattesats
        val sjablonTrinnvisSkattesatsListe = SjablonProvider.hentSjablonTrinnvisSkattesats()

        return SjablonListe(
            sjablonSjablontallResponse = sjablontallListe,
            sjablonBidragsevneResponse = sjablonBidragsevneListe,
            sjablonTrinnvisSkattesatsResponse = sjablonTrinnvisSkattesatsListe,
            sjablonSamværsfradragResponse = sjablonSamværsfradragListe,
        )
    }

    // ===============================================================================================================================================

    // Lager en liste over resultatgrunnlag for delberegning bidragsevne som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - "delberegninger" som er brukt i beregningen (og mottatte grunnlag som er brukt i delberegningene)
    //   - sjabloner som er brukt i beregningen
    private fun lagGrunnlagslisteBidragsevne(
        beregnGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnBidragsevneResultatCore,
        grunnlagTilCore: BeregnBidragsevneGrunnlagCore,
        innslagKapitalinntektSjablon: Sjablontall?,
        bidragspliktigReferanse: String,
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
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val sumInntektListe = grunnlagTilCore.inntektPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val boforholdListe = grunnlagTilCore.boforholdPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val sumAntallBarnListe = grunnlagTilCore.barnIHusstandenPeriodeListe
            .filter { boforholdListe.flatMap { it.grunnlagsreferanseListe }.contains(it.referanse) }
        val voksneIHusstandenListe = grunnlagTilCore.voksneIHusstandenPeriodeListe
            .filter { boforholdListe.flatMap { it.grunnlagsreferanseListe }.contains(it.referanse) }

        // Mapper ut DelberegningSumInntekt. Inntektskategorier summeres opp.
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = bidragspliktigReferanse,
            ),
        )

        // Mapper ut DelberegningBoforhold
        resultatGrunnlagListe.addAll(
            boforholdListe.mapTilGrunnlag(bidragspliktigReferanse),
        )

        // Mapper ut DelberegningBarnIHusstand (sub-delberegning til DelberegningBoforhold)
        resultatGrunnlagListe.addAll(
            sumAntallBarnListe.mapTilGrunnlag(bidragspliktigReferanse),
        )

        // Mapper ut DelberegningVoksneIHusstand (sub-delberegning til DelberegningBoforhold)
        resultatGrunnlagListe.addAll(
            voksneIHusstandenListe.mapTilGrunnlag(bidragspliktigReferanse),
        )

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe =
            sumInntektListe.flatMap { it.grunnlagsreferanseListe }
                .union(
                    sumAntallBarnListe.flatMap { it.grunnlagsreferanseListe }
                        .union(
                            voksneIHusstandenListe.flatMap { it.grunnlagsreferanseListe },
                        ),
                )
                .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            mapDelberegningGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                delberegningReferanseListe = delberegningReferanseListe,
            ),
        )

        // Mapper ut grunnlag og justerer referanser basert på lister over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjablonSjablontallGrunnlag(resultatFraCore.sjablonListe))
        resultatGrunnlagListe.addAll(mapSjablonTrinnvisSkattesatsGrunnlag(resultatFraCore.sjablonListe))
        resultatGrunnlagListe.addAll(mapSjablonBidragsevneGrunnlag(resultatFraCore.sjablonListe))
        justerReferanserForSjabloner(resultatFraCore.resultatPeriodeListe)

        // Mapper ut grunnlag for sjablon 0006 hvis kapitalinntekt er brukt i beregningen
        if (delberegningReferanseListe.any { it.contains("kapitalinntekt", ignoreCase = true) } && innslagKapitalinntektSjablon != null) {
            resultatGrunnlagListe.add(mapSjablontallKapitalinntektGrunnlag(innslagKapitalinntektSjablon))
        }

        return resultatGrunnlagListe
    }

    // ===============================================================================================================================================

    // Lager en liste over resultatgrunnlag for delberegning BPs Beregnede Totalbidrag som inneholder:
    //   - mottatte grunnlag som er brukt i beregningen
    //   - sjablon for samværsfradrag
    private fun lagGrunnlagslisteBPsBeregnedeTotalbidrag(
        beregnGrunnlag: BeregnGrunnlag,
        resultatFraCore: BeregnBPsBeregnedeTotalbidragResultatCore,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.resultatPeriode
                .grunnlagsreferanseListe
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapMottatteGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Mapper ut grunnlag og justerer referanser basert på lister over sjabloner som er brukt i beregningen
        resultatGrunnlagListe.addAll(mapSjablonSamværsfradragGrunnlag(resultatFraCore.sjablonListe))

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
        bidragsmottakerReferanse: String,
        bidragspliktigReferanse: String,
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
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
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
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = bidragspliktigReferanse,
            ),
        )
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektBMListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = bidragsmottakerReferanse,
            ),
        )
        resultatGrunnlagListe.addAll(
            mapDelberegningSumInntekt(
                sumInntektListe = sumInntektSBListe,
                beregnGrunnlag = beregnGrunnlag,
                innslagKapitalinntektSjablon = innslagKapitalinntektSjablon,
                referanseTilRolle = beregnGrunnlag.søknadsbarnReferanse,
            ),
        )

        // Lager en liste av referanser som refereres til av delberegningene
        val delberegningReferanseListe =
            sumInntektBPListe.flatMap { it.grunnlagsreferanseListe }
                .union(
                    sumInntektBMListe.flatMap { it.grunnlagsreferanseListe }
                        .union(
                            sumInntektSBListe.flatMap { it.grunnlagsreferanseListe },
                        ),
                )
                .distinct()

        // Mapper ut grunnlag som er brukt av delberegningene
        resultatGrunnlagListe.addAll(
            mapDelberegningGrunnlag(
                grunnlagListe = beregnGrunnlag.grunnlagListe,
                delberegningReferanseListe = delberegningReferanseListe,
            ),
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
        resultatFraCore: BeregnSærbidragResultatCore,
        grunnlagTilCore: BeregnSærbidragGrunnlagCore,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
        bPsBeregnedeTotalbidragResultatCore: BeregnBPsBeregnedeTotalbidragResultatCore,
        bpAndelSærbidragResultatFraCore: BeregnBPsAndelSærbidragResultatCore,
        bidragspliktigReferanse: String,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()
        val grunnlagReferanseListe =
            resultatFraCore.resultatPeriodeListe
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Filtrerer ut delberegninger som er brukt som grunnlag
        val bidragsevneListe = grunnlagTilCore.bidragsevnePeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }
        val bPsBeregnedeTotalbidrag = grunnlagTilCore.bPsBeregnedeTotalbidragPeriodeCore
        val bPsAndelSærbidragListe = grunnlagTilCore.bPsAndelSærbidragPeriodeListe
            .filter { grunnlagReferanseListe.contains(it.referanse) }

        // Mapper ut delberegning bidragsevne
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragsevne(
                bidragsevneListe = bidragsevneListe,
                bidragsevneResultatFraCore = bidragsevneResultatFraCore,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        // Mapper ut delberegning sum løpende bidrag
        resultatGrunnlagListe.add(
            mapDelberegningBPsBeregnedeTotalbidrag(
                bPsBeregnedeTotalbidrag = bPsBeregnedeTotalbidrag,
                beregnBPsBeregnedeTotalbidragResultatCore = bPsBeregnedeTotalbidragResultatCore,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        // Mapper ut delberegning BPs andel særbidrag
        resultatGrunnlagListe.addAll(
            mapDelberegningBpsAndelSærbidrag(
                bPsAndelSærbidragListe = bPsAndelSærbidragListe,
                bpAndelSærbidragResultatFraCore = bpAndelSærbidragResultatFraCore,
                bidragspliktigReferanse = bidragspliktigReferanse,
            ),
        )

        return resultatGrunnlagListe
    }

    // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
    private fun mapMottatteGrunnlag(grunnlagListe: List<GrunnlagDto>, grunnlagReferanseListe: List<String>) = grunnlagListe
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

    // ===============================================================================================================================================

    // Mapper ut DelberegningBidragsevne
    private fun mapDelberegningBidragsevne(
        bidragsevneListe: List<BidragsevnePeriodeCore>,
        bidragsevneResultatFraCore: BeregnBidragsevneResultatCore,
        bidragspliktigReferanse: String,
    ) = bidragsevneListe
        .map { bidragsevne ->
            GrunnlagDto(
                referanse = bidragsevne.referanse,
                type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                innhold = POJONode(
                    DelberegningBidragsevne(
                        periode = ÅrMånedsperiode(fom = bidragsevne.periode.datoFom, til = bidragsevne.periode.datoTil),
                        beløp = bidragsevne.beløp,
                        skatt = bidragsevne.skatt,
                        underholdBarnEgenHusstand = bidragsevne.underholdBarnEgenHusstand,
                    ),
                ),
                grunnlagsreferanseListe = bidragsevneResultatFraCore.resultatPeriodeListe
                    .firstOrNull { resultatPeriode -> resultatPeriode.periode.datoFom == bidragsevne.periode.datoFom }
                    ?.grunnlagsreferanseListe
                    ?.distinct()
                    ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
                    ?: emptyList(),
                gjelderReferanse = bidragspliktigReferanse,
            )
        }

    // Mapper ut DelberegningBPsBeregnedeTotalbidrag
    private fun mapDelberegningBPsBeregnedeTotalbidrag(
        bPsBeregnedeTotalbidrag: BPsBeregnedeTotalbidragPeriodeCore,
        beregnBPsBeregnedeTotalbidragResultatCore: BeregnBPsBeregnedeTotalbidragResultatCore,
        bidragspliktigReferanse: String,
    ) = GrunnlagDto(
        referanse = bPsBeregnedeTotalbidrag.referanse,
        type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG,
        innhold = POJONode(
            DelberegningBidragspliktigesBeregnedeTotalbidrag(
                periode = ÅrMånedsperiode(fom = bPsBeregnedeTotalbidrag.periode.datoFom, til = bPsBeregnedeTotalbidrag.periode.datoTil),
                bidragspliktigesBeregnedeTotalbidrag = bPsBeregnedeTotalbidrag.bPsBeregnedeTotalbidrag,
                beregnetBidragPerBarnListe = bPsBeregnedeTotalbidrag.beregnetBidragPerBarnListe,
            ),
        ),
        grunnlagsreferanseListe = beregnBPsBeregnedeTotalbidragResultatCore.resultatPeriode
            .grunnlagsreferanseListe
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
        gjelderReferanse = bidragspliktigReferanse,
    )

    // Mapper ut DelberegningBpsAndelSærbidrag
    private fun mapDelberegningBpsAndelSærbidrag(
        bPsAndelSærbidragListe: List<BPsAndelSærbidragPeriodeCore>,
        bpAndelSærbidragResultatFraCore: BeregnBPsAndelSærbidragResultatCore,
        bidragspliktigReferanse: String,
    ) = bPsAndelSærbidragListe
        .map { bPsAndelSærbidrag ->
            GrunnlagDto(
                referanse = bPsAndelSærbidrag.referanse,
                type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                innhold = POJONode(
                    DelberegningBidragspliktigesAndel(
                        periode = ÅrMånedsperiode(fom = bPsAndelSærbidrag.periode.datoFom, til = bPsAndelSærbidrag.periode.datoTil),
                        endeligAndelFaktor = bPsAndelSærbidrag.endeligAndelFaktor,
                        andelBeløp = bPsAndelSærbidrag.andelBeløp,
                        beregnetAndelFaktor = bPsAndelSærbidrag.beregnetAndelFaktor,
                        barnEndeligInntekt = bPsAndelSærbidrag.barnEndeligInntekt,
                        barnetErSelvforsørget = bPsAndelSærbidrag.barnetErSelvforsørget,
                    ),
                ),
                grunnlagsreferanseListe = bpAndelSærbidragResultatFraCore.resultatPeriodeListe
                    .firstOrNull { resultatPeriode -> resultatPeriode.periode.datoFom == bPsAndelSærbidrag.periode.datoFom }
                    ?.grunnlagsreferanseListe
                    ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
                    ?: emptyList(),
                gjelderReferanse = bidragspliktigReferanse,
            )
        }

    // Oppretter resultatperioder som refererer til sluttberegning, som igjen refererer til delberegninger og grunnlag
    private fun lagSluttperiodeOgResultatperioder(
        resultatPeriodeCoreListe: List<ResultatPeriodeCore>,
        grunnlagReferanseListe: MutableList<GrunnlagDto>,
        søknadsbarnReferanse: String,
    ): List<ResultatPeriode> = resultatPeriodeCoreListe.map { resultatPeriode ->

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
                        beregnetBeløp = resultatPeriode.resultat.beregnetBeløp,
                        resultatKode = resultatPeriode.resultat.resultatKode,
                        resultatBeløp = resultatPeriode.resultat.resultatBeløp,
                    ),
                ),
                grunnlagsreferanseListe = resultatPeriode.grunnlagsreferanseListe.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it }),
                gjelderReferanse = søknadsbarnReferanse,
            ),
        )

        // Oppretter resultatperioder, som refererer til sluttberegning
        ResultatPeriode(
            periode = ÅrMånedsperiode(fom = resultatPeriode.periode.datoFom, til = resultatPeriode.periode.datoTil),
            resultat =
            ResultatBeregning(
                beløp = resultatPeriode.resultat.resultatBeløp,
                resultatkode = resultatPeriode.resultat.resultatKode,
            ),
            grunnlagsreferanseListe = listOf(sluttberegningReferanse),
        )
    }

    // ===============================================================================================================================================

    // Legger til grunnlagsobjekter for objekter referert av gjelderReferanse som mangler i outputen
    private fun sjekkGrunnlagRefereranseListeGrunnlag(
        grunnlagReferanseListe: List<GrunnlagDto>,
        beregnGrunnlag: BeregnGrunnlag,
    ): MutableList<GrunnlagDto> {
        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()

        val gjelderReferanseListe = grunnlagReferanseListe.mapNotNull { it.gjelderReferanse }.distinct()
        val referanseListe = grunnlagReferanseListe.map { it.referanse }.distinct()
        val referanserSomManglerListe = gjelderReferanseListe.subtract(referanseListe.toSet())
        referanserSomManglerListe.forEach { referanse ->
            beregnGrunnlag.grunnlagListe.filter { grunnlag -> grunnlag.referanse == referanse }.first {
                resultatGrunnlagListe.add(it)
            }
        }

        return resultatGrunnlagListe
    }

    // Mapper ut grunnlag som er brukt av delberegningene
    private fun mapDelberegningGrunnlag(grunnlagListe: List<GrunnlagDto>, delberegningReferanseListe: List<String>) = grunnlagListe
        .filter { it.referanse in delberegningReferanseListe }
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

    // Mapper ut grunnlag basert på liste over sjabloner av type Sjablontall som er brukt i beregningen
    private fun mapSjablonSjablontallGrunnlag(sjablonListe: List<SjablonResultatGrunnlagCore>) = sjablonListe
        .filter { sjablon -> SjablonTallNavn.entries.any { it.navn == sjablon.navn } }
        .map {
            GrunnlagDto(
                referanse = it.referanse,
                type = Grunnlagstype.SJABLON_SJABLONTALL,
                innhold = POJONode(
                    SjablonSjablontallPeriode(
                        periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                        sjablon = SjablonTallNavn.from(it.navn),
                        verdi = it.verdi,
                    ),
                ),
            )
        }

    // Mapper ut grunnlag basert på liste over sjabloner av type Trinnvis Skattesats som er brukt i beregningen
    private fun mapSjablonTrinnvisSkattesatsGrunnlag(sjablonListe: List<SjablonResultatGrunnlagCore>): List<GrunnlagDto> {
        val grunnlagDtoListe = mutableListOf<GrunnlagDto>()

        // Henter ut unike perioder
        val unikePerioderListe = sjablonListe
            .filter { it.navn.contains(SjablonNavn.TRINNVIS_SKATTESATS.navn + "InntektGrense") }
            .map { it.periode }
            .distinct()

        // For hver unike periode dannes det ett grunnlag ut fra sjablonverdiene som er returnert fra core (normalt 4 verdier for inntektsgrense
        // og 4 verdier for sats)
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

            // Bygger opp tabell over inntektsgrenser
            filtrertListeInntektGrense.forEach {
                sjablonTrinnvisSkattesatsListe.add(
                    SjablonTrinnvisSkattesats(
                        inntektsgrense = it.verdi.intValueExact(),
                        sats = filtrertListeSats[indeks].verdi,
                    ),
                )
                indeks++
            }

            // Danner nytt grunnlag
            val referanse = "Sjablon_TrinnvisSkattesats_${periode.datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            grunnlagDtoListe.add(
                GrunnlagDto(
                    referanse = referanse,
                    type = Grunnlagstype.SJABLON_TRINNVIS_SKATTESATS,
                    innhold = POJONode(
                        SjablonTrinnvisSkattesatsPeriode(
                            periode = ÅrMånedsperiode(periode.datoFom, periode.datoTil),
                            trinnliste = sjablonTrinnvisSkattesatsListe,
                        ),
                    ),
                ),
            )
        }

        return grunnlagDtoListe
    }

    // Mapper ut grunnlag basert på liste over sjabloner for bidragsevne som er brukt i beregningen
    private fun mapSjablonBidragsevneGrunnlag(sjablonListe: List<SjablonResultatGrunnlagCore>): List<GrunnlagDto> {
        val grunnlagDtoListe = mutableListOf<GrunnlagDto>()

        // Henter ut unike perioder
        val unikePerioderListe = sjablonListe
            .filter { it.navn == SjablonInnholdNavn.BOUTGIFT_BELØP.navn || it.navn == SjablonInnholdNavn.UNDERHOLD_BELØP.navn }
            .map { it.periode }
            .distinct()

        // For hver unike periode dannes det ett grunnlag ut fra sjablonverdiene som er returnert fra core (2 verdier)
        unikePerioderListe.forEach { periode ->
            val boutgiftBeløp = sjablonListe.firstOrNull { it.navn == SjablonInnholdNavn.BOUTGIFT_BELØP.navn }?.verdi
            val underholdBeløp = sjablonListe.firstOrNull { it.navn == SjablonInnholdNavn.UNDERHOLD_BELØP.navn }?.verdi

            // Danner nytt grunnlag
            val referanse = "Sjablon_Bidragsevne_${periode.datoFom.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}"
            grunnlagDtoListe.add(
                GrunnlagDto(
                    referanse = referanse,
                    type = Grunnlagstype.SJABLON_BIDRAGSEVNE,
                    innhold = POJONode(
                        SjablonBidragsevnePeriode(
                            periode = ÅrMånedsperiode(periode.datoFom, periode.datoTil),
                            boutgiftBeløp = boutgiftBeløp ?: BigDecimal.ZERO,
                            underholdBeløp = underholdBeløp ?: BigDecimal.ZERO,
                        ),
                    ),
                ),
            )
        }

        return grunnlagDtoListe
    }

    // Mapper ut sjablon for samværsfradrag
    private fun mapSjablonSamværsfradragGrunnlag(sjablonListe: List<SjablonResultatGrunnlagCore>): List<GrunnlagDto> {
        // Danner nytt grunnlag
        val grunnlagDtoListe = sjablonListe.filter { it.navn.startsWith(SjablonNavn.SAMVÆRSFRADRAG.navn) }
            .map {
                GrunnlagDto(
                    referanse = it.referanse,
                    type = Grunnlagstype.SJABLON_SAMVARSFRADRAG,
                    innhold = POJONode(
                        SjablonSamværsfradragPeriode(
                            periode = ÅrMånedsperiode(it.periode.datoFom, it.periode.datoTil),
                            samværsklasse = "",
                            alderTom = 0,
                            antallDagerTom = 0,
                            antallNetterTom = 0,
                            beløpFradrag = it.verdi,
                        ),
                    ),
                    gjelderReferanse = it.navn.substringAfter("Samværsfradrag_"),
                )
            }
        return grunnlagDtoListe
    }

    // Referansene for sjablon TrinnvisSkattesats og Bidragsevne er basert på at de splittes opp i core. I mapSjablonxxx slås de sammen til en sjablon
    // pr periode og det må reflekteres i grunnlagsreferanselisten
    private fun justerReferanserForSjabloner(resultatPeriodeListe: List<no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatPeriodeCore>) {
        val sjablonTrinnvisSkattesats = "Sjablon_TrinnvisSkattesats"
        resultatPeriodeListe.forEach { resultatPeriode ->
            val referanse = resultatPeriode.grunnlagsreferanseListe.firstOrNull { it.contains(sjablonTrinnvisSkattesats) }
            if (referanse != null) {
                resultatPeriode.grunnlagsreferanseListe.removeAll { it.contains(sjablonTrinnvisSkattesats) }
                resultatPeriode.grunnlagsreferanseListe.add("${sjablonTrinnvisSkattesats}_${referanse.takeLast(8)}")
            }
        }

        val sjablonBoutgiftBeløp = "Sjablon_BoutgiftBeløp"
        val sjablonUnderholdBeløp = "Sjablon_UnderholdBeløp"
        val sjablonBidragsevne = "Sjablon_Bidragsevne"
        resultatPeriodeListe.forEach { resultatPeriode ->
            val referanseBoutgift = resultatPeriode.grunnlagsreferanseListe.firstOrNull { it.contains(sjablonBoutgiftBeløp) }
            val referanseUnderhold = resultatPeriode.grunnlagsreferanseListe.firstOrNull { it.contains(sjablonUnderholdBeløp) }
            if (referanseBoutgift != null && referanseUnderhold != null) {
                resultatPeriode.grunnlagsreferanseListe.removeAll { it.contains(referanseBoutgift) || it.contains(referanseUnderhold) }
                resultatPeriode.grunnlagsreferanseListe.add("${sjablonBidragsevne}_${referanseBoutgift.takeLast(8)}")
            }
        }
    }

    private fun lagResponsGodkjentBeløpUnderForskuddssats(beregnGrunnlag: BeregnGrunnlag, forskuddssats: Sjablontall): BeregnetSærbidragResultat {
        val grunnlagForskuddssats = mapSjablontallForskuddssats(forskuddssats)
        val grunnlagDelberegningUtgift = beregnGrunnlag.grunnlagListe.first { it.type == Grunnlagstype.DELBEREGNING_UTGIFT }
        val referanseSluttberegning = opprettSluttberegningreferanse(
            barnreferanse = beregnGrunnlag.søknadsbarnReferanse,
            periode = ÅrMånedsperiode(fom = beregnGrunnlag.periode.fom, til = beregnGrunnlag.periode.til),
        )

        val sluttberegning = GrunnlagDto(
            referanse = referanseSluttberegning,
            type = Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG,
            innhold = POJONode(
                SluttberegningSærbidrag(
                    periode = ÅrMånedsperiode(beregnGrunnlag.periode.fom, beregnGrunnlag.periode.til),
                    beregnetBeløp = BigDecimal.ZERO,
                    resultatKode = Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS,
                    resultatBeløp = null,
                ),
            ),
            grunnlagsreferanseListe = listOf(grunnlagForskuddssats.referanse, grunnlagDelberegningUtgift.referanse),
            gjelderReferanse = beregnGrunnlag.søknadsbarnReferanse,
        )

        return BeregnetSærbidragResultat(
            beregnetSærbidragPeriodeListe = listOf(
                ResultatPeriode(
                    periode = ÅrMånedsperiode(
                        fom = beregnGrunnlag.periode.fom,
                        til = beregnGrunnlag.periode.til,
                    ),
                    resultat = ResultatBeregning(
                        beløp = null,
                        resultatkode = Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS,
                    ),
                    listOf(referanseSluttberegning),
                ),
            ),
            grunnlagListe = listOf(grunnlagDelberegningUtgift, sluttberegning, grunnlagForskuddssats),
        )
    }
}
