package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsEmit extends WsFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    WsPool.emit(arg(0).item(qc, info));
    return Empty.VALUE;
  }
}
