package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnCompare extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it1 = exprs[0].atomItem(qc, info), it2 = exprs[1].atomItem(qc, info);
    final Collation coll = toCollation(2, qc);
    if(it1 == null || it2 == null) return null;
    return Int.get(Math.max(-1, Math.min(1, coll == null ? diff(toToken(it1), toToken(it2)) :
      coll.compare(toToken(it1), toToken(it2)))));
  }
}
