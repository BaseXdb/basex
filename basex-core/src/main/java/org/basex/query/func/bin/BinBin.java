package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinBin extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toDigits(qc);
    return value == null ? Empty.VALUE : B64.get(binary2bytes(value));
  }
}
