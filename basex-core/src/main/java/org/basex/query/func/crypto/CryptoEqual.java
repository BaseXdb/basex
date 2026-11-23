package org.basex.query.func.crypto;

import java.security.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CryptoEqual extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    final byte[] value1 = toBytes(arg(0), qc);
    final byte[] value2 = toBytes(arg(1), qc);
    return Bln.get(MessageDigest.isEqual(value1, value2));
  }
}
