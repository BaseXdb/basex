package org.basex.query.func.hash;

import static org.basex.query.util.Err.*;

import java.security.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Hashing functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNHash extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _HASH_MD5:         return hash("MD5", qc);
      case _HASH_SHA1:        return hash("SHA", qc);
      case _HASH_SHA256:      return hash("SHA-256", qc);
      case _HASH_HASH:        return hash(qc);
      default:                return super.item(qc, ii);
    }
  }

  /**
   * Creates the hash of the given xs:string.
   * @param qc query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private B64 hash(final QueryContext qc) throws QueryException {
    return hash(Token.string(toToken(exprs[1], qc)), qc);
  }

  /**
   * Creates the hash of a string, using the given algorithm.
   * @param algo hashing algorithm
   * @param qc query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private B64 hash(final String algo, final QueryContext qc) throws QueryException {
    return hashBinary(toBinary(exprs[0], qc), algo);
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param value value to be hashed
   * @param algo hashing algorithm
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private B64 hashBinary(final byte[] value, final String algo) throws QueryException {
    try {
      return new B64(MessageDigest.getInstance(algo).digest(value));
    } catch(final NoSuchAlgorithmException ex) {
      throw HASH_ALG_X.get(info, algo);
    }
  }
}
