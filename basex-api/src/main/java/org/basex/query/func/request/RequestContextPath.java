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
public final class RequestContextPath extends ApiFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return toStr(state(qc).contextPath());
  }
}
