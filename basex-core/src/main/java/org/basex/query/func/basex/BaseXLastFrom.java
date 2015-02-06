package org.basex.query.func.basex;

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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BaseXLastFrom extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // fast route if the size is known
    final Iter iter = qc.iter(exprs[0]);
    final long max = iter.size();
    if(max >= 0) return max == 0 ? null : iter.get(max - 1);

    // loop through all items
    Item litem = null;
    for(Item item; (item = iter.next()) != null;) {
      qc.checkStop();
      litem = item;
    }
    return litem;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    seqType = exprs[0].seqType().withOcc(Occ.ZERO_ONE);
    return this;
  }
}
