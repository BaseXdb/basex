package org.basex.query.func.http;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class HttpSendRequest extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode request = toNodeOrNull(arg(0), qc);
    final byte[] href = toZeroToken(arg(1), qc);
    final Value body = arg(2).value(qc);

    return new Client(info, qc.context.options).sendRequest(href, request, body);
  }
}
