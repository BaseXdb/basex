package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FtScore extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final boolean s = qc.scoring;
    try {
      qc.scoring = true;
      final Iter iter = exprs[0].iter(qc);
      final DoubleList values = new DoubleList(Seq.initialCapacity(iter.size()));
      for(Item item; (item = qc.next(iter)) != null;) values.add(item.score());
      return DblSeq.get(values.finish());
    } finally {
      qc.scoring = s;
    }
  }
}
