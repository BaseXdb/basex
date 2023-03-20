package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Lukas Kircher
 */
public final class CryptoHmac extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final byte[] key = toBytes(arg(1), qc);
    final String algorithm = toString(arg(2), qc);
    final String encoding = toStringOrNull(arg(3), qc);

    return new Encryption(info).hmac(value, key, algorithm, encoding);
  }
}
