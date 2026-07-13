package org.basex.query.func.string;

import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringLevenshteinDistance extends StringFn {
  /** Options. */
  public static final class LevenshteinOptions extends StringOptions {
    /** Maximum distance. */
    public static final NumberOption MAX = new NumberOption("max");
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final LevenshteinOptions options = toOptions(arg(2), new LevenshteinOptions(), qc);

    final Integer mx = options.get(LevenshteinOptions.MAX);
    final FTOpt opt = ftOpt(options);
    final int[] cps1 = cps(value1, opt), cps2 = cps(value2, opt);
    // the length is only limited if the distance is computed exhaustively
    if(mx == null) {
      checkLength(cps1.length);
      checkLength(cps2.length);
    } else if(mx < 0) {
      return Empty.VALUE;
    }

    final int dist = distance(cps1, cps2, mx != null ? mx : -1);
    return dist == -1 ? Empty.VALUE : Itr.get(dist);
  }
}
