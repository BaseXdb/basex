package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
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
  protected Dbl item(final QueryContext qc) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final NgramOptions options = options(2, NgramOptions::new, qc);

    final int n = n(options);
    final boolean padding = options.get(NgramOptions.PADDING);
    final FTOpt opt = ftOpt(options);

    return Dbl.get(NGram.similarity(cps(value1, opt), cps(value2, opt), n, padding));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, NgramOptions::new, cc);
    return this;
  }
}
