package org.basex.query.func.fetch;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FetchCollection extends Docs {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return collection(qc, false);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return true;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }
}
