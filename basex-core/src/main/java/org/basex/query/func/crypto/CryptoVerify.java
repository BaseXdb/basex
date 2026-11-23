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
public final class CryptoVerify extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    final byte[] value = toBytes(arg(0), qc);
    final byte[] signature = toBin(arg(1), qc).binary(info);
    final Item key = arg(2).item(qc, info);
    final AlgorithmOptions options = toOptions(qc);

    return Bln.get(Signer.get(options, info).verify(value, signature, key));
  }
}
