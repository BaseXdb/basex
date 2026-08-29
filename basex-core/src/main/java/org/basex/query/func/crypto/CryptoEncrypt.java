package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoEncrypt extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final String type = toString(arg(1), qc);
    final byte[] key = toBytes(arg(2), qc);
    final String algorithm = toString(arg(3), qc);
    return new Encryption(info).encryption(value, type, key, algorithm, true);
  }
}
