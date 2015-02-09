package org.basex.query.func.xquery;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XQueryType extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return type(qc).value(qc);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    return type(qc);
  }

  /**
   * Dumps the argument's type and size and returns it unchanged.
   * @param qc query context
   * @return the argument expression
   */
  private Expr type(final QueryContext qc) {
    FnTrace.dump(Util.inf("{ type: %, size: %, exprSize: % }", exprs[0].seqType(), exprs[0].size(),
        exprs[0].exprSize()), token(exprs[0].toString()), qc);
    return exprs[0];
  }
}
