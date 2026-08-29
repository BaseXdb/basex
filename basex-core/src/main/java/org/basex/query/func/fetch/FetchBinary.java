package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FetchBinary extends FetchDoc {
  @Override
  public B64IOLazy value(final QueryContext qc) throws QueryException {
    final IO source = toIO(arg(0), qc);
    return new B64IOLazy(source, FETCH_OPEN_X);
  }
}
