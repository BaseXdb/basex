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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnExists extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(qc.iter(exprs[0]).next() != null);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // ignore non-deterministic expressions (e.g.: exists(error()))
    final Expr e = exprs[0];
    final long es = e.size();
    return es == -1 || e.has(Flag.NDT, Flag.UPD) ? this : Bln.get(es != 0);
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // if(exists(node*)) -> if(node*)
    final Expr e = exprs[0];
    return e.seqType().type instanceof NodeType ? cc.replaceEbv(this, e) : this;
  }
}
