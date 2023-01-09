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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = exprs[0].iter(qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      if(toBoolean(predicate.invoke(qc, info, item).item(qc, info))) {
        vb.add(item);
      }
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    // create filter expression
    // filter(INPUT, PREDICATE)  ->  INPUT[FUNCTION(.)]
    final Expr predicate = cc.get(input, () -> {
      final Expr dfc = new DynFuncCall(info, sc, exprs[1], ContextValue.get(cc, info)).optimize(cc);
      return new TypeCheck(sc, info, dfc, SeqType.BOOLEAN_O, true).optimize(cc);
    });
    return Filter.get(cc, info, input, cc.function(Function.BOOLEAN, info, predicate));
  }
}
