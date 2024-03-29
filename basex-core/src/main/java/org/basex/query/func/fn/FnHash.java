package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.security.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnHash extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    final HashOptions options = toOptions(arg(1), new HashOptions(), qc);
    final String alg = options.get(HashOptions.ALGORITHM);;

    final MessageDigest md;
    try {
      md = MessageDigest.getInstance(alg.trim().toUpperCase(Locale.ENGLISH));
    } catch(final NoSuchAlgorithmException ex) {
      Util.debug(ex);
      throw HASH_ALGORITHM_X.get(info, alg);
    }

    if(value instanceof B64Lazy) {
      try(BufferInput bi = value.input(info)) {
        final byte[] tmp = new byte[IO.BLOCKSIZE];
        while(true) {
          qc.checkStop();
          final int n = bi.read(tmp);
          if(n == -1) return B64.get(md.digest());
          md.update(tmp, 0, n);
        }
      } catch(final IOException ex) {
        throw FILE_IO_ERROR_X.get(info, ex);
      }
    }
    // non-streaming item, string
    return new Hex(md.digest(toBytes(value)));
  }

  /** Hash options. */
  public static final class HashOptions extends Options {
    /** Algorithm. */
    public static final StringOption ALGORITHM = new StringOption("algorithm", "MD5");
  }
}
