package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerToBase extends StandardFunc {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final long value = toLong(arg(0), qc), base = toLong(arg(1), qc);
    if(base < 2 || base > 36) throw CONVERT_BASE_X.get(info, base);
    return Str.get(token(value, (int) base));
  }
}
