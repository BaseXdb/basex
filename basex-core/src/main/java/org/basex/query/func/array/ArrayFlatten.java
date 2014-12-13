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
public final class ArrayFlatten extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    final Iter ir = qc.iter(exprs[0]);
    for(Item it; (it = ir.next()) != null;) flatten(vb, it);
    return vb;
  }

  /**
   * Recursively flattens arrays in the input.
   * @param vb value builder
   * @param it current item
   */
  private void flatten(final ValueBuilder vb, final Item it) {
    if(it instanceof Array) {
      for(final Value v : ((Array) it).members()) {
        for(final Item i : v) flatten(vb, i);
      }
    } else {
      vb.add(it);
    }
  }
}
