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
    // e.g.: not(boolean(A)) -> not(A)
    final Expr ex = exprs[0].optimizeEbv(cc);

    // not(empty(A)) -> exists(A)
    if(ex.isFunction(Function.EMPTY)) {
      return cc.function(Function.EXISTS, info, ((FnEmpty) ex).exprs);
    }
    // not(exists(A)) -> empty(A)
    if(ex.isFunction(Function.EXISTS)) {
      return cc.function(Function.EMPTY, info, exprs = ((FnExists) ex).exprs);
    }
    // not(not(A)) -> boolean(A)
    if(ex.isFunction(Function.NOT)) {
      return FnBoolean.get(((FnNot) ex).exprs[0], info, cc.sc());
    }
    // not('a' = 'b') -> 'a' != 'b'
    if(ex instanceof CmpV || ex instanceof CmpG) {
      final Expr e = ((Cmp) ex).invert(cc);
      if(e != ex) return e;
    }
    // not($node/text()) -> empty($node/text())
    final SeqType st = ex.seqType();
    if(st.type instanceof NodeType) return cc.function(Function.EMPTY, info, ex);

    exprs[0] = ex;
    return this;
  }
}
