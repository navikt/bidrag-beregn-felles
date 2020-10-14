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

enum class SjablonTallNavn(val navn: String, val id: String, val bidragsevne: Boolean, val nettoBarnetilsyn: Boolean, val underholdskostnad: Boolean,
    val bpAndelUnderholdskostnad: Boolean, val barnebidrag: Boolean, val forskudd: Boolean) {
  ORDINAER_BARNETRYGD_BELOP("OrdinærBarnetrygdBeløp", "0001", false, false, true, false, false, false),
  ORDINAER_SMAABARNSTILLEGG_BELOP("OrdinærSmåbarnstilleggBeløp", "0002", false, false, false, false, false, false),
  BOUTGIFTER_BIDRAGSBARN_BELOP("BoutgifterBidragsbarnBeløp", "0003", false, false, true, false, false, false),
  FORDEL_SKATTEKLASSE2_BELOP("FordelSkatteklasse2Beløp", "0004", true, false, false, false, false, false),
  FORSKUDDSSATS_BELOP("ForskuddssatsBeløp", "0005", false, false, false, true, false, true),
  INNSLAG_KAPITALINNTEKT_BELOP("InnslagKapitalInntektBeløp", "0006", false, false, false, false, false, false),
  INNTEKTSINTERVALL_TILLEGGSBIDRAG_BELOP("InntektsintervallTilleggsbidragBeløp", "0007", false, false, false, false, false, false),
  MAKS_INNTEKT_BP_PROSENT("MaksInntektBPProsent", "0008", false, false, false, false, false, false),
  HOY_INNTEKT_BP_MULTIPLIKATOR("HøyInntektBPMultiplikator", "0009", false, false, false, false, false, false),
  INNTEKT_BB_MULTIPLIKATOR("InntektBBMultiplikator", "0010", false, false, false, false, false, false),
  MAKS_BIDRAG_MULTIPLIKATOR("MaksBidragMultiplikator", "0011", false, false, false, false, false, false),
  MAKS_INNTEKT_BB_MULTIPLIKATOR("MaksInntektBBMultiplikator", "0012", false, false, false, false, false, false),
  MAKS_INNTEKT_FORSKUDD_MOTTAKER_MULTIPLIKATOR("MaksInntektForskuddMottakerMultiplikator", "0013", false, false, false, false, false, true),
  NEDRE_INNTEKTSGRENSE_GEBYR_BELOP("NedreInntektsgrenseGebyrBeløp", "0014", false, false, false, false, false, false),
  SKATT_ALMINNELIG_INNTEKT_PROSENT("SkattAlminneligInntektProsent", "0015", false, true, false, false, false, false),
  TILLEGGSBIDRAG_PROSENT("TilleggsbidragProsent", "0016", false, false, false, false, false, false),
  TRYGDEAVGIFT_PROSENT("TrygdeavgiftProsent", "0017", true, false, false, false, false, false),
  BARNETILLEGG_SKATT_PROSENT("BarneTilleggSkattProsent", "0018", false, false, false, false, false, false),
  UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP("UnderholdEgneBarnIHusstandBeløp", "0019", true, false, false, false, false, false),
  ENDRING_BIDRAG_GRENSE_PROSENT("EndringBidragGrenseProsent", "0020", false, false, false, false, false, false),
  BARNETILLEGG_FORSVARET_FORSTE_BARN_BELOP("BarnetilleggForsvaretFørsteBarnBeløp", "0021", false, false, false, false, true, false),
  BARNETILLEGG_FORSVARET_OVRIGE_BARN_BELOP("BarnetilleggForsvaretØvrigeBarnBeløp", "0022", false, false, false, false, true, false),
  MINSTEFRADRAG_INNTEKT_BELOP("MinstefradragInntektBeløp", "0023", true, false, false, false, false, false),
  GJENNOMSNITT_VIRKEDAGER_PR_MAANED_ANTALL("GjennomsnittVirkedagerPrMånedAntall", "0024", false, false, false, false, false, false),
  MINSTEFRADRAG_INNTEKT_PROSENT("MinstefradragInntektProsent", "0025", true, false, false, false, false, false),
  DAGLIG_SATS_BARNETILLEGG_BELOP("DagligSatsBarnetilleggBeløp", "0026", false, false, false, false, false, false),
  PERSONFRADRAG_KLASSE1_BELOP("PersonfradragKlasse1Beløp", "0027", true, false, false, false, false, false),
  PERSONFRADRAG_KLASSE2_BELOP("PersonfradragKlasse2Beløp", "0028", true, false, false, false, false, false),
  KONTANTSTOTTE_BELOP("KontantstøtteBeløp", "0029", false, false, false, false, false, false),
  OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP("ØvreInntektsgrenseIkkeISkatteposisjonBeløp", "0030", false, false, false, false, false, false),
  NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP("NedreInntektsgrenseFullSkatteposisjonBeløp", "0031", false, false, false, false, false, false),
  EKSTRA_SMAABARNSTILLEGG_BELOP("EkstraSmåbarnstilleggBeløp", "0032", false, false, false, false, false, false),
  OVRE_INNTEKTSGRENSE_FULLT_FORSKUDD_BELOP("ØvreInntektsgrenseFulltForskuddBeløp", "0033", false, false, false, false, false, true),
  OVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_EN_BELOP("ØvreInntektsgrense75ProsentForskuddEnBeløp", "0034", false, false, false, false, false, true),
  OVRE_INNTEKTSGRENSE_75PROSENT_FORSKUDD_GS_BELOP("ØvreInntektsgrense75ProsentForskuddGSBeløp", "0035", false, false, false, false, false, true),
  INNTEKTSINTERVALL_FORSKUDD_BELOP("InntektsintervallForskuddBeløp", "0036", false, false, false, false, false, true),
  OVRE_GRENSE_SAERTILSKUDD_BELOP("ØvreGrenseSærtilskuddBeløp", "0037", false, false, false, false, false, false),
  FORSKUDDSSATS_75PROSENT_BELOP("Forskuddssats75ProsentBeløp", "0038", false, false, false, false, false, false),
  FORDEL_SAERFRADRAG_BELOP("FordelSærfradragBeløp", "0039", true, false, false, false, false, false),
  SKATTESATS_ALMINNELIG_INNTEKT_PROSENT("SkattesatsAlminneligInntektProsent", "0040", true, false, false, false, false, false),
  FORHOYET_BARNETRYGD_BELOP("ForhøyetBarnetrygdBeløp", "0041", false, false, true, false, false, false),
  FASTSETTELSESGEBYR_BELOP("FastsettelsesgebyrBeløp", "0100", false, false, false, false, false, false),
  DUMMY("Dummy", "9999", false, false, false, false, false, false)
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
