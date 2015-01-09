package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnTranslate extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] tok =  cps(toEmptyToken(exprs[0], qc));
    final int[] srch = cps(toToken(exprs[1], qc));
    final int[] rep =  cps(toToken(exprs[2], qc));

    final TokenBuilder tb = new TokenBuilder(tok.length);
    for(final int t : tok) {
      int j = -1;
      final int sl = srch.length, rl = rep.length;
      while(++j < sl && t != srch[j]) ;
      if(j < sl) {
        if(j >= rl) continue;
        tb.add(rep[j]);
      } else {
        tb.add(t);
      }
    }
    return Str.get(tb.finish());
  }
}
