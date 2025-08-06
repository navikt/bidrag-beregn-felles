package no.nav.bidrag.indeksregulering.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.core.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.beløp.Beløp
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.samhandler.Valutakode
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.indeksregulering.bo.BeregnIndeksreguleringGrunnlag
import no.nav.bidrag.indeksregulering.bo.IndeksreguleringGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
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

internal class IndeksreguleringService : BeregnService() {

    fun beregn(grunnlag: BeregnIndeksreguleringGrunnlag): List<GrunnlagDto> {
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

        val gjelderReferanse = grunnlag.personobjektListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(
                grunnlagType = Grunnlagstype.BELØPSHISTORIKK_BIDRAG,
            ).first().gjelderReferanse

        val gjelderBarnReferanse = grunnlag.personobjektListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(
                grunnlagType = Grunnlagstype.BELØPSHISTORIKK_BIDRAG,
            ).first().gjelderBarnReferanse

        if (beløpshistorikk == null ||
            beløpshistorikk.perioder.isEmpty()
        ) {
            // hent beløpshistorikk fra vedtakservice
            //TODO: Implementer logikk for å hente beløpshistorikk fra vedtakservice

        }


        if (gjelderReferanse == null ||
            gjelderBarnReferanse == null ||
            (grunnlag.stønadsid.type != Stønadstype.BIDRAG && grunnlag.stønadsid.type != Stønadstype.BIDRAG18AAR &&
            grunnlag.stønadsid.type != Stønadstype.OPPFOSTRINGSBIDRAG)
        ) {
            throw IllegalArgumentException("Feil i grunnlaget til indeksregulering")
        }

        val perioder = beløpshistorikk?.perioder
            ?.map {
                Periode(
                    periode = it.periode,
                    beløp = it.beløp!!,
                    valutakode = it.valutakode!!,
                )
            }

        val nesteIndeksregulering = YearMonth.of(beløpshistorikk.nesteIndeksreguleringsår!!, 7)

        val periode = ÅrMånedsperiode(
            fom = nesteIndeksregulering,
            til = null,
        )

        val sjablonListe = mapSjablonSjablontallGrunnlag(
            periode = periode,
            sjablonListe = SjablonProvider.hentSjablontall(),
        ) { it.indeksregulering }

        val sjablonIndeksreguleringFaktorListe = mapSjablonSjablontall(sjablonListe)

        // Lager liste over bruddperioder
        val (indeksregulerPeriode, beregningsperiodeListe) = lagBruddperiodeListe(
            nesteIndeksregulering = nesteIndeksregulering,
            beløpshistorikkPeriodeListe = perioder,
            beregningsperiode = periode,
        )

        val resultatliste = mutableListOf<GrunnlagDto>()
        val grunnlagsliste = mutableSetOf<String>()

        var beløpFraForrigeDelberegning: BigDecimal? = null

        beregningsperiodeListe.forEach {
            val grunnlagBeregning = lagIndeksreguleringBeregningGrunnlag(
                beregningsperiode = it.periode,
                periodeSkalIndeksreguleres = it.periodeSkalIndeksreguleres,
                referanseTilRolle = gjelderReferanse,
                gjelderBarnReferanse = gjelderBarnReferanse,
                beløpshistorikk = beløpshistorikk,
                perioder = perioder,
                sjablonIndeksreguleringFaktorListe = sjablonIndeksreguleringFaktorListe,
                beløpFraForrigeDelberegning = beløpFraForrigeDelberegning,
            )
            grunnlagsliste.addAll(grunnlagBeregning.referanseliste)

            val resultat = beregnPeriode(grunnlagBeregning)

            if (it.periodeSkalIndeksreguleres) {
                beløpFraForrigeDelberegning = resultat.beløp.verdi
            }

            resultatliste.add(
                GrunnlagDto(
                    type = Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
                    referanse = opprettSluttberegningreferanse(
                        barnreferanse = grunnlag.søknadsbarnReferanse,
                        periode = resultat.periode,
                    ),
                    innhold = POJONode(resultat),
                    gjelderReferanse = gjelderReferanse,
                    gjelderBarnReferanse = gjelderBarnReferanse,
                    grunnlagsreferanseListe = grunnlagBeregning.referanseliste,
                ),
            )
        }

//        val sluttberegningIndeksreguleringListe = resultatliste.map {
//            GrunnlagDto(
//                type = Grunnlagstype.SLUTTBEREGNING_INDEKSREGULERING,
//                referanse = opprettSluttberegningreferanse(
//                    barnreferanse = grunnlag.søknadsbarnReferanse,
//                    periode = it.periode,
//                ),
//                innhold = POJONode(it),
//                gjelderReferanse = gjelderReferanse,
//                gjelderBarnReferanse = grunnlag.søknadsbarnReferanse,
//                grunnlagsreferanseListe = grunnlagsliste.toList(),
//            )
//        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = resultatliste.map { it.grunnlagsreferanseListe }.flatten().distinct(),
            mottattGrunnlag = grunnlag,
            sjablonGrunnlag = sjablonListe,
        ).toMutableList()

