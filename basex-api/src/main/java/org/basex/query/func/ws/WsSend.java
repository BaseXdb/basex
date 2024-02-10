package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.iter.*;
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
public final class WsSend extends WsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item message = arg(0).item(qc, info);
    if(message.isEmpty()) throw EMPTYFOUND.get(info);

    final StringList ids = new StringList();
    final Iter iter = arg(1).iter(qc);
    for(Item it; (it = qc.next(iter)) != null;) {
      ids.add(toString(it));
    }
    WsPool.send(message, ids.finish());
    return Empty.VALUE;
  }
}
