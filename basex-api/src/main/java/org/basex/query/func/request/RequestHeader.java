package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RequestHeader extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(arg(0), qc);
    final String value = request(qc).getHeader(name);
    if(value != null) return Str.get(value);

    return defined(1) ? Str.get(toToken(arg(1), qc)) : Empty.VALUE;
  }
}