        return resultatliste + resultatGrunnlagListe
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

    // Lager en liste over alle bruddperioder med indikator for indeksregulering
    private fun lagBruddperiodeListe(
        nesteIndeksregulering: YearMonth,
        beløpshistorikkPeriodeListe: List<Periode>,
        beregningsperiode: ÅrMånedsperiode,
    ): Pair<YearMonth, List<Beregningsperiode>> {
        var beregningsperiodeListe = mutableListOf<Beregningsperiode>()

        var indeksregulerPeriode = maxOf(
            YearMonth.of(nesteIndeksregulering.year, 7),
            beløpshistorikkPeriodeListe.last().periode.fom.withMonth(7).plusYears(1),
        )

        if (indeksregulerPeriode <= YearMonth.now() && beløpshistorikkPeriodeListe.last().periode.til == null) {
            beløpshistorikkPeriodeListe.forEach {
                if (it.periode.fom.isBefore(indeksregulerPeriode)) {
                    beregningsperiodeListe.add(
                        Beregningsperiode(
                            periode = ÅrMånedsperiode(it.periode.fom, it.periode.fom),
                            periodeSkalIndeksreguleres = false,
                        ),
                    )
                }
            }

            while (indeksregulerPeriode <= YearMonth.now()) {
                if ((beregningsperiode.til != null && indeksregulerPeriode.isBefore(beregningsperiode.til)) || beregningsperiode.til == null) {
                    beregningsperiodeListe.add(
                        Beregningsperiode(
                            periode = ÅrMånedsperiode(indeksregulerPeriode, indeksregulerPeriode),
                            periodeSkalIndeksreguleres = true,
                        ),
                    )
                    indeksregulerPeriode = indeksregulerPeriode.plusYears(1)
                }
            }

            val periodeListe = beregningsperiodeListe.asSequence().map { it.periode }

            // Slår sammen og lager periodene som skal beregnes.
            val sammenslåttePerioder = lagBruddPeriodeListe(periodeListe, beregningsperiode)
            // Til slutt legges det til en periode med åpen tildato
            val endeligListe = sammenslåttePerioder.plus(ÅrMånedsperiode(sammenslåttePerioder.last().til!!, null))

            beregningsperiodeListe = endeligListe.map {
                Beregningsperiode(
                    periode = it,
                    periodeSkalIndeksreguleres = beregningsperiodeListe.first { periode -> periode.periode.fom == it.fom }.periodeSkalIndeksreguleres,
                )
            }.toMutableList()
        } else {
            beregningsperiodeListe =
                beløpshistorikkPeriodeListe.asSequence().map { Beregningsperiode(periode = it.periode, periodeSkalIndeksreguleres = false) }.toList()
                    .toMutableList()
        }
        return indeksregulerPeriode to beregningsperiodeListe.sortedBy { it.periode.fom }
    }

