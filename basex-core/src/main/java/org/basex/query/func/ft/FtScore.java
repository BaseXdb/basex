package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FtScore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final boolean s = qc.scoring;
    final Iter iter;
    try {
      qc.scoring = true;
      iter = exprs[0].iter(qc);
    } finally {
      qc.scoring = s;
    }

    return new Iter() {
      @Override
      public Dbl next() throws QueryException {
        final Item item = iter.next();
        return item == null ? null : Dbl.get(item.score());
      }
    };
  }
}
