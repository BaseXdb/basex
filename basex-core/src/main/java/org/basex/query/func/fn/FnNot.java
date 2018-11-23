package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnNot extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // e.g.: not(boolean(A)) -> not(A)
    final Expr expr = exprs[0].optimizeEbv(cc);

    // not(empty(A)) -> exists(A)
    if(Function.EMPTY.is(expr)) {
      return cc.function(Function.EXISTS, info, args(expr));
    }
    // not(exists(A)) -> empty(A)
    if(Function.EXISTS.is(expr)) {
      return cc.function(Function.EMPTY, info, args(expr));
    }
    // not(not(A)) -> boolean(A)
    if(Function.NOT.is(expr)) {
      return FnBoolean.get(args(expr)[0], info, cc.sc());
    }
    // not('a' = 'b') -> 'a' != 'b'
    if(expr instanceof CmpV || expr instanceof CmpG) {
      final Expr ex = ((Cmp) expr).invert(cc);
      if(ex != expr) return ex;
    }
    // not($node/text()) -> empty($node/text())
    final SeqType st = expr.seqType();
    if(st.type instanceof NodeType) return cc.function(Function.EMPTY, info, expr);

    exprs[0] = expr;
    return this;
  }
}
