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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayGet extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Item position = toAtomItem(arg(1), qc);

    Value value;
    if(defined(2)) {
      value = array.getOrNull(position, qc, info);
      if(value == null) value = arg(2).value(qc);
    } else {
      value = array.get(position, qc, info);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);

    // combine result type with return type of fallback function
    final Type type = array.seqType().type;
    if(type instanceof final ArrayType at) {
      final SeqType st = at.valueType();
      exprType.assign(defined(2) ? st.union(arg(2).seqType()) : st);
    }
    return this;
  }
}
