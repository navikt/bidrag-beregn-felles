# bidrag-beregn-felles
![](https://github.com/navikt/bidrag-beregn-felles/workflows/maven%20deploy/badge.svg)

Repo for alle felles beregninger for bidrag. Disse erstatter beregninger i BBM.

## Changelog:

Versjon | Endringstype | Beskrivelse
--------|--------------|------------
0.13.0  | Opprettet    | Lagt til logikk for utvidet barnetrygd og generering av nye inntekter. Gjort noen justeringer i inntekt og sjablon enums.
0.12.1  | Endret       | Gjort noen justeringer i inntekt enum
0.12.0  | Endret       | Endret SjablonUtil + DTO/BO fra Double til BigDecimal
0.11.0  | Endret       | Endret fra Double til BigDecimal i InntektUtil
0.10.0  | Endret       | Splittet validering og justering av inntekter i 2 public-metoder
0.9.0   | Opprettet    | Lagt til nye sjablonklasser
0.8.0   | Endret       | Endret PeriodeUtil.validerBeregnPeriodeInput til public
0.7.0   | Endret       | Lagt til flere egenskaper på enum SjablonTallNavn
0.6.0   | Opprettet    | Lagt til funksjonalitet for validering og justering av inntekter. Lagt til / endret enum for inntektstyper med egenskaper
0.5.2   | Opprettet    | Lagt til ny enum for resultatkode og endret en eksisterende enum
0.5.1   | Opprettet    | Lagt til enums for resultatkoder
0.5.0   | Slettet      | Slettet funksjonalitet for bidragsevneberegning (flyttet til bidrag-beregn-barnebidrag-core)
0.4.10  | Endret       | Lagt til ny sjablontype
0.4.9   | Endret       | Flyttet input-kontroll av datoer ut i en felles-modul (util)
0.4.8   | Endret       | Forbedret input-kontroll på datoer
0.4.7   | Opprettet    | Lagt til ny verdi i enum AVVIK_TYPE
0.4.6   | Endret       | Beregn-til-dato lagt med i periodisering for beregning av bidragsevne.
0.4.5   | Endret       | Rettet feil i input-validering av særfradrag.
0.4.4   | Endret       | Rettet feil i sortering i SjablonUtil. Lagt til noen flere tester i SjablonTest.
0.4.3   | Endret       | Flyttet også sjablonPeriode og Avvik fra bidragsevne over til felles.
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
