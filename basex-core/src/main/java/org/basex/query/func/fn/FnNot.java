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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnNot extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];

    // not(empty(A))  ->  exists(A)
    if(EMPTY.is(expr)) return cc.function(EXISTS, info, expr.args());
    // not(exists(A))  ->  empty(A)
    if(EXISTS.is(expr)) return cc.function(EMPTY, info, expr.args());
    // not(not(A))  ->  boolean(A)
    if(NOT.is(expr)) return cc.function(BOOLEAN, info, expr.args());

    // not('a' = 'b')  ->  'a' != 'b'
    if(expr instanceof Cmp) {
      final Expr ex = ((Cmp) expr).invert(cc);
      if(ex != expr) return ex;
    }
    // not(position() = 1)  ->  position() != 1
    // not(position() = <_>3</_>)  ->  position() != 3
    if(expr instanceof CmpPos) {
      final Expr ex = ((CmpPos) expr).invert(cc);
      if(ex != expr) return ex;
    }
    // not($node/text())  ->  empty($node/text())
    final SeqType st = expr.seqType();
    if(st.type instanceof NodeType) return cc.function(EMPTY, info, expr);

    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // not(boolean(A))  ->  not(A)
    exprs[0] = exprs[0].simplifyFor(Simplify.EBV, cc);
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {
    // negation: operator may be inverted in general comparison merge
    return NOT.is(ex) ? null : ex.mergeEbv(this, or, cc);
  }
}
