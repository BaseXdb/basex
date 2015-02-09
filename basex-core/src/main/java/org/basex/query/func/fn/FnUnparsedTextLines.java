package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnUnparsedTextLines extends Parse {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return textIter(unparsedText(qc, false).string(info));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value();
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return true;
  }
}
