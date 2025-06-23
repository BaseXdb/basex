package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionArity extends StandardFunc {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem function = toFunction(arg(0), qc);

    return Itr.get(function.arity());
  }
}
