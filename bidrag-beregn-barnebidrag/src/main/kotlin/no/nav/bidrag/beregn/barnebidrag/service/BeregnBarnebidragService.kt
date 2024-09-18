package no.nav.bidrag.beregn.barnebidrag.service

import no.nav.bidrag.beregn.barnebidrag.beregning.BPAndelUnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.BarnebidragBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.BidragsevneBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.NettoTilsynsutgiftBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.SamværsfradragBeregning
import no.nav.bidrag.beregn.barnebidrag.beregning.UnderholdskostnadBeregning
import no.nav.bidrag.beregn.barnebidrag.grunnlag.AntallBarnIHusstand
import no.nav.bidrag.beregn.barnebidrag.grunnlag.BidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.grunnlag.BidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.grunnlag.BostatusVoksneIHusstand
import no.nav.bidrag.beregn.barnebidrag.grunnlag.Inntekt
import no.nav.bidrag.beregn.barnebidrag.mapper.BPAndelUnderholdskostnadMapper
import no.nav.bidrag.beregn.barnebidrag.mapper.BarnebidragMapper
import no.nav.bidrag.beregn.barnebidrag.mapper.BidragsevneMapper
import no.nav.bidrag.beregn.barnebidrag.mapper.NettoTilsynsutgiftMapper
import no.nav.bidrag.beregn.barnebidrag.mapper.SamværsfradragMapper
import no.nav.bidrag.beregn.barnebidrag.mapper.UnderholdskostnadMapper
import no.nav.bidrag.beregn.barnebidrag.resultat.BidragsevneBeregningResultat
import no.nav.bidrag.beregn.core.dto.BarnIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.dto.InntektPeriodeCore
import no.nav.bidrag.beregn.core.dto.VoksneIHusstandenPeriodeCore
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.rolle.Rolle
import no.nav.bidrag.domene.tid.Datoperiode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.felles.valider
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import java.math.BigDecimal

class BeregnBarnebidragService : BeregnService() {
    private val bidragsevneMapper = BidragsevneMapper
    private val nettoTilsynsutgiftMapper = NettoTilsynsutgiftMapper()
    private val underholdskostnadMapper = UnderholdskostnadMapper()
    private val bpAndelUnderholdskostnadMapper = BPAndelUnderholdskostnadMapper()
    private val samværsfradragMapper = SamværsfradragMapper()
    private val barnebidragMapper = BarnebidragMapper()

    private val bidragsevneBeregning = BidragsevneBeregning
    private val nettoTilsynsutgiftBeregning = NettoTilsynsutgiftBeregning()
    private val underholdskostnadBeregning = UnderholdskostnadBeregning()
    private val bpAndelUnderholdskostnadBeregning = BPAndelUnderholdskostnadBeregning()
    private val samværsfradragBeregning = SamværsfradragBeregning()
    private val barnebidragBeregning = BarnebidragBeregning()

    // Full beregning av barnebidrag, inkludert alle delberegninger
    fun beregnBarnebidrag(mottattGrunnlag: BeregnGrunnlag) {
        secureLogger.debug { "Beregning av barnebidrag - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av barnebidrag: " + e.message)
        }

        val sjablonListe = listOf("sjablonListe") // Liste over sjabloner som skal hentes
        val sjablonGrunnlag = hentSjablonerOgLagGrunnlagsobjekter(mottattGrunnlag.periode, sjablonListe)

        val sumInntektBPListe = akkumulerOgPeriodiserInntekter(mottattGrunnlag, Rolle.BIDRAGSPLIKTIG)
        val sumInntektBMListe = akkumulerOgPeriodiserInntekter(mottattGrunnlag, Rolle.BIDRAGSMOTTAKER)
        val sumInntektBAListe = akkumulerOgPeriodiserInntekter(mottattGrunnlag, Rolle.SØKNADSBARN)

