package org.basex.query.func.fn;

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
public final class FnCeiling extends NumericFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : value.ceiling();
  }

  @Override
  boolean integral() {
    return true;
  }
}
