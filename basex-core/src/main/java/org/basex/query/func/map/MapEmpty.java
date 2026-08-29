package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapEmpty extends MapFn {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    return toMap(arg(0), qc).structSize() == 0;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final long size = mapSize(map);
    // map:empty({}) → true(), map:empty($nonempty) → false()
    return size == -1 || map.has(Flag.NDT) ? this : Bln.get(size == 0);
  }
}
