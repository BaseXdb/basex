package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDeepEqual extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(new DeepEqual(info).collation(coll).equal(iter1, iter2, qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1.equals(expr2) && !expr1.has(Flag.NDT)) return Bln.TRUE;

    final long size1 = expr1.size(), size2 = expr2.size();
    if(size1 != -1 && size2 != -1 && size1 != size2) return Bln.FALSE;

    return this;
  }
}
