package org.basex.query.func.rest;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestUri extends ApiFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    return Uri.get(request(qc).getRequestURI());
  }
}
