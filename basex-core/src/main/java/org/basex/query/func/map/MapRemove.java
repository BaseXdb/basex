package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
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
public final class MapRemove extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = toMap(arg(0), qc);
    final Iter keys = arg(1).atomIter(qc, info);

    for(Item item; (item = qc.next(keys)) != null;) {
      map = map.remove(toAtomItem(item, qc));
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    if(map == XQMap.empty()) return map;

    Type type = null;
    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.key != null) {
      // try to propagate record type
      final boolean extensible = mc.record.isExtensible();
      if(mc.field == null && !extensible) return map;
      if(mc.key == MapCompilation.EXTENDED && extensible || mc.field == null ||
          mc.field.isOptional()) {
        type = mc.record;
      }
    }

    if(type == null && mc.mapType != null) {
      // map:get({ 1: 1 }, 'string')  ->  ()
      if(mc.keyMismatch) return map;

      // create new map type (potentially assigned record type must not be propagated)
      type = MapType.get(mc.mapType.keyType(), mc.mapType.valueType());
    }
    if(type != null) exprType.assign(type);
    return this;
  }
}
