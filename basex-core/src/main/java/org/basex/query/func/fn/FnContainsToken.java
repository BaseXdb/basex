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

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnContainsToken extends StandardFunc {
  @Override
  protected Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(test(qc, 0));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
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
    if(defined(2) || !token.seqType().zeroOrOne()) return false;
    // the runtime trims the lookup token; the same must happen for index access
    final Expr expr = ii.cc.function(Function.NORMALIZE_SPACE, info, token);
    return ii.create(expr, ii.type(value, IndexType.TOKEN), info);
  }
}
