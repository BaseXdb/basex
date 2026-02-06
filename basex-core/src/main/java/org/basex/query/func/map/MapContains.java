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
 * @author Christian Gruen
 */
public final class MapContains extends MapFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    return map.contains(key);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    if(map == XQMap.empty()) return Bln.FALSE;

    if(!map.has(Flag.NDT)) {
      final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
      if(mti.field != null) {
        if(!mti.field.isOptional()) return Bln.TRUE;
      } else if(mti.validKey) {
        return Bln.FALSE;
      }
      if(mti.mapType != null) {
        // map:contains({ 1: 1 }, 'string') → false()
        if(mti.keyMismatch) return Bln.FALSE;
      }
    }
    return this;
  }
}
