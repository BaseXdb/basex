package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WsIds extends WsFn {
  @Override
  public Value value(final QueryContext qc) {
    return StrSeq.get(WsPool.ids());
  }
}
