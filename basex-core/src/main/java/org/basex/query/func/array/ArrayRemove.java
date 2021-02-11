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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArrayRemove extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQArray array = toArray(exprs[0], qc);

    // collect positions, sort and remove duplicates
    final LongList list = new LongList();
    final Iter pos = exprs[1].iter(qc);
    for(Item item; (item = qc.next(pos)) != null;) list.add(checkPos(array, toLong(item), false));
    list.sort().distinct();

    // delete entries backwards
    for(int l = list.size() - 1; l >= 0; l--) array = array.remove(list.get(l), qc);
    return array;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type1 = exprs[0].seqType().type;
    if(type1 instanceof ArrayType) exprType.assign(type1);
    return this;
  }
}