        val voksneIHusstandBPListe = evaluerOgPeriodiserVoksneIHusstand(mottattGrunnlag)
        val antallBarnIHusstandBPListe = akkumulerOgPeriodiserBarnIHusstand(mottattGrunnlag)

        val delberegningBidragsevne =
            delberegningBidragsevne(mottattGrunnlag, sjablonGrunnlag, sumInntektBPListe, voksneIHusstandBPListe, antallBarnIHusstandBPListe)
        val delberegningUnderholdskostnad = delberegningUnderholdskostnad(mottattGrunnlag, sjablonGrunnlag)
//        val delberegningBpAndelUnderholdskostnad =
//            delberegningBpAndelUnderholdskostnad(mottattGrunnlag, sjablonGrunnlag, sumInntektBPListe, sumInntektBMListe, sumInntektBAListe)
//        val delberegningSamværsfradrag = delberegningSamværsfradrag(mottattGrunnlag, sjablonGrunnlag)
//        val delberegningBarnebidrag = delberegningBarnebidrag(mottattGrunnlag, sjablonGrunnlag)
        // opprettSluttberegning
        // byggResponsobjekt
    }

    // Beregning av underholdskostnad, inkludert netto tilsynsutgift
    fun beregnUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag) {
        secureLogger.debug { "Beregning av underholdskostnad - følgende request mottatt: ${tilJson(mottattGrunnlag)}" }

        // Kontroll av inputdata
        try {
            mottattGrunnlag.valider()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ugyldig input ved beregning av underholdskostnad: " + e.message)
        }

        val sjablonListe = listOf("sjablonListe") // Liste over sjabloner som skal hentes
        val sjablonGrunnlag = hentSjablonerOgLagGrunnlagsobjekter(mottattGrunnlag.periode, sjablonListe)
        val delberegningUnderholdskostnad = delberegningUnderholdskostnad(mottattGrunnlag, sjablonGrunnlag)
        // byggResponsobjekt
    }

    // Skal hente sjabloner som brukes av barnebidragberegning og lage grunnlagsobjekter (ett objekt pr sjablonverdi som er innenfor perioden)
    private fun hentSjablonerOgLagGrunnlagsobjekter(periode: ÅrMånedsperiode, sjablonListe: List<String>): List<GrunnlagDto> = emptyList()

    // Skal akkumulere og periodisere inntekter for en gitt rolle (delberegning). Bør gjøres felles (brukes også av forskudd og særbidrag)
    private fun akkumulerOgPeriodiserInntekter(mottattGrunnlag: BeregnGrunnlag, rolle: Rolle): List<InntektPeriodeCore> = emptyList()

    // Skal evaluere og periodisere voksne i husstanden til BP (delberegning). Bør gjøres felles (brukes også av særbidrag)
    private fun evaluerOgPeriodiserVoksneIHusstand(mottattGrunnlag: BeregnGrunnlag): List<VoksneIHusstandenPeriodeCore> = emptyList()

    // Skal akkumulere og periodisere barn i husstanden til BP (delberegning). Bør gjøres felles (brukes også av særbidrag)
    private fun akkumulerOgPeriodiserBarnIHusstand(mottattGrunnlag: BeregnGrunnlag): List<BarnIHusstandenPeriodeCore> = emptyList()

    // ========== Beregning av bidragsevne ==========

    private fun delberegningBidragsevne(
        mottattGrunnlag: BeregnGrunnlag,
        sjablonGrunnlag: List<GrunnlagDto>,
        sumInntektBPListe: List<InntektPeriodeCore>,
        voksneIHusstandBPListe: List<VoksneIHusstandenPeriodeCore>,
        antallBarnIHusstandBPListe: List<BarnIHusstandenPeriodeCore>,
    ) {
        val bidragsevnePeriodeGrunnlag = bidragsevneMapper.mapBidragsevneGrunnlag(mottattGrunnlag, sjablonGrunnlag)
        val bruddPeriodeListe = lagBruddPeriodeListe(bidragsevnePeriodeGrunnlag)
        val bidragsevneBeregningResultatListe = mutableListOf<BidragsevneBeregningResultat>()
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragsevneBeregningGrunnlag = lagBidragsevneBeregningGrunnlag(bidragsevnePeriodeGrunnlag, bruddPeriode)
            bidragsevneBeregningResultatListe.add(bidragsevneBeregning.beregn(bidragsevneBeregningGrunnlag))
        }
    }

    // Skal lage grunnlag for beregning som ligger innenfor bruddPeriode (se BidragsevnePeriodeImpl)
    private fun lagBidragsevneBeregningGrunnlag(bidragsevnePeriodeGrunnlag: BidragsevnePeriodeGrunnlag, bruddPeriode: Datoperiode) =
        BidragsevneBeregningGrunnlag(
            inntekt = Inntekt(referanse = "", inntektBeløp = BigDecimal.ZERO),
            antallBarnIHusstand = AntallBarnIHusstand(referanse = "", antallBarn = 0.0),
            bostatusVoksneIHusstand = BostatusVoksneIHusstand(referanse = "", borMedAndre = true),
            sjablonListe = emptyList(),
        )

    // ========== Beregning av underholdskostnad ==========

    private fun delberegningUnderholdskostnad(mottattGrunnlag: BeregnGrunnlag, sjablonGrunnlag: List<GrunnlagDto>) {
        // val nettoTilsynsutgiftBeregningResultatListe = delberegningNettoTilsynsutgift(mottattGrunnlag, sjablonGrunnlag)
        // val underholdskostnadGrunnlag = bidragsevneMapper.mapUnderholdskostnadGrunnlag(mottattGrunnlag, sjablonGrunnlag, nettoTilsynsutgiftBeregningResultatListe)
        // val bruddPeriodeListe = lagBruddPeriodeListe(underholdskostnadGrunnlag)
        // bruddPeriodeListe.forEach { bruddPeriode ->
        //     val beregningGrunnlag = lagBeregningGrunnlag(underholdskostnadGrunnlag, bruddPeriode)
        //     val underholdskostnadBeregningResultatListe.add(underholdskostnadBeregning.beregn(beregningGrunnlag))
        // }
    }

    private fun delberegningNettoTilsynsutgift() {
        // val nettoTilsysnsutgiftGrunnlag = nettoTilsynsutgiftMapper.mapNettoTilsysnsutgiftGrunnlag(mottattGrunnlag, sjablonGrunnlag)
        // val bruddPeriodeListe = lagBruddPeriodeListe(nettoTilsynsutgiftGrunnlag)
        // bruddPeriodeListe.forEach { bruddPeriode ->
        //     val beregningGrunnlag = lagBeregningGrunnlag(nettoTilsynsutgiftGrunnlag, bruddPeriode)
        //     val nettoTilsynsutgiftBeregningResultatListe.add(nettoTilsynsutgiftBeregning.beregn(beregningGrunnlag))
        // }
    }

    // Skal lage en liste over alle bruddperioder basert på mottatte grunnlagsobjekter (se akkumulerOgPeriodiser)
    private fun lagBruddPeriodeListe(grunnlagListe: BidragsevnePeriodeGrunnlag): List<Datoperiode> = emptyList()
//    private fun <T : Delberegning> lagBruddPeriodeListe(grunnlagListe: List<T>): List<Periode> {
//        // Lager unik, sortert liste over alle bruddatoer og legger evt. null-forekomst bakerst
//        // TODO: Forventer grunnlag som implementerer Delberegning (må gjøres om)
//        return grunnlagListe
//            .asSequence()
//            .flatMap { listOf(it.periode.datoFom, it.periode.datoTil) }
//            .distinct()
//            .sortedBy { it }
//            .sortedWith(compareBy { it == null })
//            .zipWithNext()
//            .map { no.nav.bidrag.beregn.core.bo.Periode(it.first!!, it.second) }
//            .toList()
//    }
}
