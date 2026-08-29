package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoHmac extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final byte[] key = toBytes(arg(1), qc);
    final String algorithm = toString(arg(2), qc);
    final String encoding = toStringOrNull(arg(3), qc);

    return new Encryption(info).hmac(value, key, algorithm, encoding);
  }
}
