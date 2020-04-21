package no.nav.bidrag.beregn.felles.enums

enum class BostatusKode {
  ALENE,
  MED_FORELDRE,
  MED_ANDRE_ENN_FORELDRE,
  ENSLIG_ASYLANT
}

enum class SivilstandKode {
  GIFT,
  ENSLIG
}

enum class AvvikType {
  PERIODER_OVERLAPPER,
  PERIODER_HAR_OPPHOLD,
  NULL_VERDI_I_DATO,
  DATO_FRA_ETTER_DATO_TIL
}