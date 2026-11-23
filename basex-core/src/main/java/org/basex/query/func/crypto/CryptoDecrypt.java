package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class CryptoDecrypt extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final String type = toString(arg(1), qc);
    final Item key = arg(2).item(qc, info);
    final String algorithm = toString(arg(3), qc);
    return new Encryption(info).encryption(value, type, key, algorithm, false);
  }
}