    // Lager grunnlag for indeksregulering som ligger innenfor bruddPeriode
    private fun lagIndeksreguleringBeregningGrunnlag(
        beregningsperiode: ÅrMånedsperiode,
        periodeSkalIndeksreguleres: Boolean,
        referanseTilRolle: Grunnlagsreferanse,
        gjelderBarnReferanse: Grunnlagsreferanse,
        beløpshistorikk: Beløpshistorikk,
        perioder: List<Periode>,
        sjablonIndeksreguleringFaktorListe: List<SjablonSjablontallPeriodeGrunnlag>,
        beløpFraForrigeDelberegning: BigDecimal?,
    ): IndeksreguleringGrunnlag {
        val periode = perioder
            .firstOrNull { ÅrMånedsperiode(it.periode.fom, it.periode.til).inneholder(beregningsperiode) }
            ?.let {
                Periode(
                    periode = it.periode,
                    beløp = it.beløp,
                    valutakode = it.valutakode,
                )
            }
            ?: throw IllegalArgumentException("Grunnlag ikke funnet for periode $beregningsperiode")

        val sjablonIndeksreguleringFaktor = if (periodeSkalIndeksreguleres) {
            sjablonIndeksreguleringFaktorListe
                .firstOrNull { it.sjablonSjablontallPeriode.periode.fom == beregningsperiode.fom }
                ?.let {
                    SjablonSjablontallBeregningGrunnlag(
                        referanse = it.referanse,
                        type = it.sjablonSjablontallPeriode.sjablon.navn,
                        verdi = it.sjablonSjablontallPeriode.verdi.toDouble(),
                    )
                }
                ?: throw IllegalArgumentException("Sjablon indeksregulering faktor ikke funnet for periode $beregningsperiode")
        } else {
            null
        }

        return IndeksreguleringGrunnlag(
            beregningsperiode = beregningsperiode,
            periodeSkalIndeksreguleres = periodeSkalIndeksreguleres,
            referanseTilRolle = referanseTilRolle,
            gjelderBarnReferanse = gjelderBarnReferanse,
            beløpshistorikk = beløpshistorikk,
            periode = periode,
            sjablonIndeksreguleringFaktor = sjablonIndeksreguleringFaktor,
            beløpFraForrigeDelberegning = beløpFraForrigeDelberegning,
            referanseliste = listOfNotNull(
                beløpshistorikk.referanse,
                sjablonIndeksreguleringFaktor?.referanse,
            ),
        )
    }

    private fun beregnPeriode(grunnlag: IndeksreguleringGrunnlag): SluttberegningIndeksregulering = if (grunnlag.periodeSkalIndeksreguleres) {
        val indeksreguleringFaktor = BigDecimal.valueOf(grunnlag.sjablonIndeksreguleringFaktor!!.verdi).divide(BigDecimal(100)).setScale(4)
        val beløp = grunnlag.beløpFraForrigeDelberegning ?: grunnlag.periode.beløp
        val indeksregulertBeløp = beløp.plus(beløp.multiply(indeksreguleringFaktor)).avrundetTilNærmesteTier
        SluttberegningIndeksregulering(
            periode = grunnlag.beregningsperiode,
            beløp = Beløp(indeksregulertBeløp, Valutakode.valueOf(grunnlag.periode.valutakode)),
            originaltBeløp = Beløp(grunnlag.periode.beløp, Valutakode.valueOf(grunnlag.periode.valutakode)),
            nesteIndeksreguleringsår = Year.of(grunnlag.beregningsperiode.fom.year.plus(1)),

            )
    } else {
        SluttberegningIndeksregulering(
            periode = grunnlag.beregningsperiode,
            beløp = Beløp(grunnlag.periode.beløp, Valutakode.valueOf(grunnlag.periode.valutakode)),
            originaltBeløp = Beløp(grunnlag.periode.beløp, Valutakode.valueOf(grunnlag.periode.valutakode)),
            nesteIndeksreguleringsår = Year.of(grunnlag.beregningsperiode.fom.year.plus(1)),

            )
    }
}

data class Beregningsperiode(val periode: ÅrMånedsperiode, val periodeSkalIndeksreguleres: Boolean)

data class Beløpshistorikk(
    val referanse: String,
    val nesteIndeksreguleringsår: Int?,
    val skalIndeksreguleres: Boolean,
    val perioder: List<BeløpshistorikkPeriode>,
)

data class Periode(val periode: ÅrMånedsperiode, val beløp: BigDecimal, val valutakode: String)
