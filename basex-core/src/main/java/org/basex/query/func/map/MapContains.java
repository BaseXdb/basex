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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class MapContains extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);

    return Bln.get(map.contains(key, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    if(map == XQMap.empty()) return Bln.FALSE;

    return this;
  }
}
