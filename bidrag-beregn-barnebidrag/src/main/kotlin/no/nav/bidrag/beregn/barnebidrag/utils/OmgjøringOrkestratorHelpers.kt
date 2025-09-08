package no.nav.bidrag.beregn.barnebidrag.utils

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.service.BeregnIndeksreguleringPrivatAvtaleService.delberegningPrivatAvtalePeriode
import no.nav.bidrag.beregn.barnebidrag.service.ByggetBeløpshistorikk
import no.nav.bidrag.beregn.barnebidrag.service.VedtakService
import no.nav.bidrag.beregn.barnebidrag.service.omgjøringFeilet
import no.nav.bidrag.beregn.core.util.justerVedtakstidspunktVedtak
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Stønadsid
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import no.nav.bidrag.transport.behandling.beregning.barnebidrag.BeregnetBarnebidragResultat
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningPrivatAvtale
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.PrivatAvtaleGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.erResultatEndringUnderGrense
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.tilGrunnlagstype
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.erIndeksEllerAldersjustering
import no.nav.bidrag.transport.behandling.vedtak.response.finnStønadsendring
import java.time.LocalDateTime
import java.time.YearMonth

internal val vedtaksidBeregnetBeløpshistorikk = 1
internal val vedtaksidAutomatiskJobb = 2
internal val vedtaksidPrivatavtale = 3
class OmgjøringOrkestratorHelpers(private val vedtakService: VedtakService, private val identUtils: IdentUtils) {
    internal fun List<StønadPeriodeDto>.justerSistePeriodeTilÅBliLøpende() = mapIndexed { index, periode ->
        if (index == this.size - 1) {
            periode.copy(periode = periode.periode.copy(til = null))
        } else {
            periode
        }
    }
    fun utførDelberegningPrivatAvtalePeriode(omgjøringsberegningGrunnlag: BeregnGrunnlag): List<GrunnlagDto> =
        if (omgjøringsberegningGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<PrivatAvtaleGrunnlag>(Grunnlagstype.PRIVAT_AVTALE_GRUNNLAG)
                .none { it.gjelderBarnReferanse == omgjøringsberegningGrunnlag.søknadsbarnReferanse }
        ) {
            emptyList()
        } else {
            delberegningPrivatAvtalePeriode(omgjøringsberegningGrunnlag)
        }
    fun finnBeløpshistorikkFørOmgjøringsVedtak(
        vedtak: VedtakDto,
        stønad: Stønadsid,
        personobjekter: List<GrunnlagDto>? = null,
        omgjøringsberegningGrunnlag: BeregnGrunnlag,
        omgjortVedtakVirkningstidspunkt: YearMonth,
    ): BeløpshistorikkGrunnlag {
        val delberegningIndeksreguleringPrivatAvtalePeriodeResultat = utførDelberegningPrivatAvtalePeriode(omgjøringsberegningGrunnlag)
        val beløpshistorikk = vedtak.finnBeløpshistorikkGrunnlag(stønad, identUtils)
            ?: vedtakService.hentBeløpshistorikkTilGrunnlag(
                stønadsid = stønad,
                personer = personobjekter ?: vedtak.grunnlagListe.hentAllePersoner() as List<GrunnlagDto>,
                tidspunkt = vedtak.justerVedtakstidspunktVedtak().vedtakstidspunkt!!.minusSeconds(1),
            ).innholdTilObjekt<BeløpshistorikkGrunnlag>()

        return if (delberegningIndeksreguleringPrivatAvtalePeriodeResultat.isNotEmpty()) {
            val søknadsbarn = delberegningIndeksreguleringPrivatAvtalePeriodeResultat.hentPersonMedIdent(stønad.kravhaver.verdi)!!
            val privatavtalePerioder = delberegningIndeksreguleringPrivatAvtalePeriodeResultat
                .filtrerOgKonverterBasertPåFremmedReferanse<DelberegningPrivatAvtale>(
                    Grunnlagstype.DELBEREGNING_PRIVAT_AVTALE,
                    gjelderBarnReferanse = søknadsbarn.referanse,
                ).first()

            val førstePeriodeFraBeløpshistorikk =
                beløpshistorikk.beløpshistorikk.minByOrNull { it.periode.fom }?.periode

            val periodeStartInnkreving =
                førstePeriodeFraBeløpshistorikk?.fom?.let { minOf(it, omgjortVedtakVirkningstidspunkt) } ?: omgjortVedtakVirkningstidspunkt

            // Bare ta med privat avtale perioder til første periode i historikken
            val privatAvtalePerioderFiltrert = privatavtalePerioder.innhold.perioder
                .filter { it.periode.fom.isBefore(periodeStartInnkreving) }

            // Juster siste periode i privat avtale historikk slik at den slutter samme tidspunkt som neste periode starter
            // Dette inkluderer også klageberegningen da den er første periode etter
            val privatavtalePerioderJustert = privatAvtalePerioderFiltrert
                .mapIndexed { index, periode ->
                    val erSistePeriode = index == privatAvtalePerioderFiltrert.size - 1
                    val tilDato = if (erSistePeriode) periodeStartInnkreving else periode.periode.til
                    periode.copy(periode = ÅrMånedsperiode(fom = periode.periode.fom, til = tilDato))
                }

            beløpshistorikk.copy(
                nesteIndeksreguleringsår = maxOf(
                    beløpshistorikk.nesteIndeksreguleringsår ?: 0,
                    privatavtalePerioder.innhold.nesteIndeksreguleringsår?.toInt() ?: 0,
                ).takeIf { it != 0 },
                beløpshistorikk = beløpshistorikk.beløpshistorikk + privatavtalePerioderJustert.map {
                    BeløpshistorikkPeriode(
                        periode = it.periode,
                        beløp = it.beløp,
                        valutakode = "NOK",
                        vedtaksid = null,
                    )
                },
            )
        } else {
            beløpshistorikk
        }
    }

