package org.basex.query.func.string;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringNgramSimilarity extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value1 = toStr(arg(0), qc), value2 = toStr(arg(1), qc);
    final Long nn = toLongOrNull(arg(2), qc);
    final int n = nn != null ? (int) Math.min(Integer.MAX_VALUE, nn) : 2;
    if(n < 1) throw STRING_NGRAM_X.get(info, n);

    final int[] cps1 = value1.codepoints(info), cps2 = value2.codepoints(info);
    return Dbl.get(NGram.similarity(cps1, cps2, n));
  }
}
