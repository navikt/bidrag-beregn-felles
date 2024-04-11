# bidrag-inntekt

![](https://github.com/navikt/bidrag-inntekt/workflows/maven%20deploy/badge.svg)

Maven-modul som summerer, periodiserer og transformerer inntekter.

## Funksjonalitet

### Input
Modulen tar inn følgende lister av inntekter fra ulike kilder:
- A-inntekt
- Skattegrunnlag
- Kontantstøtte
- Utvidet barnetrygd
- Småbarnstillegg
- Barnetillegg fra pensjon

### Output
Modulen leverer tilbake følgende lister:
- En liste av summerte månedsinntekter (kun for a-innteker), med tilhørende inntektsposter
- En liste av summerte årsinntekter (for alle typer inntekter), med tilhørende inntektsposter

### Generelt om beregning

#### Fastsettelse av periode
For alle beregninger av inntekter rapportert via a-inntekt brukes utbetalingsperiode for å bestemme hvilken periode inntektsposten skal tilhøre. Utbetalingsperiode dekker alltid en kalendermåned. Unntaket er overgangsstønad, hvor etterbetalingsperiode brukes hvis den er utfylt. Hvis den ikke er utfylt brukes utbetalingsperiode.

#### Cut-off dato
- Inneholder fristen Skatteetaten har satt for å levere a-melding i foregående måned, basert på hvilken dato a-inntekt ble hentet (= aInntektHentetDato i requesten).
- Denne datoen brukes til å styre hvilke inntekter som skal returneres (hvis ainntektHentetDato er før fristen går vi en måned lengre tilbake).
- Følgende regelverk gjelder for å bestemme cut-off dato (se https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/frister-og-betaling-i-a-meldingen/):
  - Fristen for å levere a-meldingen er den 5. i hver måned
  - Hvis den 5. er helg eller helligdag, er fristen første påfølgende hverdag

### Beregning av A-inntekt

#### Månedsinntekt
Det summeres inntekter pr måned. I tillegg lages det en sum pr kode/beskrivelse pr måned.

#### Årsinntekt
Følgende forekomster leveres:
- En summert liste pr (kalender)år som det finnes komplette data for. Hvis inntekter er hentet før cut-off-datoen i januar vil det ikke bli generert en årsinntekt for foregående år.
- En summert liste for de siste 3 månedene. Her summeres inntekter for de siste 3 (komplette) månedene og det ganges deretter med 4 for å finne en beregnet årsinntekt. Hvis inntekter er hentet før cut-off-datoen skyves 3-månedersperioden 1 måned bakover i tid (siste måned blir satt til inneværende måned - 2).
- En summert liste for de siste 12 månedene. Her summeres inntekter for de siste 12 (komplette) månedene. Hvis inntekter er hentet før cut-off-datoen skyves 12-månedersperioden 1 måned bakover i tid (siste måned blir satt til inneværende måned - 2).
- I tillegg vil det for alle listene bli levert en liste av tilhørende inntektsposter (summert pr kode/beskrivelse).

### Beregning av Skattegrunnlag
Følgende forekomster leveres:
- En summert liste over ligningsinntekter pr kalenderår
- En summert liste over kapitalinntekter pr kalenderår
- Hvilke inntektsposter som defineres som hhv. ligningsinntekter og kapitalinntekter, og om de skal legges til eller trekkes fra, er definert i filene `mapping_ligs.yaml` og `mapping_kaps.yaml`.
- I tillegg vil det for begge listene bli levert en liste av tilhørende inntektsposter (summert pr kode/beskrivelse).

### Beregning av kontantstøtte, utvidet barnetrygd, småbarnstillegg og barnetillegg fra pensjon
- Input er et månedsbeløp som kan strekke seg over en periode på flere måneder
- Det gjøres en omregning av månedsbeløp til årsbeløp (dvs. det ganges med 12)
- Det leveres tilbake en liste over årsbeløp
- Periodene beholdes, slik at det som leveres tilbake er det samme som ble sendt inn, bortsett fra at beløpene er omregnet til årsbeløp
- Kontantstøtte og barnetillegg er pr barn, mens utvidet barnetrygd og småbarnstillegg er uavhengig av barn

### Beregning av ytelser
- Det gjøres en egen beregning av følgende ytelser (som er en del av det som rapporteres via a-inntekt):
  - AAP
  - Dagpenger
  - Foreldrepenger
  - Introduksjonsstønad
  - Kvalifiseringsstønad
  - Overgangsstønad (se eget punkt)
  - Pensjon
  - Sykepenger
- Det leveres en summert liste pr ytelse pr (kalender)år som det finnes komplette data for. Hvis inntekter er hentet før cut-off-datoen i januar vil det ikke bli generert en årsinntekt for foregående år.
- I tillegg vil det for alle listene bli levert en liste av tilhørende inntektsposter (summert pr kode/beskrivelse).
- Mappingregler for hver ytelse er definert i filen `mapping_ytelser.yaml`.

#### Beregning av overgangsstønad
- Beregning av overgangsstønad skiller seg fra beregning av de andre ytelsene på følgende måte:
  - Perioden det beregnes for er mai-april (og ikke helt kalenderår som det er for andre ytelser)
  - Etterbetalingsperiode brukes for å bestemme år-måned-periode hvis den er utfylt; hvis ikke brukes utbetalingsperiode (for andre ytelser brukes bare utbetalingsperiode)
  - Det er lagt inn en forutsetning om at etterbetalingsperiode alltid dekker eksakt en måned, men i teorien kan den strekke seg over flere måneder og starte/slutte midt i en måned (logikken har pt ikke tatt høyde for denne muligheten).
  - Det beregnes helt fram tom. april i den årsperioden man er inne i (ved innhenting av inntekter i januar 2024 vil det f.eks. beregnes for perioden 2023-05 - 2024-04, selv om vi ikke har inntekter for 2024 enda) (for andre ytelser beregnes det bare tom. foregående år, dvs. 2023 i dette eksemplet)
  - Det beregnes en årsinntekt ved å summere alle inntekter i perioden, dele på antall måneder det er rapportert overgangsstønad for og så gange med 12 (for andre ytelser gjøres det bare en summering av rapporterte inntekter i perioden)
  - Selv om en person har overgangsstønad som eneste inntekt (rapportert fra a-ordningen) vil det med denne løsningen kunne vises forskjellige inntekter for ainntekt (lønn og trekk) og overgangsstønad, selv om de bygger på de samme grunnlagsdataene