package org.basex.query.func.rest;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RestBaseUri extends RestFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final HttpServletRequest req = request(qc);
    final String uri = req.getRequestURI(), path = req.getPathInfo();
    return Uri.uri(path != null ? uri.substring(0, uri.length() - path.length()) : uri);
  }
}
