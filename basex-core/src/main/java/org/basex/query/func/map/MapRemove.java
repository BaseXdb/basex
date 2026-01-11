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

    Type tp = null;
    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.field != null) {
      if(mc.field.isOptional()) {
        // structure does not change: propagate record type
        tp = mc.record;
      } else {
        // otherwise, derive new record type
        final RecordType rt = cc.qc.shared.record(mc.record.copy(mc.key, null, null));
        // return empty map if it will contain no entries
        if(rt.fields().isEmpty() && !rt.isExtensible()) return XQMap.empty();
        tp = rt;
      }
    } else if(mc.validKey) {
      // return input map if nothing changes: map:remove({ 'a': 1 }, 'b') → { 'a': 1 }
      if(!mc.record.isExtensible()) return map;
      // structure does not change: propagate record type
      tp = mc.record;
    }

    if(tp == null && mc.mapType != null) {
      // map:remove({ 1: 1 }, 'string') → { 1: 1 }
      if(mc.keyMismatch) return map;
      // create new map type (potentially assigned record type must not be propagated)
      tp = MapType.get(mc.mapType.keyType(), mc.mapType.valueType());
    }
    if(tp != null) exprType.assign(tp);
    return this;
  }
}
