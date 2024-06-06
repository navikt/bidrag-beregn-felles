package no.nav.bidrag.beregn.saertilskudd.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.bidrag.beregn.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnBPsAndelSaertilskuddResultatCore
import no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.BeregnedeGrunnlagCore
import no.nav.bidrag.beregn.felles.dto.AvvikCore
import no.nav.bidrag.beregn.felles.dto.PeriodeCore
import no.nav.bidrag.beregn.saertilskudd.dto.BeregnSaertilskuddResultatCore
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.Bidragsevne
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.Samvaersfradrag
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.Sjablontall
import no.nav.bidrag.beregn.saertilskudd.rest.consumer.TrinnvisSkattesats
import no.nav.bidrag.beregn.samvaersfradrag.dto.BeregnSamvaersfradragResultatCore
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.domain.enums.Rolle
import no.nav.bidrag.transport.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.beregning.felles.Grunnlag
import no.nav.bidrag.transport.beregning.saertilskudd.BMInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.BPInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.BPsAndelSaertilskuddResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.BarnIHusstand
import no.nav.bidrag.transport.beregning.saertilskudd.BeregnetTotalSaertilskuddResultat
import no.nav.bidrag.transport.beregning.saertilskudd.BidragsevneResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.Bostatus
import no.nav.bidrag.transport.beregning.saertilskudd.LopendeBidrag
import no.nav.bidrag.transport.beregning.saertilskudd.NettoSaertilskudd
import no.nav.bidrag.transport.beregning.saertilskudd.SBInntekt
import no.nav.bidrag.transport.beregning.saertilskudd.Saerfradrag
import no.nav.bidrag.transport.beregning.saertilskudd.SamvaersfradragResultatPeriode
import no.nav.bidrag.transport.beregning.saertilskudd.Samvaersklasse
import no.nav.bidrag.transport.beregning.saertilskudd.Skatteklasse
import no.nav.bidrag.transport.beregning.saertilskudd.SoknadsBarnInfo
import java.math.BigDecimal
import java.time.LocalDate

object TestUtil {
    const val BIDRAGSEVNE_REFERANSE = "BIDRAGSEVNE_REFERANSE"
    const val BPS_ANDEL_SAERTILSKUDD_REFERANSE = "BPS_ANDEL_SAERTILSKUDD_REFERANSE"
    const val SAMVAERSFRADRAG_REFERANSE = "SAMVAERSFRADRAG_REFERANSE"
    const val INNTEKT_REFERANSE = "INNTEKT_REFERANSE"
    const val SKATTEKLASSE_REFERANSE = "SKATTEKLASSE_REFERANSE"
    const val BOSTATUS_REFERANSE = "BOSTATUS_REFERANSE"
    const val BARN_I_HUSSTAND_REFERANSE = "BARN_I_HUSSTAND_REFERANSE"
    const val SAMVAERSKLASSE_REFERANSE = "SAMVAERSKLASSE_REFERANSE"

