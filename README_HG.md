# HG Insights BaseX Fork

This is a fork of the BaseX repo for internal use by HG Insights.

The main difference between this repo and upstream is that we remove the
`VOLUME` line from the Dockerfile so that we can ship `piq-customer-api`
as a single image, without having to worry about `docker-compose` trying
to create unnecessary volumes.

