# Bidrag beregn felles
Felles biblioteker for beregning av bidrag/forskudd/særbidrag og diverse biblioteker for transformering av data

[![Release Drafter](https://github.com/navikt/bidrag-beregn-felles/actions/workflows/release-draft.yaml/badge.svg?branch=main)](https://github.com/navikt/bidrag-beregn-felles/actions/workflows/release-draft.yaml)
[![Publish](https://github.com/navikt/bidrag-beregn-felles/actions/workflows/publish.yaml/badge.svg?branch=main)](https://github.com/navikt/bidrag-beregn-felles/actions/workflows/publish.yaml)

### Skru på debug logging

For å skru på debug logging, legg til følgende i `application.yaml`:

```yaml
logging:
  level:
    secureLogger: DEBUG // For logging av sikker logg som har nivå DEBUG
    no.nav.bidrag.inntekt: DEBUG
    no.nav.bidrag.beregn.beregn.forskudd: DEBUG
```


[![Publish button]][Release draft]

### Contact

This project is maintained by [navikt/bidrag](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/bidrag-felles/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-bidrag](https://nav-it.slack.com/archives/CAZ7A2074)

<!---------------------------------------------------------------------------->

[Publish button]: https://img.shields.io/badge/Publiser_siste_release_draft-37a779?style=for-the-badge
[Release draft]: https://github.com/navikt/bidrag-felles/releases
[#]: #
.
