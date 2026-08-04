package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringJaroWinkler extends StringFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final FTOpt opt = ftOpt(arg(2), qc);

    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    checkLength(cps1.length);
    checkLength(cps2.length);
    return Dbl.get(JaroWinkler.distance(cps1, cps2));
  }
}
