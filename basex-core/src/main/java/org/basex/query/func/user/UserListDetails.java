package org.basex.query.func.user;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserListDetails extends UserList {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return information on single user
    if(exprs.length > 0) return toUser(0, qc).toXML(qc);

    // return information for all users
    final Context ctx = qc.context;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final User us : ctx.users.users(null, ctx)) vb.add(us.toXML(qc));
    return vb.value(this);
  }
}
