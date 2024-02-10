package org.basex.query.func.bin;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class BinBin extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toTokenOrNull(arg(0), qc);
    return string == null ? Empty.VALUE : B64.get(binary2bytes(string));
  }
}
