package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionSet extends SessionFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, true);
    final String key = toString(arg(0), qc);
    final Value value = arg(1).value(qc);

    session.set(key, value.materialize(n -> false, info, qc));
    return Empty.VALUE;
  }
}
