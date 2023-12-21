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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnDeepEqual extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = arg(0).iter(qc), iter2 = arg(1).iter(qc);
    final Collation collation = toCollation(arg(2), qc);
    final DeepEqualOptions options = toOptions(arg(3), new DeepEqualOptions(), false, qc);
    options.finish(sc);

    return Bln.get(new DeepEqual(info, collation, qc, options).equal(iter1, iter2));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = arg(0), expr2 = arg(1);
    // do not compare identical arguments
    if(!expr1.seqType().mayBeFunction() && !expr2.seqType().mayBeFunction() &&
        expr1.equals(expr2) && !expr1.has(Flag.NDT)) return Bln.TRUE;
    // reject arguments of different size
    final long size1 = expr1.size(), size2 = expr2.size();
    if(size1 != -1 && size2 != -1 && size1 != size2) return Bln.FALSE;

    return this;
  }
}
