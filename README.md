# bidrag-beregn-felles
![](https://github.com/navikt/bidrag-beregn-felles/workflows/maven%20deploy/badge.svg)

Felles-repo for beregninger i bidrag. Brukes av bidrag-beregn-forskudd-xxx, bidrag-beregn-barnebidrag-xxx og bidrag-beregn-saertilskudd-xxx.
Tilbyr felles dataklasser og util-metoder for håndtering av inntekter, sjabloner og perioder.

## Changelog:

| Versjon | Endringstype | Beskrivelse                                                                                                                                                                      |
|---------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.0.3   | Endret       | Delvis fjernet bruk av InntektType                                                                                                                                               |
| 1.0.2   | Endret       | Liten justering i workflow                                                                                                                                                       |
| 1.0.1   | Endret       | Liten justering i workflow for å få oppdatert pom.xml med snapshot-versjon og riktige permissions.                                                                               |
| 1.0.0   | Endret       | Skrevet om til Kotlin. Flyttet enums til bidrag-domain. Refaktorert kode.                                                                                                        |
| 0.19.3  | Endret       | Oppdatert versjoner av maven-surefire-plugin og kotlin-maven-plugin                                                                                                              |
| 0.19.2  | Endret       | Lagt til Bostatuskode ALENE for bidragsevne/BP                                                                                                                                   |
| 0.19.1  | Endret       | Oppdatert versjoner av Kotlin og Spring Boot                                                                                                                                     |
| 0.19.0  | Endret       | Nye verdier for enum BostatusKode                                                                                                                                                |
| 0.18.4  | Endret       | Oppdaterte avhegigheter (Snyk)                                                                                                                                                   |
| 0.18.3  | Endret       | Sjekker om periode liste er tom før validering av perioder i PeriodeUtil                                                                                                         |
| 0.18.2  | Endret       | Liten justering på validering av datoer                                                                                                                                          |
| 0.18.1  | Endret       | Oppdatert til Java 17 + oppdatert andre avhengigheter                                                                                                                            |
| 0.18.0  | Endret       | Lagt til ny dataklasse SjablonResultatGrunnlagCore                                                                                                                               |
| 0.17.0  | Endret       | Fjernet referanse fra sjablon igjen. Lagt til noen nye dataklasser.                                                                                                              |
| 0.16.0  | Endret       | Lagt til referanse på sjablon                                                                                                                                                    |
| 0.15.0  | Endret       | Lagt til referanse på inntekt + noe refaktorering                                                                                                                                |
| 0.14.9  | Endret       | Nye enums for resultatkode for resultat av forholdsmessig fordeling                                                                                                              |
| 0.14.8  | Endret       | Lagt til noe dokumentasjon                                                                                                                                                       |
| 0.14.7  | Endret       | Sjablon 0038 brukes av Forskudd                                                                                                                                                  |
| 0.14.6  | Endret       | Ny enum for resultatkode som angir at det skal gjøres en forholdsmessig fordeling                                                                                                |
| 0.14.5  | Endret       | Lagt til les av flere sjabloner for bpandelsaertilskudd (ifbm utvidet barnetrygd)                                                                                                |
| 0.14.4  | Endret       | Tatt vekk sjabloner for delberegning særtilskudd                                                                                                                                 |
| 0.14.3  | Endret       | Endret enum under til: SAERTILSKUDD_IKKE_FULL_BIDRAGSEVNE, dette innebærer at særtilskuddet ikke innvilges                                                                       |
| 0.14.2  | Endret       | Ny resultatkode: SAERTILSKUDD_REDUSERT_AV_EVNE                                                                                                                                   |
| 0.14.1  | Endret       | Ny enum lagt til for resultatkode SAERTILSKUDD_INNVILGET                                                                                                                         |
| 0.14.0  | Opprettet    | Lagt til sjablonparametre for Særtilskudd                                                                                                                                        |
| 0.13.2  | Endret       | Ny enum lagt til for resultatkode BARNEBIDRAG_IKKE_BEREGNET_DELT_BOSTED                                                                                                          |
| 0.13.1  | Endret       | Små justeringer på inntekt enum                                                                                                                                                  |
| 0.13.0  | Opprettet    | Lagt til logikk for utvidet barnetrygd og generering av nye inntekter. Gjort noen justeringer i inntekt og sjablon enums.                                                        |
| 0.12.1  | Endret       | Gjort noen justeringer i inntekt enum                                                                                                                                            |
| 0.12.0  | Endret       | Endret SjablonUtil + DTO/BO fra Double til BigDecimal                                                                                                                            |
| 0.11.0  | Endret       | Endret fra Double til BigDecimal i InntektUtil                                                                                                                                   |
| 0.10.0  | Endret       | Splittet validering og justering av inntekter i 2 public-metoder                                                                                                                 |
| 0.9.0   | Opprettet    | Lagt til nye sjablonklasser                                                                                                                                                      |
| 0.8.0   | Endret       | Endret PeriodeUtil.validerBeregnPeriodeInput til public                                                                                                                          |
| 0.7.0   | Endret       | Lagt til flere egenskaper på enum SjablonTallNavn                                                                                                                                |
| 0.6.0   | Opprettet    | Lagt til funksjonalitet for validering og justering av inntekter. Lagt til / endret enum for inntektstyper med egenskaper                                                        |
| 0.5.2   | Opprettet    | Lagt til ny enum for resultatkode og endret en eksisterende enum                                                                                                                 |
| 0.5.1   | Opprettet    | Lagt til enums for resultatkoder                                                                                                                                                 |
| 0.5.0   | Slettet      | Slettet funksjonalitet for bidragsevneberegning (flyttet til bidrag-beregn-barnebidrag-core)                                                                                     |
| 0.4.10  | Endret       | Lagt til ny sjablontype                                                                                                                                                          |
| 0.4.9   | Endret       | Flyttet input-kontroll av datoer ut i en felles-modul (util)                                                                                                                     |
| 0.4.8   | Endret       | Forbedret input-kontroll på datoer                                                                                                                                               |
| 0.4.7   | Opprettet    | Lagt til ny verdi i enum AVVIK_TYPE                                                                                                                                              |
| 0.4.6   | Endret       | Beregn-til-dato lagt med i periodisering for beregning av bidragsevne.                                                                                                           |
| 0.4.5   | Endret       | Rettet feil i input-validering av særfradrag.                                                                                                                                    |
| 0.4.4   | Endret       | Rettet feil i sortering i SjablonUtil. Lagt til noen flere tester i SjablonTest.                                                                                                 |
| 0.4.3   | Endret       | Flyttet også sjablonPeriode og Avvik fra bidragsevne over til felles.                                                                                                            |
| 0.4.2   | Endret       | Flyttet gjenstående sjablonklasser + Avvik og Periode fra bidragsevne over til felles.                                                                                           |
| 0.4.1   | Endret       | Forenklet funksjonalitet i SjablonUtil. Opprettet TestUtil-klasse med metode for å bygge opp liste med sjabloner.                                                                |
| 0.4.0   | Endret       | SjablonCore dto (resultatet) pekte på bo-objekter. Rettet til å peke på dto-objekter.                                                                                            |
| 0.3.0   | Endret       | Bidragsevne omskrevet til å bruke ny sjablonlogikk + egen klasse for skatteklasse                                                                                                |
| 0.2.1   | Endret       | Gjort noen justeringer i SjablonUtil                                                                                                                                             |
| 0.2.0   | Endret       | Nye utilityklasser og testklasser for håndtering av sjabloner + mapping ny sjablonstruktur DTO/BO. Noen mindre justeringer. Oppdatert minor-versjon pga. endringer i grensesnitt |
| 0.1.9   | Endret       | Noen endringer i navn for sjablon-enumer                                                                                                                                         |
| 0.1.8   | Opprettet    | Ny dto + enum for sjabloner                                                                                                                                                      |
| 0.1.7   | Endret       | Ved negativ bidragsevne settes bidragsevne nå til 0                                                                                                                              |
| 0.1.6   | Endret       | Feilfiks etter null pointer exeption                                                                                                                                             |
| 0.1.5   | Endret       | Ny full gjennomgang av og endring til mer selvforklarende sjablonnavn                                                                                                            |
| 0.1.4   | Endret       | Sjablonnavn starter med stor bokstav, lagt til BidragsevneCoreTest                                                                                                               |
| 0.1.3   | Endret       | Slått av test på sjablonperioder                                                                                                                                                 |
| 0.1.2   | Endret       | Noen feilfikser etter review av bidragsevneberegning                                                                                                                             |
| 0.1.1   | Slettet      | Slettet Kotlin-unittester                                                                                                                                                        |
| 0.1.0   | Opprettet    | Bidragsevneberegning lagt inn i Master                                                                                                                                           |
| 0.0.5   | Opprettet    | Lagt til enum 'InntektType'                                                                                                                                                      |
| 0.0.4   | Endret       | Packaging jar i pom.xml samt ny workflow som lager commit av release                                                                                                             |
| 0.0.3   | Endret       | Enums var ikke del av en package                                                                                                                                                 |
| 0.0.2   | Endret       | Gjort om tester til Java, flyttet enums og fjernet overflødig kode                                                                                                               |
| 0.0.1   | Opprettet    | `Periode` og `Periodiserer`                                                                                                                                                      |
