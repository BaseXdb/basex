package org.basex.query.func.web;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WebRedirect extends WebFn {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] path = toToken(exprs[0], qc);
    final XQMap map = exprs.length < 2 ? XQMap.EMPTY : toMap(exprs[1], qc);
    final byte[] anchor = exprs.length < 3 ? Token.EMPTY : toToken(exprs[2], qc);
    final byte[] location = createUrl(path, map, anchor);

    final HashMap<String, String> output = new HashMap<>();
    final HashMap<String, String> headers = new HashMap<>();
    headers.put(HttpText.LOCATION, Token.string(location));
    final ResponseOptions response = new ResponseOptions();
    response.set(ResponseOptions.STATUS, 302);

    return createResponse(output, headers, response);
  }
}
