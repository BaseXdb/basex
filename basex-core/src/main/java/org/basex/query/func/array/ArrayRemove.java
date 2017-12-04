package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayRemove extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Array array = toArray(exprs[0], qc);

    // collect positions, sort and remove duplicates
    final LongList list = new LongList();
    final Iter pos = exprs[1].iter(qc);
    for(Item it; (it = qc.next(pos)) != null;) list.add(checkPos(array, toLong(it), false));
    list.sort().distinct();

    // delete entries backwards
    for(int i = list.size() - 1; i >= 0; i--) array = array.remove(list.get(i), qc);
    return array;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof ArrayType) exprType.assign(t);
    return this;
  }
}
