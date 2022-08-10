package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ArrayInsertBefore extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    final long pos = toPos(array, toLong(exprs[1], qc), true);
    final Value value = exprs[2].value(qc);
    return array.insertBefore(pos, value, qc);
  }

  @Override
  protected ArrayInsertBefore opt(final CompileContext cc) {
    final Type type = exprs[0].seqType().type;
    if(type instanceof ArrayType) {
      final SeqType dt = ((ArrayType) type).declType.union(exprs[2].seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
