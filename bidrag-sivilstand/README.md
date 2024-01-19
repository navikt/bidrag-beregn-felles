# bidrag-beregn-forskudd

![](https://github.com/navikt/bidrag-beregn-forskudd-core/workflows/maven%20deploy/badge.svg)

## Core
Maven-modul som inneholder forretningsregler for beregning og periodisering av bidragsforskudd.

### Funksjonalitet
Modulen tar inn parametre knyttet til bidragsmottaker og barnet det søkes om bidrag for, samt sjablonverdier som er relevante for forskuddsberegning. Modulen er delt i to nivåer; periodisering og beregning.

#### Periodisering
Det sjekkes hvilken periode (fra-/til-dato) de ulike inputparametrene gjelder for. Det gjøres en kontroll av at datoene er gyldige. Deretter samles alle datoer i en liste og det dannes bruddperioder. For hver bruddperiode finnes gjeldende verdier for de aktuelle parametrene og det foretas en beregning av forskudd. Resultatet som returneres er enten en liste av forskuddsberegninger eller en liste av avvik (hvis det er feil).

#### Forskuddsberegning
Forskudd beregnes basert på inputdata, sjablonverdier og et sett med regler. Regelverket er beskrevet i eget regneark. Ut fra forskuddsberegningen kommer det et beløp, hvilken regel som er brukt og en bekrivelse av regelen.