package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayEmpty extends ArrayFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    return Bln.get(array.isEmptyArray());
  }
}