    internal fun byggBeløpshistorikk(
        historikk: List<BeregnetBarnebidragResultat>,
        stønad: Stønadsid,
        førPeriode: YearMonth? = null,
        beløpshistorikkFørOmgjortVedtak: BeløpshistorikkGrunnlag,
    ): ByggetBeløpshistorikk {
        val personer = historikk.flatMap { it.grunnlagListe.hentAllePersoner() }.map { it.tilDto() }.toMutableList()

        val perioder = historikk.filter { it.beregnetBarnebidragPeriodeListe.isNotEmpty() }.sortedBy { it.beregnetFraDato }.flatMap {
            val grunnlagsliste = it.grunnlagListe
            it.beregnetBarnebidragPeriodeListe.map {
                val erResultatIngenEndring = grunnlagsliste.erResultatUnderGrense(it.grunnlagsreferanseListe)
                StønadPeriodeDto(
                    periodeid = 1,
                    periode = it.periode,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    beløp = it.resultat.beløp,
                    stønadsid = 1,
                    valutakode = "NOK",
                    vedtaksid = vedtaksidBeregnetBeløpshistorikk,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        }.sortedBy { it.periode.fom }.sorterOgJusterPerioder2()
            .filter { førPeriode == null || it.periode.fom.isBefore(førPeriode) }
            .justerSistePeriodeTilÅBliLøpende()

        val førstePeriode = perioder.minOfOrNull { it.periode.fom }
        val perioderFørFraBeløpshistorikk = beløpshistorikkFørOmgjortVedtak.beløpshistorikk
            .filter { førstePeriode == null || it.periode.fom.isBefore(førstePeriode) }
            .map {
                val vedtak = it.vedtaksid?.let { vedtakService.hentVedtak(it) }
                val erResultatIngenEndring = vedtak?.let { vedtaksid ->
                    val periode = vedtak.finnStønadsendring(stønad)!!.periodeListe.find { vp -> vp.periode.fom == it.periode.fom }!!
                    vedtak.grunnlagListe.erResultatUnderGrense(periode.grunnlagReferanseListe)
                } ?: false

                StønadPeriodeDto(
                    periodeid = 1,
                    periode = it.periode,
                    resultatkode = when {
                        erResultatIngenEndring -> Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name
                        else -> Resultatkode.KOSTNADSBEREGNET_BIDRAG.name
                    },
                    beløp = it.beløp,
                    stønadsid = 1,
                    valutakode = "NOK",
                    vedtaksid = when {
                        vedtak != null && vedtak.type.erIndeksEllerAldersjustering -> vedtaksidAutomatiskJobb
                        else -> it.vedtaksid ?: vedtaksidPrivatavtale
                    },
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        val sistePeriode = perioder.maxByOrNull { it.periode.fom }
        val sisteRelevantePeriode = perioder.sortedBy { it.periode.fom }
            .lastOrNull { it.resultatkode != Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name }
        val nesteIndeksår = when {
            sisteRelevantePeriode == null -> sistePeriode?.periode?.til?.year ?: sistePeriode?.periode?.fom?.year ?: YearMonth.now().year
            else -> sisteRelevantePeriode.periode.fom.year + 1
        }
        val stønadDto = opprettStønad(stønad).copy(
            førsteIndeksreguleringsår = nesteIndeksår,
            nesteIndeksreguleringsår = nesteIndeksår,
            innkreving = Innkrevingstype.MED_INNKREVING,
            periodeListe = (perioderFørFraBeløpshistorikk + perioder).sorterOgJusterPerioder2(),
        )
        personer.hentPersonMedIdent(stønad.kravhaver.verdi) ?: personer.hentPersonForNyesteIdent(identUtils, stønad.kravhaver) ?: run {
            val grunnlag = opprettPersonGrunnlag(stønad.kravhaver, Rolletype.BARN)
            personer.add(grunnlag)
        }

        personer.hentPersonMedIdent(stønad.skyldner.verdi) ?: personer.hentPersonForNyesteIdent(identUtils, stønad.skyldner) ?: run {
            val grunnlag = opprettPersonGrunnlag(stønad.skyldner, Rolletype.BIDRAGSPLIKTIG)
            personer.add(grunnlag)
        }
        val grunnlagBeløpshistorikk = stønadDto.tilGrunnlag(personer.toMutableList(), stønad, identUtils)
        val grunnlagsliste = (listOf(grunnlagBeløpshistorikk) + personer).toSet().toList()

        return ByggetBeløpshistorikk(nesteIndeksår, grunnlagsliste, stønadDto, grunnlagBeløpshistorikk)
    }

    fun opprettPersonGrunnlag(ident: Personident, rolle: Rolletype): GrunnlagDto = GrunnlagDto(
        referanse = "person_${rolle.name}_$ident",
        type = rolle.tilGrunnlagstype(),
        innhold = POJONode(
            Person(
                ident = ident,
                navn = null,
                fødselsdato = identUtils.hentFødselsdato(ident) ?: omgjøringFeilet("Fant ikke fødselsdato for person $ident med rolle $rolle"),
            ),
        ),
    )
    private fun List<StønadPeriodeDto>.sorterOgJusterPerioder2(): List<StønadPeriodeDto> {
        val sortert = sortedBy { it.periode.fom }

        return sortert.mapIndexed { indeks, resultatPeriode ->
            val nesteFom = sortert.getOrNull(indeks + 1)?.periode?.fom
            resultatPeriode.copy(
                periode = ÅrMånedsperiode(fom = resultatPeriode.periode.fom, til = nesteFom ?: resultatPeriode.periode.til),
            )
        }
    }
    private fun List<GrunnlagDto>.erResultatUnderGrense(grunnlagsreferanseListe: List<Grunnlagsreferanse>): Boolean {
        val søknadsbarn = finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<Person>(
            Grunnlagstype.PERSON_SØKNADSBARN,
            grunnlagsreferanseListe,
        ).firstOrNull() ?: run {
            val refererTil = grunnlagsreferanseListe.mapNotNull { gr -> find { it.referanse == gr }?.gjelderBarnReferanse }
            filtrerOgKonverterBasertPåEgenReferanse<Person>(
                Grunnlagstype.PERSON_SØKNADSBARN,
                referanse = refererTil.firstOrNull() ?: "",
            ).firstOrNull()
        }
        return søknadsbarn?.let { erResultatEndringUnderGrense(søknadsbarn.referanse) } ?: false
    }
}
