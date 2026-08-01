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

    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    if(mti.field != null) {
      // remove the only field of a record: map:remove(RECORD, FIELD) → {}
      if(mti.shape.fields().size() == 1) return XQMap.empty();
    } else if(mti.validKey) {
      // return input map if nothing changes: map:remove({ 'a': 1 }, 'b') → { 'a': 1 }
      return map;
    }

    if(mti.mapType != null) {
      // map:remove({ 1: 1 }, 'string') → { 1: 1 }
      if(mti.keyMismatch) return map;
      exprType.assign(MapType.get(mti.mapType));
    }
    return this;
  }
}
