package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionIdentity extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final FItem function = toFunction(arg(0), qc);
    return Str.get(function.funcIdentity());
  }
}
