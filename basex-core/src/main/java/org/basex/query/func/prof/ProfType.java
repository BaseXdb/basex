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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProfType extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    type(qc);
    return exprs[0].value(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    type(cc.qc);
    return exprs[0];
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param qc query context
   */
  private void type(final QueryContext qc) {
    final Expr expr = exprs[0];
    FnTrace.trace(Util.inf("{ type: %, size: %, exprSize: % }", expr.seqType(), expr.size(),
        expr.exprSize()), token(expr.toString()), qc);
  }
}
