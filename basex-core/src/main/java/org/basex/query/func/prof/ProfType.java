package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ProfType extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    FnTrace.trace(Util.inf("{ type: %, size: %, exprSize: % }", exprs[0].seqType(), exprs[0].size(),
        exprs[0].exprSize()), token(exprs[0].toString()), cc.qc);
    return exprs[0];
  }
}
