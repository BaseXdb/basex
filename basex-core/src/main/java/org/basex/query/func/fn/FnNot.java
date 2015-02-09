package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnNot extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr e = exprs[0];
    // simplify: not(empty(A)) -> exists(A)
    if(e.isFunction(Function.EMPTY)) {
      qc.compInfo(QueryText.OPTWRITE, this);
      exprs = ((Arr) e).exprs;
      return Function.EXISTS.get(sc, info, exprs);
    }
    // simplify: not(exists(A)) -> empty(A)
    if(e.isFunction(Function.EXISTS)) {
      qc.compInfo(QueryText.OPTWRITE, this);
      exprs = ((Arr) e).exprs;
      return Function.EMPTY.get(sc, info, exprs);
    }
    // simplify: not('a' = 'b') -> 'a' != 'b'
    if(e instanceof CmpV || e instanceof CmpG) {
      final Cmp c = ((Cmp) e).invert();
      return c == e ? this : c;
    }
    // simplify: not(not(A)) -> boolean(A)
    if(e.isFunction(Function.NOT)) {
      return compBln(((Arr) e).exprs[0], info);
    }
    // simplify, e.g.: not(boolean(A)) -> not(A)
    exprs[0] = e.optimizeEbv(qc, scp);
    return this;
  }
}
