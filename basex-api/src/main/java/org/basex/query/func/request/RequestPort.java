package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestPort extends ApiFunc {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    return Itr.get(state(qc).serverPort());
  }
}
