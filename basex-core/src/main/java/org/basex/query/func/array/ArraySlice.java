package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySlice extends FnSlice {
  @Override
  public XQArray value(final QueryContext qc) throws QueryException {
    XQArray array = toArray(arg(0), qc);
    final Slice slice = slice(array.structSize(), qc);

    if(slice.length == 0) return XQArray.empty();
    if(slice.reverse) array = array.reverseArray(qc);
    if(slice.step == 1) return array.subArray(slice.start - 1, slice.length, qc);

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(long i = slice.start; i <= slice.end; i += slice.step) {
      ab.add(array.valueAt(i - 1));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }
}
