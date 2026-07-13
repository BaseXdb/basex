package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringNgramSimilarity extends StringFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final NgramOptions options = toOptions(arg(2), new NgramOptions(), qc);

    final int n = n(options);
    final boolean padding = options.get(NgramOptions.PADDING);
    final FTOpt opt = ftOpt(options);

    return Dbl.get(NGram.similarity(cps(value1, opt), cps(value2, opt), n, padding));
  }
}
