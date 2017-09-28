FROM maven:3-jdk-8
MAINTAINER BaseX Team <basex-talk@mailman.uni-konstanz.de>

# Compile BaseX, install
COPY . /usr/src/basex/
RUN cd /usr/src/basex && \
    mvn clean install -DskipTests && \
    ln -s /usr/src/basex/basex-*/etc/* /usr/local/bin && \
    cp -r /usr/src/basex/basex-api/src/main/webapp/WEB-INF /srv

# Run as non-privileged user with fixed UID
# $MAVEN_CONFIG is expected to point to a volume by the parent maven image
RUN adduser --home /srv --disabled-password --disabled-login --uid 1984 basex && \
    mkdir /srv/.m2 /srv/BaseXData /srv/BaseXRepo /srv/BaseXWeb && \
    chown -R basex /srv
USER basex
ENV MAVEN_CONFIG=/srv/.m2

# 1984/tcp: API
# 8984/tcp: HTTP
# 8985/tcp: HTTP stop
EXPOSE 1984 8984 8985
VOLUME ["/srv/.m2", "/srv/BaseXData"]
WORKDIR /srv

# Run BaseX HTTP server by default
CMD ["/usr/local/bin/basexhttp"]
