package no.nav.bidrag.beregn.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.felles.dto.SjablonInnholdCore
import no.nav.bidrag.beregn.felles.dto.SjablonNokkelCore
import no.nav.bidrag.beregn.felles.dto.SjablonPeriodeCore
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.Samvaersfradrag
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.Sjablontall
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.TrinnvisSkattesats
import no.nav.bidrag.beregn.saertilskudd.rest.exception.UgyldigInputException
import no.nav.bidrag.domain.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domain.enums.sjablon.SjablonNavn
import no.nav.bidrag.domain.enums.sjablon.SjablonNokkelNavn
import no.nav.bidrag.domain.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.felles.Grunnlag
import java.math.BigDecimal
import java.util.*

abstract class CoreMapper {
    // Mapper sjabloner av typen sjablontall
    // Filtrerer bort de sjablonene som ikke brukes i den aktuelle delberegningen og de som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    fun mapSjablonSjablontall(
        sjablonSjablontallListe: List<Sjablontall>,
        delberegning: String,
        beregnGrunnlag: BeregnGrunnlag,
        sjablontallMap: Map<String, SjablonTallNavn>,
    ): List<SjablonPeriodeCore> {
        val beregnDatoFra = beregnGrunnlag.beregnDatoFra
        val beregnDatoTil = beregnGrunnlag.beregnDatoTil
        return sjablonSjablontallListe
            .stream()
            .filter { (_, datoFom, datoTom): Sjablontall -> datoFom!!.isBefore(beregnDatoTil) && !datoTom!!.isBefore(beregnDatoFra) }
            .filter { (typeSjablon): Sjablontall -> filtrerSjablonTall(sjablontallMap[typeSjablon] ?: SjablonTallNavn.DUMMY, delberegning) }
            .map { (typeSjablon, datoFom, datoTom, verdi): Sjablontall ->
                SjablonPeriodeCore(
                    PeriodeCore(datoFom!!, datoTom),
                    (sjablontallMap[typeSjablon] ?: SjablonTallNavn.DUMMY).navn,
                    emptyList(),
                    listOf(SjablonInnholdCore(SjablonInnholdNavn.SJABLON_VERDI.navn, verdi!!)),
                )
            }
            .toList()
    }

    // Mapper sjabloner av typen trinnvis skattesats
    // Filtrerer bort de sjablonene som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    fun mapSjablonTrinnvisSkattesats(
        sjablonTrinnvisSkattesatsListe: List<TrinnvisSkattesats>,
        beregnGrunnlag: BeregnGrunnlag,
    ): List<SjablonPeriodeCore> {
        val beregnDatoFra = beregnGrunnlag.beregnDatoFra
        val beregnDatoTil = beregnGrunnlag.beregnDatoTil
        return sjablonTrinnvisSkattesatsListe
            .stream()
            .filter { (datoFom, datoTom): TrinnvisSkattesats -> datoFom!!.isBefore(beregnDatoTil) && !datoTom!!.isBefore(beregnDatoFra) }
            .map { (datoFom, datoTom, inntektgrense, sats): TrinnvisSkattesats ->
                SjablonPeriodeCore(
                    PeriodeCore(datoFom!!, datoTom),
                    SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    emptyList(),
                    Arrays.asList(
                        SjablonInnholdCore(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.navn, inntektgrense!!),
                        SjablonInnholdCore(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, sats!!),
                    ),
                )
            }
            .toList()
    }

    // Mapper sjabloner av typen samværsfradrag
    // Filtrerer bort de sjablonene som ikke er innenfor intervallet beregnDatoFra-beregnDatoTil
    protected fun mapSjablonSamvaersfradrag(
        sjablonSamvaersfradragListe: List<Samvaersfradrag>,
        beregnGrunnlag: BeregnGrunnlag,
    ): List<SjablonPeriodeCore> {
        val beregnDatoFra = beregnGrunnlag.beregnDatoFra
        val beregnDatoTil = beregnGrunnlag.beregnDatoTil
        return sjablonSamvaersfradragListe
            .stream()
            .filter { (_, _, datoFom, datoTom): Samvaersfradrag -> datoFom!!.isBefore(beregnDatoTil) && !datoTom!!.isBefore(beregnDatoFra) }
            .map { (samvaersklasse, alderTom, datoFom, datoTom, antDagerTom, antNetterTom, belopFradrag): Samvaersfradrag ->
                SjablonPeriodeCore(
                    PeriodeCore(datoFom!!, datoTom),
                    SjablonNavn.SAMVAERSFRADRAG.navn,
                    Arrays.asList(
                        SjablonNokkelCore(SjablonNokkelNavn.SAMVAERSKLASSE.navn, samvaersklasse!!),
                        SjablonNokkelCore(SjablonNokkelNavn.ALDER_TOM.navn, alderTom.toString()),
                    ),
                    Arrays.asList(
                        SjablonInnholdCore(
                            SjablonInnholdNavn.ANTALL_DAGER_TOM.navn,
                            BigDecimal.valueOf(
                                antDagerTom!!.toLong(),
                            ),
                        ),
                        SjablonInnholdCore(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(antNetterTom!!.toLong())),
                        SjablonInnholdCore(SjablonInnholdNavn.FRADRAG_BELOP.navn, belopFradrag!!),
                    ),
                )
            }
            .toList()
    }

    protected fun mapSjablontall(): Map<String, SjablonTallNavn> {
        val sjablontallMap = HashMap<String, SjablonTallNavn>()
        for (sjablonTallNavn in SjablonTallNavn.entries) {
            sjablontallMap[sjablonTallNavn.id] = sjablonTallNavn
        }
        return sjablontallMap
    }

    companion object {
        const val BP_ANDEL_SAERTILSKUDD = "BPsAndelSaertilskudd"
        const val BIDRAGSEVNE = "Bidragsevne"
        const val SAERTILSKUDD = "Saertilskudd"

        // Sjekker om en type SjablonTall er i bruk for en delberegning
        private fun filtrerSjablonTall(sjablonTallNavn: SjablonTallNavn, delberegning: String): Boolean {
            return when (delberegning) {
                SAERTILSKUDD -> sjablonTallNavn.saertilskudd
                BIDRAGSEVNE -> sjablonTallNavn.bidragsevne
                BP_ANDEL_SAERTILSKUDD -> sjablonTallNavn.bpAndelSaertilskudd
                else -> false
            }
        }

        @JvmStatic
        fun <T> grunnlagTilObjekt(grunnlag: Grunnlag, contentClass: Class<T>): T {
            return try {
                jacksonObjectMapper().registerModule(JavaTimeModule())
                jacksonObjectMapper().readValue(grunnlag.innhold.toString(), contentClass)
            } catch (e: JsonProcessingException) {
                throw UgyldigInputException("Kunne ikke deserialisere " + contentClass.getName() + ". " + e.message)
            }
        }

        @JvmStatic
        fun tilJsonNode(`object`: Any?): JsonNode {
            val mapper = ObjectMapper()
            return mapper.valueToTree(`object`)
        }
    }
}
