package org.basex.query.func.bin;

import java.nio.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinPackDouble extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final double d = toDouble(exprs[0], qc);
    final ByteOrder bo = order(1, qc);
    return B64.get(ByteBuffer.wrap(new byte[8]).order(bo).putDouble(d).array());
  }
}
