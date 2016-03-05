# This Dockerfile provides an image for the BaseX DBA application.
# At the same time, it demonstrates general useage of the basexhttp
# Docker image for RestXQ applications.

# For production applications, better choose a fixed version tag
FROM basex/basexhttp:latest
MAINTAINER BaseX Team <basex-talk@mailman.uni-konstanz.de>

# If you need to install additional Java dependencies, switch to root
# USER root
# RUN apt-get update && \
#     apt-get install some-debian-package

# Don't forget to switch back to the BaseX user!
# USER basex

# Setting environment variables like CLASSPATH and JVM options
# to increase BaseX' memory limit
# ENV BASEX_JVM="-Xmx2048m"

# Add source code
COPY . /srv/BaseXWeb

# RUN is inherited from basex/basexhttp and not required again
