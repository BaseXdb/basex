package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class DbNodeId extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    return new Iter() {
      @Override
      public Int next() throws QueryException {
        final Item item = qc.next(iter);
        if(item == null) return null;
        final DBNode node = toDBNode(item);
        return Int.get(node.data().id(node.pre()));
      }
    };
  }
}
