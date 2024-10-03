package no.nav.bidrag.beregn.særbidrag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.bidrag.beregn.core.bo.Periode
import no.nav.bidrag.beregn.core.bo.Sjablon
import no.nav.bidrag.beregn.core.bo.SjablonInnhold
import no.nav.bidrag.beregn.core.bo.SjablonNøkkel
import no.nav.bidrag.beregn.core.bo.SjablonPeriode
import no.nav.bidrag.beregn.core.dto.AvvikCore
import no.nav.bidrag.beregn.core.dto.PeriodeCore
import no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.BeregnBidragsevneResultatCore
import no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.BeregnBPsAndelSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.BeregnSumLøpendeBidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.BeregnSærbidragResultatCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatBeregningCore
import no.nav.bidrag.beregn.særbidrag.core.særbidrag.dto.ResultatPeriodeCore
import no.nav.bidrag.commons.service.sjablon.Bidragsevne
import no.nav.bidrag.commons.service.sjablon.Samværsfradrag
import no.nav.bidrag.commons.service.sjablon.Sjablontall
import no.nav.bidrag.commons.service.sjablon.TrinnvisSkattesats
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNøkkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.beregning.felles.BeregnGrunnlag
import no.nav.bidrag.transport.behandling.beregning.særbidrag.BeregnetSærbidragResultat
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.LøpendeBidragGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth

object TestUtil {
    const val BETALT_AV_BP_REFERANSE = "BETALT_AV_BP_REFERANSE"
    const val BIDRAGSEVNE_REFERANSE = "BIDRAGSEVNE_REFERANSE"
    const val BPS_ANDEL_SÆRBIDRAG_REFERANSE = "BPS_ANDEL_SÆRBIDRAG_REFERANSE"
    const val INNTEKT_REFERANSE = "INNTEKT_REFERANSE"
    private const val SKATTEKLASSE_REFERANSE = "SKATTEKLASSE_REFERANSE"
    private const val BOSTATUS_REFERANSE = "BOSTATUS_REFERANSE"
    const val BARN_I_HUSSTANDEN_REFERANSE = "BARN_I_HUSSTANDEN_REFERANSE"
    const val VOKSNE_I_HUSSTANDEN_REFERANSE = "VOKSNE_I_HUSSTANDEN_REFERANSE"
    private const val SAMVÆRSKLASSE_REFERANSE = "SAMVÆRSKLASSE_REFERANSE"
    const val UTGIFT_REFERANSE = "UTGIFT_REFERANSE"
    const val LØPENDE_BIDRAG_GRUNNLAG = "LØPENDE_BIDRAG_GRUNNLAG"

