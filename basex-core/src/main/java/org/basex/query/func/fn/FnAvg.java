package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnAvg extends FnSum {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq || expr instanceof Range) {
      final Item item = range(expr.value(qc), true);
      return item != null ? item : Empty.VALUE;
    }
    if(expr instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr, true);
      if(item != null) return item;
    }
    final Iter iter = expr.atomIter(qc, info);
    final Item item = iter.next();
    return item == null ? Empty.VALUE : sum(iter, item, true, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = optFirst();
    if(expr != this) return expr;

    expr = exprs[0];
    if(expr instanceof RangeSeq) return range((Value) expr, true);
    if(expr instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr, true);
      if(item != null) return item;
    }

    final SeqType st = expr.seqType();
    if(!st.mayBeArray()) exprType.assign(Calc.DIV.type(st.type, st.type));

    return this;
  }
}
