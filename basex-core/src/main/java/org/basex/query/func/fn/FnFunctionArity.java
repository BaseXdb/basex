package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionArity extends StandardFunc {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Int.get(toFunction(arg(0), qc).arity());
  }
}
