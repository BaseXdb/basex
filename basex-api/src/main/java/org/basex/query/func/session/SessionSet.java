package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class SessionSet extends SessionFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc, true);
    final String name = toString(exprs[0], qc);
    final Value value = exprs[1].value(qc);

    session.set(name, value.materialize(n -> false, ii, qc));
    return Empty.VALUE;
  }
}
