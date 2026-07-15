#!/bin/sh
set -e

# Route the log to standard output (docker logs). Prepended, so additional
# options in $BASEX_JVM can still override it.
export BASEX_JVM="-Dorg.basex.LOG=stdout $BASEX_JVM"

# On first run, apply the admin password from $BASEX_ADMIN_PASSWORD (if provided).
# If unset, BaseX generates a random initial password and writes it to the log.
if [ -n "$BASEX_ADMIN_PASSWORD" ] && [ ! -e data/users.xml ]; then
  basex -c "PASSWORD $BASEX_ADMIN_PASSWORD"
fi

exec "$@"
