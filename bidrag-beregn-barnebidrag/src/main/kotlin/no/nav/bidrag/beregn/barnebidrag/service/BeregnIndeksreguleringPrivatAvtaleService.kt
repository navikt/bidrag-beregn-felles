package no.nav.bidrag.beregn.barnebidrag.service

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.bo.IndeksreguleringPrivatAvtaleGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SjablonSjablontallPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper.finnReferanseTilRolle
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.service.sjablon.SjablonProvider
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.avrundetTilNærmesteTier
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtalePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.PrivatAvtaleGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.PrivatAvtalePeriodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

internal object BeregnIndeksreguleringPrivatAvtaleService : BeregnService() {

    fun delberegningPrivatAvtalePeriode(grunnlag: BeregnGrunnlag): List<GrunnlagDto> {
        val referanseTilBM = finnReferanseTilRolle(
            grunnlagListe = grunnlag.grunnlagListe,
            grunnlagstype = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        )

        val sjablonListe = mapSjablonSjablontallGrunnlag(
            periode = grunnlag.periode,
            sjablonListe = SjablonProvider.hentSjablontall(),
        ) { it.indeksregulering }

        val sjablonIndeksreguleringFaktorListe = mapSjablonSjablontall(sjablonListe)

        val privatAvtale = grunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<PrivatAvtaleGrunnlag>(Grunnlagstype.PRIVAT_AVTALE_GRUNNLAG)
            .map {
                PrivatAvtale(
                    referanse = it.referanse,
                    avtaleInngåttDato = it.innhold.avtaleInngåttDato,
                    skalIndeksreguleres = it.innhold.skalIndeksreguleres,
                )
            }.first()

        val privatAvtalePeriodeListe = grunnlag.grunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<PrivatAvtalePeriodeGrunnlag>(
                grunnlagType = Grunnlagstype.PRIVAT_AVTALE_PERIODE_GRUNNLAG,
            )
            .filter { it.gjelderBarnReferanse == grunnlag.søknadsbarnReferanse }
            .map {
                PrivatAvtalePeriode(
                    referanse = it.referanse,
                    periode = it.innhold.periode,
                    beløp = it.innhold.beløp,
                )
            }.sortedBy { it.periode.fom }

        // Lager liste over bruddperioder
        val beregningsperiodeListe = lagBruddperiodeListe(
            privatAvtale = privatAvtale,
            privatAvtaleListe = privatAvtalePeriodeListe,
            beregningsperiode = grunnlag.periode,
        )

        val resultatliste = mutableListOf<GrunnlagDto>()

        var beløpFraForrigeDelberegning: BigDecimal? = null
        var referanseForrigeDelberegning: String? = null

        beregningsperiodeListe.forEach {
            val grunnlagBeregning = lagIndeksreguleringBeregningGrunnlag(
                beregningsperiode = it.periode,
                periodeSkalIndeksreguleres = it.periodeSkalIndeksreguleres,
                referanseTilRolle = referanseTilBM,
                søknadsbarnReferanse = grunnlag.søknadsbarnReferanse,
                privatAvtale = privatAvtale,
                privatAvtalePeriodeListe = privatAvtalePeriodeListe,
                sjablonIndeksreguleringFaktorListe = sjablonIndeksreguleringFaktorListe,
                beløpFraForrigeDelberegning = beløpFraForrigeDelberegning,
                referanseForrigeDelberegning = referanseForrigeDelberegning,
            )

            val resultat = beregn(grunnlagBeregning)

            if (privatAvtale.skalIndeksreguleres) {
                beløpFraForrigeDelberegning = resultat.innholdTilObjekt<DelberegningPrivatAvtalePeriode>().beløp
                referanseForrigeDelberegning = resultat.referanse
            }

            resultatliste.add(resultat)
        }

        val resultatGrunnlagListe = mutableListOf<GrunnlagDto>()

        val grunnlagReferanseListe =
            resultatliste
                .flatMap { it.grunnlagsreferanseListe }
                .distinct()

        // Matcher mottatte grunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = grunnlag.grunnlagListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )

        // Matcher sjablongrunnlag med grunnlag som er brukt i beregningen og mapper ut
        resultatGrunnlagListe.addAll(
            mapGrunnlag(
                grunnlagListe = sjablonListe,
                grunnlagReferanseListe = grunnlagReferanseListe,
            ),
        )
        return resultatliste + resultatGrunnlagListe
    }

