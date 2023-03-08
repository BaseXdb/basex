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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnTranslate extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final int[] replace = cps(toToken(arg(1), qc)), with = cps(toToken(arg(2), qc));
    if(value.length == 0 || replace.length == 0) return Str.get(value);

    final TokenBuilder tb = new TokenBuilder(value.length);
    final int sl = replace.length, rl = with.length;
    for(final int cp : cps(value)) {
      int s = -1;
      while(++s < sl && cp != replace[s]);
      if(s == sl) {
        tb.add(cp);
      } else if(s < rl) {
        tb.add(with[s]);
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
      return cc.function(Function.STRING, info, value);
    }
    return this;
  }
}
