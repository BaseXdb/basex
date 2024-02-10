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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class AdminSessions extends AdminFn {
  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ClientListener cl : qc.context.sessions) {
      final Context ctx = cl.context();
      final Data data = ctx.data();
      final FBuilder elem = FElem.build(Q_SESSION);
      elem.add(Q_USER, ctx.user().name()).add(Q_ADDRESS, cl.clientAddress());
      if(data != null) elem.add(Q_DATABASE, data.meta.name);
      vb.add(elem.finish());
    }
    return vb.value(this);
  }
}
