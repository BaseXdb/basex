package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestSetAttribute extends ApiFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toString(arg(0), qc);
    final Value value = arg(1).value(qc);

    request(qc).setAttribute(name, value.materialize(n -> false, info, qc));
    return Empty.VALUE;
  }
}
