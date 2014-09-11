package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArrayFoldLeft extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    Value res = qc.value(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);
    for(final Value v : array.members()) res = fun.invokeValue(qc, info, res, v);
    return res;
  }
}
