package org.basex.query.func.rest;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RestUri extends RestFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Uri.uri(request(qc).getRequestURI());
  }
}
