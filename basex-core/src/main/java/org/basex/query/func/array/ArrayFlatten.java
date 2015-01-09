package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArrayFlatten extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Iter ir = qc.iter(exprs[0]);
    for(Item it; (it = ir.next()) != null;) vb.addFlattened(it);
    return vb;
  }
}
