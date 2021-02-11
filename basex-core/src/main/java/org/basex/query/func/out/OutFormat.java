package org.basex.query.func.out;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class OutFormat extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String form = string(toToken(exprs[0], qc));
    final int el = exprs.length;
    final Object[] args = new Object[el - 1];
    for(int e = 1; e < el; e++) {
      final Item item = exprs[e].item(qc, info);
      args[e - 1] = item == Empty.VALUE ? null : item.type.isUntyped() ? string(item.string(info)) :
        item.toJava();
    }
    try {
      return Str.get(String.format(form, args));
    } catch(final IllegalArgumentException ex) {
      throw OUTPUT_FORMAT_X_X.get(info, Util.className(ex), ex);
    }
  }
}
