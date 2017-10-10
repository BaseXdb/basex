package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayInsertBefore extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final long p = checkPos(array, toLong(exprs[1], qc), true);
    return array.insertBefore(p, qc.value(exprs[2]));
  }

  @Override
  protected ArrayInsertBefore opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof ArrayType) {
      final SeqType vt = ((ArrayType) t).valueType.union(exprs[2].seqType());
      seqType = SeqType.get(ArrayType.get(vt), Occ.ONE);
    }
    return this;
  }
}
