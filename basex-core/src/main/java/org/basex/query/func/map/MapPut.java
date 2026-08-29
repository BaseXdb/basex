package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapPut extends MapFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final Value value = arg(2).value(qc);

    return map.put(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), value = arg(2);
    // map:put({}, $k, $v) → map:entry($k, $v)
    if(map == XQMap.empty()) return cc.function(_MAP_ENTRY, info, key, value);

    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    if(mti.index != 0) {
      // use optimized setter for records
      return new ShapeSet(info, map, mti.index, value).optimize(cc);
    }
    if(mti.shape != null && key instanceof final Str str && key.seqType().eq(Types.STRING_O)) {
      // extend the shape: map:put({ 'a': 1 }, 'b', 2) → map with fields a, b
      final ShapeType sh = mti.shape.put(str.string(), value.seqType());
      if(sh != null) {
        exprType.assign(cc.qc.shared.shape(sh));
        return this;
      }
    }

    if(mti.mapType != null) {
      final Type akt = key.seqType().type.atomic();
      if(akt != null) exprType.assign(mti.mapType.union(akt, value.seqType()));
    }
    return this;
  }
}
