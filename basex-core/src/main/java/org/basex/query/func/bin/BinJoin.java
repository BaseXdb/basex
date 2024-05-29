package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.Value;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class BinJoin extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr binaries = arg(0);

    final ByteList bl;
    if(binaries instanceof SingletonSeq && ((SingletonSeq) binaries).singleItem()) {
      final byte[] bytes = toBin(((Value) binaries).itemAt(0)).binary(info);
      final long bs = binaries.size();
      bl = new ByteList(bs * bytes.length);
      for(int b = 0; b < bs; b++) bl.add(bytes);
    } else {
      bl = new ByteList();
      final Iter iter = binaries.atomIter(qc, info);
      for(Item item; (item = qc.next(iter)) != null;) {
        bl.add(toBin(item).binary(info));
      }
    }
    return B64.get(bl.finish());
  }
}
