package org.basex.query.func.fn;

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
public final class FnFloor extends NumericFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : value.floor();
  }
}
