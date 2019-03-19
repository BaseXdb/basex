package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnUnordered extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return exprs[0];
  }
}
