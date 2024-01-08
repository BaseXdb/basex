package org.basex.query.func.string;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class StringFormat extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String pattern = toString(arg(0), qc);

    final int al = args().length;
    final Object[] args = new Object[al - 1];
    for(int a = 1; a < al; a++) {
      final Item item = arg(a).item(qc, info);
      args[a - 1] = item.isEmpty() ? null : item.type.isUntyped() ? string(item.string(info)) :
        item.toJava();
    }
    try {
      return Str.get(String.format(pattern, args));
    } catch(final IllegalArgumentException ex) {
      throw STRING_FORMAT_X_X.get(info, Util.className(ex), ex);
    }
  }
}
