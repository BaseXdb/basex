package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinJoin extends StandardFunc {
  @Override
  public B64 item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = arg(0).atomIter(qc, info);
    final ByteList bl = new ByteList(iter.size());
    for(Item item; (item = qc.next(iter)) != null;) {
      bl.add(toBin(item).binary(info));
    }
    return B64.get(bl.finish());
  }
}
