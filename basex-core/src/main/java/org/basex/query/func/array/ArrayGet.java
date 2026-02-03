package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayGet extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final long position = toLong(arg(1), qc);

    final long as = array.structSize();
    if(position > 0 && position <= as) return array.valueAt(position - 1);
    if(defined(2)) return arg(2).value(qc);
    throw as == 0 ? ARRAYEMPTY.get(info) : ARRAYBOUNDS_X_X.get(info, position, as);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    if(array.seqType().type instanceof final ArrayType at) {
      final SeqType st = at.valueType();
      // combine result type with return type of fallback function
      exprType.assign(defined(2) ? st.union(arg(2).seqType()) : st);
    }
    return this;
  }
}
