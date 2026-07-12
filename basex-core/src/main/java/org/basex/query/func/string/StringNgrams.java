package org.basex.query.func.string;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.ft.*;
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
    final AStr value = toStr(arg(0), qc);
    final Long nn = toLongOrNull(arg(1), qc);
    final FTOpt opt = ftOpt(toOptions(arg(2), new StringOptions(), qc));

    final int n = nn != null ? (int) Math.min(Integer.MAX_VALUE, nn) : 2;
    if(n < 1) throw STRING_NGRAM_X.get(info, n);

    final TokenList tokens = new TokenList();
    for(final String gram : NGram.grams(cps(value, opt), n)) tokens.add(gram);
    return StrSeq.get(tokens);
  }
}
