package org.basex.query.func.string;

import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshtein extends StringFn {
  @Override
  protected Dbl item(final QueryContext qc) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final FTOpt opt = ftOpt(arg(2), qc);

    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    checkLength(cps1.length);
    checkLength(cps2.length);
    return Dbl.get(distance(cps1, cps2));
  }
}
