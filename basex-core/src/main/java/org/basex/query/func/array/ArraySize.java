package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArraySize extends ArrayFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Int.get(toArray(arg(0), qc).arraySize());
  }
}
