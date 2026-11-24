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
public final class CryptoVerifyPassword extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    final String password = toString(arg(0), qc);
    final String hash = toString(arg(1), qc);
    return Bln.get(new PasswordHash(qc, info).verify(password, hash));
  }
}
