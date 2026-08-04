package org.basex.query.func.ws;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsId extends WsFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return Str.get(ws(qc).id);
  }
}
