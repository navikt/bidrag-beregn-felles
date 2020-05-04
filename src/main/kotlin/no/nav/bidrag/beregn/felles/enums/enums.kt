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
  DATO_FRA_ETTER_DATO_TIL
}

enum class SaerfradragKode {
  INGEN,
  HALVT,
  HELT
}
