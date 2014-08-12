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
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] sep = exprs.length == 2 ? toToken(exprs[1], qc) : EMPTY;
    final TokenBuilder tb = new TokenBuilder();
    final Iter iter = exprs[0].atomIter(qc, info);
    int c = 0;
    for(Item it; (it = iter.next()) != null;) {
      if(c++ != 0) tb.add(sep);
      tb.add(toToken(it));
    }
    return Str.get(tb.finish());
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.X30 && exprs.length == 1 || super.has(flag);
  }
}
