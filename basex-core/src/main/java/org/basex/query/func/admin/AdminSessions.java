package org.basex.query.func.admin;

import static org.basex.core.users.UserText.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.server.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class AdminSessions extends AdminFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ClientListener cl : qc.context.sessions) {
      final Context ctx = cl.context();
      final String user = ctx.user().name();
      final String addr = cl.clientAddress();
      final Data data = ctx.data();
      final FElem elem = new FElem(SESSION).add(USER, user).add(ADDRESS, addr);
      if(data != null) elem.add(DATABASE, data.meta.name);
      vb.add(elem);
    }
    return vb.value(this);
  }
}