    fun byggTotalSaertilskuddGrunnlag(): BeregnGrunnlag {
        val grunnlagListe = ArrayList<Grunnlag>()
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_SoknadsbarnInfo_SB_1",
                GrunnlagType.SOKNADSBARN_INFO,
                tilJsonNodeInnhold(SoknadsBarnInfo(1, LocalDate.parse("2006-08-19"))),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Inntekt_AG_20200801_SB_1",
                GrunnlagType.INNTEKT,
                tilJsonNodeInnhold(
                    SBInntekt(
                        LocalDate.parse("2020-08-01"),
                        LocalDate.parse("2020-09-01"),
                        Rolle.SOKNADSBARN,
                        "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                        BigDecimal.valueOf(0),
                        1,
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Inntekt_AG_20200801_BM",
                GrunnlagType.INNTEKT,
                tilJsonNodeInnhold(
                    BMInntekt(
                        LocalDate.parse("2020-08-01"),
                        LocalDate.parse("2020-09-01"),
                        "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                        BigDecimal.valueOf(300000),
                        Rolle.BIDRAGSMOTTAKER,
                        false,
                        false,
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Inntekt_AG_20200801_BM",
                GrunnlagType.INNTEKT,
                tilJsonNodeInnhold(
                    BMInntekt(
                        LocalDate.parse("2020-08-01"),
                        LocalDate.parse("2020-09-01"),
                        "UTVIDET_BARNETRYGD",
                        BigDecimal.valueOf(12688),
                        Rolle.BIDRAGSMOTTAKER,
                        false,
                        false,
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Inntekt_AG_20200801_BP",
                GrunnlagType.INNTEKT,
                tilJsonNodeInnhold(
                    BPInntekt(
                        LocalDate.parse("2020-08-01"),
                        LocalDate.parse("2020-09-01"),
                        Rolle.BIDRAGSPLIKTIG,
                        "INNTEKTSOPPLYSNINGER_ARBEIDSGIVER",
                        BigDecimal.valueOf(500000),
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_BarnIHusstand_20200801",
                GrunnlagType.BARN_I_HUSSTAND,
                tilJsonNodeInnhold(BarnIHusstand(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), 0.0)),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Bostatus_20200801",
                GrunnlagType.BOSTATUS,
                tilJsonNodeInnhold(Bostatus(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), "ALENE")),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Saerfradrag_20200801",
                GrunnlagType.SAERFRADRAG,
                tilJsonNodeInnhold(Saerfradrag(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), "INGEN")),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Skatteklasse_20200801",
                GrunnlagType.SKATTEKLASSE,
                tilJsonNodeInnhold(Skatteklasse(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), 1)),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Netto_Saertilskudd_20200801",
                GrunnlagType.NETTO_SAERTILSKUDD,
                tilJsonNodeInnhold(
                    NettoSaertilskudd(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), BigDecimal.valueOf(7000)),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_Samvaersklasse_20200801_SB_1",
                GrunnlagType.SAMVAERSKLASSE,
                tilJsonNodeInnhold(
                    Samvaersklasse(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), 1, LocalDate.parse("2006-08-19"), "01"),
                ),
            ),
        )
        grunnlagListe.add(
            Grunnlag(
                "Mottatt_LoependeBidrag_20200801_SB_1",
                GrunnlagType.LOPENDE_BIDRAG,
                tilJsonNodeInnhold(
                    LopendeBidrag(
                        LocalDate.parse("2020-08-01"),
                        LocalDate.parse("2020-09-01"),
                        1,
                        BigDecimal.valueOf(2500),
                        BigDecimal.valueOf(2957),
                        BigDecimal.valueOf(2500),
                        BigDecimal.valueOf(457),
                    ),
                ),
            ),
        )
        return BeregnGrunnlag(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01"), grunnlagListe)
    }

    fun tilJsonNodeInnhold(`object`: Any?): JsonNode {
        jacksonObjectMapper().registerModule(JavaTimeModule())
        return jacksonObjectMapper().convertValue(`object`, JsonNode::class.java)
    }

    fun hentAlleReferanser(beregnetTotalSaertilskuddResultat: BeregnetTotalSaertilskuddResultat): List<String> {
        val alleReferanser: MutableList<String> = ArrayList()
        for ((_, _, _, grunnlagReferanseListe) in beregnetTotalSaertilskuddResultat.beregnetSaertilskuddPeriodeListe) {
            alleReferanser.addAll(grunnlagReferanseListe)
        }
        for ((_, _, innhold) in beregnetTotalSaertilskuddResultat.grunnlagListe) {
            if (innhold!!.has("grunnlagReferanseListe")) {
                val grunnlagReferanseListe = innhold["grunnlagReferanseListe"]
                if (grunnlagReferanseListe.isArray) {
                    for (grunnlagReferanse in grunnlagReferanseListe) {
                        alleReferanser.add(grunnlagReferanse.asText())
                    }
                }
            }
        }
        return alleReferanser
    }

    // Bygger opp BeregnBidragsevneResultat
    fun dummyBidragsevneResultat(): Grunnlag {
        val objectMapper = ObjectMapper()
        val bidragsevne =
            BidragsevneResultatPeriode(
                LocalDate.parse("2020-08-01"),
                LocalDate.parse("2020-09-01"),
                BigDecimal.valueOf(100),
                arrayListOf(
                    INNTEKT_REFERANSE,
                    SKATTEKLASSE_REFERANSE,
                    BOSTATUS_REFERANSE,
                    BARN_I_HUSSTAND_REFERANSE,
                    SAMVAERSKLASSE_REFERANSE,
                ),
            )
        return Grunnlag(BIDRAGSEVNE_REFERANSE, GrunnlagType.BIDRAGSEVNE, objectMapper.valueToTree(bidragsevne))
    }

    // Bygger opp BeregnBidragsevneResultatCore
    fun dummyBidragsevneResultatCore(): BeregnBidragsevneResultatCore {
        val bidragPeriodeResultatListe = ArrayList<no.nav.bidrag.beregn.bidragsevne.dto.ResultatPeriodeCore>()
        bidragPeriodeResultatListe.add(
            no.nav.bidrag.beregn.bidragsevne.dto.ResultatPeriodeCore(
                PeriodeCore(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01")),
                no.nav.bidrag.beregn.bidragsevne.dto.ResultatBeregningCore(BigDecimal.valueOf(100)),
                arrayListOf(
                    INNTEKT_REFERANSE,
                    SKATTEKLASSE_REFERANSE,
                    BOSTATUS_REFERANSE,
                    BARN_I_HUSSTAND_REFERANSE,
                    SAMVAERSKLASSE_REFERANSE,
                ),
            ),
        )
        return BeregnBidragsevneResultatCore(bidragPeriodeResultatListe, emptyList(), emptyList())
    }

    // Bygger opp BeregnBidragsevneResultatCore med avvik
    fun dummyBidragsevneResultatCoreMedAvvik(): BeregnBidragsevneResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i inntektPeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnBidragsevneResultatCore(emptyList(), emptyList(), avvikListe)
    }

    // Bygger opp BeregnBPAndelSaertilskuddResultat
    fun dummyBPsAndelSaertilskuddResultat(): Grunnlag {
        val objectMapper = ObjectMapper()
        val bpsAndelSaertilskudd =
            BPsAndelSaertilskuddResultatPeriode(
                LocalDate.parse("2020-08-01"),
                LocalDate.parse("2020-09-01"),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                false,
                arrayListOf(
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                ),
            )

        return Grunnlag(
            BPS_ANDEL_SAERTILSKUDD_REFERANSE,
            GrunnlagType.BPS_ANDEL_SAERTILSKUDD,
            objectMapper.valueToTree(bpsAndelSaertilskudd),
        )
    }

    // Bygger opp BeregnBPsAndelSaertilskuddResultatCore
    fun dummyBPsAndelSaertilskuddResultatCore(): BeregnBPsAndelSaertilskuddResultatCore {
        val bidragPeriodeResultatListe = ArrayList<no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatPeriodeCore>()
        bidragPeriodeResultatListe.add(
            no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatPeriodeCore(
                PeriodeCore(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01")),
                no.nav.bidrag.beregn.bpsandelsaertilskudd.dto.ResultatBeregningCore(
                    BigDecimal.valueOf(10),
                    BigDecimal.valueOf(100),
                    false,
                ),
                BeregnedeGrunnlagCore(emptyList(), emptyList(), emptyList()),
                arrayListOf(
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                ),
            ),
        )
        return BeregnBPsAndelSaertilskuddResultatCore(bidragPeriodeResultatListe, emptyList(), emptyList())
    }

    // Bygger opp BeregnBPsAndelSaertilskuddResultatCore med avvik
    fun dummyBPsAndelSaertilskuddResultatCoreMedAvvik(): BeregnBPsAndelSaertilskuddResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i inntektBPPeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnBPsAndelSaertilskuddResultatCore(emptyList(), emptyList(), avvikListe)
    }

    // Bygger opp BeregnSamvaersfradragResultat
    fun dummySamvaersfradragResultat(): Grunnlag {
        val objectMapper = ObjectMapper()
        val samvaersfradrag =
            SamvaersfradragResultatPeriode(
                LocalDate.parse("2020-08-01"),
                LocalDate.parse("2020-09-01"),
                BigDecimal.valueOf(100),
                1,
                arrayListOf(
                    SAMVAERSFRADRAG_REFERANSE,
                ),
            )
        return Grunnlag(SAMVAERSFRADRAG_REFERANSE, GrunnlagType.SAMVAERSFRADRAG, objectMapper.valueToTree(samvaersfradrag))
    }

    //
    // Bygger opp BeregnSamvaersfradragResultatCore
    fun dummySamvaersfradragResultatCore(): BeregnSamvaersfradragResultatCore {
        val bidragPeriodeResultatListe = ArrayList<no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatPeriodeCore>()
        bidragPeriodeResultatListe.add(
            no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatPeriodeCore(
                PeriodeCore(LocalDate.parse("2020-08-01"), LocalDate.parse("2020-09-01")),
                listOf(no.nav.bidrag.beregn.samvaersfradrag.dto.ResultatBeregningCore(1, BigDecimal.valueOf(100))),
                arrayListOf(
                    SAMVAERSFRADRAG_REFERANSE,
                ),
            ),
        )
        return BeregnSamvaersfradragResultatCore(bidragPeriodeResultatListe, emptyList(), emptyList())
    }

    //
    //  // Bygger opp BeregnSamvaersfradragResultatCore med avvik
    fun dummySamvaersfradragResultatCoreMedAvvik(): BeregnSamvaersfradragResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i samvaersklassePeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnSamvaersfradragResultatCore(emptyList(), emptyList(), avvikListe)
    }

    // Bygger opp BeregnSaertilskuddResultatCore
    fun dummySaertilskuddResultatCore(): BeregnSaertilskuddResultatCore {
        val beregnetSaertilskuddPeriodeListe = ArrayList<no.nav.bidrag.beregn.saertilskudd.dto.ResultatPeriodeCore>()
        beregnetSaertilskuddPeriodeListe.add(
            no.nav.bidrag.beregn.saertilskudd.dto.ResultatPeriodeCore(
                PeriodeCore(LocalDate.parse("2017-01-01"), LocalDate.parse("2019-01-01")),
                1,
                no.nav.bidrag.beregn.saertilskudd.dto.ResultatBeregningCore(BigDecimal.valueOf(100), "SAERTILSKUDD_INNVILGET"),
                java.util.List.of(INNTEKT_REFERANSE, BIDRAGSEVNE_REFERANSE),
            ),
        )
        return BeregnSaertilskuddResultatCore(beregnetSaertilskuddPeriodeListe, emptyList())
    }

    // Bygger opp BeregnSaertilskuddResultatCore med avvik
    fun dummySaertilskuddResultatCoreMedAvvik(): BeregnSaertilskuddResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i samvaersfradragPeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnSaertilskuddResultatCore(emptyList(), avvikListe)
    }

