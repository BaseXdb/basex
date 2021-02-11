package org.basex.query.func.rest;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestBaseUri extends ApiFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final HttpServletRequest request = request(qc);
    final String uri = request.getRequestURI(), path = request.getPathInfo();
    return Uri.uri(path != null ? uri.substring(0, uri.length() - path.length()) : uri);
  }
}
