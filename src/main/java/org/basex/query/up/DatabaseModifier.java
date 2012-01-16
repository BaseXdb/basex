package org.basex.query.up;

import static org.basex.query.util.Err.*;

import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.UpdatePrimitive;

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
  void add(final UpdatePrimitive p, final QueryContext ctx)
    throws QueryException {

    add(p);
    // check permissions
    if(!ctx.context.perm(User.WRITE, p.data.meta))
      PERMNO.thrw(p.input, CmdPerm.WRITE);
  }
}
