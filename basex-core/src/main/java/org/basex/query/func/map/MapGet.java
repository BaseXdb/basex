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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class MapGet extends StandardFunc {
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
    final Expr map = arg(0);
    final boolean fallback = defined(2);
    if(fallback) {
      final Type type = arg(1).seqType().type.atomic();
      if(type != null) arg(2, arg -> refineFunc(arg, cc, SeqType.ITEM_ZM, type.seqType()));
    } else if(map == XQMap.empty()) {
      return Empty.VALUE;
    }

    // combine result type with return type of fallback function, or with empty sequence
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      SeqType st = ((MapType) type).valueType;
      if(fallback) {
        final FuncType ft = arg(2).funcType();
        if(ft != null) st = st.union(ft.declType);
      } else {
        st = st.union(Occ.ZERO);
      }
      exprType.assign(st);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
