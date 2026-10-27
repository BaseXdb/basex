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
  public Uri value(final QueryContext qc) throws QueryException {
    final HttpServletRequest request = request(qc);
    return Uri.get(request.getContextPath() + request.getServletPath());
  }
}
