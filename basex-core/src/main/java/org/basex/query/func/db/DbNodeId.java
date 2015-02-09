package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbNodeId extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter ir = qc.iter(exprs[0]);

      @Override
      public Int next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        final DBNode node = toDBNode(it);
        return Int.get(node.data.id(node.pre));
      }
    };
  }
}
