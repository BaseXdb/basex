package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class BinFromOctets extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, info);
    final ByteList bl = new ByteList(Math.max(Array.CAPACITY, (int) iter.size()));
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      final long l = toLong(it);
      if(l < 0 || l > 255) throw BIN_OOR_X.get(info, l);
      bl.add((int) l);
    }
    return B64.get(bl.finish());
  }
}
