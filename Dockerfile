FROM maven:3-jdk-7
MAINTAINER BaseX Team <basex-talk@mailman.uni-konstanz.de>

# Compile BaseX, install
COPY . /usr/src/basex/
RUN cd /usr/src/basex && \
    mvn clean install -DskipTests && \
    ln -s /usr/src/basex/basex-*/etc/* /usr/local/bin && \
    cp -r /usr/src/basex/basex-api/src/main/webapp/WEB-INF /srv

# Run as non-privileged user with fixed UID
RUN adduser --system --home /srv --disabled-password --disabled-login --uid 1984 basex && \
    mkdir /srv/BaseXData /srv/BaseXRepo /srv/BaseXWeb && \
    chown -R basex /srv
USER basex

# 1984/tcp: API
# 8984/tcp: HTTP
# 8985/tcp: HTTP stop
EXPOSE 1984 8984 8985
VOLUME ["/srv/BaseXData"]
WORKDIR /srv

# Run BaseX HTTP server by default, logging to STDOUT
ENTRYPOINT ["/usr/local/bin/basexhttp"]
CMD ["-d"]
