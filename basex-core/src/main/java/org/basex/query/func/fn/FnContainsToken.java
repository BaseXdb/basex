package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.index.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnContainsToken extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = trim(toToken(exprs[1], qc));
    final Collation coll = toCollation(2, qc);
    if(token.length != 0) {
      final Iter iter = exprs[0].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        for(final byte[] tok : distinctTokens(toToken(item))) {
          if(coll == null ? eq(token, tok) : coll.compare(token, tok) == 0) return Bln.TRUE;
        }
      }
    }
    return Bln.FALSE;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // support limited to default collation
    return exprs.length == 2 && exprs[1].seqType().zeroOrOne() &&
      ii.create(exprs[1], ii.type(exprs[0], IndexType.TOKEN), true, info);
  }
}
