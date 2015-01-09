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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BinJoin extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ByteList bl = new ByteList();
    final Iter ir = exprs[0].atomIter(qc, info);
    for(Item it; (it = ir.next()) != null;) bl.add(toB64(it, true).binary(info));
    return new B64(bl.finish());
  }
}
