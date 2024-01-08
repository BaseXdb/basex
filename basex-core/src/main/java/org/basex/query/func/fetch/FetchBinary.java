package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FetchBinary extends FetchDoc {
  @Override
  public B64Lazy item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IO io = toIO(arg(0), qc);
    return new B64Lazy(io, FETCH_OPEN_X);
  }
}
