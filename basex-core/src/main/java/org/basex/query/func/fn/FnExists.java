package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
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
    Expr expr = this;
    final Expr input = arg(0);
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE) && input.seqType().type instanceof NodeType) {
      // if(exists($nodes)) → if($nodes)
      expr = input;
    }
    return cc.simplify(this, expr, mode);
  }
}
