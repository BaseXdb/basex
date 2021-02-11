package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnStringToCodepoints extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int[] cps = cps(toZeroToken(exprs[0], qc));
    return new BasicIter<Int>(cps.length) {
      @Override
      public Int get(final long i) {
        return Int.get(cps[(int) i]);
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) {
        return IntSeq.get(cps);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return IntSeq.get(cps(toZeroToken(exprs[0], qc)));
  }
}
