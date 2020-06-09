package no.nav.bidrag.beregn.felles.enums

enum class SjablonNavn (val navn: String) {
  BARNETILSYN("Barnetilsyn"),
  BIDRAGSEVNE("Bidragsevne"),
  FORBRUKSUTGIFTER("Forbruksutgifter"),
  MAKS_FRADRAG("MaksFradrag"),
  MAKS_TILSYN("MaksTilsyn"),
  SAMVAERSFRADRAG("Samværsfradrag"),
  TRINNVIS_SKATTESATS("TrinnvisSkattesats"),
}

enum class SjablonTallNavn (val navn: String) {
  ORDINAER_BARNETRYGD_BELOP("OrdinærBarnetrygdBeløp"),
  ORDINAER_SMAABARNSTILLEGG_BELOP("OrdinærSmåbarnstilleggBeløp"),
  BOUTGIFTER_BIDRAGSBARN_BELOP("BoutgifterBidragsbarnBeløp"),
  FORDEL_SKATTEKLASSE2_BELOP("FordelSkatteklasse2Beløp"),
  FORSKUDDSSATS_BELOP("ForskuddssatsBeløp"),
  INNSLAG_KAPITALINNTEKT_BELOP("InnslagKapitalInntektBeløp"),
  INNTEKTSINTERVALL_TILLEGGSBIDRAG_BELOP("InntektsintervallTilleggsbidragBeløp"),
  MAKS_INNTEKT_BP_PROSENT("MaksInntektBPProsent"),
  HOY_INNTEKT_BP_MULTIPLIKATOR("HøyInntektBPMultiplikator"),
  INNTEKT_BB_MULTIPLIKATOR("InntektBBMultiplikator"),
  MAKS_BIDRAG_MULTIPLIKATOR("MaksBidragMultiplikator"),
  MAKS_INNTEKT_BB_MULTIPLIKATOR("MaksInntektBBMultiplikator"),
  MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR("MaksInntektForskuddMottakerMultiplikator"),
  NEDRE_INNTEKTSGRENSE_GEBYR_BELOP("NedreInntektsgrenseGebyrBeløp"),
  SKATT_ALMINNELIG_INNTEKT_PROSENT("SkattAlminneligInntektProsent"),
  TILLEGGSBIDRAG_PROSENT("TilleggsbidragProsent"),
  TRYGDEAVGIFT_PROSENT("TrygdeavgiftProsent"),
  BARNETILLEGG_SKATT_PROSENT("BarneTilleggSkattProsent"),
  UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP("UnderholdEgneBarnIHusstandBeløp"),
  ENDRING_BIDRAG_GRENSE_PROSENT("EndringBidragGrenseProsent"),
  BARNETILLEGG_FORSVARET_FORSTE_BARN_BELOP("BarnetilleggForsvaretFørsteBarnBeløp"),
  BARNETILLEGG_FORSVARET_OVRIGE_BARN_BELOP("BarnetilleggForsvaretØvrigeBarnBeløp"),
  MINSTEFRADRAG_INNTEKT_BELOP("MinstefradragInntektBeløp"),
  GJENNOMSNITT_VIRKEDAGER_PR_MAANED_ANTALL("GjennomsnittVirkedagerPrMånedAntall"),
  MINSTEFRADRAG_INNTEKT_PROSENT("MinstefradragInntektProsent"),
  DAGLIG_SATS_BARNETILLEGG_BELOP("DagligSatsBarnetilleggBeløp"),
  PERSONFRADRAG_KLASSE1_BELOP("PersonfradragKlasse1Beløp"),
  PERSONFRADRAG_KLASSE2_BELOP("PersonfradragKlasse2Beløp"),
  KONTANTSTOTTE_BELOP("KontantstøtteBeløp"),
  OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP("ØvreInntektsgrenseIkkeISkatteposisjonBeløp"),
  NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP("NedreInntektsgrenseFullSkatteposisjonBeløp"),
  EKSTRA_SMAABARNSTILLEGG_BELOP("EkstraSmåbarnstilleggBeløp"),
  OVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELOP("ØvreInntektsgrenseFulltForskuddBeløp"),
  OVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELOP("ØvreInntektsgrense75ProsentForskuddEnBeløp"),
  OVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELOP("ØvreInntektsgrense75ProsentForskuddGSBeløp"),
  INNTEKTSINTERVALL_FORSKUDD_BELOP("InntektsintervallForskuddBeløp"),
  OVRE_GRENSE_SAERTILSKUDD_BELOP("ØvreGrenseSærtilskuddBeløp"),
  FORSKUDDSSATS_75PROSENT_BELOP("Forskuddssats75ProsentBeløp"),
  FORDEL_SAERFRADRAG_BELOP("FordelSærfradragBeløp"),
  SKATTESATS_ALMINNELIG_INNTEKT_PROSENT("SkattesatsAlminneligInntektProsent"),
  FASTSETTELSESGEBYR_BELOP("FastsettelsesgebyrBeløp")
}


enum class SjablonNokkelNavn (val navn: String) {
  STONAD_TYPE("StønadType"),
  TILSYN_TYPE("TilsynType"),
  BOSTATUS("Bostatus"),
  ALDER_TOM("AlderTOM"),
  ANTALL_BARN_TOM("AntallBarnTOM"),
  SAMVAERSKLASSE("Samværsklasse")
}

enum class SjablonInnholdNavn (val navn: String) {
  BARNETILSYN_BELOP("BarnetilsynBeløp"),
  BOUTGIFT_BELOP("BoutgiftBeløp"),
  UNDERHOLD_BELOP("UnderholdBeløp"),
  FORBRUK_TOTAL_BELOP("ForbrukTotalBeløp"),
  MAKS_FRADRAG_BELOP("MaksFradragBeløp"),
  MAKS_TILSYN_BELOP("MaksTilsynBeløp"),
  ANTALL_DAGER_TOM("AntallDagerTOM"),
  ANTALL_NETTER_TOM("AntallNetterTOM"),
  FRADRAG_BELOP("FradragBeløp"),
  SJABLON_VERDI("SjablonVerdi"),
  INNTEKTSGRENSE_BELOP("InntektsgrenseBeløp"),
  SKATTESATS_PROSENT("SkattesatsProsent")
}
