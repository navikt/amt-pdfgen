# amt-pdfgen

## Kom i gang
Bygg docker-imaget med:
```shell
docker build -t amt-pdfgen .
```

Kjør lokalt med:
```shell
docker-compose up
```

Gå til http://localhost:8080/api/v1/genpdf/amt/hovedvedtak for å se et eksempel på en pdf. Endringer i templates eller data blir visende i pdfen etter en refresh.

Hvert template i `templates/amt/*` skal ha en json fil med testdata i `data/amt/`

