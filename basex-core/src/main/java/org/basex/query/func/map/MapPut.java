package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
public final class MapPut extends StandardFunc {
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
    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.key != null) {
      if(mc.field != null) {
        // map:put({ 'a': 1, 'b': 2 }, 'b', 3)  ->  util:map-put-at({ 'a': 1, 'b': 2 }, 2, 3)
        if(!mc.record.hasOptional())
          return cc.function(_UTIL_MAP_PUT_AT, info, map, Itr.get(mc.index), value);
        if(value.seqType().instanceOf(mc.field.seqType())) type = mc.record;
      } else {
        // try to propagate record type
        if(mc.record.isExtensible()) type = mc.record;
      }
    }

    if(type == null && mc.mapType != null) {
      // create new map type (potentially assigned record type must not be propagated)
      final Type akt = key.seqType().type.atomic();
      if(akt != null) type = mc.mapType.union(akt, value.seqType());
    }
    if(type != null) exprType.assign(type);
    return this;
  }
}
