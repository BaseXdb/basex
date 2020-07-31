package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

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
public final class FnNot extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // e.g.: not(boolean(A))  ->  not(A)
    final Expr expr = exprs[0].simplifyFor(Simplify.EBV, cc);

    // not(empty(A))  ->  exists(A)
    if(EMPTY.is(expr)) return cc.function(EXISTS, info, args(expr));
    // not(exists(A))  ->  empty(A)
    if(EXISTS.is(expr)) return cc.function(EMPTY, info, args(expr));
    // not(not(A))  ->  boolean(A)
    if(NOT.is(expr)) return cc.function(BOOLEAN, info, args(expr));

    // not('a' = 'b')  ->  'a' != 'b'
    if(expr instanceof Cmp) {
      final Expr ex = ((Cmp) expr).invert(cc);
      if(ex != expr) return ex;
    }
    // not($node/text())  ->  empty($node/text())
    final SeqType st = expr.seqType();
    if(st.type instanceof NodeType) return cc.function(EMPTY, info, expr);

    exprs[0] = expr;
    return this;
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {
    // negation: operator may be inverted in general comparison merge
    return NOT.is(ex) ? null : ex.mergeEbv(this, or, cc);
  }
}
