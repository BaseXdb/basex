package org.basex.query.func.bin;

import java.nio.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinPackDouble extends BinFn {
  @Override
  public B64 value(final QueryContext qc) throws QueryException {
    final double value = toDouble(arg(0), qc);
    final ByteOrder order = order(arg(1), qc);
    return B64.get(ByteBuffer.wrap(new byte[8]).order(order).putDouble(value).array());
  }
}
