package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnCollection extends Docs {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return collection(qc);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return true;
  }
}
