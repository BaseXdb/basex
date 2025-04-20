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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayRemove extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQArray array = toArray(arg(0), qc);

    // collect and sort positions and remove duplicates
    final LongList list = new LongList();
    final Iter positions = arg(1).iter(qc);
    for(Item item; (item = qc.next(positions)) != null;) {
      list.add(toPos(array, toLong(item), false));
    }
    list.ddo();

    // delete entries backwards
    for(int l = list.size() - 1; l >= 0; l--) {
      final long pos = list.get(l), size = array.structSize();
      final boolean first = pos == 0, last = pos == size - 1;
      if(first || last) {
        // remove first or last member
        array = array.subArray(first ? 1 : 0, size - 1, qc);
      } else {
        // remove member at supplied position
        array = array.removeMember(pos, qc);
      }
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
