package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDecodeFromUri extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    return Str.get(XMLToken.decodeUri(toZeroToken(arg(0), qc)));
  }
}
