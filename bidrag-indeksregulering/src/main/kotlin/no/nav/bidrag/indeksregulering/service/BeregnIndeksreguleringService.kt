package no.nav.bidrag.indeksregulering.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.beløp.Beløp
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.samhandler.Valutakode
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.indeksregulering.bo.BeregnIndeksreguleringGrunnlag
import no.nav.bidrag.indeksregulering.bo.IndeksregulerPeriodeGrunnlag
import no.nav.bidrag.indeksregulering.mapper.IndeksreguleringMapper.finnReferanseTilRolle
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningIndeksregulering
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettSluttberegningreferanse
import java.math.BigDecimal
import java.time.Year
import java.time.YearMonth

internal class BeregnIndeksreguleringService : BeregnService() {

    fun beregn(grunnlag: BeregnIndeksreguleringGrunnlag): List<GrunnlagDto> {
        secureLogger.info { "Beregning av indeksregulering barnebidrag - følgende request mottatt: ${tilJson(grunnlag)}" }

        // Kontroll av inputdata
        try {
            grunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved indeksregulering barnebidrag: " + e.message)
        }

        if (grunnlag.stønadsid.type != Stønadstype.BIDRAG && grunnlag.stønadsid.type != Stønadstype.BIDRAG18AAR &&
            grunnlag.stønadsid.type != Stønadstype.OPPFOSTRINGSBIDRAG
        ) {
            throw IllegalArgumentException("Feil stønadstype i grunnlaget til indeksregulering")
        }

        val beløpshistorikk = grunnlag.beløpshistorikkListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(
                grunnlagType = Grunnlagstype.BELØPSHISTORIKK_BIDRAG,
            )
            .map { it ->
                Beløpshistorikk(
                    referanse = it.referanse,
                    nesteIndeksreguleringsår = it.innhold.nesteIndeksreguleringsår,
                    skalIndeksreguleres = it.innhold.beløpshistorikk.any { it.beløp != null },
                    perioder = it.innhold.beløpshistorikk,
                )
            }.firstOrNull()

        val gjelderBarnReferanse = finnReferanseTilRolle(
            grunnlagListe = grunnlag.personobjektListe,
            grunnlagstype = Grunnlagstype.PERSON_SØKNADSBARN,
        )

        val beregningsperiode = ÅrMånedsperiode(
            fom = YearMonth.of(grunnlag.indeksregulerÅr.value, 7),
            til = null,
        )

        val sjablonListe = mapSjablonSjablontallGrunnlag(
            periode = beregningsperiode,
            sjablonListe = SjablonProvider.hentSjablontall(),
        ) { it.indeksregulering }

        val sjablonIndeksreguleringFaktorListe = mapSjablonSjablontall(sjablonListe)

        val resultatliste = mutableListOf<GrunnlagDto>()
        val grunnlagsliste = mutableSetOf<String>()

        val grunnlagBeregning = lagIndeksreguleringBeregningGrunnlag(
            beregningsperiode = beregningsperiode,
            gjelderBarnReferanse = gjelderBarnReferanse,
            beløpshistorikk = beløpshistorikk!!,
            sjablonIndeksreguleringFaktorListe = sjablonIndeksreguleringFaktorListe,
        )
        grunnlagsliste.addAll(grunnlagBeregning.referanseliste)

        val resultat = beregnPeriode(grunnlagBeregning)

        resultatliste.add(
            GrunnlagDto(
                type = Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
                referanse = opprettSluttberegningreferanse(
                    barnreferanse = gjelderBarnReferanse,
                    periode = resultat.periode,
                    type = Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
                ),
                innhold = POJONode(resultat),
                gjelderBarnReferanse = gjelderBarnReferanse,
                grunnlagsreferanseListe = grunnlagBeregning.referanseliste,
            ),
        )

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = resultatliste.map { it.grunnlagsreferanseListe }.flatten().distinct(),
            mottattGrunnlag = grunnlag.personobjektListe + grunnlag.beløpshistorikkListe,
            sjablonGrunnlag = sjablonListe,
        ).toMutableList()

