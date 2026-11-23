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
public final class CryptoHashPassword extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final String password = toString(arg(0), qc);
    final PasswordOptions options = toOptions(qc);
    return Str.get(new PasswordHash(qc, info).hash(password, options));
  }
}
