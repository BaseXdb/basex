package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCollationKey extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final byte[] token = toToken(arg(0), qc);
    final Collation collation = toCollation(arg(1), qc);

    return B64.get(collation != null ? collation.key(token, info) : Collation.key(token));
  }
}
