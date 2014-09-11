package org.basex.query.func.hash;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class HashHash extends HashFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return hash(Token.string(toToken(exprs[1], qc)), qc);
  }
}
