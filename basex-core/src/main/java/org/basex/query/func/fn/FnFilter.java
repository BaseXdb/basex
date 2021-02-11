package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[1], 1, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = iter.next()) != null;) {
      if(toBoolean(func.invoke(qc, info, item).item(qc, info))) vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr items = exprs[0];
    final SeqType st = items.seqType();
    if(st.zero()) return items;

    // create filter expression
    final Expr pred = cc.get(items, () -> {
      Expr p = ContextValue.get(cc, info);
      p = new DynFuncCall(info, sc, exprs[1], p).optimize(cc);
      p = new TypeCheck(sc, info, p, SeqType.BOOLEAN_O, true).optimize(cc);
      return p;
    });
    return Filter.get(cc, info, items, cc.function(Function.BOOLEAN, info, pred));
  }
}
