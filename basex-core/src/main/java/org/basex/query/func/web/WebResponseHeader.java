package org.basex.query.func.web;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebResponseHeader extends WebFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final HashMap<String, String> output = toOptions(0, new Options(), qc).free();
    final HashMap<String, String> headers = toOptions(1, new Options(), qc).free();
    final ResponseOptions response = toOptions(2, new ResponseOptions(), qc);

    return createResponse(response, headers, output);
  }
}
