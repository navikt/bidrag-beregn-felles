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

enum class AvvikType {
  PERIODER_OVERLAPPER,
  PERIODER_HAR_OPPHOLD,
  NULL_VERDI_I_DATO,
  DATO_FRA_ETTER_DATO_TIL,
  PERIODE_MANGLER_DATA,
  UGYLDIG_INNTEKT_TYPE,
  UGYLDIG_INNTEKT_PERIODE,
  OVERLAPPENDE_INNTEKT
}

enum class SaerfradragKode {
  INGEN,
  HALVT,
  HELT
}

enum class SoknadType {
  FORSKUDD,
  BIDRAG,
  SAERTILSKUDD
}

enum class Rolle {
  BIDRAGSPLIKTIG,
  BIDRAGSMOTTAKER,
  SOKNADSBARN
}

enum class ResultatKode {
  KOSTNADSBEREGNET_BIDRAG,                                   // Kostnadsberegnet bidrag
  BARNET_ER_SELVFORSORGET,                                   // Barnet er selvforsørget
  BIDRAG_REDUSERT_AV_EVNE,                                   // Bidrag redusert pga ikke full evne
  INGEN_EVNE,                                                // BP har 0.- i bidragsevne, bidrag satt til 0.-
  BIDRAG_REDUSERT_TIL_25_PROSENT_AV_INNTEKT,                 // Maks 25% av inntekt
  BIDRAG_SATT_TIL_BARNETILLEGG_BP,                           // BarnetilleggBP er høyere enn beregnet bidrag
  BIDRAG_SATT_TIL_UNDERHOLDSKOSTNAD_MINUS_BARNETILLEGG_BM,   // Beregnet bidrag er lavere enn underholdskostnad minus barnetilleggBM
  BIDRAG_SATT_TIL_BARNETILLEGG_FORSVARET,                    // Barnebidrag settes likt barnetillegg fra forsvaret
  DELT_BOSTED,                                               // Barnet bor like mye hos begge foreldre
  BEGRENSET_REVURDERING,                                     // Beregnet bidrag er større enn forskuddsats, settes lik forskuddssats
  BARNEBIDRAG_IKKE_BEREGNET_DELT_BOSTED,                     // Barnet har delt bosted og BPs andel av U er under 50%, bidrag skal ikke beregnes
  SAERTILSKUDD_INNVILGET,                                    // Resultat av beregning av særtilskudd
  SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE,                        // Resultat av beregning av særtilskudd
  BEGRENSET_EVNE_FLERE_SAKER_UTFOER_FORHOLDSMESSIG_FORDELING // Resultat av beregning av barnebidrag, angir at det må gjøres en forholdsmessig fordeling
}