    // Bygger opp liste av sjabloner av typen Sjablontall
    fun dummySjablonSjablontallListe(): List<Sjablontall> {
        val sjablonSjablontallListe = ArrayList<Sjablontall>()
        sjablonSjablontallListe.add(
            Sjablontall("0001", LocalDate.parse("2004-01-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(970)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0001", LocalDate.parse("2019-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(1054)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(2504)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(2577)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(2649)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(2692)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(2775)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0003", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(2825)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0004", LocalDate.parse("2012-07-01"), LocalDate.parse("2012-12-31"), BigDecimal.valueOf(12712)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0004", LocalDate.parse("2013-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(0)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(1490)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(1530)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(1570)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(1600)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(1640)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0005", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(1670)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0015", LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"), BigDecimal.valueOf(26.07)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0015", LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(25.67)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0015", LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), BigDecimal.valueOf(25.35)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0015", LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(25.05)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0017", LocalDate.parse("2003-01-01"), LocalDate.parse("2003-12-31"), BigDecimal.valueOf(7.8)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0017", LocalDate.parse("2014-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(8.2)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(3150)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(3294)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(3365)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(3417)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(3487)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0019", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(3841)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0021", LocalDate.parse("2012-07-01"), LocalDate.parse("2014-06-30"), BigDecimal.valueOf(4923)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0021", LocalDate.parse("2014-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(5100)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0021", LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(5254)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0021", LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(5313)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0022", LocalDate.parse("2012-07-01"), LocalDate.parse("2014-06-30"), BigDecimal.valueOf(2028)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0022", LocalDate.parse("2014-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(2100)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0022", LocalDate.parse("2017-07-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(2163)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0022", LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(2187)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(72200)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(73600)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(75000)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(83000)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(85050)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0023", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(87450)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0025", LocalDate.parse("2015-01-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(29)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0025", LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(31)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(50400)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(51750)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(53150)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(54750)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(56550)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0027", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(51300)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), BigDecimal.valueOf(74250)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), BigDecimal.valueOf(76250)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), BigDecimal.valueOf(78300)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), BigDecimal.valueOf(54750)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), BigDecimal.valueOf(56550)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0028", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(51300)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0030", LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"), BigDecimal.valueOf(90850)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0030", LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(94850)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0030", LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), BigDecimal.valueOf(99540)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0030", LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31"), BigDecimal.valueOf(102820)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0030", LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(93273)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0031", LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"), BigDecimal.valueOf(112350)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0031", LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(117350)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0031", LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), BigDecimal.valueOf(99540)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0031", LocalDate.parse("2019-01-01"), LocalDate.parse("2019-12-31"), BigDecimal.valueOf(102820)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0031", LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(93273)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0039", LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"), BigDecimal.valueOf(13505)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0039", LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), BigDecimal.valueOf(13298)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0039", LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), BigDecimal.valueOf(13132)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0039", LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(12977)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0040", LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), BigDecimal.valueOf(23)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0040", LocalDate.parse("2019-01-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(22)),
        )
        sjablonSjablontallListe.add(
            Sjablontall("0041", LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), BigDecimal.valueOf(1354)),
        )
        return sjablonSjablontallListe
    }

    // Bygger opp liste av sjabloner av typen Samvaersfradrag
    fun dummySjablonSamvaersfradragListe(): List<Samvaersfradrag> {
        val sjablonSamvaersfradragListe = ArrayList<Samvaersfradrag>()
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("00", 99, LocalDate.parse("2013-07-01"), LocalDate.parse("9999-12-31"), 1, 1, BigDecimal.valueOf(0)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(204)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(208)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(212)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(215)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(219)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(256)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(296)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(301)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(306)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(312)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(318)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(353)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(358)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(378)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(385)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(390)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(400)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(457)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(422)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(436)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(443)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(450)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(460)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(528)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(422)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(436)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(443)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(450)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(460)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("01", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(528)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(674)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(689)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(701)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(712)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(727)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(849)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(979)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(998)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1012)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1034)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1052)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1167)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1184)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1252)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1275)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1293)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1323)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1513)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1397)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1444)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1468)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1490)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1525)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1749)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1397)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1444)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1468)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1490)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1525)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("02", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1749)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(1904)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(1953)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(1998)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2029)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2082)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(2272)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2330)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(2385)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(2432)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2478)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2536)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(2716)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2616)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(2739)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(2798)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2839)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2914)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3199)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2912)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(3007)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(3067)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(3115)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(3196)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3528)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2912)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(3007)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(3067)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(3115)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(3196)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("03", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3528)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(2391)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(2452)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(2509)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(2548)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(2614)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(2852)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(2925)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(2994)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3053)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3111)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(3184)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(3410)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3284)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3428)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3512)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3565)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(3658)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4016)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3656)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3774)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3851)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3910)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(4012)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4429)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3656)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3774)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3851)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3910)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(4012)),
        )
        sjablonSamvaersfradragListe.add(
            Samvaersfradrag("04", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4429)),
        )
        return sjablonSamvaersfradragListe
    }

    // Bygger opp liste av sjabloner av typen Bidragsevne
    fun dummySjablonBidragsevneListe(): List<Bidragsevne> {
        val sjablonBidragsevneListe = ArrayList<Bidragsevne>()
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2015-07-01"),
                LocalDate.parse("2016-06-30"),
                BigDecimal.valueOf(7711),
                BigDecimal.valueOf(8048),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2016-07-01"),
                LocalDate.parse("2017-06-30"),
                BigDecimal.valueOf(8907),
                BigDecimal.valueOf(8289),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2017-07-01"),
                LocalDate.parse("2018-06-30"),
                BigDecimal.valueOf(9156),
                BigDecimal.valueOf(8521),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2018-07-01"),
                LocalDate.parse("2019-06-30"),
                BigDecimal.valueOf(9303),
                BigDecimal.valueOf(8657),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2019-07-01"),
                LocalDate.parse("2020-06-30"),
                BigDecimal.valueOf(9591),
                BigDecimal.valueOf(8925),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "EN",
                LocalDate.parse("2020-07-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(9764),
                BigDecimal.valueOf(9818),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2015-07-01"),
                LocalDate.parse("2016-06-30"),
                BigDecimal.valueOf(5073),
                BigDecimal.valueOf(6814),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2016-07-01"),
                LocalDate.parse("2017-06-30"),
                BigDecimal.valueOf(5456),
                BigDecimal.valueOf(7018),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2017-07-01"),
                LocalDate.parse("2018-06-30"),
                BigDecimal.valueOf(5609),
                BigDecimal.valueOf(7215),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2018-07-01"),
                LocalDate.parse("2019-06-30"),
                BigDecimal.valueOf(5698),
                BigDecimal.valueOf(7330),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2019-07-01"),
                LocalDate.parse("2020-06-30"),
                BigDecimal.valueOf(5875),
                BigDecimal.valueOf(7557),
            ),
        )
        sjablonBidragsevneListe.add(
            Bidragsevne(
                "GS",
                LocalDate.parse("2020-07-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(5981),
                BigDecimal.valueOf(8313),
            ),
        )
        return sjablonBidragsevneListe
    }

    // Bygger opp liste av sjabloner av typen TrinnvisSkattesats
    fun dummySjablonTrinnvisSkattesatsListe(): List<TrinnvisSkattesats> {
        val sjablonTrinnvisSkattesatsListe = ArrayList<TrinnvisSkattesats>()
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2018-01-01"),
                LocalDate.parse("2018-12-31"),
                BigDecimal.valueOf(169000),
                BigDecimal.valueOf(1.4),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2018-01-01"),
                LocalDate.parse("2018-12-31"),
                BigDecimal.valueOf(237900),
                BigDecimal.valueOf(3.3),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2018-01-01"),
                LocalDate.parse("2018-12-31"),
                BigDecimal.valueOf(598050),
                BigDecimal.valueOf(12.4),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2018-01-01"),
                LocalDate.parse("2018-12-31"),
                BigDecimal.valueOf(962050),
                BigDecimal.valueOf(15.4),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2019-01-01"),
                LocalDate.parse("2019-12-31"),
                BigDecimal.valueOf(174500),
                BigDecimal.valueOf(1.9),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2019-01-01"),
                LocalDate.parse("2019-12-31"),
                BigDecimal.valueOf(245650),
                BigDecimal.valueOf(4.2),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2019-01-01"),
                LocalDate.parse("2019-12-31"),
                BigDecimal.valueOf(617500),
                BigDecimal.valueOf(13.2),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2019-01-01"),
                LocalDate.parse("2019-12-31"),
                BigDecimal.valueOf(964800),
                BigDecimal.valueOf(16.2),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(180800),
                BigDecimal.valueOf(1.9),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(254500),
                BigDecimal.valueOf(4.2),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(639750),
                BigDecimal.valueOf(13.2),
            ),
        )
        sjablonTrinnvisSkattesatsListe.add(
            TrinnvisSkattesats(
                LocalDate.parse("2020-01-01"),
                LocalDate.parse("9999-12-31"),
                BigDecimal.valueOf(999550),
                BigDecimal.valueOf(16.2),
            ),
        )
        return sjablonTrinnvisSkattesatsListe
    }
}