        return resultatliste + resultatGrunnlagListe + grunnlag.personobjektListe
    }

    private fun mapSjablonSjablontall(sjablonGrunnlag: List<GrunnlagDto>): List<SjablonSjablontallPeriodeGrunnlag> {
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

    // Lager grunnlag for indeksregulering som ligger innenfor bruddPeriode
    private fun lagIndeksreguleringBeregningGrunnlag(
        beregningsperiode: ÅrMånedsperiode,
        gjelderBarnReferanse: Grunnlagsreferanse,
        beløpshistorikk: Beløpshistorikk,
        sjablonIndeksreguleringFaktorListe: List<SjablonSjablontallPeriodeGrunnlag>,
    ): IndeksregulerPeriodeGrunnlag {
        val sjablonIndeksreguleringFaktor =
            sjablonIndeksreguleringFaktorListe
                .firstOrNull { it.sjablonSjablontallPeriode.periode.inneholder(beregningsperiode.fom) }
                ?.let {
                    SjablonSjablontallBeregningGrunnlag(
                        referanse = it.referanse,
                        type = it.sjablonSjablontallPeriode.sjablon.navn,
                        verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                    )
                }
                ?: throw IllegalArgumentException("Sjablon indeksregulering faktor ikke funnet for periode $beregningsperiode")

        val beløp = beløpshistorikk.perioder
            .firstOrNull { it.periode.inneholder(beregningsperiode.fom) }
            ?.beløp ?: throw IllegalArgumentException("Beløp ikke funnet for periode $beregningsperiode")

        val valutakode = beløpshistorikk.perioder
            .firstOrNull { it.periode.inneholder(beregningsperiode.fom) }
            ?.valutakode ?: Valutakode.NOK.toString()

        return IndeksregulerPeriodeGrunnlag(
            beregningsperiode = beregningsperiode,
            gjelderBarnReferanse = gjelderBarnReferanse,
            beløp = Beløp(beløp, Valutakode.valueOf(valutakode)),
            sjablonIndeksreguleringFaktor = sjablonIndeksreguleringFaktor,
            referanseliste = listOfNotNull(
                sjablonIndeksreguleringFaktor.referanse,
                beløpshistorikk.referanse,
            ),
        )
    }

    private fun beregnPeriode(grunnlag: IndeksregulerPeriodeGrunnlag): SluttberegningIndeksregulering {
        val indeksreguleringFaktor = BigDecimal.valueOf(grunnlag.sjablonIndeksreguleringFaktor.verdi).divide(BigDecimal(100)).setScale(4)
        val indeksregulertBeløp = grunnlag.beløp.verdi.plus(grunnlag.beløp.verdi.multiply(indeksreguleringFaktor)).avrundetTilNærmesteTier
        return SluttberegningIndeksregulering(
            periode = grunnlag.beregningsperiode,
            beløp = Beløp(indeksregulertBeløp, grunnlag.beløp.valutakode),
            originaltBeløp = grunnlag.beløp,
            nesteIndeksreguleringsår = Year.of(grunnlag.beregningsperiode.fom.year.plus(1)),

        )
    }
}

data class Beløpshistorikk(
    val referanse: String,
    val nesteIndeksreguleringsår: Int?,
    val skalIndeksreguleres: Boolean,
    val perioder: List<BeløpshistorikkPeriode>,
)

fun BeregnIndeksreguleringGrunnlag.valider() {
    requireNotNull(indeksregulerÅr) { "indeksregulerÅr kan ikke være null" }
    requireNotNull(stønadsid) { "stønadsid kan ikke være null" }
    require(personobjektListe.isNotEmpty()) { "personobjektListe kan ikke være tom" }
    require(beløpshistorikkListe.isNotEmpty()) { "beløpshistorikkListe kan ikke være tom" }
    personobjektListe.forEach(GrunnlagDto::valider)
    beløpshistorikkListe.forEach(GrunnlagDto::valider)
}
