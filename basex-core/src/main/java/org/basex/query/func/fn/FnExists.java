package org.basex.query.func.fn;

import java.util.*;

import org.basex.data.*;
import org.basex.index.path.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends FnEmpty {
  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    return !super.ebv(qc);
  }

  @Override
  public boolean noMatches(final ArrayList<PathNode> nodes, final Data data)
      throws QueryException {
    return arg(0).noMatches(nodes, data);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    final Expr input = arg(0);
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // if(exists($nodes)) → if($nodes)
      if(input.seqType().type instanceof NodeType) expr = input;
    }
    return cc.simplify(this, expr, mode);
  }
}
