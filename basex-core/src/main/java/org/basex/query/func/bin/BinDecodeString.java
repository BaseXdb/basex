package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BinDecodeString extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final B64 binary = toB64OrNull(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), BIN_UE_X, qc);
    final Item offset = arg(2).atomItem(qc, info);
    final Item size = arg(3).atomItem(qc, info);
    if(binary == null) return Empty.VALUE;

    byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, offset.isEmpty() ? Empty.VALUE : size, bl);
    final int o = bounds[0], tl = bounds[1];
    if(o > 0 || tl < bl) bytes = Arrays.copyOfRange(bytes, o, o + tl);

    try {
      return Str.get(ConvertFn.toString(new ArrayInput(bytes), encoding, true));
    } catch(final IOException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
