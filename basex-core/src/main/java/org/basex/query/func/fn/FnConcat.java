package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnConcat extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      final Item item = expr.atomItem(qc, info);
      if(item != Empty.VALUE) tb.add(item.string(info));
    }
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    for(final Expr expr : exprs) {
      if(expr != Empty.VALUE) list.add(expr);
    }
    final int ls = list.size();
    if(ls == el) return this;
    if(ls == 1) return cc.function(Function.STRING, info, list.get(0));
    if(ls == 0) return Str.ZERO;
    return cc.function(Function.CONCAT, info, list.finish());
  }
}
