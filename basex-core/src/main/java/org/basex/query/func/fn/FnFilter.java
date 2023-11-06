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
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 1, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      if(toBoolean(predicate.invoke(qc, info, item).item(qc, info))) vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    // create filter expression
    // filter(INPUT, PREDICATE)  ->  INPUT[PREDICATE(.)]
    final Expr predicate = cc.get(input, () ->
      new DynFuncCall(info, sc, coerce(1, cc), ContextValue.get(cc, info)).optimize(cc)
    );
    return Filter.get(cc, info, input, predicate);
  }
}
