package org.basex.query.func.bin;

import java.nio.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class BinPackDouble extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final double dbl = toDouble(arg(0), qc);
    final ByteOrder order = order(arg(1), qc);
    return B64.get(ByteBuffer.wrap(new byte[8]).order(order).putDouble(dbl).array());
  }
}
