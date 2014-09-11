package org.basex.query.func.hash;

import static org.basex.query.util.Err.*;

import java.security.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Hashing functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class HashFn extends StandardFunc {
  /**
   * Creates the hash of a string, using the given algorithm.
   * @param algo hashing algorithm
   * @param qc query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  final B64 hash(final String algo, final QueryContext qc) throws QueryException {
    try {
      return new B64(MessageDigest.getInstance(algo).digest(toBinary(exprs[0], qc)));
    } catch(final NoSuchAlgorithmException ex) {
      throw HASH_ALG_X.get(info, algo);
    }
  }
}
