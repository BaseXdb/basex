package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class SessionGet extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);
    final String name = toString(arg(0), qc);

    if(session != null) {
      final Object object = session.get(name);
      if(object != null) return JavaCall.toValue(object, qc, info);
    }
    return defined(1) ? arg(1).value(qc) : Empty.VALUE;
  }
}
