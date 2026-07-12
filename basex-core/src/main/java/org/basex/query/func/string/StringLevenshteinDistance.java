package org.basex.query.func.string;

import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshteinDistance extends StringFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr value1 = toStr(arg(0), qc), value2 = toStr(arg(1), qc);
    final Long mx = toLongOrNull(arg(2), qc);
    final FTOpt opt = ftOpt(toOptions(arg(3), new StringOptions(), qc));

    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    // the length is only limited if the distance is computed exhaustively
    if(mx == null) {
      checkLength(cps1);
      checkLength(cps2);
    } else if(mx < 0) {
      return Empty.VALUE;
    }

    final int max = mx != null ? (int) Math.min(Integer.MAX_VALUE, mx) : -1;
    final int dist = distance(cps1, cps2, max);
    return dist == -1 ? Empty.VALUE : Itr.get(dist);
  }
}