    fun byggTotalSærbidragGrunnlag(): BeregnGrunnlag {
        val grunnlagListe = ArrayList<GrunnlagDto>()
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Person_Søknadsbarn",
                type = Grunnlagstype.PERSON_SØKNADSBARN,
                innhold = POJONode(
                    Person(
                        ident = Personident("11111111111"),
                        navn = "SBNavn",
                        fødselsdato = LocalDate.parse("2006-08-19"),
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Person_Bidragsmottaker",
                type = Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
                innhold = POJONode(
                    Person(
                        ident = Personident("22222222222"),
                        fødselsdato = LocalDate.parse("1985-07-11"),
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Person_Bidragspliktig",
                type = Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
                innhold = POJONode(
                    Person(
                        ident = Personident("33333333333"),
                        fødselsdato = LocalDate.parse("1983-01-19"),
                    ),
                ),
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Mottatt_Inntekt_AG_20200801_SB_1",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                innhold = POJONode(
                    InntektsrapporteringPeriode(
                        periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
                        manueltRegistrert = false,
                        inntektsrapportering = Inntektsrapportering.INNTEKTSOPPLYSNINGER_FRA_ARBEIDSGIVER,
                        beløp = BigDecimal.ZERO,
                        valgt = true,
                        inntekstpostListe = emptyList(),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Søknadsbarn",
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Mottatt_Inntekt_AG_20200801_BM",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                innhold = POJONode(
                    InntektsrapporteringPeriode(
                        periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
                        manueltRegistrert = false,
                        inntektsrapportering = Inntektsrapportering.INNTEKTSOPPLYSNINGER_FRA_ARBEIDSGIVER,
                        beløp = BigDecimal.valueOf(300000),
                        valgt = true,
                        inntekstpostListe = emptyList(),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragsmottaker",
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Mottatt_Inntekt_UB_20200801_BM",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                innhold = POJONode(
                    InntektsrapporteringPeriode(
                        periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
                        manueltRegistrert = false,
                        inntektsrapportering = Inntektsrapportering.UTVIDET_BARNETRYGD,
                        beløp = BigDecimal.valueOf(12688),
                        valgt = true,
                        inntekstpostListe = emptyList(),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragsmottaker",
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Mottatt_Inntekt_AG_20200801_BP",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                innhold = POJONode(
                    InntektsrapporteringPeriode(
                        periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
                        manueltRegistrert = false,
                        inntektsrapportering = Inntektsrapportering.INNTEKTSOPPLYSNINGER_FRA_ARBEIDSGIVER,
                        beløp = BigDecimal.valueOf(500000),
                        valgt = true,
                        inntekstpostListe = emptyList(),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragspliktig",
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "Delberegning_Utgift",
                type = Grunnlagstype.DELBEREGNING_UTGIFT,
                innhold = POJONode(
                    DelberegningUtgift(
                        periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
                        sumBetaltAvBp = BigDecimal.ZERO,
                        sumGodkjent = BigDecimal.valueOf(7000),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragsmottaker",
            ),
        )
        grunnlagListe.add(
            GrunnlagDto(
                referanse = "løpende_Bidrag_grunnlag",
                type = Grunnlagstype.LØPENDE_BIDRAG,
                innhold = POJONode(
                    LøpendeBidragGrunnlag(
                        løpendeBidragListe = emptyList(),
                    ),
                ),
                grunnlagsreferanseListe = emptyList(),
                gjelderReferanse = "Person_Bidragspliktig",
            ),
        )

        return BeregnGrunnlag(
            periode = ÅrMånedsperiode(fom = LocalDate.parse("2020-08-01"), til = LocalDate.parse("2020-09-01")),
            søknadsbarnReferanse = "Person_Søknadsbarn",
            grunnlagListe = grunnlagListe,
        )
    }

    fun hentAlleReferanser(totalSærbidragResultat: BeregnetSærbidragResultat) =
        totalSærbidragResultat.beregnetSærbidragPeriodeListe.flatMap { it.grunnlagsreferanseListe } +
            totalSærbidragResultat.grunnlagListe.flatMap { it.grunnlagsreferanseListe } +
            totalSærbidragResultat.grunnlagListe.flatMap { grunnlag -> grunnlag.gjelderReferanse?.let { listOf(it) } ?: emptyList() }
                .distinct()

    // Bygger opp BeregnBidragsevneResultatCore
    fun dummyBidragsevneResultatCore(): BeregnBidragsevneResultatCore {
        val bidragPeriodeResultatListe = ArrayList<no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatPeriodeCore>()
        bidragPeriodeResultatListe.add(
            no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-08-01"), datoTil = LocalDate.parse("2020-09-01")),
                resultat = no.nav.bidrag.beregn.særbidrag.core.bidragsevne.dto.ResultatBeregningCore(
                    beløp = BigDecimal.valueOf(100),
                    skatt = DelberegningBidragsevne.Skatt(
                        minstefradrag = BigDecimal.valueOf(80000),
                        skattAlminneligInntekt = BigDecimal.valueOf(80000),
                        trinnskatt = BigDecimal.valueOf(20000),
                        trygdeavgift = BigDecimal.valueOf(30000),
                        sumSkatt = BigDecimal.valueOf(130000),
                    ),
                    underholdBarnEgenHusstand = BigDecimal.valueOf(10000),
                ),
                grunnlagsreferanseListe = mutableListOf(
                    INNTEKT_REFERANSE,
                    SKATTEKLASSE_REFERANSE,
                    BOSTATUS_REFERANSE,
                    BARN_I_HUSSTANDEN_REFERANSE,
                    SAMVÆRSKLASSE_REFERANSE,
                ),
            ),
        )
        return BeregnBidragsevneResultatCore(
            resultatPeriodeListe = bidragPeriodeResultatListe,
            sjablonListe = mutableListOf(),
            avvikListe = emptyList(),
        )
    }

    // Bygger opp BeregnBidragsevneResultatCore
    fun dummySumLøpendeBidragResultatCore(): BeregnSumLøpendeBidragResultatCore {
        val resultat = no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.ResultatPeriodeCore(
            periode = PeriodeCore(datoFom = LocalDate.parse("2020-08-01"), datoTil = LocalDate.parse("2020-09-01")),
            resultat = no.nav.bidrag.beregn.særbidrag.core.sumløpendebidrag.dto.ResultatBeregningCore(BigDecimal.valueOf(100)),
            grunnlagsreferanseListe = mutableListOf(
                INNTEKT_REFERANSE,
                SKATTEKLASSE_REFERANSE,
                BOSTATUS_REFERANSE,
                BARN_I_HUSSTANDEN_REFERANSE,
                SAMVÆRSKLASSE_REFERANSE,
            ),
        )

        return BeregnSumLøpendeBidragResultatCore(
            resultatPeriode = resultat,
            sjablonListe = mutableListOf(),
        )
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
        return BeregnBidragsevneResultatCore(emptyList(), mutableListOf(), avvikListe)
    }

    // Bygger opp BeregnBPsAndelSærbidragResultatCore
    fun dummyBPsAndelSærbidragResultatCore(): BeregnBPsAndelSærbidragResultatCore {
        val bidragPeriodeResultatListe = ArrayList<no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore>()
        bidragPeriodeResultatListe.add(
            no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatPeriodeCore(
                periode = PeriodeCore(datoFom = LocalDate.parse("2020-08-01"), datoTil = LocalDate.parse("2020-09-01")),
                resultat = no.nav.bidrag.beregn.særbidrag.core.bpsandelsærbidrag.dto.ResultatBeregningCore(
                    endeligAndelFaktor = BigDecimal.valueOf(0.10),
                    andelBeløp = BigDecimal.valueOf(100),
                    beregnetAndelFaktor = BigDecimal.valueOf(0.10),
                    barnEndeligInntekt = BigDecimal.ZERO,
                    barnetErSelvforsørget = false,
                ),
                grunnlagsreferanseListe = mutableListOf(
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                    INNTEKT_REFERANSE,
                ),
            ),
        )
        return BeregnBPsAndelSærbidragResultatCore(
            resultatPeriodeListe = bidragPeriodeResultatListe,
            sjablonListe = emptyList(),
            avvikListe = emptyList(),
        )
    }

    // Bygger opp BeregnBPsAndelSærbidragResultatCore med avvik
    fun dummyBPsAndelSærbidragResultatCoreMedAvvik(): BeregnBPsAndelSærbidragResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i inntektBPPeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnBPsAndelSærbidragResultatCore(emptyList(), emptyList(), avvikListe)
    }

    // Bygger opp BeregnSærbidragResultatCore
    fun dummySærbidragResultatCore(): BeregnSærbidragResultatCore {
        val beregnetSærbidragPeriodeListe = ArrayList<ResultatPeriodeCore>()
        beregnetSærbidragPeriodeListe.add(
            ResultatPeriodeCore(
                PeriodeCore(LocalDate.parse("2017-01-01"), LocalDate.parse("2019-01-01")),
                "1",
                ResultatBeregningCore(BigDecimal.valueOf(100), Resultatkode.SÆRBIDRAG_INNVILGET, BigDecimal.valueOf(100)),
                mutableListOf(INNTEKT_REFERANSE, BIDRAGSEVNE_REFERANSE),
            ),
        )
        return BeregnSærbidragResultatCore(beregnetSærbidragPeriodeListe, emptyList())
    }

    // Bygger opp BeregnSærbidragResultatCore med avvik
    fun dummySærbidragResultatCoreMedAvvik(): BeregnSærbidragResultatCore {
        val avvikListe = ArrayList<AvvikCore>()
        avvikListe.add(AvvikCore("beregnDatoFra kan ikke være null", "NULL_VERDI_I_DATO"))
        avvikListe.add(
            AvvikCore(
                "periodeDatoTil må være etter periodeDatoFra i samvÆrsfradragPeriodeListe: datoFra=2018-04-01, datoTil=2018-03-01",
                "DATO_FRA_ETTER_DATO_TIL",
            ),
        )
        return BeregnSærbidragResultatCore(emptyList(), avvikListe)
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

    // Bygger opp liste av sjabloner av typen Samværsfradrag
    fun dummySjablonSamværsfradragListe(): List<Samværsfradrag> {
        val sjablonSamværsfradragListe = ArrayList<Samværsfradrag>()
        sjablonSamværsfradragListe.add(
            Samværsfradrag("00", 99, LocalDate.parse("2013-07-01"), LocalDate.parse("9999-12-31"), 1, 1, BigDecimal.valueOf(0)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(204)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(208)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(212)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(215)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(219)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(256)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(296)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(301)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(306)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(312)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(318)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(353)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(358)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(378)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(385)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(390)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(400)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(457)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(422)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(436)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(443)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(450)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(460)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(528)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 3, 3, BigDecimal.valueOf(422)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 3, 3, BigDecimal.valueOf(436)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 3, 3, BigDecimal.valueOf(443)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 3, 3, BigDecimal.valueOf(450)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 3, 3, BigDecimal.valueOf(460)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("01", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 3, 3, BigDecimal.valueOf(528)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(674)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(689)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(701)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(712)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(727)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(849)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(979)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(998)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1012)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1034)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1052)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1167)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1184)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1252)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1275)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1293)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1323)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1513)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1397)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1444)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1468)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1490)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1525)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1749)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 8, BigDecimal.valueOf(1397)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 8, BigDecimal.valueOf(1444)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 8, BigDecimal.valueOf(1468)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 8, BigDecimal.valueOf(1490)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 8, BigDecimal.valueOf(1525)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("02", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 8, BigDecimal.valueOf(1749)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(1904)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(1953)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(1998)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2029)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2082)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(2272)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2330)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(2385)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(2432)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2478)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2536)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(2716)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2616)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(2739)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(2798)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(2839)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(2914)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3199)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2912)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(3007)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(3067)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(3115)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(3196)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3528)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 13, BigDecimal.valueOf(2912)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 13, BigDecimal.valueOf(3007)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 13, BigDecimal.valueOf(3067)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 13, BigDecimal.valueOf(3115)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 13, BigDecimal.valueOf(3196)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("03", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 13, BigDecimal.valueOf(3528)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(2391)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(2452)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(2509)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(2548)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(2614)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 5, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(2852)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(2925)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(2994)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3053)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3111)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(3184)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 10, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(3410)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3284)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3428)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3512)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3565)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(3658)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 14, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4016)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3656)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3774)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3851)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3910)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(4012)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 18, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4429)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2015-07-01"), LocalDate.parse("2016-06-30"), 0, 15, BigDecimal.valueOf(3656)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2016-07-01"), LocalDate.parse("2017-06-30"), 0, 15, BigDecimal.valueOf(3774)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2017-07-01"), LocalDate.parse("2018-06-30"), 0, 15, BigDecimal.valueOf(3851)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2018-07-01"), LocalDate.parse("2019-06-30"), 0, 15, BigDecimal.valueOf(3910)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2019-07-01"), LocalDate.parse("2020-06-30"), 0, 15, BigDecimal.valueOf(4012)),
        )
        sjablonSamværsfradragListe.add(
            Samværsfradrag("04", 99, LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31"), 0, 15, BigDecimal.valueOf(4429)),
        )
        return sjablonSamværsfradragListe
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

    fun byggDummySærbidragGrunnlag(): BeregnGrunnlag = byggDummySærbidragGrunnlag("")

    fun byggSærbidragGrunnlagUtenBeregningsperiodeTil(): BeregnGrunnlag = byggDummySærbidragGrunnlag("beregningsperiodeTil")

    fun byggSærbidragGrunnlagUtenGrunnlagListe(): BeregnGrunnlag = byggDummySærbidragGrunnlag("grunnlagListe")

    fun byggSærbidragGrunnlagUtenReferanse(): BeregnGrunnlag = byggDummySærbidragGrunnlag("referanse")

    fun byggSærbidragGrunnlagUtenInnhold(): BeregnGrunnlag = byggDummySærbidragGrunnlag("innhold")

    // Bygger opp BeregnGrunnlag
    private fun byggDummySærbidragGrunnlag(nullVerdi: String): BeregnGrunnlag {
        val mapper = ObjectMapper()
        val beregningsperiodeFom = YearMonth.parse("2017-01")
        val beregningsperiodeTil = if (nullVerdi == "beregningsperiodeTil") null else YearMonth.parse("2020-01")
        val referanse = if (nullVerdi == "referanse") "" else "Mottatt_BM_Inntekt_AG_20201201"
        val type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE
        val periodeFom = "2017-01"
        val periodeTil = "2020-01"
        val innhold =
            if (nullVerdi == "innhold") {
                POJONode(null)
            } else {
                mapper.valueToTree<JsonNode>(
                    mapOf(
                        "periode" to
                            mapOf(
                                "fom" to periodeFom,
                                "til" to periodeTil,
                            ),
                        "inntektsrapportering" to Inntektsrapportering.AINNTEKT.name,
                        "gelderBarn" to null,
                        "beløp" to 290000,
                        "manueltRegistrert" to false,
                        "valgt" to true,
                    ),
                )
            }
        val grunnlagListe =
            if (nullVerdi == "grunnlagListe") {
                emptyList()
            } else {
                listOf(
                    GrunnlagDto(
                        referanse = referanse,
                        type = type,
                        grunnlagsreferanseListe = emptyList(),
                        innhold = innhold,
                    ),
                )
            }

        return BeregnGrunnlag(
            periode = ÅrMånedsperiode(fom = beregningsperiodeFom, til = beregningsperiodeTil),
            søknadsbarnReferanse = "1",
            grunnlagListe = grunnlagListe,
        )
    }

    fun byggSjablonPeriodeListe(): MutableList<SjablonPeriode> {
        val sjablonPeriodeListe = mutableListOf<SjablonPeriode>()

        // Barnetilsyn
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BARNETILSYN.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"),
                        SjablonNøkkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "DO"),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELØP.navn, BigDecimal.valueOf(358))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BARNETILSYN.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"),
                        SjablonNøkkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "DU"),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELØP.navn, BigDecimal.valueOf(257))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BARNETILSYN.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"),
                        SjablonNøkkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "HO"),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELØP.navn, BigDecimal.valueOf(589))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BARNETILSYN.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.STØNAD_TYPE.navn, "64"),
                        SjablonNøkkel(SjablonNøkkelNavn.TILSYN_TYPE.navn, "HU"),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELØP.navn, BigDecimal.valueOf(643))),
                ),
            ),
        )

        // Bidragsevne
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BIDRAGSEVNE.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.BOSTATUS.navn, "EN")),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELØP.navn, BigDecimal.valueOf(9764)),
                        SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELØP.navn, BigDecimal.valueOf(9818)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.BIDRAGSEVNE.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.BOSTATUS.navn, "GS")),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELØP.navn, BigDecimal.valueOf(5981)),
                        SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELØP.navn, BigDecimal.valueOf(8313)),
                    ),
                ),
            ),
        )

        // Forbruksutgifter
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(
                        SjablonNøkkel(
                            SjablonNøkkelNavn.ALDER_TOM.navn,
                            "18",
                        ),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELØP.navn, BigDecimal.valueOf(7953))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(
                        SjablonNøkkel(
                            SjablonNøkkelNavn.ALDER_TOM.navn,
                            "5",
                        ),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELØP.navn, BigDecimal.valueOf(4228))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(
                        SjablonNøkkel(
                            SjablonNøkkelNavn.ALDER_TOM.navn,
                            "99",
                        ),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELØP.navn, BigDecimal.valueOf(7953))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(
                        SjablonNøkkel(
                            SjablonNøkkelNavn.ALDER_TOM.navn,
                            "10",
                        ),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELØP.navn, BigDecimal.valueOf(5710))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(
                        SjablonNøkkel(
                            SjablonNøkkelNavn.ALDER_TOM.navn,
                            "14",
                        ),
                    ),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELØP.navn, BigDecimal.valueOf(6913))),
                ),
            ),
        )

        // Maks fradrag
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "1")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(2083.33))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "2")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(3333))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "3")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(4583))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "4")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(5833))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "5")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(7083))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "6")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(8333))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "7")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(9583))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "8")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(10833))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "99")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELØP.navn, BigDecimal.valueOf(12083))),
                ),
            ),
        )

        // Maks tilsyn
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "1")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELØP.navn, BigDecimal.valueOf(6333))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "2")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELØP.navn, BigDecimal.valueOf(8264))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNøkkel(SjablonNøkkelNavn.ANTALL_BARN_TOM.navn, "99")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELØP.navn, BigDecimal.valueOf(9364))),
                ),
            ),
        )

        // Samværsfradrag
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "00"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "99"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(1)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(1)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.ZERO),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "01"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "5"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(256)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "01"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "10"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(353)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "01"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "14"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(457)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "01"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "18"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(528)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "01"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "99"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(528)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "02"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "5"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(849)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "02"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "10"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(1167)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "02"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "14"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(1513)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "02"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "18"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(1749)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "02"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "99"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(1749)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "5"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(2272)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "10"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(2716)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "14"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(3199)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "18"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(3528)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "03"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "99"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(3528)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "04"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "5"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(2852)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "04"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "10"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(3410)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "04"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "14"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(4016)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "04"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "18"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(4429)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.SAMVÆRSFRADRAG.navn,
                    listOf(
                        SjablonNøkkel(SjablonNøkkelNavn.SAMVÆRSKLASSE.navn, "04"),
                        SjablonNøkkel(SjablonNøkkelNavn.ALDER_TOM.navn, "99"),
                    ),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                        SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                        SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELØP.navn, BigDecimal.valueOf(4429)),
                    ),
                ),
            ),
        )

        // Sjablontall
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.ORDINÆR_BARNETRYGD_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(1054))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.ORDINÆR_SMÅBARNSTILLEGG_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.ZERO)),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(2825))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(31))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(87450))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(51300))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(51300))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(22))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(8.2))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SÆRFRADRAG_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(12977))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.ZERO)),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(3841))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.SKATT_ALMINNELIG_INNTEKT_PROSENT.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(25.05))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.FORSKUDDSSATS_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(1670))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.BARNETILLEGG_FORSVARET_FØRSTE_BARN_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(5667))),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.BARNETILLEGG_FORSVARET_ØVRIGE_BARN_BELØP.navn,
                    emptyList(),
                    listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(2334))),
                ),
            ),
        )

        // Trinnvis skattesats
        // Oppdatert med sjablonverdier gyldig fra 01.07.2020
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, BigDecimal.valueOf(999550)),
                        SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(16.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, BigDecimal.valueOf(254500)),
                        SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(4.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, BigDecimal.valueOf(639750)),
                        SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(13.2)),
                    ),
                ),
            ),
        )
        sjablonPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2017-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonNavn.TRINNVIS_SKATTESATS.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELØP.navn, BigDecimal.valueOf(180800)),
                        SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(1.9)),
                    ),
                ),
            ),
        )
        return sjablonPeriodeListe
    }

    fun <T> printJson(json: T) {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        println(objectMapper.writeValueAsString(json))
    }
}
