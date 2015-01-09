package org.basex.query.func.stream;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class StreamMaterialize extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(exprs[0]).materialize(info);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    seqType = exprs[0].seqType();
    return this;
  }
}
