package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Item position = toAtomItem(arg(1), qc);
    final FItem fallback = toFunctionOrNull(arg(2), 1, qc);

    if(fallback != null) {
      final Value value = array.getOrNull(position, qc, info);
      return value != null ? value : invoke(fallback, new HofArgs(position), qc);
    }
    return array.get(position, qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);

    // combine result type with return type of fallback function
    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      if(array.structSize() == 0) return Empty.VALUE;

      SeqType st = ((ArrayType) type).valueType();
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
