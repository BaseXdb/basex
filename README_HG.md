# HG Insights BaseX Fork

This is a fork of the BaseX repo for internal use by HG Insights.

## Changes from upstream

The main difference between this repo and upstream is that we remove the
`VOLUME` line from the Dockerfile so that we can ship `piq-customer-api`
as a single image, without having to worry about `docker-compose` trying
to create unnecessary volumes.

## Development

* Keep changes to a minimum to maximize forward compatibility
* Tag releases in git as `<basex_version>-hg-<datestamp>`. Example: `9.2.4-hg-2019.09.18T12.00.00`
* Tag images on dockerhub using the same format

## Building

```
docker-compose build basex
```

## Pushing to Dockerhub

1. Update the image tag in `docker-compose.yml` using the format `hgdata1/basex:<version>-hg-<datestamp>`
1. Build: `docker-compose build`
1. Push to dockerhub: `docker-compose push`
