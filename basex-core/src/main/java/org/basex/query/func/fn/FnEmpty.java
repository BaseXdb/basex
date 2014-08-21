package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnEmpty extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].iter(qc).next() == null);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr e = exprs[0].optimizeEbv(qc, scp);
    exprs[0] = e;
    // ignore non-deterministic expressions (e.g.: error())
    return e.size() == -1 || e.has(Flag.NDT) || e.has(Flag.CNS) || e.has(Flag.UPD) ? this :
      Bln.get(e.size() == 0);
  }
}
