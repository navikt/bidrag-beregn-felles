package no.nav.bidrag.beregn.felles.enums

import java.time.LocalDate

enum class InntektType(val beskrivelse: String, val belopstype: String, val gruppe: String, val maaVelges: Boolean, val forskudd: Boolean,
    val bidrag: Boolean, val saertilskudd: Boolean, val bidragspliktig: Boolean, val bidragsmottaker: Boolean, val soknadsbarn: Boolean,
    val gyldigFom: LocalDate, val gyldigTom: LocalDate) {

  INNTEKTSOPPL_ARBEIDSGIVER("Inntektsopplysninger fra arbeidsgiver", "AG", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  ALOYSE("Aløyse", "AL", "A",
      false, false, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  ATTFORING_AAP("Attføring/AAP", "AT", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  BARNETRYGD_MAN_VURDERING("Barnetrygd manuell vurdering", "BAMV", "",
      true, true, true, true, true, true, false, LocalDate.MIN, LocalDate.MAX),
  BARNS_SYKDOM("Barns sykdom", "BS", "",
      false, true, true, true, true, true, false, LocalDate.MIN, LocalDate.parse("2017-12-31")),
  OVERGANGSSTONAD("Overgangsstønad", "EFOS", "",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  EKSTRA_SMAABARNSTILLEGG("Ekstra småbarnstillegg", "ESBT", "BMA",
      true, true, true, true, true, true, false, LocalDate.MIN, LocalDate.MAX),
  MANGLENDE_BRUK_EVNE_SKJONN("Manglende bruk av evne (skjønn)", "EVNE", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  FOEDSEL_ADOPSJON("Fødsels- og adopsjonspenger", "FA", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  FORDEL_SAERFRADRAG_ENSLIG_FORSOERGER("Fordel særfradrag enslig forsørger", "FSEF", "C",
      false, false, true, true, true, true, false, LocalDate.parse("2013-01-01"), LocalDate.MAX),
  LIGNINGSOPPL_MANGLER("Ingen ligningsopplysninger finnes", "ILOF", "",
      false, false, true, false, true, true, false, LocalDate.MIN, LocalDate.parse("2006-12-31")),
  NETTO_KAPITALINNTEKT("Netto kapitalinntekt", "KAP", "B",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.parse("2016-12-31")),
  KAPITALINNTEKT_SKE("Kapitalinntekt fra Skatteetaten", "KAPS", "B",
      false, true, true, true, true, true, true, LocalDate.parse("2015-01-01"), LocalDate.MAX),
  KAPITALINNTEKT_EGNE_OPPL("Kapitalinntekt egne opplysninger", "KIEO", "B",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  KONTANTSTOTTE("Kontantstøtte", "KONT", "BMB",
      true, true, true, true, true, true, false, LocalDate.MIN, LocalDate.MAX),
  LIGNING_KORRIGERT_BARNETILLEGG("Ligning korrigert for barnetillegg", "LIGB", "A",
      false, true, true, false, false, true, false, LocalDate.parse("2013-01-01"), LocalDate.parse("2018-12-31")),
  LIGNING_SKE("Ligning fra Skatteetaten", "LIGN", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.parse("2017-12-31")),
  SKATTEGRUNNLAG_SKE("Skattegrunnlag fra Skatteetaten", "LIGS", "A",
      false, true, true, true, true, true, true, LocalDate.parse("2015-01-01"), LocalDate.MAX),
  LONN_TREKK("Lønns- og trekkoppgave", "LTA", "A",
      false, true, true, true, true, true, true, LocalDate.parse("2015-01-01"), LocalDate.MAX),
  AINNTEKT_BEREGNET("Ainntekt beregnet", "LTAB", "",
      false, true, true, false, false, true, true, LocalDate.parse("2019-01-01"), LocalDate.MAX),
  LONN_SKE("Lønnsoppgave fra Skatteetaten", "LTR", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.parse("2015-12-31")),
  LONN_SKE_KORR_BARNETILLEGG("Lønnsoppgave fra Skatteetaten korrigert for barnetillegg", "LTRB", "A",
      false, true, true, false, false, true, false, LocalDate.parse("2014-01-01"), LocalDate.MAX),
  DOK_MANGLER_SKJONN("Dokumentasjon mangler (skjønn)", "MDOK", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  PENSJON("Pensjon", "PE", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  PENSJON_KORR_BARNETILLEGG("Pensjon korrigert for barnetillegg", "PEB", "A",
      false, true, false, false, false, true, false, LocalDate.parse("2015-01-01"), LocalDate.parse("2015-12-31")),
  PERSONINNTEKT_EGNE_OPPL("Personinntekt egne opplysninger", "PIEO", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  REHABILITERINGSPENGER("Rehabiliteringspenger", "RP", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.parse("2012-12-31")),
  SAKSBEHANDLER_BEREGNET_INNTEKT("Saksbehandler beregnet inntekt", "SAK", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  SYKEPENGER("Sykepenger", "SP", "A",
      false, true, true, true, true, true, true, LocalDate.MIN, LocalDate.MAX),
  UTVIDET_BARNETRYGD("Utvidet barnetrygd", "UBAT", "BMC",
      true, true, true, true, true, true, false, LocalDate.MIN, LocalDate.MAX)
}
