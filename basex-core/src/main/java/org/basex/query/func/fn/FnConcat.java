package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
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
    // merge adjacent values, ignore empty sequences
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      if(expr instanceof Value) {
        final Item item = expr.atomItem(cc.qc, info);
        if(item != Empty.VALUE) tb.add(item.string(info));
      } else {
        if(!tb.isEmpty()) list.add(Str.get(tb.next()));
        list.add(expr);
      }
    }
    if(list.isEmpty()) return Str.get(tb.finish());
    if(!tb.isEmpty()) list.add(Str.get(tb.finish()));

    // single expression left: replace with string call
    final int ls = list.size();
    if(ls == 1) return cc.function(Function.STRING, info, list.peek());

    // replace old with new expressions
    if(ls != el) cc.info(QueryText.OPTMERGE_X, this);
    exprs = list.finish();
    return this;
  }
}
