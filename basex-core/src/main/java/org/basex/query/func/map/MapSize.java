package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapSize extends StandardFunc {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    return Int.get(map.structSize());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    if(map.seqType().type instanceof MapType) {
      final long size = map.structSize();
      if(size >= 0 && !map.has(Flag.NDT)) return Int.get(size);
    }
    return this;
  }
}
