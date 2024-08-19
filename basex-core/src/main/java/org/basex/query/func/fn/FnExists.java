package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends FnEmpty {
  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return !super.test(qc, ii, pos);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(exists(nodes))  ->  if(nodes)
    return cc.simplify(this, mode.oneOf(Simplify.EBV, Simplify.PREDICATE) &&
        arg(0).seqType().type instanceof NodeType ? arg(0) : this, mode);
  }
}
