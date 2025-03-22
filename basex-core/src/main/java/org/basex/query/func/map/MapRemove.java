package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class MapRemove extends MapFn {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = toMap(arg(0), qc);
    final Iter keys = arg(1).iter(qc);

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
    final MapInfo mi = mapInfo(map, key);
    if(mi.key != null) {
      // try to propagate record type
      final boolean ext = mi.record.isExtensible();
      if(mi.field == null && !ext) return map;
      if(mi.key == EXTENDED && ext || mi.field == null || mi.field.isOptional()) type = mi.record;
    }

    if(type == null && mi.mapType != null) {
      // create new map type (potentially assigned record type must not be propagated)
      type = MapType.get(mi.mapType.keyType(), mi.mapType.valueType());
    }
    if(type != null) exprType.assign(type);
    return this;
  }
}
