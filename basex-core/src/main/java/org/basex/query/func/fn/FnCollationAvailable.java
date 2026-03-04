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
public final class FnCollationAvailable extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, info, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
   final byte[] collation = toToken(arg(0), qc);
    try {
      toCollation(collation, qc);
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
    return true;
  }
}
