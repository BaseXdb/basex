package org.basex.query.func.web;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class WebResponseHeader extends WebFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final HashMap<String, String> output = toOptions(0, qc);
    final HashMap<String, String> headers = toOptions(1, qc);
    final ResponseOptions response = toOptions(2, new ResponseOptions(), true, qc);

    return createResponse(response, headers, output);
  }
}
