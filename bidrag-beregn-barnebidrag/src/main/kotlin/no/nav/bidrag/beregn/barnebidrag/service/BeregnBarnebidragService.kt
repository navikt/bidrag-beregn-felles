package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnNettoTilsynsutgiftService.delberegningNettoTilsynsutgift
import no.nav.bidrag.beregn.barnebidrag.service.BeregnSamværsfradragService.delberegningSamværsfradrag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.MaksFradrag
import no.nav.bidrag.commons.service.sjablon.MaksTilsyn
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksFradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonMaksTilsynPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BeregnBarnebidragService : BeregnService() {

    // Full beregning av barnebidrag, inkludert alle delberegninger
    fun beregnBarnebidrag(mottattGrunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        secureLogger.debug { "Beregning av barnebidrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            // TODO Bør være mulig å ha null i beregnDatoTil?
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av barnebidrag: " + e.message)
        }

        // Lager sjablon grunnlagsobjekter
        val sjablonGrunnlag = lagSjablonGrunnlagsobjekter(mottattGrunnlag.periode)

        // Kaller delberegninger
        val delberegningNettoTilsynsutgiftResultat = delberegningNettoTilsynsutgift(mottattGrunnlag, sjablonGrunnlag)

        val delberegningSamværsfradragResultat = delberegningSamværsfradrag(mottattGrunnlag, sjablonGrunnlag)

        return delberegningSamværsfradragResultat
    }

    // Lager grunnlagsobjekter for sjabloner (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun lagSjablonGrunnlagsobjekter(periode: ÅrMånedsperiode): List<GrunnlagDto> =
        mapSjablonSjablontallGrunnlag(periode, SjablonProvider.hentSjablontall()) +
            mapSjablonSamværsfradragGrunnlag(periode, SjablonProvider.hentSjablonSamværsfradrag()) +
            mapSjablonMaksTilsynsbeløpGrunnlag(periode, SjablonProvider.hentSjablonMaksTilsyn()) +
            mapSjablonMaksFradragsbeløpGrunnlag(periode, SjablonProvider.hentSjablonMaksFradrag())

    // Lager grunnlagsobjekter for sjabloner av type Sjablontall som er innenfor perioden
    private fun mapSjablonSjablontallGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Sjablontall>): List<GrunnlagDto> {
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            if (sjablonTallNavn.bidragsevne ||
                sjablonTallNavn.nettoBarnetilsyn ||
                sjablonTallNavn.underholdskostnad ||
                sjablonTallNavn.bpAndelUnderholdskostnad ||
                sjablonTallNavn.barnebidrag
            ) {
                sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
            }
        }

        return sjablonListe
            .filter { sjablontallMap.containsKey(it.typeSjablon) }
            // TODO Sjekk om periode.overlapper er dekkende (legger til en måned på datoTom (tom --> til))
            .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
            .map {
                GrunnlagDto(
                    referanse = lagSjablonReferanse(sjablontallMap[it.typeSjablon]!!.navn, it.datoFom!!),
                    type = Grunnlagstype.SJABLON,
                    innhold = POJONode(
                        SjablonSjablontallPeriode(
                            periode = ÅrMånedsperiode(it.datoFom!!, it.datoTom!!.plusMonths(1)),
                            sjablon = SjablonTallNavn.from(sjablontallMap[it.typeSjablon]!!.navn),
                            verdi = it.verdi!!,
                        ),
                    ),
                )
            }
    }

    // Lager grunnlagsobjekter for sjabloner av type Samværsfradrag som er innenfor perioden
    private fun mapSjablonSamværsfradragGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<Samværsfradrag>): List<GrunnlagDto> = sjablonListe
        // TODO Sjekk om periode.overlapper er dekkende (legger til en måned på datoTom (tom --> til))
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map {
            GrunnlagDto(
                referanse = lagSjablonReferanse(SjablonNavn.SAMVÆRSFRADRAG.navn, it.datoFom!!, "_${it.samvaersklasse}_${it.alderTom}"),
                type = Grunnlagstype.SJABLON,
                innhold = POJONode(
                    SjablonSamværsfradragPeriode(
                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                        samværsklasse = it.samvaersklasse!!,
                        alderTom = it.alderTom!!,
                        antallDagerTom = it.antDagerTom!!,
                        antallNetterTom = it.antNetterTom!!,
                        beløpFradrag = it.belopFradrag!!,
                    ),
                ),
            )
        }

    // Lager grunnlagsobjekter for sjabloner av type Maks tilsynsbeløp som er innenfor perioden
    private fun mapSjablonMaksTilsynsbeløpGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksTilsyn>): List<GrunnlagDto> = sjablonListe
        // TODO Sjekk om periode.overlapper er dekkende (legger til en måned på datoTom (tom --> til))
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map {
            GrunnlagDto(
                referanse = lagSjablonReferanse(SjablonNavn.MAKS_TILSYN.navn, it.datoFom!!, "_${it.maksBeløpTilsyn}_${it.antallBarnTom}"),
                type = Grunnlagstype.SJABLON,
                innhold = POJONode(
                    SjablonMaksTilsynPeriode(
                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                        antallBarnTom = it.antallBarnTom!!,
                        maksBeløpTilsyn = it.maksBeløpTilsyn!!,
                    ),
                ),
            )
        }

    // Lager grunnlagsobjekter for sjabloner av type Maks fradragsbeløp som er innenfor perioden
    private fun mapSjablonMaksFradragsbeløpGrunnlag(periode: ÅrMånedsperiode, sjablonListe: List<MaksFradrag>): List<GrunnlagDto> = sjablonListe
        // TODO Sjekk om periode.overlapper er dekkende (legger til en måned på datoTom (tom --> til))
        .filter { periode.overlapper(ÅrMånedsperiode(it.datoFom!!, it.datoTom)) }
        .map {
            GrunnlagDto(
                referanse = lagSjablonReferanse(SjablonNavn.MAKS_FRADRAG.navn, it.datoFom!!, "_${it.maksBeløpFradrag}_${it.antallBarnTom}"),
                type = Grunnlagstype.SJABLON,
                innhold = POJONode(
                    SjablonMaksFradragPeriode(
                        periode = ÅrMånedsperiode(it.datoFom!!, justerSjablonTomDato(it.datoTom!!)),
                        antallBarnTom = it.antallBarnTom!!,
                        maksBeløpFradrag = it.maksBeløpFradrag!!,
                    ),
                ),
            )
        }

    private fun justerSjablonTomDato(datoTom: LocalDate): LocalDate? = if (datoTom == LocalDate.parse("9999-12-31")) null else datoTom.plusMonths(1)

    private fun lagSjablonReferanse(sjablonNavn: String, fomDato: LocalDate, postfix: String = ""): String =
        "Sjablon_${sjablonNavn}_${fomDato.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}$postfix"
}
