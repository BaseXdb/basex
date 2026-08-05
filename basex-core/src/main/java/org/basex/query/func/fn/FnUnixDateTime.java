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
  protected Dtm item(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? Dtm.ZERO : Dtm.get(toLong(value, 0));
  }
}
