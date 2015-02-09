package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtScore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final boolean s = qc.scoring;
    try {
      qc.scoring = true;
      final Iter iter = exprs[0].iter(qc);
      final ValueList vl = new ValueList(Math.max(1, (int) iter.size()));
      for(Item it; (it = iter.next()) != null;) vl.add(Dbl.get(it.score()));
      return DblSeq.get(vl.finish(), vl.size());
    } finally {
      qc.scoring = s;
    }
  }
}
