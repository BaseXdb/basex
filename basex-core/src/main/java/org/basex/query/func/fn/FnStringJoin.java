package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnStringJoin extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, info);
    final byte[] token = exprs.length == 2 ? toToken(exprs[1], qc) : EMPTY;

    // no results: empty string
    Item item = iter.next();
    if(item == null) return Str.ZERO;

    // single result
    final byte[] first = item.string(info);
    if((item = iter.next()) == null) return Str.get(first);

    // join multiple strings
    final TokenBuilder tb = new TokenBuilder().add(first);
    do {
      tb.add(token).add(item.string(info));
    } while((item = qc.next(iter)) != null);
    return Str.get(tb.finish());
  }
}
