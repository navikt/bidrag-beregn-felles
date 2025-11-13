package no.nav.bidrag.beregn.barnebidrag.service.beregning

import com.fasterxml.jackson.databind.node.POJONode
import no.nav.bidrag.beregn.barnebidrag.beregning.EndeligBidragBeregningV2
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevneBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevneDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevnePeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.AndelAvBidragsevnePeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BarnetilleggDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragJustertForBPBarnetilleggPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingLøpendeBidragPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BidragTilFordelingPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.BidragsevneDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.BpAndelUnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregnetBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregnetPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.EndeligBidragBeregnetPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.Evne25ProsentAvInntektPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.LøpendeBidragTilFordelingBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SamværsfradragDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingPeriodeGrunnlag
import no.nav.bidrag.beregn.barnebidrag.bo.SumBidragTilFordelingPeriodeResultat
import no.nav.bidrag.beregn.barnebidrag.bo.UnderholdskostnadDelberegningBeregningGrunnlag
import no.nav.bidrag.beregn.barnebidrag.mapper.EndeligBidragMapperV2
import no.nav.bidrag.beregn.core.service.BeregnService
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningAndelAvBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragJustertForBPBarnetillegg
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragTilFordelingLøpendeBidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEndeligBidragBeregnet
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningEvne25ProsentAvInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumBidragTilFordeling
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragspliktig
import no.nav.bidrag.transport.felles.toCompactString

internal object BeregnEndeligBidragServiceV2 : BeregnService() {

    fun delberegningEndeligBidrag(
        grunnlagSøknadsbarnListe: List<BeregnGrunnlagJustert>,
        grunnlagLøpendeBidragListe: List<BeregnGrunnlag>,
    ): List<BeregnGrunnlagJustert> {
        // Søknadsbarn: Kaller delberegning Bidrag til fordeling
        var utvidetGrunnlagSøknadsbarnListe = grunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val delberegningBidragTilFordeling = delberegningBidragTilFordeling(
                mottattGrunnlag = beregnGrunnlag.beregnGrunnlag,
                åpenSluttperiode = beregnGrunnlag.åpenSluttperiode,
            )
            beregnGrunnlag.utvidMedNyeGrunnlag(delberegningBidragTilFordeling)
        }

        // Løpende bidrag: Kaller delberegning Bidrag til fordeling løpende bidrag
        val utvidetGrunnlagLøpendeBidragListe = grunnlagLøpendeBidragListe.map { beregnGrunnlag ->
            val delberegningBidragTilFordelingLøpendeBidrag = delberegningBidragTilFordelingLøpendeBidrag(
                mottattGrunnlag = beregnGrunnlag,
                åpenSluttperiode = false, // TODO Sjekk åpenSluttperiode
            )
            beregnGrunnlag.copy(
                grunnlagListe = (beregnGrunnlag.grunnlagListe + delberegningBidragTilFordelingLøpendeBidrag).distinctBy {
                    it.referanse
                },
            )
        }

        // Søknadsbarn og løpende bidrag: Kaller delberegning Sum bidrag til fordeling
        val sumBidragTilFordelingGrunnlagListe = delberegningSumBidragTilFordeling(
            mottattGrunnlagListe = utvidetGrunnlagSøknadsbarnListe.map { it.beregnGrunnlag } + utvidetGrunnlagLøpendeBidragListe,
            åpenSluttperiode = true, // TODO Sjekk åpenSluttperiode
        )

