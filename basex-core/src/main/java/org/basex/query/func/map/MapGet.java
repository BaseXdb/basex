package org.basex.query.func.map;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class MapGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final FItem fallback = defined(2) ? toFunction(arg(2), 1, qc) : null;

    final Value value = map.get(key, info);
    return !value.isEmpty() || fallback == null || map.contains(key, info) ? value :
      fallback.invoke(qc, info, key);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    final boolean fallback = defined(2);
    if(fallback) {
      final Type type = arg(1).seqType().type.atomic();
      if(type != null) arg(2, arg -> coerceFunc(arg, cc, SeqType.ITEM_ZM, type.seqType()));
    } else {
      if(map == XQMap.empty()) return Empty.VALUE;
    }

    // combine result type with return type of fallback function, or with empty sequence
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      SeqType st = ((MapType) type).declType;
      if(fallback) {
        final Type ftype = arg(2).seqType().type;
        if(ftype instanceof FuncType) st = st.union(((FuncType) ftype).declType);
      } else {
        st = st.union(Occ.ZERO);
      }
      exprType.assign(st);
    }
    return this;
  }
}
