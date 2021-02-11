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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProfVoid extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].iter(qc);

    // caches items; ensures that lazy items will be evaluated
    final Value value = iter.iterValue();
    if(value == null) {
      for(Item item; (item = qc.next(iter)) != null;) item.cache(false, info);
    } else {
      value.cache(false, ii);
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    return expr.has(Flag.NDT) && expr.size() == 0 ? expr : this;
  }
}
