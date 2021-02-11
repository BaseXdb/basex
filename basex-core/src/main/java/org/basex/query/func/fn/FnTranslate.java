package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnTranslate extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] token = cps(toZeroToken(exprs[0], qc));
    final int[] search = cps(toToken(exprs[1], qc));
    final int[] replace = cps(toToken(exprs[2], qc));

    final TokenBuilder tb = new TokenBuilder(token.length);
    for(final int b : token) {
      int j = -1;
      final int sl = search.length, rl = replace.length;
      while(++j < sl && b != search[j]);
      if(j < sl) {
        if(j >= rl) continue;
        tb.add(replace[j]);
      } else {
        tb.add(b);
      }
    }
    return Str.get(tb.finish());
  }
}
