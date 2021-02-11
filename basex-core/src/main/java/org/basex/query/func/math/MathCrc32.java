package org.basex.query.func.math;

import java.util.zip.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class MathCrc32 extends MathFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = toTokenOrNull(exprs[0], qc);
    if(token == null) return Empty.VALUE;

    final CRC32 crc = new CRC32();
    crc.update(token);
    final byte[] r = new byte[4];
    for(int i = r.length, c = (int) crc.getValue(); i-- > 0; c >>>= 8) r[i] = (byte) c;
    return new Hex(r);
  }
}
