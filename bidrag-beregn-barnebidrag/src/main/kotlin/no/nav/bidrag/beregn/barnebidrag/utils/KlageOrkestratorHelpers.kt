package no.nav.bidrag.beregn.barnebidrag.utils

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.service.ByggetBeløpshistorikk
import no.nav.bidrag.beregn.barnebidrag.service.VedtakService
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
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.tilGrunnlagstype
import no.nav.bidrag.transport.behandling.vedtak.response.erResultatEndringUnderGrense
import no.nav.bidrag.transport.behandling.vedtak.response.finnStønadsendring
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class KlageOrkestratorHelpers(private val vedtakService: VedtakService, private val identUtils: IdentUtils) {
    internal fun List<StønadPeriodeDto>.justerSistePeriodeTilÅBliLøpende() = mapIndexed { index, periode ->
        if (index == this.size - 1) {
            periode.copy(periode = periode.periode.copy(til = null))
        } else {
            periode
        }
    }

    internal fun byggBeløpshistorikk(
        historikk: List<BeregnetBarnebidragResultat>,
        stønad: Stønadsid,
        førPeriode: YearMonth? = null,
        beløpshistorikkFørPåklagetVedtak: BeløpshistorikkGrunnlag,
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
                    vedtaksid = 1,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        }.sortedBy { it.periode.fom }.sorterOgJusterPerioder2()
            .filter { førPeriode == null || it.periode.fom.isBefore(førPeriode) }
            .justerSistePeriodeTilÅBliLøpende()

        val førstePeriode = perioder.minOfOrNull { it.periode.fom }
        val perioderFørFraBeløpshistorikk = beløpshistorikkFørPåklagetVedtak.beløpshistorikk
            .filter { førstePeriode == null || it.periode.fom.isBefore(førstePeriode) }
            .map {
                val vedtak = vedtakService.hentVedtak(it.vedtaksid!!)!!
                val periode = vedtak.finnStønadsendring(stønad)!!.periodeListe.find { vp -> vp.periode.fom == it.periode.fom }!!
                val erResultatIngenEndring = vedtak.grunnlagListe.erResultatUnderGrense(periode.grunnlagReferanseListe)

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
                    vedtaksid = it.vedtaksid!!,
                    gyldigFra = LocalDateTime.now(),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                )
            }
        val nesteIndeksår = perioder.fold(LocalDate.now().plusYears(1).year) { acc, dto ->
            if (dto.resultatkode == Resultatkode.INGEN_ENDRING_UNDER_GRENSE.name) {
                acc
            } else {
                dto.periode.fom.year + 1
            }
        }
        val stønadDto = StønadDto(
            stønadsid = -1,
            type = stønad.type,
            kravhaver = stønad.kravhaver,
            skyldner = stønad.skyldner,
            sak = stønad.sak,
            mottaker = Personident(""),
            førsteIndeksreguleringsår = nesteIndeksår,
            nesteIndeksreguleringsår = nesteIndeksår,
            innkreving = Innkrevingstype.MED_INNKREVING,
            opprettetAv = "",
            opprettetTidspunkt = LocalDateTime.now(),
            endretAv = "",
            endretTidspunkt = LocalDateTime.now(),
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
                navn = "",
                fødselsdato = LocalDate.now(),
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
