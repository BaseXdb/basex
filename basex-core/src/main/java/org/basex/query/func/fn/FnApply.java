package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnApply extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem fun = toFunc(exprs[0], qc);
    final Array array = toArray(exprs[1], qc);
    final int as = array.arraySize();
    if(fun.arity() != as) throw APPLY_X_X.get(info, fun.arity(), as);

    final ValueList vl = new ValueList(as);
    for(final Value v : array.members()) vl.add(v);
    return fun.invokeValue(qc, info, vl.finish());
  }
}
