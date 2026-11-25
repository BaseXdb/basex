package org.basex.query.func.admin;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.server.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AdminCloseSessions extends AdminFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String target = toString(arg(0), qc);

    for(final ClientListener cl : qc.context.sessions) {
      if(cl.context() != qc.context && (target.equals(cl.context().user().name()) ||
          target.equals(cl.clientAddress()))) cl.close();
    }
    return Empty.VALUE;
  }
}
