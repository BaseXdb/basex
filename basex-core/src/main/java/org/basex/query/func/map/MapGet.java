package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);

    Value value = map.getOrNull(key);
    if(value == null) {
      final FItem fallback = toFunctionOrNull(arg(2), 1, qc);
      if(fallback != null) value = fallback.invoke(qc, info, key);
      if(value == null) return Empty.VALUE;
    }
    if(value instanceof FuncItem && toBooleanOrFalse(arg(3), qc)) {
      value = ((FuncItem) value).toMethod(map);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), func = arg(2);
    if(map == XQMap.empty()) return Empty.VALUE;

    final boolean fallback = defined(2);
    if(fallback) {
      final Type type = arg(1).seqType().type.atomic();
      arg(2, arg -> refineFunc(arg, cc, type != null ? type.seqType() : SeqType.ANY_ATOMIC_TYPE_O));
    }

    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.key != null) {
      if(mc.field == null) {
        // map:get({ 'a': 1, 'a': 2 }, 'c')  ->  ()
        if(!mc.record.isExtensible() && !fallback) return Empty.VALUE;
      } else if(!mc.record.hasOptional()) {
        // map:get({ 'a': 1, 'b': 2 }, 'b')  ->  util:map-value-at({ 'a': 1, 'b': 2 }, 2)
        return cc.function(_UTIL_MAP_VALUE_AT, info, map, Int.get(mc.index), arg(3));
      }
    }
    if(mc.mapType != null) {
      SeqType st = mc.mapType.valueType();
      if(fallback) {
        final FuncType ft = func.funcType();
        if(ft != null) st = st.union(ft.declType);
      } else {
        st = st.union(Occ.ZERO);
      }
      // invalidate function type (%method annotation would need to be removed from type)
      if(!(st.mayBeFunction() && defined(3))) exprType.assign(st);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
