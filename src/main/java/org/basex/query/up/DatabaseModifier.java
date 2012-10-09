package org.basex.query.up;

import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;

/**
 * The database modifier holds all database updates during a snapshot.
 * Database permissions are checked to ensure that a user possesses enough
 * privileges to alter database contents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class DatabaseModifier extends ContextModifier {
  @Override
  void add(final Operation o, final QueryContext ctx) throws QueryException {
    add(o);
    // check permissions
    if(!ctx.context.perm(Perm.WRITE, o.getData().meta))
      BASX_PERM.thrw(o.getInfo(), Perm.WRITE);
  }
}
