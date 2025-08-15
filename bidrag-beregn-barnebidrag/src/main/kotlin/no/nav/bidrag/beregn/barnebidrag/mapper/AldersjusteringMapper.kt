package no.nav.bidrag.beregn.barnebidrag.mapper

import no.nav.bidrag.beregn.barnebidrag.bo.AldersjusteringBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BeløpshistorikkPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SøknadsbarnPeriodeGrunnlag
import no.nav.bidrag.beregn.core.exception.UgyldigInputException
import no.nav.bidrag.beregn.core.mapper.CoreMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlagVedtak
import no.nav.bidrag.transport.behandling.felles.grunnlag.BeløpshistorikkGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnSluttberegningIReferanser
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.time.Period

internal object AldersjusteringMapper : CoreMapper() {

    fun mapAldersjusteringGrunnlagFraVedtak(
        beregningsperiode: ÅrMånedsperiode,
        grunnlagsperiode: ÅrMånedsperiode,
        søknadsbarn: SøknadsbarnPeriodeGrunnlag,
        bidragsmottakerReferanse: String,
        bidragspliktigReferanse: String,
        vedtak: BeregnGrunnlagVedtak,
        sjablonGrunnlagListe: List<GrunnlagDto>,
        beløpshistorikkGrunnlagListe: List<GrunnlagDto>,
    ): AldersjusteringBeregningGrunnlag {
        // Henter ut alle grunnlag fra vedtak
        val grunnlagListeFraVedtak = vedtak.vedtakInnhold.grunnlagListe

        // Henter ut stønadsendringer som gjelder bidrag fra vedtaket. Hvis det ikke finnes stønadsendringer kastes exception.
        val stønadsendringBidragListe = hentStønadsendringBidragListeFraVedtak(vedtak)

        // Henter ut alle perioder fra stønadsendringslisten som matcher med grunnlagsperioden
        val periodeListe = hentPeriodeListeFraStønadsendringListe(
            stønadsendringListe = stønadsendringBidragListe,
            grunnlagsperiode = grunnlagsperiode,
            vedtakId = vedtak.vedtakId,
        )

        // Henter ut sluttberegningsobjekt fra stønadsendringen. Hvis sluttberegning ikke finnes kastes exception.
        val sluttberegning = hentSluttberegningFraGrunnlag(
            grunnlagListeFraVedtak = grunnlagListeFraVedtak,
            grunnlagReferanseListe = periodeListe.flatMap { it.grunnlagReferanseListe },
            vedtakId = vedtak.vedtakId,
            søknadsbarnReferanse = søknadsbarn.referanse,
        )

        // Henter ut alle grunnlagsreferanser som refereres til av sluttberegningen - direkte eller indirekte
        val grunnlagReferanseListeSluttberegning = traverserGrunnlagRekursivt(
            grunnlagListe = grunnlagListeFraVedtak,
            startGrunnlag = sluttberegning,
        )

        // Henter ut grunnlagsobjekter basert på grunnlagsreferanser
        val grunnlagListeSluttberegningSistePeriode =
            grunnlagListeFraVedtak.filter { it.referanse in grunnlagReferanseListeSluttberegning }

        // Henter ut data fra grunnlagsobjektene som skal brukes i beregningen
        val underholdskostnadSistePeriode = grunnlagListeSluttberegningSistePeriode
            .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD)
            .firstOrNull()
            ?: throw UgyldigInputException(
                "Aldersjustering: Delberegning underholdskostnad ikke funnet for søknadsbarn med referanse ${søknadsbarn.referanse} " +
                    "og vedtak med id ${vedtak.vedtakId}",
            )

        val nettoTilsynsutgift = underholdskostnadSistePeriode.innhold.nettoTilsynsutgift

        val barnetilsynMedStønad = underholdskostnadSistePeriode.innhold.barnetilsynMedStønad

