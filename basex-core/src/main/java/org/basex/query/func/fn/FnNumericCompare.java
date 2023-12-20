package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnNumericCompare extends StandardFunc {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum value1 = toNumber(arg(0), qc);
    final ANum value2 = toNumber(arg(1), qc);
    return Int.get(value1.compare(value2, null, true, ii));
  }
}
