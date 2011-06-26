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
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class DatabaseModifier extends ContextModifier {

  /**
   * Constructor.
   */
  public DatabaseModifier() {
    super();
  }

  /**
   * Adds an update primitive to this context modifier.
   * @param p update primitive
   * @param ctx query context
   * @throws QueryException query exception
   */
  @Override
  public void add(final UpdatePrimitive p, final QueryContext ctx)
  throws QueryException {
    super.add(p, ctx);

    // check permissions
    if(!ctx.context.perm(User.WRITE, p.data.meta))
      PERMNO.thrw(p.input, CmdPerm.WRITE);
  }
}
