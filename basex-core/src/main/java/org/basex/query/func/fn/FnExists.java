package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];
    final Item item = expr.seqType().zeroOrOne() ? expr.item(qc, info) : expr.iter(qc).next();
    return Bln.get(item != null);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // ignore non-deterministic expressions (e.g.: exists(error()))
    final Expr expr = exprs[0];
    if(!expr.has(Flag.NDT)) {
      final long size = expr.size();
      if(size != -1) return Bln.get(size != 0);
      if(expr.seqType().oneOrMore()) return Bln.TRUE;
    }
    return this;
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // if(exists(node*)) -> if(node*)
    final Expr expr = exprs[0];
    return expr.seqType().type instanceof NodeType ? cc.replaceEbv(this, expr) : this;
  }
}
