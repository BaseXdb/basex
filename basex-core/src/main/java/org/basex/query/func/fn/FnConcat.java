package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnConcat extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    for(final Expr expr : exprs) {
      final Item item = expr.atomItem(qc, info);
      if(item != null) tb.add(item.string(info));
    }
    return Str.get(tb.finish());
  }
}
