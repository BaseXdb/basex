package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnHash extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    final Item arg = arg(1).item(qc, info);

    final String algorithm;
    if(arg instanceof XQMap) {
      algorithm = toOptions(arg, new HashOptions(), qc).get(HashOptions.ALGORITHM);
    } else {
      final Item item = arg.atomItem(qc, info);
      algorithm = item.isEmpty() ? HashOptions.ALGORITHM.value() : toString(item);
    }
    if(value.isEmpty()) return Empty.VALUE;

    return new Hex(hash(value, algorithm.trim().toUpperCase(Locale.ENGLISH), qc));
  }

  /**
   * Computes the hash value.
   * @param value value to be hashed
   * @param algorithm algorithm
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private byte[] hash(final Item value, final String algorithm, final QueryContext qc)
      throws QueryException {

    if(algorithm.equals("CRC-32")) {
      final CRC32 crc = new CRC32();
      crc.update(toBytes(value));
      final byte[] result = new byte[4];
      for(int i = result.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8)
        result[i] = (byte) c;
      return result;
    }

    if(algorithm.equals("BLAKE3")) {
      return new Blake3().digest(toBytes(value));
    }

    final MessageDigest md;
    try {
      md = MessageDigest.getInstance(algorithm);
    } catch(final NoSuchAlgorithmException ex) {
      Util.debug(ex);
      throw HASH_ALGORITHM_X.get(info, algorithm);
    }

    if(value instanceof B64Lazy) {
      try(BufferInput bi = value.input(info)) {
        final byte[] tmp = new byte[IO.BLOCKSIZE];
        while(true) {
          qc.checkStop();
          final int n = bi.read(tmp);
          if(n == -1) return md.digest();
          md.update(tmp, 0, n);
        }
      } catch(final IOException ex) {
        throw FILE_IO_ERROR_X.get(info, ex);
      }
    }
    // non-streaming item, string
    return md.digest(toBytes(value));
  }

  /** Hash options. */
  public static final class HashOptions extends Options {
    /** Algorithm. */
    public static final StringOption ALGORITHM = new StringOption("algorithm", "MD5");
  }
}
