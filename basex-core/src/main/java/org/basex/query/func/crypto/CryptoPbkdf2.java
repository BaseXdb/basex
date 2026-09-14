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
public final class CryptoPbkdf2 extends StandardFunc {
  @Override
  public Hex value(final QueryContext qc) throws QueryException {
    final String password = toString(arg(0), qc);
    final byte[] salt = toBytes(arg(1), qc);
    final int iterations = (int) Math.min(toLong(arg(2).atomItem(qc, info), 1), Integer.MAX_VALUE);
    final int length = (int) Math.min(toLong(arg(3).atomItem(qc, info), 1), Integer.MAX_VALUE / 8);
    final String algorithm = toStringOrNull(arg(4), qc);

    return new Encryption(info).pbkdf2(password, salt, iterations, length, algorithm);
  }
}
