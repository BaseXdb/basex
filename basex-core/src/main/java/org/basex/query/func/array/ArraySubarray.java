package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySubarray extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final long start = toLong(arg(1), qc) - 1;

    final long size = array.structSize();
    if(start < 0 || start > size) throw ARRAYBOUNDS_X_X.get(info, start + 1, size + 1);

    final Item length = arg(2).atomItem(qc, info);
    if(length.isEmpty()) return array.subArray(start, size - start, qc);

    final long len = toLong(length);
    if(len < 0) throw ARRAYNEG_X.get(info, len);
    if(start + len > size) throw ARRAYBOUNDS_X_X.get(info, start + 1 + len, size + 1);
    return array.subArray(start, len, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }
}
