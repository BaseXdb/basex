package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
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
    final XQArray array = toArray(arg(0), qc);
    final Value positions = arg(1).atomValue(qc, info);

    // collect and sort positions and remove duplicates
    final LongList list = new LongList(positions.size());
    for(final Item item : positions) list.add(toPos(array, toLong(item), false));
    list.ddo();

    // delete entries backwards
    XQArray arr = array;
    for(int l = list.size() - 1; l >= 0; l--) {
      final long pos = list.get(l), size = arr.structSize();
      final boolean first = pos == 0, last = pos == size - 1;
      if(first || last) {
        // remove first or last member
        arr = arr.subArray(first ? 1 : 0, size - 1, qc);
      } else {
        // remove member at supplied position
        arr = arr.removeMember(pos, qc);
      }
    }
    return arr;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof ArrayType) exprType.assign(type);
    return this;
  }

  @Override
  public long structSize() {
    final long as = structSize(0);
    return as != -1 ? as - 1 : -1;
  }
}
