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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends FnEmpty {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(!empty(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Bln empty = opt();
    return empty == null ? this : Bln.get(!empty.bool(info));
  }

  @Override
  public Expr simplify(final CompileContext cc, final Simplify simplify) {
    // if(exists(node*)) -> if(node*)
    if(simplify == Simplify.EBV) {
      final Expr expr = exprs[0];
      if(expr.seqType().type instanceof NodeType) return cc.simplify(this, expr);
    }
    return this;
  }
}
