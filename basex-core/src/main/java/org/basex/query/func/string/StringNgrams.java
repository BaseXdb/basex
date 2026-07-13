package org.basex.query.func.string;

import org.basex.query.*;
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
    final NgramOptions options = toOptions(arg(1), new NgramOptions(), qc);

    final int n = n(options);
    final boolean padding = options.get(NgramOptions.PADDING);

    final TokenList tokens = new TokenList();
    for(final String gram : NGram.grams(cps(value, ftOpt(options)), n, padding)) tokens.add(gram);
    return StrSeq.get(tokens);
  }
}
