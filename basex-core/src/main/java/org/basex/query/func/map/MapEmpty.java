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
 * @author Christian Gruen
 */
public final class MapEmpty extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return toMap(arg(0), qc) == XQMap.empty();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    if(map.seqType().type instanceof MapType) {
      final long size = map.structSize();
      if(size >= 0 && !map.has(Flag.NDT)) return Bln.get(size == 0);
    }
    return this;
  }
}
