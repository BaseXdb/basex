package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnLocation extends ContextFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    // always empty, as no location information is retained
    toNodeOrNull(context(qc), qc);
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(false, false, cc.qc.focus.value);
  }
}