        val bpAndelFaktor = (
            grunnlagListeSluttberegningSistePeriode
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL)
                .firstOrNull()
                ?: throw UgyldigInputException(
                    "Aldersjustering: Delberegning bidragspliktiges andel ikke funnet for søknadsbarn med referanse ${søknadsbarn.referanse} " +
                        "og vedtak med id ${vedtak.vedtakId}",
                )
            ).innhold.endeligAndelFaktor

        val samværsklasse = (
            grunnlagListeSluttberegningSistePeriode
                .filtrerOgKonverterBasertPåEgenReferanse<SamværsperiodeGrunnlag>(Grunnlagstype.SAMVÆRSPERIODE)
                .firstOrNull()
                ?: throw UgyldigInputException(
                    "Aldersjustering: Samværsperiode ikke funnet for søknadsbarn med referanse ${søknadsbarn.referanse} " +
                        "og vedtak med id ${vedtak.vedtakId}",
                )
            ).innhold.samværsklasse

        val søknadsbarnAlder = Period.between(
            søknadsbarn.fødselsdato.withMonth(7).withDayOfMonth(1),
            beregningsperiode.fom.atDay(1),
        ).years

        val beløpshistorikk = beløpshistorikkGrunnlagListe
            .filtrerOgKonverterBasertPåEgenReferanse<BeløpshistorikkGrunnlag>(Grunnlagstype.BELØPSHISTORIKK_BIDRAG)
            .filter { it.gjelderBarnReferanse == søknadsbarn.referanse }
            .map {
                BeløpshistorikkPeriodeGrunnlag(
                    referanse = it.referanse,
                    beløpshistorikkPeriode = it.innhold,
                )
            }
            .firstOrNull()

        // Henter sjabloner
        val sjablonSjablontallPeriodeGrunnlagListe = mapSjablonSjablontall(sjablonGrunnlagListe)
        val sjablonBarnetilsynPeriodeGrunnlagListe = mapSjablonBarnetilsyn(sjablonGrunnlagListe)
        val sjablonForbruksutgifterPeriodeGrunnlagListe = mapSjablonForbruksutgifter(sjablonGrunnlagListe)
        val sjablonSamværsfradragPeriodeGrunnlagListe = mapSjablonSamværsfradrag(sjablonGrunnlagListe)

        return AldersjusteringBeregningGrunnlag(
            beregningsperiode = beregningsperiode,
            søknadsbarnReferanse = søknadsbarn.referanse,
            bidragsmottakerReferanse = bidragsmottakerReferanse,
            bidragspliktigReferanse = bidragspliktigReferanse,
            søknadsbarnPeriodeGrunnlag = søknadsbarn,
            vedtakId = vedtak.vedtakId,
            nettoTilsynsutgift = nettoTilsynsutgift,
            barnetilsynMedStønad = barnetilsynMedStønad,
            bpAndelFaktor = bpAndelFaktor,
            samværsklasse = samværsklasse,
            søknadsbarnAlder = søknadsbarnAlder,
            beløpshistorikk = beløpshistorikk,
            sjablonSjablontallPeriodeGrunnlagListe = sjablonSjablontallPeriodeGrunnlagListe,
            sjablonBarnetilsynPeriodeGrunnlagListe = sjablonBarnetilsynPeriodeGrunnlagListe,
            sjablonForbruksutgifterPeriodeGrunnlagListe = sjablonForbruksutgifterPeriodeGrunnlagListe,
            sjablonSamværsfradragPeriodeGrunnlagListe = sjablonSamværsfradragPeriodeGrunnlagListe,
        )
    }

    // Mapper ut SøknadsbarnperiodeGrunnlag
    fun mapSøknadsbarnGrunnlag(personObjektListe: List<GrunnlagDto>) = personObjektListe
        .filtrerOgKonverterBasertPåEgenReferanse<Person>(grunnlagType = Grunnlagstype.PERSON_SØKNADSBARN)
        .map {
            SøknadsbarnPeriodeGrunnlag(
                referanse = it.referanse,
                fødselsdato = it.innhold.fødselsdato,
            )
        }

    // Henter ut stønadsendringer som gjelder bidrag fra vedtaket. Hvis det ikke finnes stønadsendringer kastes exception.
    private fun hentStønadsendringBidragListeFraVedtak(vedtak: BeregnGrunnlagVedtak): List<StønadsendringDto> {
        val stønadsendringBidragListe = vedtak.vedtakInnhold.stønadsendringListe
            .filter { it.type == Stønadstype.BIDRAG }

        if (stønadsendringBidragListe.isEmpty()) {
            throw UgyldigInputException(
                "Aldersjustering: Ingen stønadsendringer av type BIDRAG funnet for vedtak med id ${vedtak.vedtakId}",
            )
        } else {
            return stønadsendringBidragListe
        }
    }

    // Henter ut alle perioder fra stønadsendringslisten som matcher med grunnlagsperioden
    private fun hentPeriodeListeFraStønadsendringListe(
        stønadsendringListe: List<StønadsendringDto>,
        grunnlagsperiode: ÅrMånedsperiode,
        vedtakId: Int,
    ): List<VedtakPeriodeDto> {
        val periodeListe = stønadsendringListe
            .flatMap { it.periodeListe }
            .filter { it.periode.inneholder(grunnlagsperiode) }

        if (periodeListe.isEmpty()) {
            throw UgyldigInputException(
                "Aldersjustering: Stønadsendring av type BIDRAG inneholder ingen perioder som inneholder grunnlagsperiode for " +
                    "vedtak med id $vedtakId",
            )
        } else {
            return periodeListe
        }
    }

    // Henter sluttberegningsobjekt fra grunnlagsreferanseliste. Hvis det finnes flere som matcher hentes det første.
    // Hvis det ikke finnes noen som matcher kastes exception.
    private fun hentSluttberegningFraGrunnlag(
        grunnlagListeFraVedtak: List<GrunnlagDto>,
        grunnlagReferanseListe: List<String>,
        vedtakId: Int,
        søknadsbarnReferanse: String,
    ): GrunnlagDto = grunnlagListeFraVedtak
        .filter { it.gjelderBarnReferanse == søknadsbarnReferanse || it.grunnlagsreferanseListe.contains(søknadsbarnReferanse) }
        .finnSluttberegningIReferanser(grunnlagReferanseListe)
        ?: throw UgyldigInputException(
            "Aldersjustering: Sluttberegning ikke funnet for søknadsbarn med referanse $søknadsbarnReferanse og vedtak med id $vedtakId",
        )

    // Rekursiv funksjon som finner alle grunnlagsreferanser som refereres til fra toppnivået og nedover
    private fun traverserGrunnlagRekursivt(
        grunnlagListe: List<GrunnlagDto>,
        startGrunnlag: GrunnlagDto,
        innsamledeReferanser: MutableSet<String> = mutableSetOf(),
    ): Set<String> {
        // Unngå evig loop ved sirkulære referanser
        if (!innsamledeReferanser.add(startGrunnlag.referanse)) return innsamledeReferanser

        // Finn refererte grunnlagselementer og prosesser dem rekursivt
        startGrunnlag.grunnlagsreferanseListe
            .mapNotNull { ref -> grunnlagListe.find { it.referanse == ref } }
            .forEach {
                traverserGrunnlagRekursivt(
                    grunnlagListe = grunnlagListe,
                    startGrunnlag = it,
                    innsamledeReferanser = innsamledeReferanser,
                )
            }

        return innsamledeReferanser
    }
}
