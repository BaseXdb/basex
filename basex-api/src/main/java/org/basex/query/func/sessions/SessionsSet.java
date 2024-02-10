package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.session.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class SessionsSet extends SessionsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc);
    final String name = toString(arg(1), qc);
    final Value value = arg(2).value(qc);

    session.set(name, value.materialize(n -> false, info, qc));
    return Empty.VALUE;
  }
}
