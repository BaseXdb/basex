package org.basex.query.func.string;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringNgrams extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr value = toStr(arg(0), qc);
    final long n = defined(1) ? Math.min(Integer.MAX_VALUE, toLong(arg(1), qc)) : 2;
    if(n < 1) throw STRING_NGRAM_X.get(info, n);

    final TokenList tokens = new TokenList();
    for(final String gram : NGram.grams(value.codepoints(info), (int) n)) tokens.add(gram);
    return StrSeq.get(tokens);
  }
}
