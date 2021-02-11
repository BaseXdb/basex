package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArraySubarray extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    final long n = array.arraySize();
    final long from = toLong(exprs[1], qc) - 1;
    if(from < 0 || from > n) throw ARRAYBOUNDS_X_X.get(info, from + 1, n + 1);
    if(exprs.length == 2) return array.subArray(from, n - from, qc);

    final long len = toLong(exprs[2], qc);
    if(len < 0) throw ARRAYNEG_X.get(info, len);
    if(from + len > n) throw ARRAYBOUNDS_X_X.get(info, from + 1 + len, n + 1);
    return array.subArray(from, len, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = exprs[0].seqType().type;
    if(type instanceof ArrayType) exprType.assign(type);
    return this;
  }
}
