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
public final class MathExp10 extends MathFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Double value = toDoubleOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : Dbl.get(pow(10, value));
  }
}