        // Søknadsbarn: Kaller delberegning Evne 25 prosent av inntekt
        utvidetGrunnlagSøknadsbarnListe = utvidetGrunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val delberegningEvne25ProsentAvInntekt = delberegningEvne25ProsentAvInntekt(
                mottattGrunnlag = beregnGrunnlag.beregnGrunnlag,
                åpenSluttperiode = beregnGrunnlag.åpenSluttperiode,
            )
            beregnGrunnlag.utvidMedNyeGrunnlag(delberegningEvne25ProsentAvInntekt + sumBidragTilFordelingGrunnlagListe)
        }

        // Søknadsbarn: Kaller delberegning Andel av bidragsevne
        utvidetGrunnlagSøknadsbarnListe = utvidetGrunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val delberegningAndelAvBidragsevne = delberegningAndelAvBidragsevne(
                mottattGrunnlag = beregnGrunnlag.beregnGrunnlag,
                åpenSluttperiode = beregnGrunnlag.åpenSluttperiode,
            )
            beregnGrunnlag.utvidMedNyeGrunnlag(delberegningAndelAvBidragsevne)
        }

        // Søknadsbarn: Kaller delberegning Bidrag justert for BP barnetillegg
        utvidetGrunnlagSøknadsbarnListe = utvidetGrunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val delberegningBidragJustertForBPBarnetillegg = delberegningBidragJustertForBPBarnetillegg(
                mottattGrunnlag = beregnGrunnlag.beregnGrunnlag,
                åpenSluttperiode = beregnGrunnlag.åpenSluttperiode,
            )
            beregnGrunnlag.utvidMedNyeGrunnlag(delberegningBidragJustertForBPBarnetillegg)
        }

        // Søknadsbarn: Kaller delberegning Endelig bidrag beregnet
        utvidetGrunnlagSøknadsbarnListe = utvidetGrunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val delberegningEndeligBidragBeregnet = delberegningEndeligBidragBeregnet(
                mottattGrunnlag = beregnGrunnlag.beregnGrunnlag,
                åpenSluttperiode = beregnGrunnlag.åpenSluttperiode,
            )
            beregnGrunnlag.utvidMedNyeGrunnlag(delberegningEndeligBidragBeregnet)
        }

        // Filtrerer bort grunnlag som tilhører andra barn (som refereres av delberegning Sum bidrag til fordeling)
        utvidetGrunnlagSøknadsbarnListe = utvidetGrunnlagSøknadsbarnListe.map { beregnGrunnlag ->
            val søknadsbarnReferanse = beregnGrunnlag.beregnGrunnlag.søknadsbarnReferanse
            beregnGrunnlag.copy(
                beregnGrunnlag = beregnGrunnlag.beregnGrunnlag.copy(
                    grunnlagListe = beregnGrunnlag.beregnGrunnlag.grunnlagListe
                        .filter { it.gjelderBarnReferanse == søknadsbarnReferanse || it.gjelderBarnReferanse == null },
                ),
            )
        }

        return utvidetGrunnlagSøknadsbarnListe
        // TODO Bør også returnere løpende bidrag?
    }

    fun delberegningBidragTilFordeling(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne bidrag til fordeling
        val bidragTilFordelingPeriodeGrunnlag = EndeligBidragMapperV2.mapBidragTilFordelingGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBidragTilFordeling(
            grunnlagListe = bidragTilFordelingPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val bidragTilFordelingBeregningResultatListe = mutableListOf<BidragTilFordelingPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidrag til fordeling
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragTilFordelingBeregningGrunnlag =
                lagBidragTilFordelingBeregningGrunnlag(
                    bidragTilFordelingPeriodeGrunnlag = bidragTilFordelingPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            bidragTilFordelingBeregningResultatListe.add(
                BidragTilFordelingPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnBidragTilFordeling(bidragTilFordelingBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (bidragTilFordelingBeregningResultatListe.isNotEmpty()) {
            val sisteElement = bidragTilFordelingBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                bidragTilFordelingBeregningResultatListe[bidragTilFordelingBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = bidragTilFordelingBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning bidrag til fordeling
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragTilFordeling(
                bidragTilFordelingPeriodeResultatListe = bidragTilFordelingBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningSumBidragTilFordeling(mottattGrunnlagListe: List<BeregnGrunnlag>, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne sum bidrag til fordeling
        val sumBidragTilFordelingPeriodeGrunnlag = EndeligBidragMapperV2.mapSumBidragTilFordelingGrunnlag(
            mottattGrunnlagListe = mottattGrunnlagListe,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeSumBidragTilFordeling(
            grunnlagListe = sumBidragTilFordelingPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlagListe[0].periode,
        )

        val sumBidragTilFordelingBeregningResultatListe = mutableListOf<SumBidragTilFordelingPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner sum bidrag til fordeling
        bruddPeriodeListe.forEach { bruddPeriode ->
            val sumBidragTilFordelingBeregningGrunnlag =
                lagSumBidragTilFordelingBeregningGrunnlag(
                    sumBidragTilFordelingPeriodeGrunnlag = sumBidragTilFordelingPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            sumBidragTilFordelingBeregningResultatListe.add(
                SumBidragTilFordelingPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnSumBidragTilFordeling(sumBidragTilFordelingBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (sumBidragTilFordelingBeregningResultatListe.isNotEmpty()) {
            val sisteElement = sumBidragTilFordelingBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                sumBidragTilFordelingBeregningResultatListe[sumBidragTilFordelingBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = sumBidragTilFordelingBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlagListe.flatMap { it.grunnlagListe },
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning sum bidrag til fordeling
        resultatGrunnlagListe.addAll(
            mapDelberegningSumBidragTilFordeling(
                sumBidragTilFordelingPeriodeResultatListe = sumBidragTilFordelingBeregningResultatListe,
                bidragspliktigReferanse = mottattGrunnlagListe[0].grunnlagListe.bidragspliktig?.referanse,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlagListe.flatMap { it.grunnlagListe },
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningEvne25ProsentAvInntekt(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne bidragsevne justert for 25% av inntekt
        val evne25ProsentAvInntektPeriodeGrunnlag = EndeligBidragMapperV2.mapEvne25ProsentAvInntektGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeEvne25ProsentAvInntekt(
            grunnlagListe = evne25ProsentAvInntektPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val evne25ProsentAvInntektBeregningResultatListe = mutableListOf<Evne25ProsentAvInntektPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidragsevne justert for 25% av inntekt
        bruddPeriodeListe.forEach { bruddPeriode ->
            val evne25ProsentAvInntektBeregningGrunnlag =
                lagEvne25ProsentAvInntektBeregningGrunnlag(
                    evne25ProsentAvInntektPeriodeGrunnlag = evne25ProsentAvInntektPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            evne25ProsentAvInntektBeregningResultatListe.add(
                Evne25ProsentAvInntektPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnEvne25ProsentAvInntekt(evne25ProsentAvInntektBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (evne25ProsentAvInntektBeregningResultatListe.isNotEmpty()) {
            val sisteElement = evne25ProsentAvInntektBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                evne25ProsentAvInntektBeregningResultatListe[evne25ProsentAvInntektBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = evne25ProsentAvInntektBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning evne 25 prosent av inntekt
        resultatGrunnlagListe.addAll(
            mapDelberegningEvne25ProsentAvInntekt(
                evne25ProsentAvInntektPeriodeResultatListe = evne25ProsentAvInntektBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningAndelAvBidragsevne(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne andel av bidragsevne
        val andelAvBidragsevnePeriodeGrunnlag = EndeligBidragMapperV2.mapAndelAvBidragsevneGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeAndelAvBidragsevne(
            grunnlagListe = andelAvBidragsevnePeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val andelAvBidragsevneBeregningResultatListe = mutableListOf<AndelAvBidragsevnePeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner andel av bidragsevne
        bruddPeriodeListe.forEach { bruddPeriode ->
            val andelAvBidragsevneBeregningGrunnlag =
                lagAndelAvBidragsevneBeregningGrunnlag(
                    andelAvBidragsevnePeriodeGrunnlag = andelAvBidragsevnePeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            andelAvBidragsevneBeregningResultatListe.add(
                AndelAvBidragsevnePeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnAndelAvBidragsevne(andelAvBidragsevneBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (andelAvBidragsevneBeregningResultatListe.isNotEmpty()) {
            val sisteElement = andelAvBidragsevneBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                andelAvBidragsevneBeregningResultatListe[andelAvBidragsevneBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = andelAvBidragsevneBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning andel av bidragsevne
        resultatGrunnlagListe.addAll(
            mapDelberegningAndelAvBidragsevne(
                andelAvBidragsevnePeriodeResultatListe = andelAvBidragsevneBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningBidragTilFordelingLøpendeBidrag(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        val bidragTilFordelingLøpendeBidragPeriodeGrunnlag = EndeligBidragMapperV2.mapBidragTilFordelingLøpendeBidragGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBidragTilFordelingLøpendeBidrag(
            grunnlagListe = bidragTilFordelingLøpendeBidragPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val bidragTilFordelingLøpendeBidragBeregningResultatListe = mutableListOf<BidragTilFordelingLøpendeBidragPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidrag til fordeling løpende bidrag
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragTilFordelingLøpendeBidragBeregningGrunnlag =
                lagBidragTilFordelingLøpendeBidragBeregningGrunnlag(
                    bidragTilFordelingLøpendeBidragPeriodeGrunnlag = bidragTilFordelingLøpendeBidragPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            bidragTilFordelingLøpendeBidragBeregningResultatListe.add(
                BidragTilFordelingLøpendeBidragPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnBidragTilFordelingLøpendeBidrag(bidragTilFordelingLøpendeBidragBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (bidragTilFordelingLøpendeBidragBeregningResultatListe.isNotEmpty()) {
            val sisteElement = bidragTilFordelingLøpendeBidragBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                bidragTilFordelingLøpendeBidragBeregningResultatListe[bidragTilFordelingLøpendeBidragBeregningResultatListe.size - 1] =
                    oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = bidragTilFordelingLøpendeBidragBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning bidrag til fordeling løpende bidrag
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragTilFordelingLøpendeBidrag(
                bidragTilFordelingLøpendeBidragPeriodeResultatListe = bidragTilFordelingLøpendeBidragBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningBidragJustertForBPBarnetillegg(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne bidrag justert for BP barnetillegg
        val bidragJustertForBPBarnetilleggPeriodeGrunnlag = EndeligBidragMapperV2.mapBidragJustertForBPBarnetilleggGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeBidragJustertForBPBarnetillegg(
            grunnlagListe = bidragJustertForBPBarnetilleggPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val bidragJustertForBPBarnetilleggBeregningResultatListe = mutableListOf<BidragJustertForBPBarnetilleggPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner bidrag justert for BP barnetillegg
        bruddPeriodeListe.forEach { bruddPeriode ->
            val bidragJustertForBPBarnetilleggBeregningGrunnlag =
                lagBidragJustertForBPBarnetilleggBeregningGrunnlag(
                    bidragJustertForBPBarnetilleggPeriodeGrunnlag = bidragJustertForBPBarnetilleggPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            bidragJustertForBPBarnetilleggBeregningResultatListe.add(
                BidragJustertForBPBarnetilleggPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnBidragJustertForBPBarnetillegg(bidragJustertForBPBarnetilleggBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (bidragJustertForBPBarnetilleggBeregningResultatListe.isNotEmpty()) {
            val sisteElement = bidragJustertForBPBarnetilleggBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                bidragJustertForBPBarnetilleggBeregningResultatListe[bidragJustertForBPBarnetilleggBeregningResultatListe.size - 1] =
                    oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = bidragJustertForBPBarnetilleggBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning bidrag justert for BP barnetillegg
        resultatGrunnlagListe.addAll(
            mapDelberegningBidragJustertForBPBarnetillegg(
                bidragJustertForBPBarnetilleggPeriodeResultatListe = bidragJustertForBPBarnetilleggBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    fun delberegningEndeligBidragBeregnet(mottattGrunnlag: BeregnGrunnlag, åpenSluttperiode: Boolean = true): List<GrunnlagDto> {
        // Mapper ut grunnlag som skal brukes for å beregne endelig bidrag beregnet
        val endeligBidragBeregnetPeriodeGrunnlag = EndeligBidragMapperV2.mapEndeligBidragBeregnetGrunnlag(
            mottattGrunnlag = mottattGrunnlag,
        )

        // Lager liste over bruddperioder
        val bruddPeriodeListe = lagBruddPeriodeListeEndeligBidragBeregnet(
            grunnlagListe = endeligBidragBeregnetPeriodeGrunnlag,
            beregningsperiode = mottattGrunnlag.periode,
        )

        val endeligBidragBeregnetBeregningResultatListe = mutableListOf<EndeligBidragBeregnetPeriodeResultat>()

        // Løper gjennom hver bruddperiode og beregner endelig bidrag beregnet
        bruddPeriodeListe.forEach { bruddPeriode ->
            val endeligBidragBeregnetBeregningGrunnlag =
                lagEndeligBidragBeregnetBeregningGrunnlag(
                    endeligBidragBeregnetPeriodeGrunnlag = endeligBidragBeregnetPeriodeGrunnlag,
                    bruddPeriode = bruddPeriode,
                )
            endeligBidragBeregnetBeregningResultatListe.add(
                EndeligBidragBeregnetPeriodeResultat(
                    periode = bruddPeriode,
                    resultat = EndeligBidragBeregningV2.beregnEndeligBidragBeregnet(endeligBidragBeregnetBeregningGrunnlag),
                ),
            )
        }

        // Setter til-periode i siste element til null hvis det ikke allerede er det og åpenSluttperiode er true
        if (endeligBidragBeregnetBeregningResultatListe.isNotEmpty()) {
            val sisteElement = endeligBidragBeregnetBeregningResultatListe.last()
            if (sisteElement.periode.til != null && åpenSluttperiode) {
                val oppdatertSisteElement = sisteElement.copy(periode = sisteElement.periode.copy(til = null))
                endeligBidragBeregnetBeregningResultatListe[endeligBidragBeregnetBeregningResultatListe.size - 1] = oppdatertSisteElement
            }
        }

        // Mapper ut grunnlag som er brukt i beregningen (mottatte grunnlag og sjabloner)
        val resultatGrunnlagListe = mapDelberegningResultatGrunnlag(
            grunnlagReferanseListe = endeligBidragBeregnetBeregningResultatListe
                .flatMap { it.resultat.grunnlagsreferanseListe }
                .distinct(),
            mottattGrunnlag = mottattGrunnlag,
            sjablonGrunnlag = emptyList(),
        )

        // Mapper ut grunnlag for delberegning endelig bidrag beregnet
        resultatGrunnlagListe.addAll(
            mapDelberegningEndeligBidragBeregnet(
                endeligBidragBeregnetPeriodeResultatListe = endeligBidragBeregnetBeregningResultatListe,
                mottattGrunnlag = mottattGrunnlag,
            ),
        )

        // Mapper ut grunnlag for Person-objekter som er brukt
        resultatGrunnlagListe.addAll(
            mapPersonobjektGrunnlag(
                resultatGrunnlagListe = resultatGrunnlagListe,
                personobjektGrunnlagListe = mottattGrunnlag.grunnlagListe,
            ),
        )

        return resultatGrunnlagListe.distinctBy { it.referanse }.sortedBy { it.referanse }
    }

    // DELBEREGNING_BIDRAG_TIL_FORDELING

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBidragTilFordeling(
        grunnlagListe: BidragTilFordelingPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.underholdskostnadDelberegningPeriodeGrunnlagListe.asSequence().map { it.underholdskostnadPeriode.periode })
            .plus(
                grunnlagListe.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe.asSequence()
                    .map { it.bpAndelUnderholdskostnadPeriode.periode },
            )
            .plus(grunnlagListe.nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe.asSequence().map { it.nettoBarnetilleggPeriode.periode })
            .plus(grunnlagListe.samværsfradragDelberegningPeriodeGrunnlagListe.asSequence().map { it.samværsfradragPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for bidrag til fordeling som ligger innenfor bruddPeriode
    private fun lagBidragTilFordelingBeregningGrunnlag(
        bidragTilFordelingPeriodeGrunnlag: BidragTilFordelingPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BidragTilFordelingBeregningGrunnlag {
        val underholdskostnadBeregningGrunnlag = bidragTilFordelingPeriodeGrunnlag.underholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.underholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                UnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.underholdskostnadPeriode.underholdskostnad,
                )
            }
            ?: throw IllegalArgumentException("Underholdskostnad grunnlag mangler for periode $bruddPeriode")
        val bpAndelUnderholdskostnadBeregningGrunnlag = bidragTilFordelingPeriodeGrunnlag.bpAndelUnderholdskostnadDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bpAndelUnderholdskostnadPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BpAndelUnderholdskostnadDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    andelBeløp = it.bpAndelUnderholdskostnadPeriode.andelBeløp,
                    andelFaktor = it.bpAndelUnderholdskostnadPeriode.endeligAndelFaktor,
                    barnetErSelvforsørget = it.bpAndelUnderholdskostnadPeriode.barnetErSelvforsørget,
                )
            }
            ?: throw IllegalArgumentException("BP andel underholdskostnad grunnlag mangler for periode $bruddPeriode")
        val barnetilleggBMBeregningGrunnlag = bidragTilFordelingPeriodeGrunnlag.nettoBarnetilleggBMDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.nettoBarnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.nettoBarnetilleggPeriode.summertNettoBarnetillegg,
                )
            }
        val samværsfradragBeregningGrunnlag = bidragTilFordelingPeriodeGrunnlag.samværsfradragDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.samværsfradragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { SamværsfradragDelberegningBeregningGrunnlag(referanse = it.referanse, beløp = it.samværsfradragPeriode.beløp) }
            ?: throw IllegalArgumentException("Samværsfradrag grunnlag mangler for periode $bruddPeriode")

        return BidragTilFordelingBeregningGrunnlag(
            underholdskostnadBeregningGrunnlag = underholdskostnadBeregningGrunnlag,
            bpAndelUnderholdskostnadBeregningGrunnlag = bpAndelUnderholdskostnadBeregningGrunnlag,
            barnetilleggBMBeregningGrunnlag = barnetilleggBMBeregningGrunnlag,
            samværsfradragBeregningGrunnlag = samværsfradragBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningBidragTilFordeling
    private fun mapDelberegningBidragTilFordeling(
        bidragTilFordelingPeriodeResultatListe: List<BidragTilFordelingPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = bidragTilFordelingPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING,
                innhold = POJONode(
                    DelberegningBidragTilFordeling(
                        periode = it.periode,
                        bidragTilFordeling = it.resultat.bidragTilFordeling,
                        uMinusNettoBarnetilleggBM = it.resultat.uMinusNettoBarnetilleggBM,
                        bpAndelAvUMinusSamværsfradrag = it.resultat.bpAndelAvUMinusSamværsfradrag,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // DELBEREGNING_SUM_BIDRAG_TIL_FORDELING

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeSumBidragTilFordeling(
        grunnlagListe: SumBidragTilFordelingPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.bidragTilFordelingDelberegningPeriodeGrunnlagListe.asSequence().map { it.bidragTilFordelingPeriode.periode })
            .plus(
                grunnlagListe.bidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlagListe.asSequence()
                    .map { it.bidragTilFordelingLøpendeBidragPeriode.periode },
            )

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for sum bidrag til fordeling som ligger innenfor bruddPeriode
    private fun lagSumBidragTilFordelingBeregningGrunnlag(
        sumBidragTilFordelingPeriodeGrunnlag: SumBidragTilFordelingPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): SumBidragTilFordelingBeregningGrunnlag {
        val bidragTilFordelingBeregningGrunnlagListe = sumBidragTilFordelingPeriodeGrunnlag.bidragTilFordelingDelberegningPeriodeGrunnlagListe
            .filter { it.bidragTilFordelingPeriode.periode.inneholder(bruddPeriode) }
            .map {
                BidragTilFordelingDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    bidragTilFordeling = it.bidragTilFordelingPeriode.bidragTilFordeling,
                )
            }
        if (bidragTilFordelingBeregningGrunnlagListe.isEmpty()) {
            throw IllegalArgumentException("Bidrag til fordeling grunnlag mangler for periode $bruddPeriode")
        }

        val bidragTilFordelingLøpendeBidragBeregningGrunnlagListe =
            sumBidragTilFordelingPeriodeGrunnlag.bidragTilFordelingLøpendeBidragDelberegningPeriodeGrunnlagListe
                .filter { it.bidragTilFordelingLøpendeBidragPeriode.periode.inneholder(bruddPeriode) }
                .map {
                    BidragTilFordelingLøpendeBidragDelberegningBeregningGrunnlag(
                        referanse = it.referanse,
                        bidragTilFordeling = it.bidragTilFordelingLøpendeBidragPeriode.bidragTilFordeling,
                    )
                }

        return SumBidragTilFordelingBeregningGrunnlag(
            bidragTilFordelingBeregningGrunnlagListe = bidragTilFordelingBeregningGrunnlagListe,
            bidragTilFordelingLøpendeBidragBeregningGrunnlagListe = bidragTilFordelingLøpendeBidragBeregningGrunnlagListe,
        )
    }

    // Mapper ut DelberegningSumBidragTilFordeling
    private fun mapDelberegningSumBidragTilFordeling(
        sumBidragTilFordelingPeriodeResultatListe: List<SumBidragTilFordelingPeriodeResultat>,
        bidragspliktigReferanse: String?,
    ): List<GrunnlagDto> = sumBidragTilFordelingPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    gjelderReferanse = bidragspliktigReferanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_SUM_BIDRAG_TIL_FORDELING,
                innhold = POJONode(
                    DelberegningSumBidragTilFordeling(
                        periode = it.periode,
                        sumBidragTilFordeling = it.resultat.sumBidragTilFordeling,
                        sumPrioriterteBidragTilFordeling = it.resultat.sumPrioriterteBidragTilFordeling,
                        erKompletteGrunnlagForAlleLøpendeBidrag = it.resultat.erKompletteGrunnlagForAlleLøpendeBidrag,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = bidragspliktigReferanse,
            )
        }

    // DELBEREGNING_EVNE_25PROSENTAVINNTEKT

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeEvne25ProsentAvInntekt(
        grunnlagListe: Evne25ProsentAvInntektPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.bidragsevneDelberegningPeriodeGrunnlagListe.asSequence().map { it.bidragsevnePeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for evne 25 prosenta av inntekt som ligger innenfor bruddPeriode
    private fun lagEvne25ProsentAvInntektBeregningGrunnlag(
        evne25ProsentAvInntektPeriodeGrunnlag: Evne25ProsentAvInntektPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): Evne25ProsentAvInntektBeregningGrunnlag {
        val bidragsevneBeregningGrunnlag = evne25ProsentAvInntektPeriodeGrunnlag.bidragsevneDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bidragsevnePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BidragsevneDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.bidragsevnePeriode.beløp,
                    sumInntekt25Prosent = it.bidragsevnePeriode.sumInntekt25Prosent,
                )
            }
            ?: throw IllegalArgumentException("Bidragsevne grunnlag mangler for periode $bruddPeriode")

        return Evne25ProsentAvInntektBeregningGrunnlag(
            bidragsevneBeregningGrunnlag = bidragsevneBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningEvne25ProsentAvInntekt
    private fun mapDelberegningEvne25ProsentAvInntekt(
        evne25ProsentAvInntektPeriodeResultatListe: List<Evne25ProsentAvInntektPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = evne25ProsentAvInntektPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_EVNE_25PROSENTAVINNTEKT,
                innhold = POJONode(
                    DelberegningEvne25ProsentAvInntekt(
                        periode = it.periode,
                        evneJustertFor25ProsentAvInntekt = it.resultat.evneJustertFor25ProsentAvInntekt,
                        erEvneJustertNedTil25ProsentAvInntekt = it.resultat.erEvneJustertNedTil25ProsentAvInntekt,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // DELBEREGNING_ANDEL_AV_BIDRAGSEVNE

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeAndelAvBidragsevne(
        grunnlagListe: AndelAvBidragsevnePeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.evne25ProsentAvInntektDelberegningPeriodeGrunnlagListe.asSequence().map { it.evne25ProsentAvInntektPeriode.periode })
            .plus(grunnlagListe.sumBidragTilFordelingDelberegningPeriodeGrunnlagListe.asSequence().map { it.sumBidragTilFordelingPeriode.periode })
            .plus(grunnlagListe.bidragTilFordelingDelberegningPeriodeGrunnlagListe.asSequence().map { it.bidragTilFordelingPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for andel av bidragsevne som ligger innenfor bruddPeriode
    private fun lagAndelAvBidragsevneBeregningGrunnlag(
        andelAvBidragsevnePeriodeGrunnlag: AndelAvBidragsevnePeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): AndelAvBidragsevneBeregningGrunnlag {
        val evne25ProsentAvInntektBeregningGrunnlag = andelAvBidragsevnePeriodeGrunnlag.evne25ProsentAvInntektDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.evne25ProsentAvInntektPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                Evne25ProsentAvInntektDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    evneJustertFor25ProsentAvInntekt = it.evne25ProsentAvInntektPeriode.evneJustertFor25ProsentAvInntekt,
                )
            }
            ?: throw IllegalArgumentException("Evne 25 prosent av inntekt grunnlag mangler for periode $bruddPeriode")
        val sumBidragTilFordelingBeregningGrunnlag = andelAvBidragsevnePeriodeGrunnlag.sumBidragTilFordelingDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.sumBidragTilFordelingPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                SumBidragTilFordelingDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    sumBidragTilFordeling = it.sumBidragTilFordelingPeriode.sumBidragTilFordeling,
                )
            }
            ?: throw IllegalArgumentException("Sum bidrag til fordeling grunnlag mangler for periode $bruddPeriode")
        val bidragTilFordelingBeregningGrunnlag = andelAvBidragsevnePeriodeGrunnlag.bidragTilFordelingDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.bidragTilFordelingPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BidragTilFordelingDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    bidragTilFordeling = it.bidragTilFordelingPeriode.bidragTilFordeling,
                )
            }
            ?: throw IllegalArgumentException("Bidrag til fordeling grunnlag mangler for periode $bruddPeriode")

        return AndelAvBidragsevneBeregningGrunnlag(
            evne25ProsentAvInntektBeregningGrunnlag = evne25ProsentAvInntektBeregningGrunnlag,
            sumBidragTilFordelingBeregningGrunnlag = sumBidragTilFordelingBeregningGrunnlag,
            bidragTilFordelingBeregningGrunnlag = bidragTilFordelingBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningAndelAvBidragsevne
    private fun mapDelberegningAndelAvBidragsevne(
        andelAvBidragsevnePeriodeResultatListe: List<AndelAvBidragsevnePeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = andelAvBidragsevnePeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_ANDEL_AV_BIDRAGSEVNE,
                innhold = POJONode(
                    DelberegningAndelAvBidragsevne(
                        periode = it.periode,
                        andelAvSumBidragTilFordelingFaktor = it.resultat.andelAvSumBidragTilFordelingFaktor,
                        andelAvEvneBeløp = it.resultat.andelAvEvneBeløp,
                        bidragEtterFordeling = it.resultat.bidragEtterFordeling,
                        harBPFullEvne = it.resultat.harBPFullEvne,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBidragTilFordelingLøpendeBidrag(
        grunnlagListe: BidragTilFordelingLøpendeBidragPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.løpendeBidragPeriodeGrunnlagListe.asSequence().map { it.løpendeBidragPeriode.periode })
            .plus(grunnlagListe.samværsfradragDelberegningPeriodeGrunnlagListe.asSequence().map { it.samværsfradragPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for bidrag til fordeling løpende bidrag som ligger innenfor bruddPeriode
    private fun lagBidragTilFordelingLøpendeBidragBeregningGrunnlag(
        bidragTilFordelingLøpendeBidragPeriodeGrunnlag: BidragTilFordelingLøpendeBidragPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BidragTilFordelingLøpendeBidragBeregningGrunnlag {
        val løpendeBidragBeregningGrunnlag = bidragTilFordelingLøpendeBidragPeriodeGrunnlag.løpendeBidragPeriodeGrunnlagListe
            .firstOrNull { it.løpendeBidragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { LøpendeBidragTilFordelingBeregningGrunnlag(referanse = it.referanse, løpendeBidrag = it.løpendeBidragPeriode) }
            ?: throw IllegalArgumentException("Løpende bidrag grunnlag mangler for periode $bruddPeriode")
        val samværsfradragBeregningGrunnlag = bidragTilFordelingLøpendeBidragPeriodeGrunnlag.samværsfradragDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.samværsfradragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { SamværsfradragDelberegningBeregningGrunnlag(referanse = it.referanse, beløp = it.samværsfradragPeriode.beløp) }
            ?: throw IllegalArgumentException("Samværsfradrag grunnlag mangler for periode $bruddPeriode")

        return BidragTilFordelingLøpendeBidragBeregningGrunnlag(
            løpendeBidragBeregningGrunnlag = løpendeBidragBeregningGrunnlag,
            samværsfradragBeregningGrunnlag = samværsfradragBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningBidragTilFordelingLøpendeBidrag
    private fun mapDelberegningBidragTilFordelingLøpendeBidrag(
        bidragTilFordelingLøpendeBidragPeriodeResultatListe: List<BidragTilFordelingLøpendeBidragPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = bidragTilFordelingLøpendeBidragPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_BIDRAG_TIL_FORDELING_LØPENDE_BIDRAG,
                innhold = POJONode(
                    DelberegningBidragTilFordelingLøpendeBidrag(
                        periode = it.periode,
                        reduksjonUnderholdskostnad = it.resultat.reduksjonUnderholdskostnad,
                        bidragTilFordeling = it.resultat.bidragTilFordeling,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeBidragJustertForBPBarnetillegg(
        grunnlagListe: BidragJustertForBPBarnetilleggPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(grunnlagListe.andelAvBidragsevneDelberegningPeriodeGrunnlagListe.asSequence().map { it.andelAvBidragsevnePeriode.periode })
            .plus(grunnlagListe.nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe.asSequence().map { it.nettoBarnetilleggPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for bidrag justert for BP barnetillegg som ligger innenfor bruddPeriode
    private fun lagBidragJustertForBPBarnetilleggBeregningGrunnlag(
        bidragJustertForBPBarnetilleggPeriodeGrunnlag: BidragJustertForBPBarnetilleggPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): BidragJustertForBPBarnetilleggBeregningGrunnlag {
        val andelAvBidragsevneBeregningGrunnlag = bidragJustertForBPBarnetilleggPeriodeGrunnlag.andelAvBidragsevneDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.andelAvBidragsevnePeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                AndelAvBidragsevneDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    bidragEtterFordeling = it.andelAvBidragsevnePeriode.bidragEtterFordeling,
                )
            }
            ?: throw IllegalArgumentException("Andel av bidragsevne grunnlag mangler for periode $bruddPeriode")
        val barnetilleggBPBeregningGrunnlag = bidragJustertForBPBarnetilleggPeriodeGrunnlag.nettoBarnetilleggBPDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.nettoBarnetilleggPeriode.periode.inneholder(bruddPeriode) }
            ?.let {
                BarnetilleggDelberegningBeregningGrunnlag(
                    referanse = it.referanse,
                    beløp = it.nettoBarnetilleggPeriode.summertNettoBarnetillegg,
                )
            }

        return BidragJustertForBPBarnetilleggBeregningGrunnlag(
            andelAvBidragsevneBeregningGrunnlag = andelAvBidragsevneBeregningGrunnlag,
            barnetilleggBPBeregningGrunnlag = barnetilleggBPBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningBidragJustertForBPBarnetillegg
    private fun mapDelberegningBidragJustertForBPBarnetillegg(
        bidragJustertForBPBarnetilleggPeriodeResultatListe: List<BidragJustertForBPBarnetilleggPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = bidragJustertForBPBarnetilleggPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_BIDRAG_JUSTERT_FOR_BP_BARNETILLEGG,
                innhold = POJONode(
                    DelberegningBidragJustertForBPBarnetillegg(
                        periode = it.periode,
                        bidragJustertForNettoBarnetilleggBP = it.resultat.bidragJustertForNettoBarnetilleggBP,
                        erBidragJustertTilNettoBarnetilleggBP = it.resultat.erBidragJustertTilNettoBarnetilleggBP,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // DELBEREGNING_ENDELIG_BIDRAG_BEREGNET

    // Lager en liste over alle bruddperioder basert på grunnlag som skal brukes i beregningen
    private fun lagBruddPeriodeListeEndeligBidragBeregnet(
        grunnlagListe: EndeligBidragBeregnetPeriodeGrunnlag,
        beregningsperiode: ÅrMånedsperiode,
    ): List<ÅrMånedsperiode> {
        val periodeListe = sequenceOf(grunnlagListe.beregningsperiode)
            .plus(
                grunnlagListe.bidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlagListe.asSequence().map {
                    it.bidragJustertForBPBarnetilleggPeriode.periode
                },
            )
            .plus(grunnlagListe.samværsfradragDelberegningPeriodeGrunnlagListe.asSequence().map { it.samværsfradragPeriode.periode })

        return lagBruddPeriodeListe(periodeListe, beregningsperiode)
    }

    // Lager grunnlag for endelig bidrag beregnet som ligger innenfor bruddPeriode
    private fun lagEndeligBidragBeregnetBeregningGrunnlag(
        endeligBidragBeregnetPeriodeGrunnlag: EndeligBidragBeregnetPeriodeGrunnlag,
        bruddPeriode: ÅrMånedsperiode,
    ): EndeligBidragBeregnetBeregningGrunnlag {
        val bidragJustertForBPBarnetilleggBeregningGrunnlag =
            endeligBidragBeregnetPeriodeGrunnlag.bidragJustertForBPBarnetilleggDelberegningPeriodeGrunnlagListe
                .firstOrNull { it.bidragJustertForBPBarnetilleggPeriode.periode.inneholder(bruddPeriode) }
                ?.let {
                    BidragJustertForBPBarnetilleggDelberegningBeregningGrunnlag(
                        referanse = it.referanse,
                        bidragJustertForNettoBarnetilleggBP = it.bidragJustertForBPBarnetilleggPeriode.bidragJustertForNettoBarnetilleggBP,
                    )
                }
                ?: throw IllegalArgumentException("Bidrag justert for netto barnetillegg BP grunnlag mangler for periode $bruddPeriode")
        val samværsfradragBeregningGrunnlag = endeligBidragBeregnetPeriodeGrunnlag.samværsfradragDelberegningPeriodeGrunnlagListe
            .firstOrNull { it.samværsfradragPeriode.periode.inneholder(bruddPeriode) }
            ?.let { SamværsfradragDelberegningBeregningGrunnlag(referanse = it.referanse, beløp = it.samværsfradragPeriode.beløp) }
            ?: throw IllegalArgumentException("Samværsfradrag grunnlag mangler for periode $bruddPeriode")

        return EndeligBidragBeregnetBeregningGrunnlag(
            bidragJustertForBPBarnetilleggBeregningGrunnlag = bidragJustertForBPBarnetilleggBeregningGrunnlag,
            samværsfradragBeregningGrunnlag = samværsfradragBeregningGrunnlag,
        )
    }

    // Mapper ut DelberegningEndeligBidragBeregnet
    private fun mapDelberegningEndeligBidragBeregnet(
        endeligBidragBeregnetPeriodeResultatListe: List<EndeligBidragBeregnetPeriodeResultat>,
        mottattGrunnlag: BeregnGrunnlag,
    ): List<GrunnlagDto> = endeligBidragBeregnetPeriodeResultatListe
        .map {
            GrunnlagDto(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_ENDELIG_BIDRAG_BEREGNET,
                    periode = ÅrMånedsperiode(fom = it.periode.fom, til = null),
                    søknadsbarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
                    gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse ?: "bidragspliktig",
                ),
                type = Grunnlagstype.DELBEREGNING_ENDELIG_BIDRAG_BEREGNET,
                innhold = POJONode(
                    DelberegningEndeligBidragBeregnet(
                        periode = it.periode,
                        beregnetBeløp = it.resultat.beregnetBeløp,
                        resultatBeløp = it.resultat.resultatBeløp,
                    ),
                ),
                grunnlagsreferanseListe = it.resultat.grunnlagsreferanseListe.sorted(),
                gjelderReferanse = mottattGrunnlag.grunnlagListe.bidragspliktig?.referanse,
                gjelderBarnReferanse = mottattGrunnlag.søknadsbarnReferanse,
            )
        }

    // TODO Flytte til bidrag-felles
    private fun opprettDelberegningreferanse(
        type: Grunnlagstype,
        periode: ÅrMånedsperiode,
        søknadsbarnReferanse: Grunnlagsreferanse? = null,
        gjelderReferanse: Grunnlagsreferanse? = null,
    ) = "delberegning_${type}${gjelderReferanse?.let { "_$it" } ?: ""}${søknadsbarnReferanse?.let { "_$it" } ?: ""}" +
        "_${periode.fom.toCompactString()}${periode.til?.let { "_${it.toCompactString()}" } ?: ""}"
}
