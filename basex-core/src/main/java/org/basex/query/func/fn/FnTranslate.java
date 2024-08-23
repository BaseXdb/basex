package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnTranslate extends StandardFunc {
  @Override
  public AStr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc), replace = toStr(arg(1), qc), with = toStr(arg(2), qc);

    final int[] cps = value.codepoints(info);
    final int[] rplc = replace.codepoints(info);
    final int[] wth = with.codepoints(info);
    final int cl = cps.length, rl = rplc.length, wl = wth.length;
    if(cl == 0 || rl == 0) return value;

    final TokenBuilder tb = new TokenBuilder(cl);
    for(final int cp : cps) {
      int r = -1;
      while(++r < rl && cp != rplc[r]);
      if(r == rl) {
        tb.add(cp);
      } else if(r < wl) {
        tb.add(wth[r]);
      }
    }
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0), replace = arg(1), with = arg(2);
    final SeqType st = value.seqType(), withSt = with.seqType();

    if((st.zero() || st.one() && st.type.isStringOrUntyped()) && replace == Str.EMPTY &&
        withSt.one() && withSt.type.isStringOrUntyped()) {
      // tokenize($value, '', 'abcde')  ->  string($value)
      return cc.function(Function.STRING, info, value);
    }
    return this;
  }
}
