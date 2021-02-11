package org.basex.query.func.array;

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
public final class ArrayPut extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    return array.put(checkPos(array, toLong(exprs[1], qc), false), exprs[2].value(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type1 = exprs[0].seqType().type;
    if(type1 instanceof ArrayType) {
      final SeqType dt = ((ArrayType) type1).declType.union(exprs[2].seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
