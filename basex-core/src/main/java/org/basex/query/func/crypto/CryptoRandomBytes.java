package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;

import java.security.*;

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
public final class CryptoRandomBytes extends StandardFunc {
  /** Cryptographically strong random number generator. */
  private static final SecureRandom RANDOM = new SecureRandom();

  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    final long length = toLong(arg(0), 0, qc);
    if(length > Array.MAX_SIZE) throw RANGE_X.get(info, length);
    return B64.get(get((int) length));
  }

  /**
   * Returns random bytes.
   * @param length number of bytes
   * @return bytes
   */
  static byte[] get(final int length) {
    final byte[] bytes = new byte[length];
    RANDOM.nextBytes(bytes);
    return bytes;
  }
}
