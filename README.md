# bidrag-beregn-felles
![](https://github.com/navikt/bidrag-beregn-felles/workflows/maven%20deploy/badge.svg)

Repo for alle felles beregninger for bidrag. Disse erstatter beregninger i BBM.

## Changelog:

Versjon | Endringstype | Beskrivelse
--------|--------------|------------
0.4.2   | Endret       | Flyttet gjenstående sjablonklasser + Avvik og Periode fra bidragsevne over til felles.
0.4.1   | Endret       | Forenklet funksjonalitet i SjablonUtil. Opprettet TestUtil-klasse med metode for å bygge opp liste med sjabloner.
0.4.0   | Endret       | SjablonCore dto (resultatet) pekte på bo-objekter. Rettet til å peke på dto-objekter.
0.3.0   | Endret       | Bidragsevne omskrevet til å bruke ny sjablonlogikk + egen klasse for skatteklasse
0.2.1   | Endret       | Gjort noen justeringer i SjablonUtil      
0.2.0   | Endret       | Nye utilityklasser og testklasser for håndtering av sjabloner + mapping ny sjablonstruktur DTO/BO. Noen mindre justeringer. Oppdatert minor-versjon pga. endringer i grensesnitt     
0.1.9   | Endret       | Noen endringer i navn for sjablon-enumer
0.1.8   | Opprettet    | Ny dto + enum for sjabloner
0.1.7   | Endret       | Ved negativ bidragsevne settes bidragsevne nå til 0
0.1.6   | Endret       | Feilfiks etter null pointer exeption
0.1.5   | Endret       | Ny full gjennomgang av og endring til mer selvforklarende sjablonnavn
0.1.4   | Endret       | Sjablonnavn starter med stor bokstav, lagt til BidragsevneCoreTest
0.1.3   | Endret       | Slått av test på sjablonperioder
0.1.2   | Endret       | Noen feilfikser etter review av bidragsevneberegning
0.1.1   | Slettet      | Slettet Kotlin-unittester
0.1.0   | Opprettet    | Bidragsevneberegning lagt inn i Master
0.0.5   | Opprettet    | Lagt til enum 'InntektType'
0.0.4   | Endret       | Packaging jar i pom.xml samt ny workflow som lager commit av release
0.0.3   | Endret       | Enums var ikke del av en package
0.0.2   | Endret       | Gjort om tester til Java, flyttet enums og fjernet overflødig kode
0.0.1   | Opprettet    | `Periode` og `Periodiserer`
