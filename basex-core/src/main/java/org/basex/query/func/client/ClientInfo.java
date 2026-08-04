package org.basex.query.func.client;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientInfo extends ClientFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return Str.get(session(qc, false).info().replace("\r\n?", "\n").trim());
  }
}
