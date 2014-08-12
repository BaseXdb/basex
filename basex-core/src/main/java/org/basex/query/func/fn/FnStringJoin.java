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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnStringJoin extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, info);
    final byte[] token = exprs.length == 2 ? toToken(exprs[1], qc) : EMPTY;
    // no results: empty string
    Item it = iter.next();
    if(it == null) return Str.ZERO;
    // single result
    if((it = iter.next()) == null) return Str.get(toToken(it));
    // join multiple strings
    final TokenBuilder tb = new TokenBuilder(toToken(it));
    do tb.add(token).add(toToken(it)); while((it = iter.next()) != null);
    return Str.get(tb.finish());
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.X30 && exprs.length == 1 || super.has(flag);
  }
}
