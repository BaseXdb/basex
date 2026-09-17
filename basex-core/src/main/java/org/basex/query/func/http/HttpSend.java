package org.basex.query.func.http;

import org.basex.query.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpSend extends HttpFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    // methods are case-sensitive and sent unchanged
    return send(toString(arg(1), qc), arg(2), arg(3), qc);
  }
}
