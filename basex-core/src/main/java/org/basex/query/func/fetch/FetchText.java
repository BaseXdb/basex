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
public final class FetchText extends FetchDoc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final IO source = toIO(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), FETCH_ENCODING_X, qc);
    final boolean fallback = toBooleanOrFalse(arg(2), qc);

    return new StrLazy(source, encoding, FETCH_OPEN_X, fallback);
  }
}
