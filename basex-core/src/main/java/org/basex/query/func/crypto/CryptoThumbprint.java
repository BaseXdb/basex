package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CryptoThumbprint extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Item key = arg(0).item(qc, info);
    final CryptoKey ck = new CryptoKey(info);
    return Str.get(ck.thumbprint(ck.any(key)));
  }
}
