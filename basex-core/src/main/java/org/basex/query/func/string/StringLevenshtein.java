package org.basex.query.func.string;

import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshtein extends StringFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value1 = toStr(arg(0), qc), value2 = toStr(arg(1), qc);
    final FTOpt opt = ftOpt(toOptions(arg(2), new StringOptions(), qc));

    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    checkLength(cps1);
    checkLength(cps2);
    return Dbl.get(distance(cps1, cps2));
  }
}
