package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatInteger extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    final byte[] picture = toToken(arg(1), qc);
    final byte[] language = toZeroToken(arg(2), qc);
    if(value == null) return Str.EMPTY;

    final IntFormat format = qc.globalCaches.intFormat(picture, info);
    return Str.get(Formatter.get(language).formatInt(value, format));
  }
}
