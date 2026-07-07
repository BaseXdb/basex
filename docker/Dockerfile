# Fetch stage: plain Alpine suffices (busybox wget is built in, only unzip is added).
FROM alpine:3.22 AS fetch

# Release paths use the dotted version (12.4), the zip name the undotted one (BaseX124.zip).
ARG BASEX_VERSION=12.4

RUN apk add --no-cache unzip \
    && ZIP="BaseX$(echo "${BASEX_VERSION}" | tr -d '.').zip" \
    && wget -q "https://files.basex.org/releases/${BASEX_VERSION}/${ZIP}" \
         -O /tmp/basex.zip \
    && unzip -q /tmp/basex.zip -d /opt

FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://basex.org"

RUN apk add --no-cache bash \
    && addgroup -S basex && adduser -S basex -G basex

COPY --from=fetch --chown=basex:basex /opt/basex /opt/basex
COPY --chmod=755 docker-entrypoint.sh /usr/local/bin/

ENV PATH=/opt/basex/bin:$PATH

WORKDIR /opt/basex

VOLUME ["/opt/basex/data"]

USER basex

# 1984: database server (client API)
# 8080: HTTP server (REST, RESTXQ, DBA)
EXPOSE 1984 8080

# / is served by webapp/restxq.xqm without authentification; /rest/ would require credentials.
HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/

# Sets the admin password on first run (see docker-entrypoint.sh).
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["basexhttp"]
