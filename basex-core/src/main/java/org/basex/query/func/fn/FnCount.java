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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnCount extends StandardFunc {
  /** Item evaluation flag. */
  private boolean item;

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr ex = exprs[0];
    if(item) return ex.item(qc, info) == null ? Int.ZERO : Int.ONE;

    // iterative access: if the iterator size is unknown, iterate through all results
    final Iter iter = qc.iter(ex);
    long sz = iter.size();
    if(sz == -1) {
      do {
        qc.checkStop();
        ++sz;
      } while(iter.next() != null);
    }
    return Int.get(sz);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore non-deterministic expressions (e.g.: count(error()))
    final Expr ex = exprs[0];
    if(!ex.has(Flag.NDT, Flag.UPD)) {
      final long sz = ex.size();
      if(sz >= 0) return Int.get(sz);
    }

    // rewrite count(map:keys(...)) to map:size(...)
    if(ex instanceof MapKeys) return cc.function(Function._MAP_SIZE, info, ((MapKeys) ex).exprs);

    item = ex.seqType().zeroOrOne();
    return this;
  }
}
