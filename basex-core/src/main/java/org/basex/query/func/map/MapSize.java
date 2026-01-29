package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapSize extends MapFn {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    return Itr.get(map.structSize());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final long size = mapSize(map);
    return size == -1 || map.has(Flag.NDT) ? this : Itr.get(size);
  }
}
