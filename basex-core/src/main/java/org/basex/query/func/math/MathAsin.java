package org.basex.query.func.math;

import static java.lang.StrictMath.*;

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
public final class MathAsin extends MathFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? Empty.VALUE : Dbl.get(asin(toDouble(value)));
  }
}
