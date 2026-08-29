package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebCreateUrl extends WebFn {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    return Str.get(createUrl(qc));
  }
}
