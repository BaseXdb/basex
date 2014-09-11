package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class HofId extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(exprs[0]);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return qc.value(exprs[0]);
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return exprs[0].item(qc, ii);
  }

  @Override
  protected final Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    return exprs[0];
  }
}
