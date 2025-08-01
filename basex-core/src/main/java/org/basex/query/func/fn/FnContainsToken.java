package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.index.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnContainsToken extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final byte[] token = trim(toToken(arg(1), qc));
    final Collation collation = toCollation(arg(2), qc);
    if(token.length != 0) {
      final Iter value = arg(0).atomIter(qc, info);
      for(Item item; (item = qc.next(value)) != null;) {
        for(final byte[] distinct : distinctTokens(toToken(item))) {
          if(eq(token, distinct, collation)) return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // support limited to default collation
    final Expr value = arg(0), token = arg(1);
    return !defined(2) && token.seqType().zeroOrOne() &&
      ii.create(token, ii.type(value, IndexType.TOKEN), true, info);
  }
}
