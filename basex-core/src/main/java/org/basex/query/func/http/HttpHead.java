package org.basex.query.func.http;

import org.basex.query.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpHead extends HttpFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    return send("HEAD", null, arg(1), qc);
  }
}
