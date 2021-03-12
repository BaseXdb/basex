package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MapContains extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(toMap(exprs[0], qc).contains(toAtomItem(exprs[1], qc), info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0];
    if(expr1 == XQMap.EMPTY) return Bln.FALSE;

    return this;
  }
}
