package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnMatches extends RegEx {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    if(flags.length == 0) {
      final int ch = patternChar(pattern);
      if(ch != -1) return contains(value, ch);
    }
    return pattern(pattern, flags, false).matcher(string(value)).find();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0), pattern = arg(1);

    final SeqType st = value.seqType();
    if(st.zero() || st.one() && st.type.isStringOrUntyped()) {
      if(pattern instanceof Str && !defined(2)) {
        if(pattern == Str.EMPTY) return Bln.TRUE;
        for(final byte b : ((Str) pattern).string()) {
          if(contains(REGEX_CHARS, b)) return this;
        }
        return cc.function(CONTAINS, info, args());
      }
    }
    return this;
  }
}
