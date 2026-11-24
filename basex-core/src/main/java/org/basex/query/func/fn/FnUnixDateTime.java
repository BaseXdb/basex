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
public final class FnUnixDateTime extends StandardFunc {
  @Override
  public Dtm value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    return value == null ? Dtm.ZERO : Dtm.get(value);
  }
}
