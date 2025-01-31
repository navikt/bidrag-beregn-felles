package no.nav.bidrag.beregn.særbidrag.service.mapper

import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.core.service.mapper.CoreMapper
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsberegnedetotalbidrag.dto.BeregnBPsBeregnedeTotalbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsAndelSærbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BPsBeregnedeTotalbidragPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragGrunnlagCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BetaltAvBpPeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BidragsevnePeriodeCore
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerOgKonverterBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.opprettDelberegningreferanse

internal object SærbidragCoreMapper : CoreMapper() {
    fun mapSærbidragGrunnlagTilCore(
        beregnGrunnlag: BeregnGrunnlag,
        beregnBidragsevneResultatCore: BeregnBidragsevneResultatCore,
        beregnBPsBeregnedeTotalbidragResultatCore: BeregnBPsBeregnedeTotalbidragResultatCore,
        beregnBPsAndelSærbidragResultatCore: BeregnBPsAndelSærbidragResultatCore,
    ): BeregnSærbidragGrunnlagCore {
        // Løper gjennom output fra beregning av bidragsevne og bygger opp ny input-liste til core
        val bidragsevnePeriodeCoreListe =
            beregnBidragsevneResultatCore.resultatPeriodeListe
                .map { (periode, resultatBeregning): no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatPeriodeCore ->
                    BidragsevnePeriodeCore(
                        referanse = opprettDelberegningreferanse(
                            type = Grunnlagstype.DELBEREGNING_BIDRAGSEVNE,
                            periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
                        ),
                        periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                        beløp = resultatBeregning.beløp,
                        skatt = resultatBeregning.skatt,
                        underholdBarnEgenHusstand = resultatBeregning.underholdBarnEgenHusstand,
                    )
                }

        // Leser output fra beregning av bBPs beregnede totalbidrag og bygger opp input til core

        val resultatPeriode = beregnBPsBeregnedeTotalbidragResultatCore.resultatPeriode

        val bPsBeregnedeTotalbidragPeriodeCore =
            BPsBeregnedeTotalbidragPeriodeCore(
                referanse = opprettDelberegningreferanse(
                    type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_BEREGNEDE_TOTALBIDRAG,
                    periode = ÅrMånedsperiode(
                        fom = resultatPeriode.periode.datoFom,
                        til = resultatPeriode.periode.datoTil,
                    ),
                    søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
                ),
                periode = PeriodeCore(
                    datoFom = resultatPeriode.periode.datoFom,
                    datoTil = resultatPeriode.periode.datoTil,
                ),
                bPsBeregnedeTotalbidrag = resultatPeriode.resultat.bPsBeregnedeTotalbidrag,
                beregnetBidragPerBarnListe = resultatPeriode.resultat.beregnetBidragPerBarn,
            )

        // Løper gjennom output fra beregning av BPs andel særbidrag og bygger opp ny input-liste til core
        val bpAndelSærbidragPeriodeCoreListe =
            beregnBPsAndelSærbidragResultatCore.resultatPeriodeListe
                .map { (periode, resultatBeregning): no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore ->
                    BPsAndelSærbidragPeriodeCore(
                        referanse = opprettDelberegningreferanse(
                            type = Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL,
                            periode = ÅrMånedsperiode(fom = periode.datoFom, til = periode.datoTil),
                            søknadsbarnReferanse = beregnGrunnlag.søknadsbarnReferanse,
                        ),
                        periode = PeriodeCore(datoFom = periode.datoFom, datoTil = periode.datoTil),
                        endeligAndelFaktor = resultatBeregning.endeligAndelFaktor,
                        andelBeløp = resultatBeregning.andelBeløp,
                        beregnetAndelFaktor = resultatBeregning.beregnetAndelFaktor,
                        barnEndeligInntekt = resultatBeregning.barnEndeligInntekt,
                        barnetErSelvforsørget = resultatBeregning.barnetErSelvforsørget,
                    )
                }

        return BeregnSærbidragGrunnlagCore(
            beregnDatoFra = beregnGrunnlag.periode.fom.atDay(1),
            beregnDatoTil = beregnGrunnlag.periode.til!!.atDay(1),
            søknadsbarnPersonId = mapSøknadsbarn(beregnGrunnlag)!!.verdi,
            betaltAvBpPeriodeListe = mapUtgift(beregnGrunnlag),
            bidragsevnePeriodeListe = bidragsevnePeriodeCoreListe,
            bPsBeregnedeTotalbidragPeriodeCore = bPsBeregnedeTotalbidragPeriodeCore,
            bPsAndelSærbidragPeriodeListe = bpAndelSærbidragPeriodeCoreListe,
        )
    }

    private fun mapSøknadsbarn(beregnGrunnlag: BeregnGrunnlag): Personident? {
        try {
            val søknadsbarnGrunnlag =
                beregnGrunnlag.grunnlagListe.filtrerOgKonverterBasertPåEgenReferanse<Person>(
                    referanse = beregnGrunnlag.søknadsbarnReferanse,
                )

            return søknadsbarnGrunnlag[0].innhold.ident
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særbidrag. Innhold i Grunnlagstype.PERSON_SØKNADSBARN er ikke gyldig: " + e.message,
            )
        }
    }

    private fun mapUtgift(beregnSærbidragGrunnlag: BeregnGrunnlag): List<BetaltAvBpPeriodeCore> {
        try {
            return beregnSærbidragGrunnlag.grunnlagListe
                .filtrerOgKonverterBasertPåEgenReferanse<DelberegningUtgift>(Grunnlagstype.DELBEREGNING_UTGIFT)
                .map {
                    BetaltAvBpPeriodeCore(
                        referanse = it.referanse,
                        periode =
                        PeriodeCore(
                            datoFom = it.innhold.periode.toDatoperiode().fom,
                            datoTil = it.innhold.periode.toDatoperiode().til,
                        ),
                        beløp = it.innhold.sumBetaltAvBp,
                    )
                }
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Ugyldig input ved beregning av særlige utgifter. Innhold i Grunnlagstype.DELBEREGNING_UTGIFT er ikke gyldig: " + e.message,
            )
        }
    }
}
