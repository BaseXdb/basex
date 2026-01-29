package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayTrunk extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    if(array == XQArray.empty()) throw ARRAYEMPTY.get(info);
    return array.subArray(0, array.structSize() - 1, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }

  @Override
  public long structSize() {
    return arraySize(arg(0), -1);
  }
}
