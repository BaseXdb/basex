package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CryptoDerive extends StandardFunc {
  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    final Item key = arg(0).item(qc, info);
    final AlgorithmOptions options = toOptions(qc);
    // BaseX 14.0: unit differs from the length of crypto:generate-key (bits)
    final long length = defined(2) ? toLong(arg(2), 1, qc) : -1;
    if(length > Array.MAX_SIZE) throw RANGE_X.get(info, length);
    return B64.get(new KeyDerivation(info).derive(key, options, (int) length));
  }
}
