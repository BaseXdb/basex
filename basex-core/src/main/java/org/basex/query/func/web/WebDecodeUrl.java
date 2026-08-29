package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDecodeUrl extends WebFn {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    return Str.get(XMLToken.decodeUri(value, true));
  }
}
