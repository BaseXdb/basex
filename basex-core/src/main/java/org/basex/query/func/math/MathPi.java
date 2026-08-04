package org.basex.query.func.math;

import static java.lang.StrictMath.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MathPi extends MathFn {
  @Override
  protected Item item(final QueryContext qc) {
    return Dbl.get(PI);
  }
}
