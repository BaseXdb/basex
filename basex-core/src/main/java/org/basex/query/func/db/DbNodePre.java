package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DbNodePre extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    return new Iter() {
      @Override
      public Int next() throws QueryException {
        final Item item = qc.next(iter);
        return item == null ? null : Int.get(toDBNode(item).pre());
      }
    };
  }
}
