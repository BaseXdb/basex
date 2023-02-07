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
    final XQMap map = toMap(exprs[0], qc);
    final Item key = toAtomItem(exprs[1], qc);
    final FItem fallback = exprs.length > 2 ? toFunction(exprs[2], 1, qc) : null;

    final Value value = map.get(key, info);
    return value != Empty.VALUE || fallback == null || map.contains(key, info) ? value :
      fallback.invoke(qc, info, key);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = exprs[0];
    final boolean fallback = exprs.length > 2;
    if(fallback) {
      final Type type = exprs[1].seqType().type.atomic();
      if(type != null) exprs[2] = coerceFunc(exprs[2], cc, SeqType.ITEM_ZM, type.seqType());
    } else {
      if(map == XQMap.empty()) return Empty.VALUE;
    }

    // combine result type with return type of fallback function, or with empty sequence
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      SeqType st = ((MapType) type).declType;
      if(fallback) {
        final Type ftype = exprs[2].seqType().type;
        if(ftype instanceof FuncType) st = st.union(((FuncType) ftype).declType);
      } else {
        st = st.union(Occ.ZERO);
      }
      exprType.assign(st);
    }
    return this;
  }
}
