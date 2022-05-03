package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FetchBinary extends FetchXml {
  @Override
  public B64Lazy item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new B64Lazy(toIO(0, qc), FETCH_OPEN_X);
  }
}
