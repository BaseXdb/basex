package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WebCreateUrl extends WebFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] path = toToken(exprs[0], qc);
    final XQMap map = toMap(exprs[1], qc);
    final byte[] anchor = exprs.length < 3 ? Token.EMPTY : toToken(exprs[2], qc);
    return Str.get(createUrl(path, map, anchor));
  }
}
