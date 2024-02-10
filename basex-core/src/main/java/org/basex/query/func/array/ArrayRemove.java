package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayRemove extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQArray array = toArray(arg(0), qc);

    // collect and sort positions and remove duplicates
    final LongList pos = new LongList();
    final Iter iter = arg(1).iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      pos.add(toPos(array, toLong(item), false));
    }
    pos.ddo();

    // delete entries backwards
    for(int l = pos.size() - 1; l >= 0; l--) {
      final long p = pos.get(l);
      if(p < array.arraySize()) array = array.remove(p, qc);
    }
    return array;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof ArrayType) exprType.assign(type);
    return this;
  }
}
