package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Item position = toAtomItem(arg(1), qc);
    final FItem fallback = toFunctionOrNull(arg(2), 1, qc);

    if(fallback == null) return array.get(position, info);

    final long pos = toLong(position), size = array.arraySize();
    return pos > 0 && pos <= size ? array.get(pos - 1) : fallback.invoke(qc, info, position);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);

    // combine result type with return type of fallback function
    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      SeqType st = ((ArrayType) type).declType;
      if(defined(2)) {
        final FuncType ft = arg(2).funcType();
        if(ft != null) st = st.union(ft.declType);
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
