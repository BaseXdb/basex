package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArraySlice extends FnSlice {
  @Override
  public XQArray value(final QueryContext qc) throws QueryException {
    XQArray array = toArray(arg(0), qc);
    final Slice slice = slice(array.arraySize(), qc);

    if(slice.length == 0) return XQArray.empty();
    if(slice.reverse) array = array.reverseArray(qc);
    if(slice.step == 1) return array.subArray(slice.start - 1, slice.length, qc);

    final ArrayBuilder ab = new ArrayBuilder();
    for(long i = slice.start; i <= slice.end; i += slice.step) {
      ab.append(array.get(i - 1));
    }
    return ab.array();
  }

  @Override
  public Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof ArrayType) exprType.assign(type);
    return this;
  }
}
