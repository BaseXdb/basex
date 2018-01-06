package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.map.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnCount extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) return expr.item(qc, info) == null ? Int.ZERO : Int.ONE;

    // iterative access: if the iterator size is unknown, iterate through all results
    final Iter iter = expr.iter(qc);
    long size = iter.size();
    if(size == -1) {
      do ++size; while(qc.next(iter) != null);
    }
    return Int.get(size);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore non-deterministic expressions (e.g.: count(error()))
    final Expr expr = exprs[0];
    if(!expr.has(Flag.NDT)) {
      final long size = expr.size();
      if(size >= 0) return Int.get(size);
    }

    // rewrite count(map:keys(...)) to map:size(...)
    if(expr instanceof MapKeys)
      return cc.function(Function._MAP_SIZE, info, ((MapKeys) expr).exprs);

    return this;
  }
}
