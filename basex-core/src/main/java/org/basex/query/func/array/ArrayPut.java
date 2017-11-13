package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayPut extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    return array.put(checkPos(array, toLong(exprs[1], qc), false), qc.value(exprs[2]));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof ArrayType) {
      final SeqType dt = ((ArrayType) t).declType.union(exprs[2].seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
