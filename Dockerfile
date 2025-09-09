# Dockerfile
FROM ghcr.io/navikt/pdfgen:2.0.91

ENV TZ="Europe/Oslo"
COPY templates /app/templates
COPY fonts /app/fonts
COPY resources /app/resources
