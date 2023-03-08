package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FetchText extends FetchDoc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IO io = toIO(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), FETCH_ENCODING_X, qc);
    final boolean fallback = toBooleanOrFalse(arg(2), qc);

    return new StrLazy(io, encoding, FETCH_OPEN_X, !fallback);
  }
}
