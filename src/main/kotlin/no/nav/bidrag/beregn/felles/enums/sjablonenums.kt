package no.nav.bidrag.beregn.felles.enums

enum class SjablonNavn (val navn: String) {
  BARNETILSYN("Barnetilsyn"),
  BIDRAGSEVNE("Bidragsevne"),
  FORBRUKSUTGIFTER("Forbruksutgifter"),
  MAX_FRADRAG("MaxFradrag"),
  MAX_TILSYN("MaxTilsyn"),
  SAMVAERSFRADRAG("Samværsfradrag"),
  TRINNVIS_SKATTESATS("TrinnvisSkattesats"),
}

enum class SjablonTallNavn (val navn: String) {
  ORDINAER_BARNETRYGD_BELOEP("OrdinærBarnetrygdBeløp"),
  ORDINAER_SMAABARNSTILLEGG_BELOP("OrdinærSmåbarnstilleggBeløp"),
  BOUTGIFTER_BIDRAGSBARN_BELOEP("BoutgifterBidragsbarnBeløp"),
  FORDEL_SKATTEKLASSE2_BELOEP("FordelSkatteklasse2Beløp"),
  FORSKUDDSSATS_BELOEP("ForskuddssatsBeløp"),
  INNSLAG_KAPITALINNTEKT_BELOEP("InnslagKapitalInntektBeløp"),
  INNTEKTSINTERVALL_TILLEGGSBIDRAG_BELOEP("InntektsintervallTilleggsbidragBeløp"),
  MAKS_INNTEKT_BP_PROSENT("MaksInntektBPProsent"),
  HOEY_INNTEKT_BP_MULTIPLIKATOR("HøyInntektBPMultiplikator"),
  INNTEKT_BB_MULTIPLIKATOR("InntektBBMultiplikator"),
  MAKS_BIDRAG_MULTIPLIKATOR("MaksBidragMultiplikator"),
  MAKS_INNTEKT_BB_MULTIPLIKATOR("MaksInntektBBMultiplikator"),
  MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR("MaksInntektForskuddMottakerMultiplikator"),
  NEDRE_INNTEKTSGRENSE_GEBYR_BELOP("NedreInntektsgrenseGebyrBeløp"),
  SKATT_ALMINNELIG_INNTEKT_PROSENT("SkattAlminneligInntektProsent"),
  TILLEGGSBIDRAG_PROSENT("TilleggsbidragProsent"),
  TRYGDEAVGIFT_PROSENT("TrygdeavgiftProsent"),
  BARNETILLEGG_SKATT_PROSENT("BarneTilleggSkattProsent"),
  UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOEP("UnderholdEgneBarnIHusstandBeløp"),
  ENDRING_BIDRAG_GRENSE_PROSENT("EndringBidragGrenseProsent"),
  BARNETILLEGG_FORSVARET_FOERSTE_BARN_BELOEP("BarnetilleggForsvaretFørsteBarnBeløp"),
  BARNETILLEGG_FORSVARET_OEVRIGE_BARN_BELOEP("BarnetilleggForsvaretØvrigeBarnBeløp"),
  MINSTEFRADRAG_INNTEKT_BELOEP("MinstefradragInntektBeløp"),
  GJENNOMSNITT_VIRKEDAGER_PR_MAANED_ANTALL("GjennomsnittVirkedagerPrMånedAntall"),
  MINSTEFRADRAG_INNTEKT_PROSENT("MinstefradragInntektProsent"),
  DAGLIG_SATS_BARNETILLEGG_BELOEP("DagligSatsBarnetilleggBeløp"),
  PERSONFRADRAG_KLASSE1_BELOEP("PersonfradragKlasse1Beløp"),
  PERSONFRADRAG_KLASSE2_BELOEP("PersonfradragKlasse2Beløp"),
  KONTANTSTOETTE_BELOEP("KontantstøtteBeløp"),
  OEVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOEP("ØvreInntektsgrenseIkkeISkatteposisjonBeløp"),
  NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOEP("NedreInntektsgrenseFullSkatteposisjonBeløp"),
  EKSTRA_SMAABARNSTILLEGG_BELOEP("EkstraSmåbarnstilleggBeløp"),
  OEVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELOEP("ØvreInntektsgrenseFulltForskuddBeløp"),
  OEVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELOEP("ØvreInntektsgrense75ProsentForskuddEnBeløp"),
  OEVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELOEP("ØvreInntektsgrense75ProsentForskuddGSBeløp"),
  INNTEKTSINTERVALL_FORSKUDD_BELOEP("InntektsintervallForskuddBeløp"),
  OEVRE_GRENSE_SAERTILSKUDD_BELOEP("ØvreGrenseSærtilskuddBeløp"),
  FORSKUDDSSATS_75PROSENT_BELOEP("Forskuddssats75ProsentBeløp"),
  FORDEL_SAERFRADRAG_BELOEP("FordelSærfradragBeløp"),
  SKATTESATS_ALMINNELIG_INNTEKT_PROSENT("SkattesatsAlminneligInntektProsent"),
  FASTSETTELSESGEBYR_BELOEP("FastsettelsesgebyrBeløp")
}


enum class SjablonNoekkelNavn (val navn: String) {
  STOENAD_TYPE("StønadType"),
  TILSYN_TYPE("TilsynType"),
  BOSTATUS("Bostatus"),
  ALDER_TOM("AlderTOM"),
  ANTALL_BARN_TOM("AntallBarnTOM"),
  SAMVAERSKLASSE("Samværsklasse")
}

enum class SjablonInnholdNavn (val navn: String) {
  BARNETILSYN_BELOEP("BarnetilsynBeløp"),
  BOUTGIFT_BELOEP("BoutgiftBeløp"),
  UNDERHOLD_BELOEP("UnderholdBeløp"),
  FORBRUK_TOTAL_BELOEP("ForbrukTotalBeløp"),
  MAX_FRADRAG_BELOEP("MaxFradragBeløp"),
  ANTALL_DAGER_TOM("AntallDagerTOM"),
  ANTALL_NETTER_TOM("AntallNetterTOM"),
  FRADRAG_BELOEP("FradragBeløp"),
  SJABLON_VERDI("SjablonVerdi"),
  INNTEKTSGRENSE_BELOEP("InntektsgrenseBeløp"),
  SKATTESATS_PROSENT("SkattesatsProsent")
}
