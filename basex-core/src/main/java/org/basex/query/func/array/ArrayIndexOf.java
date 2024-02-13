package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayIndexOf extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Value search = arg(1).atomValue(qc, info);
    final Collation collation = toCollation(arg(2), qc);

    int c = 0;
    final LongList list = new LongList();
    for(final Value value : array.members()) {
      c++;
      final long s = search.size();
      if(s == value.size()) {
        int i = -1;
        while(++i < s) {
          final Item item1 = value.itemAt(i), item2 = search.itemAt(i);
          if(!(item1.comparable(item2) && item1.equal(item2, collation, sc, info))) break;
        }
        if(i == s) list.add(c);
      }
    }
    return IntSeq.get(list.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    return array == XQArray.empty() ? Empty.VALUE : this;
  }
}
