package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Leo Woerteler
 */
public class HofId extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return exprs[0];
  }
}
