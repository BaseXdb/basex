package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnNot extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr e0 = exprs[0];
    // simplify: not(empty(A)) -> exists(A)
    if(e0.isFunction(Function.EMPTY)) {
      exprs = ((Arr) e0).exprs;
      return cc.function(Function.EXISTS, info, exprs);
    }
    // simplify: not(exists(A)) -> empty(A)
    if(e0.isFunction(Function.EXISTS)) {
      exprs = ((Arr) e0).exprs;
      return cc.function(Function.EMPTY, info, exprs);
    }
    // simplify: not('a' = 'b') -> 'a' != 'b'
    if(e0 instanceof CmpV || e0 instanceof CmpG) {
      final Expr e = ((Cmp) e0).invert(cc);
      return e == e0 ? this : e;
    }
    // simplify: not(not(A)) -> boolean(A)
    if(e0.isFunction(Function.NOT)) {
      return compBln(((Arr) e0).exprs[0], info, cc.sc());
    }
    // simplify, e.g.: not(boolean(A)) -> not(A)
    exprs[0] = e0.optimizeEbv(cc);
    return this;
  }
}
