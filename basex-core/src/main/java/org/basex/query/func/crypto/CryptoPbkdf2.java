package org.basex.query.func.crypto;

import static org.basex.query.QueryError.*;

import java.util.*;

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
  /** Supported hash algorithms (those of the JDK's PBKDF2 implementation). */
  private static final Set<String> HASHES = Set.of("SHA1", "SHA224", "SHA256", "SHA384",
      "SHA512", "SHA512/224", "SHA512/256");

  @Override
  public Hex value(final QueryContext qc) throws QueryException {
    // BaseX 14.0: return xs:base64Binary (as the other crypto functions)
    final String password = toString(arg(0), qc);
    final byte[] salt = toBytes(arg(1), qc);
    final int iterations = (int) Math.min(toLong(arg(2), 1, qc), Integer.MAX_VALUE);
    final int length = (int) Math.min(toLong(arg(3), 1, qc), Integer.MAX_VALUE / 8);
    final String algorithm = toStringOrNull(arg(4), qc);

    final String hash = algorithm == null ? "SHA256" :
      algorithm.toUpperCase(Locale.ENGLISH).replace("-", "");
    if(!HASHES.contains(hash)) throw CX_INVHASH_X.get(info, algorithm);
    return new Hex(new PasswordHash(qc, info).derive(password, salt, iterations, length, hash));
  }
}
