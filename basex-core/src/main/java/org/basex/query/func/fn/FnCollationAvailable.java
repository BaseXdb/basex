package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCollationAvailable extends StandardFunc {
  @Override
  protected Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
   final byte[] collation = toToken(arg(0), qc);
    try {
      toCollation(collation, qc);
    } catch(final QueryException ignore) {
      return false;
    }
    return true;
  }
}
