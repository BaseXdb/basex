package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class UtilItemAt extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final double ds = toDouble(exprs[1], qc);
    final long pos = (long) ds;
    if(ds != pos || pos < 1) return null;

    // fast route if the size is known
    final Iter iter = qc.iter(exprs[0]);
    final long max = iter.size();
    if(max >= 0) return pos > max ? null : iter.get(pos - 1);

    // loop through all items
    long p = 0;
    for(Item item; (item = iter.next()) != null;) {
      qc.checkStop();
      if(++p == pos) return item;
    }
    return null;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    seqType = exprs[0].seqType().withOcc(Occ.ZERO_ONE);
    return this;
  }
}
