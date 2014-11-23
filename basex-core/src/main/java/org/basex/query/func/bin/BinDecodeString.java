package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BinDecodeString extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final String enc = toEncoding(1, BIN_UE_X, qc);
    final Long off = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    final Long len = exprs.length > 3 ? toLong(exprs[3], qc) : null;
    if(b64 == null) return null;

    byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    if(bounds[0] > 0 || bounds[1] < bl) {
      final byte[] tmp = new byte[bounds[1]];
      System.arraycopy(bytes, bounds[0], tmp, 0, bounds[1]);
      bytes = tmp;
    }

    try {
      return Str.get(ConvertFn.toString(new IOContent(bytes).inputStream(), enc, true));
    } catch(final IOException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
