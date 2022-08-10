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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnContainsToken extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = trim(toToken(exprs[1], qc));
    final Collation coll = toCollation(2, qc);
    if(token.length != 0) {
      final Iter value = exprs[0].iter(qc);
      for(Item item; (item = qc.next(value)) != null;) {
        for(final byte[] distinct : distinctTokens(toToken(item))) {
          if(coll == null ? eq(token, distinct) : coll.compare(token, distinct) == 0) {
            return Bln.TRUE;
          }
        }
      }
    }
    return Bln.FALSE;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // support limited to default collation
    final Expr value = exprs[0], token = exprs[1];
    return exprs.length == 2 && token.seqType().zeroOrOne() &&
      ii.create(token, ii.type(value, IndexType.TOKEN), true, info);
  }
}
