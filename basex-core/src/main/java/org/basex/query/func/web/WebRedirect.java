package org.basex.query.func.web;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebRedirect extends WebFn {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String location = createUrl(qc);

    final HashMap<String, String> headers = new HashMap<>();
    headers.put(HttpText.LOCATION, location);
    final ResponseOptions response = new ResponseOptions();
    response.set(ResponseOptions.STATUS, 302);
    return createResponse(response, headers, null);
  }
}
