package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends FnEmpty {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!empty(qc));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(exists(nodes))  ->  if(nodes)
    return cc.simplify(this, mode.oneOf(Simplify.EBV, Simplify.PREDICATE) &&
        exprs[0].seqType().type instanceof NodeType ? exprs[0] : this, mode);
  }
}
