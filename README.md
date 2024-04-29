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

Gå til http://localhost:8080/api/v1/genpdf/amt/vedtak for å se et eksempel på en pdf. 

Hvert template i `templates/amt/*` skal ha en json fil med testdata i `data/amt/`

Siden vi kjører med `DEV_MODE=true` på lokalt skal endringer i data eller templates reflekeres etter en refresh i nettleseren, men pga en bug i `pdfgen` så virker ikke det på `GET`-requests akkurat nå. Så noen workarounder er å restarte appen etter hver endring, eller gjøre et `POST`-request og lagre pdfen lokalt med noe som f.eks:


```shell
curl -X POST \
  -H "Content-Type: application/json" \
  --data @data/amt/hovedvedtak.json \
  http://0.0.0.0:8080/api/v1/genpdf/amt/hovedvedtak \
  --output hovedvedtak.pdf
```

