FROM maven:3-jdk-8-alpine
LABEL maintainer="BaseX Team <basex-talk@mailman.uni-konstanz.de>"

# Compile BaseX, install
COPY . /usr/src/basex/

# install git as "buildnumber-maven-plugin" requires git:
RUN apk update && apk add --no-cache git && \
    cd /usr/src/basex && \
    mvn clean install -DskipTests && \
    ln -s /usr/src/basex/basex-*/etc/* /usr/local/bin &&\
    adduser -h /srv -D -u 1984 basex && \
    mkdir -p /srv/.m2 /srv/basex/data /srv/basex/repo /srv/basex/webapp && \
    cp -r /usr/src/basex/basex-api/src/main/webapp/WEB-INF /srv/basex/webapp && \
    chown -R basex /srv
USER basex
ENV MAVEN_CONFIG=/srv/.m2

# 1984/tcp: API
# 8984/tcp: HTTP
# 8985/tcp: HTTP stop
EXPOSE 1984 8984 8985
VOLUME ["/srv/basex/data", "/srv/basex/repo","/srv/basex/webapp"]
WORKDIR /srv

# Run BaseX HTTP server by default
CMD ["/usr/local/bin/basexhttp"]
