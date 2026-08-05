package org.basex.query.func.math;

import static java.lang.StrictMath.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MathTanh extends MathFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Item radians = arg(0).atomItem(qc, info);
    return radians.isEmpty() ? Empty.VALUE : Dbl.get(tanh(toDouble(radians)));
  }
}
