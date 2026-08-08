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
  protected Itr item(final QueryContext qc) throws QueryException {
    final FTPosData tmp = qc.ftPosData;
    qc.ftPosData = new FTPosData();
    final Iter nodes = arg(0).unwrappedIter(qc);
    for(Item item; (item = qc.next(nodes)) != null;) {
      toNode(item);
    }
    final int size = qc.ftPosData.size();
    qc.ftPosData = tmp;
    return Itr.get(size);
  }
}
