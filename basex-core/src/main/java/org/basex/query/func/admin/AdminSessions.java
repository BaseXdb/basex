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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class AdminSessions extends AdminFn {
  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ClientListener cl : qc.context.sessions) {
      final Context ctx = cl.context();
      final Data data = ctx.data();
      final FBuilder elem = FElem.build(SESSION);
      elem.add(USER, ctx.user().name()).add(ADDRESS, cl.clientAddress());
      if(data != null) elem.add(DATABASE, data.meta.name);
      vb.add(elem.finish());
    }
    return vb.value(this);
  }
}
