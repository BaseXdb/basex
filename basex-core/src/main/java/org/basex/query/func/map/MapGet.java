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

    Value value;
    if(defined(2)) {
      value = map.getOrNull(key);
      if(value == null) value = arg(2).value(qc);
    } else {
      value = map.get(key);
    }
    if(value instanceof final FuncItem fi && toBooleanOrFalse(arg(3), qc)) {
      value = fi.toMethod(map);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    final boolean dflt = defined(2);
    if(map == XQMap.empty()) return dflt ? arg(2) : Empty.VALUE;

    final MapCompilation mc = MapCompilation.get(map).key(key);
    if(mc.key != null) {
      if(mc.field != null) {
        // map:get({ 'a': 1, 'b': 2 }, 'b')  ->  util:map-value-at({ 'a': 1, 'b': 2 }, 2)
        if(!mc.record.hasOptional())
          return cc.function(_UTIL_MAP_VALUE_AT, info, map, Int.get(mc.index), arg(3));
      } else {
        // map:get({ 'a': 1 }, 'b')  ->  ()
        if(!mc.record.isExtensible()) return dflt ? arg(2) : Empty.VALUE;
      }
    }
    if(mc.mapType != null) {
      // map:get({ 1: 1 }, 'string')  ->  ()
      if(mc.keyMismatch) return dflt ? arg(2) : Empty.VALUE;

      SeqType st = mc.mapType.valueType();
      st = dflt ? st.union(arg(2).seqType()) : st.union(Occ.ZERO);
      // invalidate function type (%method annotation would need to be removed from type)
      if(!(st.mayBeFunction() && defined(3))) exprType.assign(st);
    }
    return this;
  }
}
