package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringNgrams extends StringFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    final NgramOptions options = options(1, NgramOptions::new, qc);

    final int n = n(options);
    final boolean padding = options.get(NgramOptions.PADDING);

    final TokenList tokens = new TokenList();
    for(final String gram : NGram.grams(cps(value, ftOpt(options)), n, padding)) tokens.add(gram);
    return StrSeq.get(tokens);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(1, NgramOptions::new, cc);
    return this;
  }
}
