package org.basex.query.func.hash;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.security.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.Util;

/**
 * Hashing function.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
abstract class HashFn extends StandardFunc {
  /**
   * Creates the hash of a string, using the given algorithm.
   * @param algo hashing algorithm
   * @param qc query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  final B64 hash(final String algo, final QueryContext qc) throws QueryException {
    final Item value = exprs[0].atomItem(qc, info);
    try {
      final MessageDigest md = MessageDigest.getInstance(algo);
      if(value instanceof B64Lazy) {
        try(BufferInput bi = value.input(info)) {
          final byte[] tmp = new byte[IO.BLOCKSIZE];
          do {
            qc.checkStop();
            final int n = bi.read(tmp);
            if(n == -1) return B64.get(md.digest());
            md.update(tmp, 0, n);
          } while(true);
        } catch(final IOException ex) {
          throw FILE_IO_ERROR_X.get(info, ex);
        }
      }
      // non-streaming item, string
      return B64.get(md.digest(toBytes(value)));
    } catch(final NoSuchAlgorithmException ex) {
      Util.debug(ex);
      throw HASH_ALGORITHM_X.get(info, algo);
    }
  }
}
