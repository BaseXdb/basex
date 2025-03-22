package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
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
public final class MapPut extends MapFn {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final Value value = arg(2).value(qc);

    return map.put(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), value = arg(2);
    if(map == XQMap.empty()) return cc.function(_MAP_ENTRY, info, key, value);

    Type type = null;
    final MapInfo mi = mapInfo(map, key);
    if(mi.key != null) {
      // try to propagate record type
      final boolean ext = mi.record.isExtensible();
      if(mi.key == EXTENDED && ext || (mi.field != null ?
        value.seqType().instanceOf(mi.field.seqType()) : ext)) type = mi.record;
    }

    if(type == null && mi.mapType != null) {
      // create new map type (potentially assigned record type must not be propagated)
      final Type akt = key.seqType().type.atomic();
      if(akt != null) type = mi.mapType.union(akt, value.seqType());
    }
    if(type != null) exprType.assign(type);
    return this;
  }
}
