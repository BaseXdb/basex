package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends FnEmpty {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!empty(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(false, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    // if(exists(node*)) -> if(node*)
    if(mode == Simplify.EBV) {
      final Expr expr = exprs[0];
      if(expr.seqType().type instanceof NodeType) return cc.simplify(this, expr);
    }
    return this;
  }

  @Override
  public Expr mergeEbv(final Expr expr, final boolean union, final CompileContext cc)
      throws QueryException {

    if(union && Function.EXISTS.is(expr)) {
      exprs[0] = new List(info, exprs[0], ((FnExists) expr).exprs[0]).optimize(cc);
      return optimize(cc);
    }
    return null;
  }
}
