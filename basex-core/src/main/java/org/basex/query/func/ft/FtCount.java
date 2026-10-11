package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtCount extends FtAccessFn {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    // only the positions of the returned nodes are counted: index requests may yield
    // additional candidates, which are discarded by the subsequent name tests
    final FTPosData tmp = qc.ftPosData, ftPosData = new FTPosData();
    qc.ftPosData = ftPosData;
    try {
      int count = 0;
      final Iter nodes = arg(0).unwrappedIter(qc);
      for(Item item; (item = qc.next(nodes)) != null;) {
        // prefer positions of the argument
        final XNode node = toNode(item);
        final int size = ftPosData.size(node);
        count += size != 0 || tmp == null ? size : tmp.size(node);
      }
      return Itr.get(count);
    } finally {
      qc.ftPosData = tmp;
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // collect positions in the whole query
    if(cc.qc.ftPosData == null) cc.qc.ftPosData = new FTPosData();
    return this;
  }
}
