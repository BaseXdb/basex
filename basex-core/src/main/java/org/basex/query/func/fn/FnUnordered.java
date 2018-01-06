package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnUnordered extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return exprs[0];
  }
}
