package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinJoin extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];

    final ByteList bl;
    if(expr instanceof SingletonSeq && ((SingletonSeq) expr).singleItem()) {
      final byte[] bytes = toB64(((SingletonSeq) expr).itemAt(0), false).binary(info);
      final long bs = expr.size();
      bl = new ByteList(bs * bytes.length);
      for(int b = 0; b < bs; b++) bl.add(bytes);
    } else {
      bl = new ByteList();
      final Iter iter = expr.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) bl.add(toB64(item, false).binary(info));
    }
    return B64.get(bl.finish());
  }
}
