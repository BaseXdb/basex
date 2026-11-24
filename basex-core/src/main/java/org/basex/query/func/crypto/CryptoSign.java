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
public final class CryptoSign extends StandardFunc {
  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final Item key = arg(1).item(qc, info);
    final AlgorithmOptions options = toOptions(qc);

    return B64.get(Signer.get(options, info).sign(value, key));
  }
}
