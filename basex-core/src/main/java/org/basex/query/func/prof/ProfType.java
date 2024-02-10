package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ProfType extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    type(qc);
    return arg(0).value(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    if(cc.dynamic) {
      type(cc.qc);
      return arg(0);
    }
    return this;
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param qc query context
   */
  private void type(final QueryContext qc) {
    final Expr value = arg(0);
    FnTrace.trace(Util.inf("%, size: %, exprSize: %", value.seqType(), value.size(),
        value.exprSize()), token(value + ": "), qc);
  }
}
