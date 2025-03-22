package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class MapGet extends MapFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final FItem fallback = toFunctionOrNull(arg(2), 1, qc);

    final Value value = map.getInternal(key, fallback == null);
    return value != null ? value : fallback.invoke(qc, info, key);
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

    // combine result type with return type of fallback function, or with empty sequence
    SeqType st = null;
    final MapInfo mi = mapInfo(map, key);
    if(mi.key != null) {
      if(mi.field == null) {
        if(!mi.record.isExtensible()) return Empty.VALUE;
      } else if(!mi.field.isOptional()) {
        st = mi.field.seqType();
      }
    }

    if(st == null && mi.mapType != null) {
      st = mi.mapType.valueType();
      if(fallback) {
        final FuncType ft = func.funcType();
        if(ft != null) st = st.union(ft.declType);
      } else {
        st = st.union(Occ.ZERO);
      }
    }
    if(st != null) exprType.assign(st);
    return this;
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
