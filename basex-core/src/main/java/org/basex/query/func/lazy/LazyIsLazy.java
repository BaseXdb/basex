package org.basex.query.func.lazy;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LazyIsLazy extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    return value instanceof Lazy;
  }
}
