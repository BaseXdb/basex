# HG Insights BaseX Fork

This is a fork of the BaseX repo for internal use by HG Insights.

## Changes

The main difference between this repo and upstream is that we remove the
`VOLUME` line from the Dockerfile so that we can ship `piq-customer-api`
as a single image, without having to worry about `docker-compose` trying
to create unnecessary volumes.

## Building

```
docker-compose build basex
```

## Pushing to Dockerhub

1. Update the image tag in `docker-compose.yml` using the format `hgdata1/basex:<version>-hg-<datestamp>`
2. `docker-compose push`
