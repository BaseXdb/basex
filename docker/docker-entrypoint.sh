#!/bin/sh
set -e

# On first run, apply the admin password from $BASEX_ADMIN_PASSWORD (if provided).
# If unset, BaseX generates a random initial password and writes it to the log.
if [ -n "$BASEX_ADMIN_PASSWORD" ] && [ ! -e data/users.xml ]; then
  basex -c "PASSWORD $BASEX_ADMIN_PASSWORD"
fi

exec "$@"
