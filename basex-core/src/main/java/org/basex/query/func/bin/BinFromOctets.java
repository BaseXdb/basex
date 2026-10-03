package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinFromOctets extends StandardFunc {
  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    final Expr values = arg(0);
    final Iter iter = values.atomIter(qc, info);
    final ByteList bl = new ByteList(Seq.initialCapacity(iter.size()));
    for(Item item; (item = qc.next(iter)) != null;) {
      final long l = toLong(item, values);
      if(l < 0 || l > 255) throw BIN_OOR_X.get(info, l);
      bl.add((int) l);
    }
    return B64.get(bl.finish());
  }
}
