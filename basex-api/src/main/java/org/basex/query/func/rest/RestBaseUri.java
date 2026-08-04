package org.basex.query.func.rest;

import jakarta.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestBaseUri extends ApiFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final HttpServletRequest request = request(qc);
    final String uri = request.getRequestURI(), path = request.getPathInfo();
    return Uri.get(path != null ? uri.substring(0, uri.length() - path.length()) : uri);
  }
}
