package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;

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
        count += ftPosData.size(toNode(item));
      }
      return Itr.get(count);
    } finally {
      qc.ftPosData = tmp;
    }
  }
}
