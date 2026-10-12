package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code while} clause, accepting all tuples while predicate is satisfied.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class While extends FilterClause {
  /**
   * Constructor.
   * @param expr predicate expression
   * @param info input info (can be {@code null})
   */
  public While(final Expr expr, final InputInfo info) {
    super(info, expr);
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        return sub.next(qc) && expr.ebv(qc, info);
      }
    };
  }

  @Override
  public While copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new While(expr.copy(cc, vm), info));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof final While whl && expr.equals(whl.expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(WHILE).token(expr);
  }
}
