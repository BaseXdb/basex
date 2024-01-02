package org.basex.query.func.http;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Rositsa Shadura
 */
public final class HttpSendRequest extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // get request node
    final ANode request = toNodeOrNull(arg(0), qc);

    // get HTTP URI
    final byte[] href = toZeroToken(arg(1), qc);
    // get payload
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = arg(2).iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) vb.add(item);
    // send HTTP request
    return new Client(info, qc.context.options).sendRequest(href, request, vb.value());
  }
}