    // Lager en liste over alle bruddperioder med indikator for indeksregulering
    private fun lagBruddperiodeListe(
        privatAvtale: PrivatAvtale,
        privatAvtaleListe: List<PrivatAvtalePeriode>,
        beregningsperiode: ÅrMånedsperiode,
    ): List<Beregningsperiode> {
        var beregningsperiodeListe = mutableListOf<Beregningsperiode>()

        var indeksregulerPeriode = maxOf(
            YearMonth.of(privatAvtale.avtaleInngåttDato.year, privatAvtale.avtaleInngåttDato.month.value),
            privatAvtaleListe.last().periode.fom,
        ).plusYears(1).withMonth(7)

        if (privatAvtale.skalIndeksreguleres && indeksregulerPeriode <= YearMonth.now() && privatAvtaleListe.last().periode.til == null) {
//            val beregningsperiodeListe = mutableListOf<Beregningsperiode>()
            privatAvtaleListe.forEach {
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
            val sammenslåttePerioder = lagBruddPeriodeListe(periodeListe, beregningsperiode).toMutableList()
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
                privatAvtaleListe.asSequence().map { Beregningsperiode(periode = it.periode, periodeSkalIndeksreguleres = false) }.toList()
                    .toMutableList()
        }
        return beregningsperiodeListe.sortedBy { it.periode.fom }
    }

    // Lager grunnlag for indeksregulering som ligger innenfor bruddPeriode
    private fun lagIndeksreguleringBeregningGrunnlag(
        beregningsperiode: ÅrMånedsperiode,
        periodeSkalIndeksreguleres: Boolean,
        referanseTilRolle: Grunnlagsreferanse,
        søknadsbarnReferanse: Grunnlagsreferanse,
        privatAvtale: PrivatAvtale,
        privatAvtalePeriodeListe: List<PrivatAvtalePeriode>,
        sjablonIndeksreguleringFaktorListe: List<SjablonSjablontallPeriodeGrunnlag>,
        beløpFraForrigeDelberegning: BigDecimal?,
        referanseForrigeDelberegning: String?,
    ): IndeksreguleringPrivatAvtaleGrunnlag {
        val privatAvtalePeriode = privatAvtalePeriodeListe
            .firstOrNull { ÅrMånedsperiode(it.periode.fom, it.periode.til).inneholder(beregningsperiode) }
            ?.let {
                PrivatAvtalePeriode(
                    referanse = it.referanse,
                    periode = it.periode,
                    beløp = it.beløp,
                )
            }
            ?: throw IllegalArgumentException("Grunnlag privat avtale periode ikke funnet for periode $beregningsperiode")

        val sjablonIndeksreguleringFaktor = if (periodeSkalIndeksreguleres) {
            sjablonIndeksreguleringFaktorListe
                .firstOrNull { it.sjablonSjablontallPeriode.periode.inneholder(beregningsperiode) }
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

        return IndeksreguleringPrivatAvtaleGrunnlag(
            beregningsperiode = beregningsperiode,
            periodeSkalIndeksreguleres = periodeSkalIndeksreguleres,
            referanseTilRolle = referanseTilRolle,
            søknadsbarnReferanse = søknadsbarnReferanse,
            privatAvtalePeriode = privatAvtalePeriode,
            sjablonIndeksreguleringFaktor = sjablonIndeksreguleringFaktor,
            beløpFraForrigeDelberegning = beløpFraForrigeDelberegning,
            referanseliste = listOfNotNull(
                privatAvtale.referanse,
                privatAvtalePeriode.referanse,
                sjablonIndeksreguleringFaktor?.referanse,
                referanseForrigeDelberegning,
            ),
        )
    }

    private fun beregn(grunnlag: IndeksreguleringPrivatAvtaleGrunnlag): GrunnlagDto {
        val delberegningResultat: DelberegningPrivatAvtalePeriode

        if (grunnlag.periodeSkalIndeksreguleres) {
            val indeksreguleringFaktor = BigDecimal.valueOf(grunnlag.sjablonIndeksreguleringFaktor!!.verdi).divide(BigDecimal(100)).setScale(4)
            val beløp = grunnlag.beløpFraForrigeDelberegning ?: grunnlag.privatAvtalePeriode.beløp
            val indeksregulertBeløp = beløp.plus(beløp.multiply(indeksreguleringFaktor)).avrundetTilNærmesteTier
            delberegningResultat =
                DelberegningPrivatAvtalePeriode(
                    periode = grunnlag.beregningsperiode,
                    indeksreguleringFaktor = indeksreguleringFaktor,
                    beløp = indeksregulertBeløp,
                )
        } else {
            delberegningResultat =
                DelberegningPrivatAvtalePeriode(
                    periode = grunnlag.beregningsperiode,
                    indeksreguleringFaktor = null,
                    beløp = grunnlag.privatAvtalePeriode.beløp,

                )
        }

        val resultat =
            GrunnlagDto(
                type = Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE,
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE_PERIODE,
                    periode = grunnlag.beregningsperiode,
                    søknadsbarnReferanse = grunnlag.søknadsbarnReferanse,
                    gjelderReferanse = grunnlag.referanseTilRolle,
                ),
                innhold = POJONode(
                    delberegningResultat,
                ),
                gjelderReferanse = grunnlag.referanseTilRolle,
                gjelderBarnReferanse = grunnlag.referanseTilRolle,
                grunnlagsreferanseListe = grunnlag.referanseliste,

            )
        return resultat
    }

    // TODO Flytte til CoreMapper
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
}

data class Beregningsperiode(val periode: ÅrMånedsperiode, val periodeSkalIndeksreguleres: Boolean)

data class PrivatAvtale(val referanse: String, val avtaleInngåttDato: LocalDate, val skalIndeksreguleres: Boolean)

data class PrivatAvtalePeriode(val referanse: String, val periode: ÅrMånedsperiode, val beløp: BigDecimal)
