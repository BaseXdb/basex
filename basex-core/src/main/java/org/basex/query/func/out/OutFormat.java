package org.basex.query.func.out;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class OutFormat extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String form = string(toToken(exprs[0], qc));
    final int es = exprs.length;
    final Object[] args = new Object[es - 1];
    for(int e = 1; e < es; e++) {
      final Item it = exprs[e].item(qc, info);
      args[e - 1] = it.type.isUntyped() ? string(it.string(info)) : it.toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final RuntimeException ex) {
      throw ERRFORMAT_X_X.get(info, Util.className(ex), ex);
    }
  }
}
