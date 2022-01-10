package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnTranslate extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = toZeroToken(exprs[0], qc);
    final int[] search = cps(toToken(exprs[1], qc)), replace = cps(toToken(exprs[2], qc));
    if(token.length == 0 || search.length == 0) return Str.get(token);

    final TokenBuilder tb = new TokenBuilder(token.length);
    final int sl = search.length, rl = replace.length;
    for(final int cp : cps(token)) {
      int s = -1;
      while(++s < sl && cp != search[s]);
      if(s == sl) {
        tb.add(cp);
      } else if(s < rl) {
        tb.add(replace[s]);
      }
    }
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs[2];
    final SeqType st1 = expr1.seqType(), st3 = expr3.seqType();

    if((st1.zero() || st1.one() && st1.type.isStringOrUntyped()) && expr2 == Str.EMPTY &&
       st3.one() && st3.type.isStringOrUntyped()) {
      return cc.function(Function.STRING, info, expr1);
    }
    return this;
  }
}
