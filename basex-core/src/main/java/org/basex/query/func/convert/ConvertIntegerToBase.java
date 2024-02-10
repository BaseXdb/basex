package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerToBase extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long value = toLong(arg(0), qc), base = toLong(arg(1), qc);
    if(base < 2 || base > 36) throw CONVERT_BASE_X.get(info, base);
    return Str.get(token(value, (int) base));
  }
}
