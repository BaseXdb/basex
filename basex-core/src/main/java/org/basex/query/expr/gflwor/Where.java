package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code where} clause, filtering tuples not satisfying the predicate.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Where extends FilterClause {
  /**
   * Constructor.
   * @param expr predicate expression
   * @param info input info (can be {@code null})
   */
  public Where(final Expr expr, final InputInfo info) {
    super(info, expr);
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(sub.next(qc)) {
          if(expr.ebv(qc, info)) return true;
        }
        return false;
      }
    };
  }

  @Override
  public Where copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Where(expr.copy(cc, vm), info));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof final Where whr && expr.equals(whr.expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(WHERE).single(expr);
  }
}
