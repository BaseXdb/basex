package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class WebCreateUrl extends WebFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Str.get(createUrl(toToken(exprs[0], qc), toMap(exprs[1], qc)));
  }
}
