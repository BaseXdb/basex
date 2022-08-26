package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ProfVoid extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter value = exprs[0].iter(qc);

    // ensure that lazy items will be evaluated
    if(value.valueIter()) {
      value.value(qc, null).cache(false, info);
    } else {
      for(Item item; (item = qc.next(value)) != null;) {
        item.cache(false, info);
      }
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = exprs[0];
    return value.has(Flag.NDT) && value.size() == 0 ? value : this;
  }
}
