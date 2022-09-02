package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
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
    final Expr value = exprs[0];
    final boolean skip = exprs.length > 1 && toBoolean(exprs[1], qc);

    // ensure that deterministic input will be evaluated
    if(!skip || value.has(Flag.NDT)) {
      final Iter iter = value.iter(qc);
      if(iter.valueIter()) {
        iter.value(qc, null).cache(false, info);
      } else {
        for(Item item; (item = qc.next(iter)) != null;) {
          item.cache(false, info);
        }
      }
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = exprs[0];
    if(value.has(Flag.NDT)) {
      if(value.size() == 0) return value;
    } else if(exprs.length > 1 && exprs[1] instanceof Value && toBoolean(exprs[1], cc.qc)) {
      return Empty.VALUE;
    }
    return this;
  }
}
