package no.nav.bidrag.beregn.felles.enums

enum class BostatusKode {
  //Forskudd
  ALENE,
  MED_FORELDRE,
  MED_ANDRE_ENN_FORELDRE,
  ENSLIG_ASYLANT,
  //Bidragsevne
  MED_ANDRE
}

enum class SivilstandKode {
  GIFT,
  ENSLIG
}

enum class InntektType {
  LØNNSINNTEKT,
  KAPITALINNTEKT,
  BARNETRYGD,
  UTVIDET_BARNETRYGD,
  KONTANTSTØTTE,
  PENSJON,
  SYKEPENGER
}

enum class AvvikType {
  PERIODER_OVERLAPPER,
  PERIODER_HAR_OPPHOLD,
  NULL_VERDI_I_DATO,
  DATO_FRA_ETTER_DATO_TIL,
  PERIODE_MANGLER_DATA
}

enum class SaerfradragKode {
  INGEN,
  HALVT,
  HELT
}

enum class ResultatKode {
  KOSTNADSBEREGNET_BIDRAG,                   // Kostnadsberegnet bidrag
  BARNET_ER_SELVFORSORGET,                   // Barnet er selvforsørget
  BIDRAG_REDUSERT_AV_EVNE,                   // Bidrag redusert pga ikke full evne
  BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT, // Maks 25% av inntekt
  BIDRAG_SATT_TIL_BARNETILLEGG_BP,           // BarnetilleggBP er høyere enn beregnet bidrag
  BIDRAG_SATT_TIL_BARNETILLEGG_BM,           // BarnetilleggBM er høyere enn beregnet bidrag
  BIDRAG_SATT_TIL_BARNETILLEGG_FORSVARET,    // Barnebidrag settes likt barnetillegg fra forsvaret
  DELT_BOSTED,                               // Barnet bor like mye hos begge foreldre
  BEGRENSET_REVURDERING                      // Beregnet bidrag er større enn forskuddsats, settes lik forskuddssats
}